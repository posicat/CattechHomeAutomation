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
		$self->{reg}->{db},
		$self->{reg}->{username},
		$self->{reg}->{password}
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
	$self->_transmitDataToTCPHub($json);
}
#================================================================================
sub sendDataToHub {
	my ($self,$channels,$data)=@_;

	my $packet = {
		'destination' => $channels,
		'data' => $data,
	};

	my $json = JSON->new->utf8->canonical->encode($packet);

	if ($self->{transmitMode} eq 'url') {
		my $url = $self->{reg}->{baseUrl};
		$url.="eventHandler.cgi";
		$url.="?event=".URI::Escape::uri_escape($json);
		my $content = LWP::Simple::get $url;
	}
	if ($self->{transmitMode} eq 'hub') {
		$self->_transmitDataToTCPHub($json);
	}
}
#================================================================================
sub getDataFromHub {
	my ($self,$channel,$data)=@_;
}
#================================================================================
sub _transmitDataToTCPHub {
	my ($self,$data)=@_;
	$self->_openTCPSocket();
	print {$self->{_socket}} $data."\r\n";
	print "Sent : $data<br>\n";
}
#================================================================================
sub _openTCPSocket {
	my ($self)=@_;

#	if (! exists $self->{_socket}) {
		$self->{_socket} = IO::Socket::INET->new(
			PeerAddr => $self->{reg}->{'hub.host'},
                        PeerPort => $self->{reg}->{'hub.port'},
                        Proto    => 'tcp'
		) or die "ERROR in Socket Creation : $!\n";

#	}
}
#================================================================================
1;
