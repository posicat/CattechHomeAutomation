#!/usr/bin/perl
use strict;
BEGIN { 
	unshift @INC,'/home/websites/lib';
	unshift @INC,'./lib';
}
use URI::Escape;
use Cattech::SQLHelper;
use JSON;
require "cgi-lib.pm";
$|=1;

print "Content-type:text/html\n\n";
my %input;
ReadParse(\%input);

if ( $ENV{HTTP_HOST} eq "" ) {
print "\n\n**COMMAND LINE DEBUG**\n\n\n";
# Popup
#	$input{devname}="posiBed.lamp";
#	$input{id}='ctl_1';
#	$input{type}='x10';
#	$input{data}='{"house":"B","unit":"10","type":"lamp"}';
#	$input{mode}="popup";

# Cmd
#	$input{data}='{"house":"B","unit":"10","type":"lamp"}';
#	$input{id}='ctl_7';
#	$input{type}='x10';
#	$input{mode}='cmd';
#	$input{command}='OFF';

# Status
	$input{devName}='garage.lights';
	$input{id}='{ctl_13}';	
}

#$::Gdebug=1;


if ($::Gdebug) {
	print "\n\n";
}

my $homeautoSH=Cattech::SQLHelper->new('HomeAutomation','homeauto','6C5a3PtSqtNHAACD');

my $data=$input{data};
my $type=$input{type};
my $mode=$input{mode};
my $title=$input{title};
my $id=$input{id};
my $devname=$input{devName};

if ($devname ne '') {
	my $mod={};
	$homeautoSH->execute_raw_sql($mod,
		"SELECT * FROM `device` WHERE name=?",($input{devName})
	);
	if ($homeautoSH->next_row($mod) ) {
		$data=$mod->{data};
		$type=$mod->{type};
		$title=$mod->{title};
	}
}

eval "require Modules::$type";
my $m = "Modules::$type"->new($data);
$m->{id}=$id;
$m->{devname}=$devname;

my $url="dispatch.cgi?"
	."devName=$devname"
	."&id=$id"
	."&type=$type"
	."&title=".uri_escape($title)
	."&data=".uri_escape($data)
;

if ($mode eq '') {
	drawButton();
	print "<script>getStatusInABit(2,'x10');</script>\n"
}
if ($mode eq 'popup') {
	$m->popUP();
}
if ($mode eq 'cmd') {
	$m->command($input{command});
	drawButton();
	print "<script>getStatusInABit(2,'x10');</script>\n"
}
if ($mode eq 'status') {
	$m->getStatus();
}

sub drawButton() {
	print "<div class=\"x10_module\" name=\"act_$input{id}\" onClick=\"move_object_here(this,'ha_popup');\$('\#ha_popup').load('$url&mode=popup');return false;\">\n";

	if ($title) {
		print " <div class=\"x10_title\" name=\"".$m->moduleTitle()."\">$title</div>\n";
	}
	print "<img style=\"position:absolute;top:5px;\" src=\"" . $m->image() . "\" width=100 height=100>\n";

	print " <div class=\"x10_footer\">";
	print $m->deviceID();
	print "</div>\n";
	print "</div>\n";
}
