#!/usr/bin/perl
BEGIN { 
	unshift @INC,'./lib';
	unshift @INC,'/home/websites/lib';
}
use File::Copy;
use URI::Escape;
use Cattech::SQLHelper;
#use strict;
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
#print "<base href=\"/pw/homeAutomation/\">\n";

my $mf = $input{moduleFile};

if ($mf ne '') {
	$mf="./images/".$mf;
	print "MF : $mf<br>\n";

	if (-e $mf) {
		rename $mf,"$mf.bak";
		print "Rename : $!\n";
		open(my $OUT,">",$mf);
		print $OUT $input{upload};
		close ($OUT);
	}
}

my $modules={};
my $mod={};

$homeautoSH->select_data($mod,'device');
while ( $homeautoSH->next_row($mod) ) {
	my $type=$mod->{type};

	print "$type\n";

	eval "require Modules::$type";
	$m = "Modules::$type"->new($mod->{data});
	my $img = $m->image();

	print "<form METHOD=POST ENCTYPE=multipart/form-data action=\"index.cgi\"><div>\n";
	print "<input type=hidden name=\"moduleFile\" value=\"$img\">\n";
	print "$img - ";
	print "<img src=\"$img\" width=100 height=100 ><br>\n";
	print "<input type=\"file\" name=\"upload\" id=\"upload\">\n";
	print "</form>\n";
	print "<input type=submit value=\"upload\">\n";
	print "</div>\n";
	print "<hr>\n";
}
