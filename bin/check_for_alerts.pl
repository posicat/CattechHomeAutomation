#!/usr/bin/perl
use strict;
$|=1;

push @INC,'/home/websites/lib';

use Cattech::WatchCat;
require 'cgi-lib.pm';
require 'web-lib.pm';

my @app_names=WatchCat::find_app_names("%");
my $appdata=WatchCat::get_app_data_by_list(@app_names);


my @keys=keys %$appdata;
@keys=sort {$appdata->{$a}->{due_in} <=> $appdata->{$b}->{due_in}} @keys;

foreach my $appname (@keys) {
	if ($appdata->{$appname}->{due_in} < 0) {
		my $msg = "$appname did not check in since $appdata->{$appname}->{last_checkin}\n";
		WatchCat::process_event_alerts($appname,$msg);
	}
}
