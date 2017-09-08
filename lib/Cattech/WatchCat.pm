#!/usr/bin/perl
use strict;
use DBI;
use Cattech::SQLHelper;
my $delayBetweenAlerts = 60*60*24;

package WatchCat;
my $dbh;

BEGIN {
	my $homeautoSH=Cattech::SQLHelper->new('HomeAutomation','homeauto','6C5a3PtSqtNHAACD');
	$dbh = $homeautoSH->{dbh};
}
END {
	$dbh->disconnect();
}

#====================================================================================================
#====================================================================================================
sub update_entry {
	my ($app)=@_;

	my @freqArr = get_frequency_array($app);
	my $expect_checkin='NOW()';
	foreach my $l (@freqArr) {
		if ($l ne '') {
			$expect_checkin="DATE_ADD($expect_checkin, INTERVAL $l)";
		}
	}

	my $sql = "UPDATE watchcat SET last_checkin=NOW(),expected_checkin=$expect_checkin WHERE application_name = ?";

	#print "SQL : $sql\n\n";

	my $result = $dbh->do($sql,undef,$app);

	return $result;
}
#====================================================================================================
sub update_last_alert {
	my ($app)=@_;

	my @freqArr = get_frequency_array($app);
	my $expect_checkin='NOW()';
	foreach my $l (@freqArr) {
		if ($l ne '') {
			$expect_checkin="DATE_ADD($expect_checkin, INTERVAL $l)";
		}
	}

	my $sql = "UPDATE watchcat SET lastAlert=NOW() WHERE application_name = ?";

	my $result = $dbh->do($sql,undef,$app);

	return $result;
}
#====================================================================================================
sub create_new_entry {
	my ($app,$frequency)=@_;
	my $sql = "INSERT INTO watchcat (application_name,frequency) VALUES (?,?)";
	my $result = $dbh->do($sql,undef,($app,$frequency));

	return $result;
}
#====================================================================================================
sub get_last_update {
	my ($app)=@_;

	my $sql = "SELECT last_checkin FROM watchcat WHERE application_name = ?";

	my $sth = $dbh->prepare($sql);
	$sth->execute($app);
	my ($result) = $sth->fetchrow_array();

	return $result;
}
#====================================================================================================
sub get_frequency_array {
	my ($app)=@_;

	my $sql = "SELECT frequency FROM watchcat WHERE application_name = ?";

	my $sth = $dbh->prepare($sql);
	$sth->execute($app);
	my ($result) = $sth->fetchrow_array();

	my $single_entry = '[0-9]+\s*(?:SECOND|MINUTE|HOUR|DAY|MONTH|YEAR)(?=S|)(?=,|)\s*|';
	my (@r)=($result=~m/($single_entry)/g);

	if (! @r) {
		warn("Frequency format is invalid, terminating. ($result)");
	}

	return @r;
}
#====================================================================================================
sub find_app_names {
	my ($appFilter)=@_;

	my $sql = "SELECT application_name FROM watchcat WHERE application_name like ?";

	my $sth = $dbh->prepare($sql);
	$sth->execute($appFilter);

	my @result;
	while (my ($app)=$sth->fetchrow_array) {
		push @result,$app;
	}
	return @result;
}
#====================================================================================================
sub get_app_data_by_list {
	my @appList=@_;

	
	my $sql = "SELECT *,-TIMESTAMPDIFF(SECOND,expected_checkin,NOW()) due_in FROM watchcat WHERE application_name in (".
		'?,' x (@appList-1) ."?)";

	my $sth = $dbh->prepare($sql);

	$sth->execute(@appList);

	my %result;
	while (my $hr=$sth->fetchrow_hashref) {
		$hr->{'Due'}=in_words($hr->{due_in});
		$result{$hr->{application_name}}=$hr;
	}
	return \%result;
}
#====================================================================================================
sub in_words {
	my ($val) = @_;
	my $ago;

	if ($val < 0) {
		$ago=" ago";
		$val=-$val;
	}

	if ($val < 60) {
		return sprintf("%d s.$ago",$val);
	}
	$val=$val/60;
	if ($val < 60) {
		return sprintf("%.1f m$ago",$val);
	}
	$val=$val/60;
	if ($val < 60) {
		return sprintf("%.1f hr$ago",$val);
	}
	$val=$val/24;
	return sprintf("%.1f d$ago",$val);

}
#====================================================================================================
sub process_event_alerts {
	my ($app,$message)=@_;

#	print "----- Sending alert for $app\n";
#	print "Msg : $message\n";

	my $sql = "SELECT alertTypes,-TIMESTAMPDIFF(SECOND,lastAlert,NOW()) sinceLastAlert FROM watchcat WHERE application_name = ?";

	my $sth = $dbh->prepare($sql);
	$sth->execute($app);
	my ($alertTypes,$sinceLastAlert) = $sth->fetchrow_array();

#	print "Last Alert : $sinceLastAlert\n";
	if ($sinceLastAlert < -$delayBetweenAlerts) {

		my @types = split(/[\r\n]/msg,$alertTypes);

		foreach my $t (@types) {
			my ($cmd,$data)=($t=~m/(.*?):(.*)/);
#			print "> $data\n";

			my $json="";

			#if ($mode eq 'url') {
			        my $url = "http://pawz.cattech.org/pw/homeAutomation/eventHandler.cgi?event=".uri_escape($json);
			        my $content = get $url;
			#}



			if ($cmd eq "email") {
	        	        my $email;
				$email .= "To: $data\n";
        	        	$email .= "From: root\@cattech.org\n";
	                	$email .= "Subject: [WatchCat] $app\n";
        	       		$email .= "\n";
                		$email .= $message;
	               		$email .= "\n.\n";

			        if ( open(my $SENDMAIL,"|/usr/sbin/sendmail -f'root\@catbox.com' -t") ) {
					print $SENDMAIL "$email";
					close $SENDMAIL;
				}else{
			                print "Could not execute sendmail\n";
			        }
			}
		}
		update_last_alert($app);
	}

}
#====================================================================================================
#====================================================================================================
1;
