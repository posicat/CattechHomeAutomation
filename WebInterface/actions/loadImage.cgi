#!/usr/bin/perl
use strict;
require "cgi-lib.pm";
use GD;

$|=1;

print "Content-type:image/png\n\n";

$image = GD::Image->newFromPng($input{file}, 1);

print $image->png();
