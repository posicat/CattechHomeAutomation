use strict;

use Cattech::SQLHelper;
use JSON;
use LWP::Simple;
use URI::Escape;
use IO::Socket::INET;
use Data::Dumper;

package HomeAutomation::Database;
#================================================================================
sub new {
	my ($class,$channelName)=@_;

	my $self={
		SH => {},
		reg => {},
	};
	bless $self,$class;

	if (open (my $IN,"<","/etc/homeAutomation/settings.conf")) {
		while (my $read=<$IN>) {
			$read=~s/[\r\n]*$//;
			my ($val,$var)=split('=',$read,2);
			$self->{reg}->{$val}=$var;
		}
	}else{
		die "Can't load config file : $!\n";
	}
	return $self;
}
#================================================================================
sub connectToSQLDatabase {
	my ($self)=@_;
	$self->{SH}=Cattech::SQLHelper->new(
		$self->{reg}->{'db.name'},
		$self->{reg}->{'db.username'},
		$self->{reg}->{'db.password'}
	);

}
#================================================================================
1;
