#!/usr/bin/perl
use strict;
BEGIN {
        unshift @INC,'./lib';
        unshift @INC,'/home/websites/lib';
        unshift @INC,'/usr/local/homeAutomation/lib';
}

use HomeAutomation::DataLogging;

use HomeAutomation::Database;
use HomeAutomation::DataLogging;
use Cattech::WebSession;
use Data::Dumper;
use JSON;
use Carp;

$SIG{ __DIE__ } = sub { Carp::confess( @_ ) };


require 'cgi-lib.pm';
require 'web_tabs.pm';

#$::Gdebug=1;

# Session and input setup

my %input;
ReadParse(\%input);

if ( $ENV{HTTP_HOST} eq "" ) {
    print "\n\n**COMMAND LINE DEBUG**\n\n\n";
    
    if (1) { # Display Table
        $input{tabtop}=undef;
    }
    if (0) { # Display Graph
        $input{tabtop}='10';
    }
}

$::session=Cattech::WebSession->new();
$::session->setParam('_base','sensors.cgi');
$::session->setParam('tabtop',$input{tabtop});
$::session->setParam('startTime',$input{startTime});
$::session->setParam('endTime',$input{endTime});
$::session->setParam('resolution',$input{resolution});

if ($input{tabtop} eq '') {$input{tabtop}=0;}
if ($input{resolution} eq '') {$input{resolution}=5;}
# End  Session and input setup

my $mainTabs={};
$mainTabs->{tabuid}='top';
set_webtab($mainTabs,0,"Sensors","sensors");
set_webtab($mainTabs,10,"Graph","graph");

print "Content-type:text/html\n\n";

print <<EOB;
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html>
<head>
	<meta content="text/html; charset=utf-8" http-equiv="Content-Type" />
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js" type="text/javascript"></script>
	<script src="lib/Chart.bundle.js"></script>
	<link rel="stylesheet" href="sensors.css"/>
     <script language="javascript" type="text/javascript" src="lib/calendar.js"></script>
EOB

draw_webtabs($mainTabs,
"#FFFFFF,#555566",
"#AAAAFF,#666666",
"#444444",
$::session->url({tabtop=>undef}),"top",$input{tabtop},"100%");

my @dataTables = HomeAutomation::DataLogging::getDataTables();
if ($::Gdebug) {print "Devices : " . Dumper(\@dataTables) . "<br>\n";}

my $devices={};
HomeAutomation::DataLogging::loadDevices(\@dataTables,$devices);

if ($::Gdebug) {print "Devices : " . Dumper($devices) . "<br>\n";}


my $drawTab = $mainTabs->{$input{tabtop}};
if ($drawTab eq 'graph')   { displayGraph($devices); }
if ($drawTab eq 'sensors') { displaySensors($devices); }


print <<EOB;
</body>
</html>
EOB

#================================================================================
sub displayGraph {
	my ($devices)=@_;

    my $limiters = HomeAutomation::DataLogging::returnLimiters($devices,$input);
    
	print "<div style=\"menuBar\">$limiters</div>\n";
	print "<div class=\"pageTable\" style=\"margin-top:3em\">\n";
	print "<canvas id=\"dataGraph\" style=\"width:100%;height:100%\"></canvas>\n";
	print "</div>\n";

    my $start = $input{startTime};
    my $end = $input{endTime};
    my $resolution = 0+$input{resolution};
    my $graphData = HomeAutomation::DataLogging::returnGraphData($devices,$resolution,$start,$end);

print <<EOB;
<script>
function drawChart(id,dat) {
    var ctx = document.getElementById(id).getContext('2d');
    var myLine = new Chart(ctx,dat);
}
drawChart("dataGraph",$graphData);
</script>
EOB

}

#================================================================================
sub occCalendar {
    my ($name)=@_;
    
    return "<img style=\"border:0px;vertical-align:middle\" src=\"images/calendar.gif\" onclick=\"return(popup_calendar(limiters.$name,document.getElementById('cal')))\">\n";

}
#================================================================================
sub displaySensors {
	my ($devices)=@_;

	print "<meta http-equiv=\"refresh\" content=\"60\">\n";

	my $data={};

	foreach my $section (keys %$devices) {
		displaySectionHeader($section);
		foreach my $device (keys %{$devices->{$section}}) {
			displaySensor($section,$device,$devices->{$section}->{$device});
		}
	}

}

#================================================================================
sub displaySectionHeader {
	my ($section)=@_;
	print "<div class=\"section\">$section</div>\n";
}
#================================================================================
sub displaySensor {
	my ($section,$id,$sensor)=@_;

	my $sensorLoad=HomeAutomation::DataLogging::returnSensorData($section,$id,$sensor);

	print "<div class=\"sensor\" id=\"$id\">$sensorLoad</div>";
}

#================================================================================
sub ageFormat {
	my ($time) = @_;

	my ($h,$m,$s)=($time=~m/(\d+):(\d+):(\d)/);


	#print "[$h , $m , $s]\n";

	my $ago='';
	my $style='';

	if ($h > 0) {
		$ago.=0+$h." hrs ";
		$style="background-color:#400;";
	}elsif ($m > 0) {
		$ago.=0+$m." min ";

		if ($m > 10) {
			$style="background-color:#440;";
		}
		if ($m > 30) {
			$style="background-color:#400;";
		}

	}elsif ($s > 0) {
		$ago.=0+$s." sec ";
	}

	my $ret = "<span style=\"$style\">";
	if ($ago ne '') {
		$ret .= "$ago ago";
	}
	$ret .= "</span>\n";

	return $ret;
}
#================================================================================