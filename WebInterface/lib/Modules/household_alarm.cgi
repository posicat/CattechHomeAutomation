#!/usr/bin/perl
#use strict;

$|=1;

require "cgi-lib.pm";
my %input;
ReadParse(\%input);

$alarmscript="/etc/x10.init.d/B14_any";
$thiscgi="modules/household_alarm.cgi?name=$input{name}&id=$input{id}";
chdir("/etc/x10.init.d/");

print "Content-type:text/html\n\n";

if ($input{mode} eq "OFF")
{
	$script_out=`$alarmscript Off`;
	$input{mode}="";
}
if ($input{mode} eq "ON")
{
	$script_out=`$alarmscript On`;
	$input{mode}="";
}

$alarm_status=`$alarmscript Status`;
chomp $alarm_status;

$style="background-color:red;";
if ($alarm_status eq "Off") { $style="background-color:#884444;color:#ffffff;";}
if ($alarm_status eq "On") { $style="background-color:#448844;color:#ffffff;";}

# General status and interface controls

if ($input{mode} eq "popup")
{
	sub oc
	{ my ($mode)=@_;

		return "onClick=\"load_id('ctl_$input{id}','$thiscgi&mode=$mode');return(false);\";";
	}

	print "<div style=\"border:2px inset grey;background-color:#777777\">\n";
	print " <div class=\"titlebar\" style=\"color:white;\">$input{name}</div>\n";
	print " <div class=\"x10_po_dimbri\">\n";

	print " <a class=\"x10_po_button\" ".oc("ON").">ON</a>\n";
	print "	<br>\n";
	print " <a class=\"x10_po_button\" ".oc("OFF").">OFF</a>\n";
	print "	<br clear=all>\n";
	print "</div>\n";
}

if ($input{mode} eq "")
{

	print "<div class=\"x10_module\" onClick=\"move_object_here(this,'ha_popup');load_id('ha_popup','$thiscgi&mode=popup');\">\n";
	print "<img style=\"position:absolute;z-index:-1;\" src=\"images/x10/mod_B14.gif\" width=100% height=100%>";
	print "	<div class=\"titlebar\" style=\"$style;\">$input{name}</div>\n";
	print "	<div class=\"x10_module\">\n";
	print "		<span class=over_image_text>$alarm_status<span>\n";
	print "	</div>\n";
#	print $script_out;
	print "</div>\n";
}

