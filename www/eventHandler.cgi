#!/usr/bin/perl
use strict;
BEGIN { 
	unshift @INC,'./lib';
	unshift @INC,'/home/websites/lib';
	unshift @INC,'/usr/local/homeAutomation/bin/lib';
}
use File::Copy;
use URI::Escape;
use Cattech::SQLHelper;
use Cattech::WatchCat;
use Cattech::HomeAutomation;
use JSON;
use Data::Dumper;
use Data::Compare;
require "cgi-lib.pm";

$SIG{ __DIE__ } = sub { Carp::confess( @_ ) };

$|=1;

$cgi_lib'maxdata    = 1000000;
# maximum bytes to accept via POST - 2^17
#$cgi_lib'writefiles = 0; # '/home/websites/pawz.cattech.org/pw/homeAutomation/userImages/';
# directory to which to write files, or
# 0 if files should not be written
#$cgi_lib'filepre    = "cgi-lib";
# Prefix of file names, in directory above

my %input;
ReadParse(\%input);

my $date="";
my $event="{}";


$::HA=Cattech::HomeAutomation->new();
$::HA->connectToSQLDatabase();

print "Content-type:text/html\n\n";
print "<body>\n";

#$input{eventLogID}=2448;

my $packet={};
if (exists $input{event}) {
	my $id = logEventToDB($input{event});
	$packet = JSON->new->utf8->decode($input{event});

	(undef,$event,$date) = getEventFromDB($id);
}


print "Source XXX: $packet->{source}\n";

if ($packet->{channel} eq 'EventHandler' ) {
	my @actions = findActions($packet->{data});
	executeActions(\@actions,$packet->{data});
	print "{\"status\":\"processed\"}\n";
}else{

	print <<EOB;
	<style>
	body {
		background-color:#AAA;
		margin:0px;
	}
	.runningActions	{
		position:relative;
		right:0px;
		margin:2em;
		border:1px solid blue;
		background-color:#FFF;
	}
	.incoming {
		background-color:#CCA;
	}
	.events {
		font-size:90%;
		max-height:15em;
		overflow:auto;
		border:5px outset #444;
	}
	.bottom {
		bottom:0px;
	}
	.top {
		top:0px;
	}
	.eventLine {
		margin-top:5px;
		border:1px solid black;
	}
	</style>
EOB

	if (! exists $input{event}) {
		print "<div class=\"events incoming\">\n";
		displayLastXEvents(50);
		print "</div>\n";
	}

	my $eventLogID = $input{eventLogID};
	if ($eventLogID ne "") {
		(undef,$event,$date) = getEventFromDB($eventLogID);


		$packet = JSON->new->utf8->decode($event);
		$packet->{data}->{debug}='console';
		my @actions=();

		if ($packet->{destination}='eventHandler') {
			@actions = findActions($packet->{data});
		}

		print "<br>\n";
		print "<h2>Selected event : $eventLogID <span style=\"float:right\">$date</span></h2>\n";
		print "<div class=\"events incoming\">\n";
		print "<center>$event</center>\n";
		print "</div>\n";
		print "<br>\n";

		print "Triggers [" . join(',',@actions) . "]<br>\n";

		print "Simulating triggers:</h4>\n";

		executeActions(\@actions,$packet->{data});
	}

}

#================================================================================
sub logEventToDB {
	my ($event) = @_;

	if ($event ne "") {
		my $eventData = { event=>$event };
	        $::HA->{SH}->addupdate_data($eventData,'eventLog');
		return $eventData->{eventLog_id};
	}
}

#================================================================================
sub getLastEventFromDB {
	my $eventData={};
	my $event="";
        $::HA->{SH}->execute_raw_sql($eventData,"SELECT * FROM eventLog ORDER BY time DESC LIMIT 1");
	$::HA->{SH}->next_row($eventData);

	return ($eventData->{eventLog_id},$eventData->{event},$eventData->{time});
}
#================================================================================
sub getEventFromDB {
	my ($eventLogID)=@_;
	my $eventData={};
	my $event="";
        $::HA->{SH}->execute_raw_sql($eventData,"SELECT * FROM eventLog WHERE eventLog_id=$eventLogID");
	$::HA->{SH}->next_row($eventData);

	return ($eventData->{eventLog_id},$eventData->{event},$eventData->{time});
}
#================================================================================
sub displayLastXEvents {
	my ($limit)=@_;
	my $eventData={};
	my $event="";
        $::HA->{SH}->execute_raw_sql($eventData,"SELECT * FROM eventLog ORDER BY time DESC LIMIT $limit");

	while ($::HA->{SH}->next_row($eventData)) {
		print "<div class=\"eventLine\"><a href=\"?eventLogID=$eventData->{eventLog_id}\">$eventData->{event} "
		."<span style=\"float:right\">$eventData->{time}</span></a></div>\n";
	}
}

