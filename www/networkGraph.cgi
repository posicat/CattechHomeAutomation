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

my %input;
ReadParse(\%input);

$::HA=Cattech::HomeAutomation->new();
$::HA->connectToSQLDatabase();

#print "Content-type:application/json\n\n";
print "Content-type:text/plain\n\n";

my $trafficData={};
my $network={};

$::HA->{SH}->execute_raw_sql($trafficData,
	 "SELECT nt.mac,name,SUM(in_bytes) AS in_bytes,SUM(out_bytes) AS out_bytes"
	." FROM networkTraffic nt"
	." LEFT JOIN networkNames nn ON nn.mac=nt.mac"
	." GROUP BY mac"
);
my $totalIn;
my $totalOut;

while ($::HA->{SH}->next_row($trafficData)) {
	my $name=$trafficData->{mac};
	if ($trafficData->{name} ne "") {
		$name=$trafficData->{name};
	}
	$network->{$name}->{in}= $trafficData->{in_bytes};
	$network->{$name}->{out}= $trafficData->{out_bytes};

	$totalIn+=$trafficData->{in_bytes};
	$totalOut+=$trafficData->{out_bytes};
}

$network->{'Internet'}->{in}= $totalIn;
$network->{'Internet'}->{out}= $totalOut;


my $linkData={};
$::HA->{SH}->execute_raw_sql($linkData,"SELECT node1,node2,connection FROM networkLinks");
while ($::HA->{SH}->next_row($linkData)) {
	foreach my $nn ($linkData->{node1},$linkData->{node2}) {
		my $value=[0,'#555'];;
		if ($linkData->{connection} eq "RG6") { $value=[50,'#000'];}
		if ($linkData->{connection} eq "Cat6") { $value=[15,'#88F'];}
		if ($linkData->{connection} eq "Cat5") { $value=[10,'#555'];}
		if ($linkData->{connection} eq "Wifi") { $value=[0,'#555'];}
		
		$network->{$linkData->{node1}}->{links}->{$linkData->{node2}} = $value;
		$network->{$linkData->{node2}}->{links}->{$linkData->{node1}} = $value;
	}
}

my $data={'nodes'=>[],'links'=>[]};
my $linkDone={};

foreach my $nn (keys %$network) {
	my $node = {};

	$node->{id} = $nn;
	$node->{in} = $network->{$nn}->{in};
	$node->{out} = $network->{$nn}->{out};
	my $ttl=$node->{in}+$node->{out};

	if ($ttl == 0) {$ttl=1;}
	$node->{radius}= 2 + log($ttl)/log(10) / 2 ;

	push @{$data->{nodes}},$node;

	if (! exists $network->{$nn}->{links}) {
		$network->{$nn}->{links}->{'Wireless'}=[1,'#FF5'];
	}

	foreach my $ln (keys %{$network->{$nn}->{links}}) {

		my @c=($nn,$ln);
		my $c1 = '['.join("\t",($nn,$ln)).']';
		my $c2 = '['.join("\t",($ln,$nn)).']';

		my $done=(exists $linkDone->{$c1}) || (exists $linkDone->{$c2});
		if (!$done) {
			my $link = {};
			$link->{source}=$nn;
			$link->{target}=$ln;

#			print Dumper $network->{$nn};
			my @vc = @{$network->{$nn}->{links}->{$ln}};
			$link->{value} = $vc[0];
			$link->{color} = $vc[1];

			push @{$data->{links}},$link;
		}

		$linkDone->{$c1}=1;
		$linkDone->{$c2}=1;
	}
}

my $json = JSON->new->utf8->canonical->indent->encode($data);

print $json;

