use strict;

use Cattech::SQLHelper;

package HomeAutomation::HubCommunication;

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
