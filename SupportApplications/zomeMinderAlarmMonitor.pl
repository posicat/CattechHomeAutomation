#!/usr/bin/perl -w

use strict;

use ZoneMinder;
use warnings;
use DBI;

$| = 1;


my $driver = "mysql";
my $database = "zm";
my $user = "zmuser"; 
my $password = "zmpass";

my $dbh = DBI->connect(
"DBI:$driver:$database",
$user, $password,
) or die $DBI::errstr;

my $sql = "select M.*, max(E.Id) as LastEventId from Monitors as M left join Events as E on M.Id = E.MonitorId where M.Function != 'None' group by (M.Id)";
my $sth = $dbh->prepare_cached( $sql ) or die( "Can't prepare '$sql': ".$dbh->errstr() );

my $res = $sth->execute() or die( "Can't execute '$sql': ".$sth->errstr() );
my @monitors;
while ( my $monitor = $sth->fetchrow_hashref() )
{
    push( @monitors, $monitor );
}

while( 1 ) 
{
    foreach my $monitor ( @monitors )
    {   
        next if ( !zmMemVerify( $monitor ) );
 
        if ( my $last_event_id = zmHasAlarmed( $monitor, $monitor->{LastEventId} ) ) 
        {   
            $monitor->{LastEventId} = $last_event_id;
            print( "Monitor ".$monitor->{Name}." has alarmed\n" );
            #   
            # Do your stuff here
            #   
        }   
    }   
    sleep( 1 );
}

