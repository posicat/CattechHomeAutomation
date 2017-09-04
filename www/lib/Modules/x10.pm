package Modules::x10;
$Gheyu="/usr/local/bin/heyu";
use File::Copy;
use URI::Escape;
use JSON;
use Data::Dumper;

#====================================================================================================
sub new{
        my ($class,$data)=@_;
	my $self = decode_json $data;
	$self->{data}=$data;
        bless $self,$class;
        return $self;
}
#====================================================================================================
sub image {
	my ($self)=@_;

	my $img = "./userImages/x10_" . $self->{type} ."_".  $self->{house}.sprintf("%02d",$self->{unit}).".gif";
	if (-e $img) {return $img;}

	my $img = "./userImages/x10_" . $self->{type}.".gif";
	if (-e $img) {return $img;}

	my $img = "./userImages/x10_generic.gif";
	return $img;
}
#====================================================================================================
sub deviceID {
	my ($self)=@_;

	my $id="[".$self->{house}.sprintf("%02d",$self->{unit})."]";
	if ($self->{type} ne '') {
		$id.=" (".$self->{type}.")";
	}
	return $id;
}
#====================================================================================================
sub moduleTitle {
	my ($self)=@_;

	return "title_x10_".$self->{house}.sprintf("%02d",$self->{unit});
}
#====================================================================================================
sub loadStatus {
	my ($self)=@_;
	print "<script>getStatusInABit(2,'x10');</script>\n";
}
#====================================================================================================
sub popUP {
	my ($self)=@_;

	print "<div style=\"border:2px inset grey;background-color:#777777\">\n";
	print " <div class=\"x10_po_dimbri\">\n";

	if ($self->{type} eq "lamp") {
		for $dv (16,4,1,0,-1,-4,-16) {
			$a ="<a href=# ".$self->_oc("DIM&amt=$dv").">";
			$a.="<div style=\"width:100%;height:10%;\">";

			$fwid = int(abs($dv)/16*100)."%";
														
			$bwid = (100-$fwid)."%";


			$style="float:left;height:100%;";
			if ($dv > 0) {
				print $a;
				print "	<div style=\"$style;width:$bwid;\"></div>";
				print "	<div style=\"$style;width:$fwid;background-color:#FFFF55\"></div>";
				print "</div></a>\n";
			}
			if ($dv < 0) {
				print $a;
				print "	<div style=\"$style;width:$fwid;background-color:#888855\"></div>\n";
				print "	<div style=\"$style;width:$bwid;\"></div>\n";
				print "</div></a>\n";
			}
			if ($dv == 0) {print "	<div></div>\n";}
		}
		print "</div>\n";
	}

	print " <a class=\"x10_po_button\" ".$self->_oc("ON").">ON</a>\n";
	print "	<br>\n";
	print " <a class=\"x10_po_button\" ".$self->_oc("OFF").">OFF</a>\n";
	print "	<br clear=all>\n";
	print "</div>\n";
}
#====================================================================================================
sub command {
	my ($self,$command)=@_;

	# Any commands that may generate Heyu commands
	if ($command eq "OFF") {
		push @heyu_cmd,"turn $self->{house}$self->{unit} off";
		$command="";
	}
	if ($command eq "ON") {
		push @heyu_cmd,"turn $self->{house}$self->{unit} on";
		$command="";
	}
	if ($command eq "DIM"){
		if ($self->{amt} > 0) {
			push @heyu_cmd,"turn $self->{house}$self->{unit} bri ".abs($self->{amt});
		}else{
			push @heyu_cmd,"turn $self->{house}$self->{unit} dim ".abs($self->{amt});
		}
	}		
	# Process any generated commands

	my $refresh=0;
	foreach $c (@heyu_cmd){
		$refresh=1;
		$heyu_output.="cmd : $c<br>";
		$heyu_output.="out : ".`$Gheyu $c`."\n";
	}	

	print "<!--$heyu_output-->\n";

}
#====================================================================================================
sub getStatus {
	my ($self)=@_;
#	print "Content-type:application/json\n\n";
	_heyu_status();
}
#====================================================================================================
#================================================================================
#Internal Subs
#================================================================================
sub _oc{
	my ($self,$command)=@_;
	return "onClick=\"\$('#$self->{id}').load('dispatch.cgi"
		."?data=".uri_escape($self->{data})
		."&devName=".uri_escape($self->{devname})
		."&id=".$self->{id}."&type=x10&mode=cmd&command=$command');return(false);\";";
}
#================================================================================
sub _heyu_status {
	# Make sure heyu engine is started
	my %status={};

#              * = On  x = Dimmed
#        Unit:  1..4...8.......16
#  Housecode A (..............*.)
#  Housecode B (***....*......*.)

	my $cmd = $Gheyu . " show h";

#	print "$cmd<br>\n";

	open(my $HEYU,"$cmd|");

	while (my $r = <$HEYU>) {
		if ( $r =~ m/Housecode (.) \(([^)]+)/ ) {
			my $hk=$1;
			my $units=$2;
			foreach my $module (1..16) {
				$dat=substr($units,$module-1,1);
				$status{$hk}{$module}=$dat;
#				print "status - $hk - $module - $dat<br>\n";
			}
		}

	}


	my @houses=();
	foreach my $house ('A'..'P') {
		my @modules=();
		foreach my $module (1..16) {
			my $s="?";
			if (exists $status{$house}{$module}) {
				$s=$status{$house}{$module};
			}
			push @modules,"\"$module\":\"$s\"";
		}
		push @houses,"\"$house\": {" . join(",",@modules) . "}";
	}

	print "{\n" . join("\n,",@houses) . "}";
}
#================================================================================
sub getModuleImage {
	my ($house,$unit,$type)=@_;

	my $mod = $self->{house} . sprintf("%02d",$self->{unit});

	my $img =  "../userImages/x10_$self->{type}_$mod.gif";
	my $dflt = "../userImages/x10_$self->{type}.gif";
	my $unk =  "../userImages/x10_unknown.gif";

	$img=~s/^\.\.\///;

	$img="<img style=\"position:absolute;top:5px;\" src=\"$img\" width=100 height=100>";

}
#================================================================================

1;