#================================================================================
sub findActions {
	my ($data)=@_;
	my @actions;

#	print "Data : $data<br>\n";

	if (exists $data->{reaction}) {
		my $json = encode_json($data);
		push @actions,$json;
	}

	my $actionData={};
	my $sql = "SELECT event,action,triggers_id FROM triggers";

	if ($data->{debug} eq "") {
		$sql .=" WHERE earliestNext <= NOW()";
	}
#	print "SQL : $sql<br>\n";
        $::HA->{SH}->execute_raw_sql($actionData,$sql);

	while ($::HA->{SH}->next_row($actionData)) {
		my $triggerHash = decode_json($actionData->{event});

		if (exists $data->{debug}) {
			$triggerHash->{debug}=$data->{debug};
		}

#		print "<hr>\n";
#		print "R :".Dumper($actionData)."<br><br>\n";
#		print "ED:".Dumper($data)."<br>\n";
#		print "T :".Dumper($triggerHash)."<br>\n";

		if (matchTrigger($data,$triggerHash)) {
			push @actions,$actionData->{action};
#			print "ATI:$actionData->{triggers_id}<br>\n";
			if ($data->{debug} eq "") {
				setTriggerEarliestNext($actionData->{triggers_id});
			}
		}
	}
#	print "<hr>\n";

	return @actions;
}

#================================================================================
sub matchTrigger {
	my ($eh,$th)=@_;
	my $match=1;

	my $c = Data::Compare->new($eh, $th)->Cmp;
#	print "C :" . $c . "<br>\n";

	return $c;
		
	foreach my $key (keys %$th) {
		print "TH[$key] $th->{$key} =~m/ $eh->{$key} /<br>\n";

		if (! ($eh->{$key} =~ m/$th->{$key}/)) {
			print "No regex match<br>\n";
			$match=0;
		}
		if (! exists $eh->{$key}) {
			print "No key $key<br>\n";
			$match=0;
		}
	}

	return $match;
}
#================================================================================
sub setTriggerEarliestNext {
	my ($triggers_id)=@_;

	my $triggers={triggers_id=>$triggers_id};
	$::HA->{SH}->select_data($triggers,'triggers');
        $::HA->{SH}->next_row($triggers);

	print Dumper($triggers);

	if (defined $triggers->{frequency}) {
		my $sql = "UPDATE triggers SET earliestNext =NOW() + INTERVAL ".$triggers->{frequency}." WHERE triggers_id=".$triggers_id;
#		print "SQL : $sql<br>\n";
	        $::HA->{SH}->execute_raw_sql(my $db,$sql);
	}

}
#================================================================================
sub loadReactions { 
	my ($list,$actions)=@_;

	my $reactionData={};
	my $reactions=join(',',@$list);
	my $sql="SELECT action FROM reactions WHERE reactions_id in ($reactions)";
#	print "SQL : $sql<br>\n";
        $::HA->{SH}->execute_raw_sql($reactionData,$sql);

	while ($::HA->{SH}->next_row($reactionData)) {
		print "Adding : $reactionData->{action}<br>\n";
		push @$actions,$reactionData->{action};
	}
	
}
#================================================================================
sub executeActions {
	# This will live in the data hub eventually!!!!
	my ($actions,$data)=@_;

	foreach my $actionJson (@$actions) { 
		print "<h4>Evaluating action : $actionJson</h4>\n";
		my $action = JSON->new->utf8->canonical->decode($actionJson);
		$action->{_event}=$data;

		print "<div class=\"runningActions\">\n";
		if ($data->{debug} ne "") {
			$action->{debug} = $data->{debug};
			print "<div>Debug mode, only simulating</div>\n";
		}

		my $known=0;
		if (defined $action->{reactions} ) {
			$known=1;
			loadReactions($action->{reactions},$actions);
		}

		if (defined $action->{destination} ) {
			$known=1;
			if ($data->{debug} eq "") {
				$::HA->registerToHub('transient.eventHandler',[]);
				$::HA->sendDataToHub($action->{destination},$action->{data});
			}else{
				print "Would have forwarded action on to hub on channel(s): ".join(',',@{$action->{destination}})."<br>\n";
			}
		}

		if (lc $action->{reaction} eq "heyu") {
			$known=1;
			my $cmd = "/usr/local/bin/heyu";

#			print Dumper $action;

			my $module = $action->{house}.$action->{unit};
			if (lc $action->{action}=~m/(on|off)/) {
				$cmd .= " turn $module $action->{action}";
			}
			if (lc $action->{action}=~m/(dim|bright)/) {
				$cmd .= " $action->{action} $module $action->{level}";
			}

			if ($action->{debug} eq "") {
				print `$cmd`;
			}else{
				print "CMD : $cmd<br>\n";
			}
		}
		if (lc $action->{reaction} eq "send_phone_email") {
			$known=1;
			sendPhoneEmail($action);
		}
		
		if (!$known) {
			print "Don't understand action : $action->{reaction}<br>\n";
		}
		print "</div>\n";
	}
}
#================================================================================
sub sendPhoneEmail { 
	my ($action)=@_;

	my $mail="";
	$mail .= "To: ".$action->{to}."\n";
	$mail .= "From: ha\@cattech.org\n";
	$mail .= "Subject: ".$action->{subject}."\n";
	$mail .= "Date: $date\n";
	$mail .= "\n";
	if ($action->{message}) {
		$mail .= $action->{message}."\n";
	}
		
	$mail .= "\@ $date\n";
	$mail .= "\n";
	$mail .= "\n.\n";

	if ($action->{debug} eq "") {
		my $res= open(OUT,"|/usr/sbin/sendmail -f 'ha\@cattech.org' -t");
		if ($res) {
			print OUT $mail;
			close(OUT);
		}
	}else{
		print "<pre>$mail</pre>\n";
	}
}

#================================================================================
#================================================================================
