#!/usr/bin/perl
use strict;
BEGIN { 
	unshift @INC,'/home/websites/lib';
	unshift @INC,'/usr/local/homeAutomation/lib';
	unshift @INC,'./lib';
}
use URI::Escape;
use Cattech::SQLHelper;
use JSON;
use Cattech::HomeAutomation;
require "cgi-lib.pm";
$|=1;


print "Content-type:text/html\n\n";

#{"nodeName":"test","register":["all"]}

my %input;
ReadParse(\%input);

if ( $ENV{HTTP_HOST} eq "" ) {
print "\n\n**COMMAND LINE DEBUG**\n\n\n";
# Popup
#	$input{commonDevice}="{"device":["light","Mal","night stand"]}";
#	$input{id}='ctl_1';
	$input{mode}="popup";
#	$input{image}='lamp.gif';

# Cmd
#	$input{commonDevice}='{"device":["light","Mark","ceiling","net"]}';
#	$input{id}='ctl_7';
#	$input{mode}='cmd';
#	$input{command}='OFF';
#	$input{image}='lamp.gif';

# Status
	$input{commonDevice}='{"device":["light","garage","inside"]}';
	$input{id}='ctl_13';	
	$input{title}='title';
	$input{image}='lamp.gif';
}

#$::Gdebug=1;

$::HA=Cattech::HomeAutomation->new();

if ($::Gdebug) {
	print "\n\n";
}

my $homeautoSH=Cattech::SQLHelper->new('HomeAutomation','homeauto','6C5a3PtSqtNHAACD');

my $mode=$input{mode};
my $title=$input{title};
my $image=$input{image};
my $id=$input{id};
my $commonDevice=$input{commonDevice};

my $baseURL="dispatch.cgi?"
	."commonDevice=".uri_escape($commonDevice)
	."&id=$id"
	."&title=".uri_escape($title)
	."&image=".uri_escape($image)
;

if ($mode eq '') {
	drawButton();
	print "<script>getStatusInABit(2,'commonDevice');</script>\n"
}
if ($mode eq 'popup') {
	popUP();
}
if ($mode eq 'send') {
	sendPacket($input{packet});
}
if ($mode eq 'cmd') {
	command($input{command});
	drawButton();
	print "<script>getStatusInABit(2,'commonDevice');</script>\n"
}
if ($mode eq 'status') {
	getStatus();
}

sub drawButton() {
	print "<div class=\"module\" name=\"act_$input{id}\" onClick=\"move_object_here(this,'ha_popup');\$('\#ha_popup').load('$baseURL&mode=popup');return false;\">\n";

	if ($title) {
		print " <div class=\"title\" name=\"$title\">$title</div>\n";
	}
	print "<img style=\"position:absolute;top:5px;\" src=\"".image()."\" width=100 height=100>\n";

	print " <div class=\"footer\">";
	print "</div>\n";
	print "</div>\n";
}
#====================================================================================================
sub image {
	my $img = "./images/devices/" . $image;
	if (-e $img) {return $img;}

	my $img = "./images/devices/generic.gif";
	return $img;
}
#====================================================================================================
sub moduleTitle {
	my $titleDisp = $title;
	$titleDisp=~s/ /_/g;
	return "title_".$titleDisp;
}
#====================================================================================================
sub loadStatus {
	print "<script>getStatusInABit(2,'x10');</script>\n";
}
#====================================================================================================
sub deviceType {
	if ($commonDevice =~m/(light|lamp)/) {
		return 'lamp';
	}
	if ($commonDevice =~m/(appliance)/) {
		return 'appliance';
	}
	return 'unknown';
}
#====================================================================================================
sub popUP {
	print "<div style=\"border:2px inset grey;background-color:#777777\">\n";
	print " <div class=\"po_dimbri\">\n";

	print "Type : ".deviceType()."<br>\n";

	if (deviceType() eq "lamp") {
		for my $dv (100,50,25,5,0,-5,-25,-50,-100) {
			my $a ="<a href=# "._onClick("DIM",$dv).">";
			$a.="<div style=\"width:100%;height:10%;\">";

			my $fwid = "$dv%";
														
			my $bwid = (100-$fwid)."%";


			my $style="float:left;height:100%;";
			if ($dv > 0) {
				print $a;
				print "	<div style=\"$style;width:$bwid;\"></div>";
				print "	<div style=\"$style;width:$fwid;background-color:#FFFF55\"></div>";
				print "</div></a>\n";
			}
			if ($dv < 0) {
				print $a;
				print "	<div style=\"$style;width:$fwid;background-color:#888855\"></div>\n";
				print "	<div style=\"$style;width:$bwid;\"></div>\n";
				print "</div></a>\n";
			}
			if ($dv == 0) {print "	<div></div>\n";}
		}
		print "</div>\n";
	}

	print " <a class=\"po_button\" "._onClick("ON").">ON</a>\n";
	print "	<br>\n";
	print " <a class=\"po_button\" "._onClick("OFF").">OFF</a>\n";
	print "	<br clear=all>\n";
	print "</div>\n";
}
#====================================================================================================
sub command {
	my ($command,$amt)=@_;

	my $data = {};

	print "<script>console.log('$commonDevice')</script>\n";

	
	my $inDev = decode_json($commonDevice);
	$data->{device}=$inDev->{device};
	$data->{action}=$command;

	if ($amt ne ""){
		$data->{command}->{amount}=$amt;
	}		

	$::Gdebug=1;
	$::HA->registerToHub("dispatch.transient",[]);
	$::HA->sendDataToHub(['DeviceCommandHandler'],"dispatch.transient",$data);

	print "<script>console.log('".encode_json($data)."')</script>\n";

}
sub sendPacket {
	my ($packet)=@_;
	
	$::HA->registerToHub("dispatch.transient",[]);
	$::HA->sendPacketToHub($packet);
}
#====================================================================================================
sub getStatus {
}
#====================================================================================================
#Internal Subs
#================================================================================
sub _onClick{
	my ($command)=@_;
	return "onClick=\"\$('#$id').load('" . $baseURL . "&mode=cmd&command=$command');return(false);\"";
}
#================================================================================
sub _heyu_status {
	# Make sure heyu engine is started
	my %status={};

#              * = On  x = Dimmed
#        Unit:  1..4...8.......16
#  Housecode A (..............*.)
#  Housecode B (***....*......*.)

	my $cmd = " show h";

#	print "$cmd<br>\n";

	open(my $HEYU,"$cmd|");

	while (my $r = <$HEYU>) {
		if ( $r =~ m/Housecode (.) \(([^)]+)/ ) {
			my $hk=$1;
			my $units=$2;
			foreach my $module (1..16) {
				my $dat=substr($units,$module-1,1);
				$status{$hk}{$module}=$dat;
#				print "status - $hk - $module - $dat<br>\n";
			}
		}

	}


	my @houses=();
	foreach my $house ('A'..'P') {
		my @modules=();
		foreach my $module (1..16) {
			my $s="?";
			if (exists $status{$house}{$module}) {
				$s=$status{$house}{$module};
			}
			push @modules,"\"$module\":\"$s\"";
		}
		push @houses,"\"$house\": {" . join(",",@modules) . "}";
	}

	print "{\n" . join("\n,",@houses) . "}";
}
#================================================================================
#================================================================================
1;
