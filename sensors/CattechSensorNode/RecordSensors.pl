#!/usr/bin/perl
use lib '/home/websites/lib/';
require Cattech::WatchCat;
use Cattech::SQLHelper;
use JSON;
use Data::Dumper;
use strict;

$::Gdebug=1;
$::appName="RecordSensors\@pawz.cattech.org";

my $sensorPort = "/dev/cattech_sensornode";

system("stty 9600 -echo -F $sensorPort");

my $sensorNode=Cattech::SQLHelper->new('CattechSensorNode','sensors','4JdzYzea2pWrYusp');

open(my $SENSOR,"<:crlf",$sensorPort);

while (my $read=<$SENSOR>) {
	$read=~s/[\r\n]$//;

	if ($read ne "") {
		if ($::Gdebug) { print "R: $read\n";}

		#my $Jdata=decode_json($read);

		$read=~s/^\{|\}$//g;
		my @data=split(/,/,$read);

		my %info=();
		foreach my $blk (@data) {
#			if ($::Gdebug) { print "\tB: $blk\n"};
			my ($var,$val)=split(/:/,$blk);
			$var=~s/^"|"$//g;
			$val=~s/^"|"$//g;
			#if ($::Gdebug) { print "\tV: $var = $val\n";}
			$info{$var}=$val;
		}
		if (exists $info{node}) {

			updateWatch('node_'.$info{node});
			updateWatch('gateway');

			foreach my $k (keys %info) {
				if ($::Gdebug) { print "		K: '$k' = '$info{$k}' ";}

				if (        $k=~m/^(node|address|serverAddress|setup|debug|error)$/i ||
				     $info{$k}=~m/^( nan)$/i
				) {
					if ($::Gdebug) { print "SKIP ";}
				}else{
					if ($::Gdebug) { print "WRITE ";}
					my ($name,$subNode)=split('_',$k,2);

					my $sensor_data={
						'node'=>$info{node},
						'name'=>$name,
						'value'=>$info{$k},
						'subNode'=>int($subNode),
					};
					if ($name ne '') {
						my $g=$::Gdebug;
						$::Gdebug=0;
						$sensorNode->addupdate_data($sensor_data,'sensorData');
						$::Gdebug=$g;
					}
				}
				if ($::Gdebug) { print "\n";}
			}
		}
	}
}

sub updateWatch {
	my ($name)=@_;

	$name = $name.'.'.$::appName;

        my ($update) = WatchCat::get_last_update($name);
        if (! defined $update) {
	        WatchCat::create_new_entry($name,'1 HOUR');
        }
        return WatchCat::update_entry($name);
}



