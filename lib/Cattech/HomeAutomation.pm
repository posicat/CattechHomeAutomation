#!/usr/bin/perl
use strict;
use Cattech::SQLHelper;
use JSON;
use LWP::Simple;
use URI::Escape;

package Cattech::HomeAutomation;
#================================================================================
sub new {
	my ($class,$channelName)=@_;

	my $self={
		SH => {},
		reg => {},
		channelName => $channelName,		
		transmitMode =>  'url',
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
}
#================================================================================
sub sendDataToHub {
	my ($self,$channel,$data)=@_;

	my $packet = {
		'destination' => $channel,
		'data' => $data,
	};

	my $json = JSON->new->utf8->canonical->encode($packet);

	if ($self->{transmitMode} eq 'url') {
		my $url = $self->{reg}->{baseUrl};
		$url.="eventHandler.cgi";
		$url.="?event=".URI::Escape::uri_escape($json);

		my $content = LWP::Simple::get $url;
	}
}
#================================================================================
sub getDataFromHub {
	my ($self,$channel,$data)=@_;
}
#================================================================================
#================================================================================
1;
