#!/usr/bin/perl
use strict;
BEGIN { 
	unshift @INC,'./lib';
	unshift @INC,'/home/websites/lib';
}
use File::Copy;
use URI::Escape;
use Cattech::SQLHelper;
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

my $homeautoSH=Cattech::SQLHelper->new('HomeAutomation','homeauto','6C5a3PtSqtNHAACD');

print "Content-type:text/html\n\n";

#$input{eventLogID}=1015;

my $eventLogID = $input{eventLogID};
my $event = {};
my $date = "";
if ($eventLogID eq "") {
	($eventLogID,$event,$date) = getLastEventFromDB();
}else{
	(undef,$event,$date) = getEventFromDB($eventLogID);
}
print "<h2>Selected event : $eventLogID @ $date</h2>\n";
print "<h3>$event</h3>\n";

if ($input{event} ne "") {

	logEventToDB($input{event});
	my @reactions = findReactions($input{event});
	print "Triggers [" . join(',',@reactions) . "]<br>\n";

	executeReactions(\@reactions,$input{event},1);

}else{

	my @reactions = findReactions($event);
	print "Would have matched triggers [<br>&nbsp;&nbsp;" . join("<br>&nbsp;&nbsp;",@reactions) . "<br>]<br>\n";

	executeReactions(\@reactions,$event,0);

}

displayLastXEvents(20);

#================================================================================
sub logEventToDB {
	my ($event) = @_;

	if ($event ne "") {
		my $eventData = { event=>$event };
	        $homeautoSH->addupdate_data($eventData,'eventLog');
	}
}

#================================================================================
sub getLastEventFromDB {
	my $eventData={};
	my $event="";
        $homeautoSH->execute_raw_sql($eventData,"SELECT * FROM eventLog ORDER BY time DESC LIMIT 1");
	$homeautoSH->next_row($eventData);

	return ($eventData->{eventLog_id},$eventData->{event},$eventData->{time});
}
#================================================================================
sub getEventFromDB {
	my ($eventLogID)=@_;
	my $eventData={};
	my $event="";
        $homeautoSH->execute_raw_sql($eventData,"SELECT * FROM eventLog WHERE eventLog_id=$eventLogID");
	$homeautoSH->next_row($eventData);

	return ($eventData->{eventLog_id},$eventData->{event},$eventData->{time});
}
#================================================================================
sub displayLastXEvents {
	my ($limit)=@_;
	my $eventData={};
	my $event="";
        $homeautoSH->execute_raw_sql($eventData,"SELECT * FROM eventLog ORDER BY time DESC LIMIT $limit");

	while ($homeautoSH->next_row($eventData)) {
		print "<a href=\"?eventLogID=$eventData->{eventLog_id}\">$eventData->{event} \@ $eventData->{time}</a><br>\n";
	}
}

#================================================================================
sub findReactions {
	my ($event)=@_;
	my @reactions;
	my $eventHash = decode_json($event);

	my $reactionData={};
	my $sql =
		"SELECT t.event,t.triggers_id,reaction FROM triggers t "
		." LEFT JOIN actions a ON t.triggers_id=a.triggers_id "
		." LEFT JOIN reactions r ON a.reactions_id=r.reactions_id "
		." WHERE earliestNext <= NOW() ";
#	print "SQL : $sql<br>\n";
        $homeautoSH->execute_raw_sql($reactionData,$sql);

#	print "<hr>\n";
	while ($homeautoSH->next_row($reactionData)) {
		my $triggerHash = decode_json($reactionData->{event});
#		print "R:".Dumper($reactionData)."<br>\n";
#		print "E:".Dumper($eventHash)."<br>\n";
#		print "T:".Dumper($triggerHash)."<br>\n";

		if (matchTrigger($eventHash,$triggerHash)) {
			push @reactions,split(/\0/,$reactionData->{reaction});
			setTriggerEarliestNext($reactionData->{triggers_id});
		}
	}		
	#print "<hr>\n";


	return @reactions;
}

#================================================================================
sub matchTrigger {
	my ($eh,$th)=@_;
	my $match=1;

	foreach my $key (keys %$th) {
#		print "TH[$key] $th->{$key} =~m/ $eh->{$key} /<br>\n";

		if (! ($eh->{$key} =~ m/$th->{$key}/)) {
#			print "No regex match<br>\n";
			$match=0;
		}
		if (! exists $eh->{$key}) {
#			print "No key $key<br>\n";
			$match=0;
		}
	}

	return $match;
}
#================================================================================
sub setTriggerEarliestNext {
	my ($triggers_id)=@_;

	my $triggers={triggers_id=>$triggers_id};
	$homeautoSH->select_data($triggers,'triggers');
        $homeautoSH->next_row($triggers);

	print Dumper($triggers);

	if (defined $triggers->{frequency}) {
	        $homeautoSH->execute_raw_sql(my $db,
			"UPDATE triggers SET earliestNext =NOW() + INTERVAL ".$triggers->{frequency}
			." WHERE triggers_id=".$triggers_id
		);
	}

}
#================================================================================
sub executeReactions {
	my ($reactions,$event,$execute)=@_;

	foreach my $reaction (@$reactions) { 
		if ($execute) {
			print "Processing reaction: $reaction<br>\n";
		}else{
			print "Simulating reaction: $reaction<br>\n";
		}

		my $r = decode_json($reaction);
		my $e = decode_json($event);
		my $known=0;
		if (lc $r->{reaction} eq "heyu") {
			$known=1;
			my $cmd = "/usr/local/bin/heyu";
			my $module = $r->{house}.$r->{unit};
			if (lc $r->{action}=~m/(on|off)/) {
				$cmd .= " turn $module $r->{action}";
			}
			if (lc $r->{action}=~m/(dim|bright)/) {
				$cmd .= " $r->{action} $module $r->{level}";
			}

			if ($execute) {
				print `$cmd`;
			}else{
				print "CMD : $cmd<br>\n";
			}
		}
		if (lc $r->{reaction} eq "send_phone_email") {
			$known=1;
			sendPhoneEmail($r,$e,$execute);
		}
		
		if (!$known) {
			print "Don't understand reaction : $r->{reaction}<br>\n";
		}
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
