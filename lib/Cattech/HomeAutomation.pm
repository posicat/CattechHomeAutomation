use strict;

use Cattech::SQLHelper;
use JSON;
use LWP::Simple;
use URI::Escape;
use IO::Socket::INET;;

package Cattech::HomeAutomation;
#================================================================================
sub new {
	my ($class,$channelName)=@_;

	my $self={
		SH => {},
		reg => {},
		transmitMode =>  'hub',
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
sub registerToHub {
	my ($self,$nodeName,$channels)=@_;

	my $packet = {
		'register' => $channels,
		'nodeName' => $nodeName,
	};

	my $json = JSON->new->utf8->canonical->encode($packet);
	$self->sendPacketToHub($json);
}
#================================================================================
sub sendDataToHub {
	my ($self,$channels,$sourceChannel,$data)=@_;

	my $packet = {
		'destination' => $channels,
		'source' => $sourceChannel,
		'data' => $data,
	};

	my $json = JSON->new->utf8->canonical->encode($packet);

	$self->sendPacketToHub($json);
}
#================================================================================
sub getDataFromHub {
	my ($self,$channel,$data)=@_;
}
#================================================================================
sub sendPacketToHub {
	my ($self,$data)=@_;
	$self->_openTCPSocket();

	print {$self->{_socket}} $data."\r\n";

	if ($::Gdebug) {
		print "Sent : $data<br>\n";
	}
}
#================================================================================
sub _openTCPSocket {
	my ($self)=@_;

	if ($::Gdebug) {
		print $self->{reg}->{'hub.host'}."\r\n";
		print $self->{reg}->{'hub.port'}."\r\n";
	}
	if (! exists $self->{_socket}) {
		$self->{_socket} = IO::Socket::INET->new(
			PeerAddr => $self->{reg}->{'hub.host'},
                        PeerPort => $self->{reg}->{'hub.port'},
                        Proto    => 'tcp'
		) or die "ERROR in Socket Creation : $!\n";

	}
}
#================================================================================
1;
