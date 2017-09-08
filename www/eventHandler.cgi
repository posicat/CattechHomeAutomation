#!/usr/bin/perl
use strict;
BEGIN { 
	unshift @INC,'./lib';
	unshift @INC,'/home/websites/lib';
	unshift @INC,'/usr/local/homeAutomation/lib';
}
use File::Copy;
use URI::Escape;
use Cattech::SQLHelper;
use Cattech::WatchCat;
use Cattech::HomeAutomation;
use JSON;
use Data::Dumper;
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

$::HA=Cattech::HomeAutomation->new();
$::HA->connectToSQLDatabase();

print "Content-type:text/html\n\n";

#$input{eventLogID}=1015;

print <<EOB;
<style>
	.runningActions	{
		margin:2em;
		border:1px solid blue;
		background-color:#AAA;
	}
</style>
EOB

my $packet={};
if (exists $input{event}) {
	logEventToDB($input{event});
	$packet = JSON->new->utf8->decode($input{event});
}


if ($packet->{destination} eq 'eventHandler') {
	my @actions = findActions($packet->{data});
	executeActions(\@actions,$packet->{data});
}else{
	my $eventLogID = $input{eventLogID};
	print "<h2>Selected event : $eventLogID @ $date</h2>\n";

	my $event="{}";
	if ($eventLogID ne "") {
		(undef,$event,$date) = getEventFromDB($eventLogID);
	}
	$packet = JSON->new->utf8->decode($event);
	$packet->{data}->{debug}='console';
	my @actions=();

	if ($packet->{destination}='eventHandler') {
		@actions = findActions($packet->{data});
	}

	print "<h3>Event : $event</h3>\n";

	print "Triggers [" . join(',',@actions) . "]<br>\n";


	print "<h4>Simulating triggers</h4>\n";

	executeActions(\@actions,$packet->{data});

	displayLastXEvents(30);
}

#================================================================================
sub logEventToDB {
	my ($event) = @_;

	if ($event ne "") {
		my $eventData = { event=>$event };
	        $::HA->{SH}->addupdate_data($eventData,'eventLog');
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
		print "<a href=\"?eventLogID=$eventData->{eventLog_id}\">$eventData->{event} \@ $eventData->{time}</a><br>\n";
	}
}

#================================================================================
sub findActions {
	my ($data)=@_;
	my @actions;

	my $actionData={};
	my $sql = "SELECT event,action FROM triggers WHERE earliestNext <= NOW()";
#	print "SQL : $sql<br>\n";
        $::HA->{SH}->execute_raw_sql($actionData,$sql);

	while ($::HA->{SH}->next_row($actionData)) {
		my $triggerHash = decode_json($actionData->{event});
		print "R :".Dumper($actionData)."<br>\n";
		print "ED:".Dumper($data)."<br>\n";
		print "T :".Dumper($triggerHash)."<br>\n";

		if (matchTrigger($data,$triggerHash)) {
			push @actions,$actionData->{action};
			setTriggerEarliestNext($actionData->{triggers_id});
		}
	}		
	#print "<hr>\n";

	return @actions;
}

#================================================================================
sub matchTrigger {
	my ($eh,$th)=@_;
	my $match=1;

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

#	print Dumper($triggers);

	if (defined $triggers->{frequency}) {
	        $::HA->{SH}->execute_raw_sql(my $db,
			"UPDATE triggers SET earliestNext =NOW() + INTERVAL ".$triggers->{frequency}
			." WHERE triggers_id=".$triggers_id
		);
	}

}
#================================================================================
sub loadReactions { 
	my ($list,$actions)=@_;

	my $reactionData={};
	my $reactions=join(',',@$list);
	my $sql="SELECT action FROM reactions WHERE reactions_id in ($reactions)";
	print "SQL : $sql<br>\n";
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

	foreach my $action (@$actions) { 
		print "<div class=\"runningActions\">\n";
		if ($data->{debug} ne "") {
			print "<h4>Processing action: $action</h4>\n";
		}else{
			print "<h4>Simulating action: $action</h4>\n";
		}

		my $e = decode_json($event);
		my $known=0;
		if (defined $data->{reactions} ) {
			$known=1;
			loadReactions($data->{reactions},$actions);
		}

		if (defined $data->{watchcat} ) {
			$known=1;
			
		}

		if (lc $data->{reactions} eq "heyu") {
			$known=1;
			my $cmd = "/usr/local/bin/heyu";
			my $module = $data->{house}.$data->{unit};
			if (lc $data->{action}=~m/(on|off)/) {
				$cmd .= " turn $module $data->{action}";
			}
			if (lc $data->{action}=~m/(dim|bright)/) {
				$cmd .= " $data->{action} $module $data->{level}";
			}

			if ($execute) {
				print `$cmd`;
			}else{
				print "CMD : $cmd<br>\n";
			}
		}
		if (lc $data->{reaction} eq "send_phone_email") {
			$known=1;
			sendPhoneEmail($data,$e,$execute);
		}
		
		if (!$known) {
			print "Don't understand action : $action<br>\n";
		}
		print "</div>\n";
	}
}
#================================================================================
sub sendPhoneEmail { 
	my ($react,$event,$execute)=@_;

	my $mail="";
	$mail .= "To: ".$react->{to}."\n";
	$mail .= "From: ha\@cattech.org\n";
	$mail .= "Subject: ".$react->{subject}."\n";
	$mail .= "\n";
	$mail .= "\@ $date\n";
	$mail .= "\n";
	$mail .= "\n.\n";

	if ($execute) {
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
