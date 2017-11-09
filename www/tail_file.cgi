#!/usr/bin/perl
BEGIN {
        unshift @INC,'./lib';
        unshift @INC,'/home/websites/lib';
        unshift @INC,'/usr/local/homeAutomation/bin/lib';
}

use strict;
use POSIX qw(strftime);
use HTML::Entities;
require "cgi-lib.pm";
$|=1;

my $max_transmit_chars = 100000;
my $script="./tail_file.cgi";

my %input;
ReadParse(\%input);

my $offset=scalar $input{offset};

my $first=0;
if ($offset == 0) {$first=1;}
if ($offset == -1) {$first=1;}

print "Content-type:text/html\n";
print "\n";

print "<!DOCTYPE html>\n";

if ($first) {
        print "<script language=\"javascript\" type=\"text/javascript\" src=\"/include/jquery.js\"></script>\n";
        print "<script language=\"javascript\" type=\"text/javascript\" src=\"./include/tail_file.js\"></script>\n";
        print "<body style=\"background-color:#777\">\n";
        print "<tt>\n";
}

#$input{fname} ='[log]/HomeautomationHub.log';
#$input{highlights} ='./highlights/homeAutomationHighlights.txt';

my $fname = expandRootPaths($input{fname});
my $HLFile = expandRootPaths($input{highlights});
my $header=$input{header};

my %highlights;

#print "FileName : $input{fname} -&gt; $fname<br>\n";
#print "Highlights : $input{highlights} -&gt; $HLFile<br>\n";

if ($HLFile ne '') {
        if (open my $IN,'<',$HLFile) {
                while (my $read=<$IN>) {
                        chomp($read);
                        my $SL='(?<!\\\\)[\/]';
                        my ($match,$replace)=($read=~m/$SL(.*)$SL(.*)$SL/);

                        $match=~s/(\\\/)/\//g;
                        $replace=~s/(\\\/)/\//g;
                        $highlights{$match}=$replace;
                        #print encode_entities("M: $match // R: $replace")."<br>\n";
                }
        }else{
                print "Couldn't open $HLFile : $!<br>\n";
        }
}

#print "<hr>\n";

my $report = 0;
my $lines=0;

if ($first) {
        print "<div class=\"floatingHeader\">\n";

        if ($header eq '') {
                print "<div style=\"background-color:#FFF;color:#000;text-align:center;font-size:150%\">Viewing File : $input{fname}</div>\n";
                $lines+=1.5;
        }
        if ($header eq 'simple') {
                my $simpleName=$input{fname};

                $simpleName=~s/^\[[^\/]*\]\/[^\/]*\///;
                $simpleName=~s/\/tailLog\/.*//;

                print "<div style=\"background-color:#FFF;color:#000;text-align:center;font-size:100%\"> <a href=\"tail_file.cgi?fname=$input{fname}&highlights=$input{highlights}&offset=0\" target=\"_blank\">$simpleName</a></div>\n";
                $lines+=1;
        }

        if ($offset == -1) {
                $offset = -s $fname;
                if ($header eq '') {
                        print "<div style=\"background-color:#FFF;color:#777;text-align:center;\">Begining at end of current file, <a href=\"tail_file.cgi?fname=$input{fname}&highlights=$input{highlights}&offset=0\">Load from Beginning</a></div>\n";
                $lines+=1;
                }
        }

        print "</div>\n";
        print "<div style=\"height:".$lines."em\">&nbsp;</div>\n";

        if ($header eq 'simple') {
                print "<div style=\"font-size:75%;\">\n";
        }
}


tail_file($fname,$offset);

if ($first) {
        print "</tt>\n";
}

#--------------------------------------------------------------------------------
sub tail_file {
        my ($fname,$start)=@_;

        my $offset=0;
        my $error_message="";

        my $IN;
        if (! open( $IN, '<', $fname )) {
                $error_message.="<span style=\"color:red;\">Could not open $fname : $!</span><br>";
        }else{
                seek($IN,$start,0);
                my $read;

                read( $IN,$read,$max_transmit_chars );

                $read=encode_entities($read);

                foreach  my $hl (keys %highlights) {
                        my $rep=$highlights{$hl};
                        $rep=~s/["]/\\"/g;
                        $read=~s/$hl/eval '"'.$rep.'"'/eg;
                }

                $read=~s/(\r|\n)+/<br>/msg;
                if ($read ne '') {
                        print "<pre style=\"display:inline;\">$read</pre>";
                }

                $offset=tell($IN);
                close $IN;
        }

        my $delay=2000; # Sleep for a few seconds by default

        if ($offset > $start) {
                $delay=1; # Don't wait for next block, get it immediately if we found new data
        }

        if (defined $start) {
                print "<span id=\"nextchunk\" style=\"color:green;\" title=\"$delay ms\">$error_message(waiting for more data)</span>\n";
                print "<script>loadNextTail(\"$input{fname}\",\"$input{highlights}\",$offset,$delay);</script>\n";
        }else{
                print $error_message;
        }
}
#--------------------------------------------------------------------------------
sub expandRootPaths {
	my ($fn)=@_;

	my $logFolder='/usr/local/homeAutomation/log';

	$fn=~s/\[log\]/$logFolder/g;

	return $fn;
}
#--------------------------------------------------------------------------------
#--------------------------------------------------------------------------------

