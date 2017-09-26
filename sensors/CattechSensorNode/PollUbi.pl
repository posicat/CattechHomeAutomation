#!/usr/bin/perl
use lib '/home/websites/lib/';
use Cattech::SQLHelper;
use JSON;
use Data::Dumper;
use strict;
use LWP::Simple;

$::Gdebug=1;

my $ubiNodeID=37;
my $accessToken='bc95b5f2-fc6f-4d28-8782-7075889cb9ce';
my $ubiID='651e8e482b34db1a';

my $sensorNode=Cattech::SQLHelper->new('CattechSensorNode','sensors','4JdzYzea2pWrYusp');

# Get the UBI ID when necessary
#my $url = "https://portal.theubi.com/v2/ubi/list?access_token=$accessToken";
#my $myUbis = get($url);
#print "M: $myUbis\n";

while (1==1) {
	my $serverResponse  = get("https://portal.theubi.com/v2/ubi/$ubiID/sensors?access_token=$accessToken");
	my $response = from_json($serverResponse);

	my $data=$response->{result}->{data};

	foreach my $k (keys %$data) {
		print "K: $k = $data->{$k}\n";

		my $name='';
		my $val = $data->{$k};

		if ($k eq 'temperature') {
			$name='temp';
			$val=int($val * 9 / 5 + 32);
		}
		if ($k eq 'humidity') {$name='humid';}
		if ($k eq 'airpressure') {$name='baro';}
		if ($k eq 'soundlevel') {$name='sound';}
		if ($k eq 'light') {$name='light';}

		if ($name ne '') {
		        my $sensor_data={
        			'node'=>$ubiNodeID,
	        		'name'=>$name,
	        		'value'=>$val,
			        'subNode'=>0,
		        };

			foreach my $sk (keys %$sensor_data) {
				print "\t$sk = $sensor_data->{$sk}\n";
			}
			$sensorNode->addupdate_data($sensor_data,'sensorData');
		}
	}
	sleep(600);
}
