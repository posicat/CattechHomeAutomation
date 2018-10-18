#!/usr/bin/perl
use strict;
BEGIN {
        unshift @INC,'./lib';
        unshift @INC,'/home/websites/lib';
        unshift @INC,'/usr/local/homeAutomation/lib';
}

use Cattech::HTMLHelper;
use Cattech::WebSession;
use Cattech::SQLHelper;
use HomeAutomation::Database;
use HomeAutomation::DataLogging;
use URI::Escape;
use JSON;
use Carp;
require "cgi-lib.pm";
require "lib/debug.pl";
require "web_tabs.pm";

$SIG{ __DIE__ } = sub { Carp::confess( @_ ) };

$|=1;
$::Gdebug=0;

$cgi_lib'maxdata = 500000; # ~50k for images
my %input;
ReadParse(\%input);

$::session=Cattech::WebSession->new();

$::session->setParam('_base','./index.cgi');
$::session->setParam('_method','put');

my @cornerMenu = (
	['Menu Controls'	,{mode=>'menu'} ],
    ['House Map'		,{mode=>'map'} ],
    ['Current Sensors'	,{mode=>'dataLogTable'} ],
    ['Graphed Sensors'	,{mode=>'dataLogGraph'} ],
#	['All X10'			,{mode=>'allX10'} ],
    ['Setup'			,{mode=>'setup'} ],
);

my $renderModes = {
	'menu'			=>{ 'fmt'=>'html',	'render'=>\&displayLoadGroup },
	'map'			=>{ 'fmt'=>'html',	'render'=>\&displayMap },
	'allX10'		=>{ 'fmt'=>'html',	'render'=>\&displayAllX10 },
	'dataLogTable'  =>{ 'fmt'=>'html',	'render'=>\&displayDataLogTable },
	'dataLogGraph'	=>{ 'fmt'=>'html',	'render'=>\&displayDataLogGraph },
	'setup'			=>{ 'fmt'=>'html',	'render'=>\&displaySetupScreen },
	'devices'		=>{ 'fmt'=>'html',	'render'=>\&displaySetupDevices },

	'groups'		=>{ 'fmt'=>'ajax',	'render'=>\&displayAjaxGroupData },
	'controls'		=>{ 'fmt'=>'ajax',	'render'=>\&displayAjaxGroupControls },
};

my $menu=$input{menu};

if ( $ENV{HTTP_HOST} eq "" ) {
	print "\n\n**COMMAND LINE DEBUG**\n\n\n";

	$input{mode}="dataLogGraph";
#	$input{group}=1;
}

$::HA=HomeAutomation::Database->new(); 
$::HA->connectToSQLDatabase(); 

my $ocll="onClick=\"load_link(this);return false\";";

if ($input{mode} eq "") { $input{mode}='menu';}

my $render = $renderModes->{$input{mode}};

#========== Render page based on input ==========

if ($render->{fmt} eq 'html') {
	print "Content-type:text/html\n\n";
	print  Cattech::HTMLHelper::browserSpecificDoctype();
	displayHead();
	displayStartBody();
	&{$render->{render}}();
	displayCornerMenu();
	displayEndBody();
	exit;
}

if ($render->{fmt} eq 'ajax') {
	print "Content-type:text/html\n\n";
	&{$render->{render}}();
	exit;
}
print "Content-type:text/html\n\n";
print "No render mode for mode=$input{mode}<br>\n";

#================================================================================
#================================================================================
sub displaySetupScreen {
print <<EOB;
<div id="setupScreen">
<div class="ha_groups">
	<div class=titlebar>Setup</div>
	<a href="userImages.cgi" class="groupitem" $ocll target="setupScreen">User Images</a>	
	<a href="index.cgi?mode=devices" class="groupitem" $ocll target="setupScreen">Devices</a>	
	<a href="index.cgi?mode=watchcat" class="groupitem" $ocll target="setupScreen">Schedules</a>	
	<a href="index.cgi?mode=triggers" class="groupitem" $ocll target="setupScreen">Triggers</a>	
	<a href="index.cgi?mode=reactions" class="groupitem" $ocll target="setupScreen">Reactions</a>	
</div>
</div>
EOB
}
#================================================================================
sub displaySetupDevices {

print <<EOB;
<div class="ha_groups">
	<div class=titlebar>Devices</div>
EOB

	my $dev={};
	my $devices={};
	$::HA->{SH}->select_data($devices,'deviceMap');
	while ( $::HA->{SH}->next_row($devices) ) {
		push @{$dev->{$devices->{commonDevice}}},[$devices->{deviceMap_id},$devices->{nativeDevice}];
	}

	my $url="index.cgi?mode=setup";
	print "<a href=\"$url\" class=\"groupitem\">&bull;&bull;</a>\n";
	$url="index.cgi?mode=devices&deviceID=ADD";
	print "<a href=\"$url\" class=\"groupitem\" style=\"text-align:center;color:#944\">Add New</a>\n";

	foreach my $key (sort keys %$dev) {
		my @devs=@{$dev->{$key}};

		my $protocols="";
		for(my $i=0;$i<@devs;$i++) {
			my $d=$devs[0];
			my $native = decode_json($$d[1]);
			$protocols.="$native->{protocol} ";
		}
		$url="index.cgi?mode=devices&deviceID=$key";

		$key="{\"key\":".$key."}";
		my $j = decode_json($key);
		my $prot ="<span class=\"protocolRight\">[$protocols]</span>";
		print "<a class=\"groupitem\" href=\"$url\>".join(' , ',@{$j->{key}})."$prot</a>\n";
	}

print <<EOB;
</div>
EOB
}
#================================================================================
sub displayAllX10 {
#	my $scr="";
	my $id=0;
	print "<div class=titlebar>All X10 Modules</div>\n";
	print "<hr>\n";
	print "<tt>\n";
	for my $h ('A'..'P') {
		for my $u (1..16) {
			$id++;
			print "<div style=\"height:1.4em;width:2.0em;float:left;\" class=\"x10_title\" name=\"mod_x10_$h$u\" id=\"allX10_$id\">$h$u</div>\n";
#			$scr.="\$('#allX10_$id').load('http://pawz.cattech.org/pw/homeAutomation/modules/x10.cgi?img=0&unit=$u&house=$h&id=allX10_$id');\n";
		}
		print "<br clear=all>\n";
	}
	print "</tt>\n";
	print "<script>\ngetStatusInABit(2,'x10');</script>\n";
}
#================================================================================
sub displayAjaxGroupControls {
	my %module;

	if ($input{edit}) {
		print "<a style=\"position:relative;z-index:200;background-color:#666666;\" href=\"button_edit.cgi?group=$input{group}\">Edit Buttons</a>\n";
	}

	my $scr="";
	my $sql = "SELECT * FROM menuControl mc LEFT JOIN deviceMap dm ON dm.deviceMap_id=mc.deviceMap_id"
		." WHERE menuGrpID=?";

	my $menuCtl={};
	my $sth = $::HA->{SH}->execute_raw_sql($menuCtl,$sql,($input{group}));

        while ( $::HA->{SH}->next_row($menuCtl) ) {
		my $id ="ctl_$menuCtl->{ctlID}";

		my $style ="position:absolute;";
		$style.="top:$menuCtl->{y}px;left:$menuCtl->{x}px;";
		$style.="width:$menuCtl->{dx}px;height:$menuCtl->{dy}px;";
		$style.="max-width:$menuCtl->{dx}px;max-height:$menuCtl->{dy}px;";

		print "<div style=\"$style\" class=\"controlitem\" id=\"$id\">\n";
		print "$menuCtl->{deviceName}\n";
		print "</div>\n";

		$scr .= "\$('#$id').load('dispatch.cgi?commonDevice=".
			uri_escape($menuCtl->{commonDevice}).
			"&id=ctl_$menuCtl->{ctlID}".
		"');\n";

		my $cd=$menuCtl->{commonDevice};
		my $if=$menuCtl->{interfaceType};
		my $dn=$menuCtl->{deviceName};
		my $im=$menuCtl->{image};
		
		print <<EOB;
<script>
	devices_ha.renderIcon('$id',devices_ha.addHAP({"data":{"device":$cd,"interfaceType":"$if","deviceName":"$dn","image":"$im"}}));
</script>
EOB
	}
}
#================================================================================
sub displayAjaxGroupData() {
	my $groupQuery={};
	$groupQuery->{grpID}=$input{group};

	$::HA->{SH}->select_data($groupQuery,'group');
        $::HA->{SH}->next_row($groupQuery);

	my $url = $::session->url({tab=>undef});

	print "<div class=titlebar>$groupQuery->{name}</div>\n";
	if ($input{group} != 0)	{
		print "<a class=\"groupitem\" href=\"$url&mode=groups&group=$groupQuery->{parentGrpID}\" target=\"ha_groups\" $ocll>";
		print "&bull;&bull;";
		print "</a>\n";
	}

	print "<div class=indent1>\n";
	my $subGroup={};
	$subGroup->{parentGrpID}=$input{group};
	$::HA->{SH}->select_data($subGroup,'group');
        while ( $::HA->{SH}->next_row($subGroup) ) {
		print "<a class=\"groupitem\" href=\"$url&mode=groups&group=$subGroup->{grpID}\" target=\"ha_groups\" $ocll>";
		print "$subGroup->{name}\n";
		print "</a>\n";
	}
	print "</div>\n";

	print "<script>\n";
	print "	\$('#ha_controls').load('$url&mode=controls&group=$input{group}');\n";
	print "</script>\n";
}
#================================================================================
sub displayCornerMenu {
        print "<div class=\"menuButton\" onClick=\"\$('#menuMask').toggle();\">=</div>\n";

        print "<div class=\"backgroundMask\" id=\"menuMask\" onClick=\"\$('#menuMask').toggle();\">\n";

        print "<div class=\"menuContents\">\n";
        foreach my $item (@cornerMenu) {
		my $url;
		my $title;
		if (exists $$item[1]->{url}) {
			$url = $$item[1];
                        $title=$$item[0];
		}
		if (exists $$item[1]->{mode}) {
	                $url=$::session->url($$item[1]);
        	        $title=$$item[0];
		}
                print "<a class=\"menuItem\" href=\"$url\">$title</a>\n";
        }
        print "</div>\n";

        print "</div>\n";
}

#================================================================================
sub displayHead {
print <<EOB;
<head>
	<title>Home Automation</title>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />

    <script language="javascript" type="text/javascript" src="/include/jquery.js"></script>
    <script language="javascript" type="text/javascript" src="/include/override_getelementbyid.js"></script>
    <script language="javascript" type="text/javascript" src="/include/getElementsByClassName.js"></script>
    <script language="javascript" type="text/javascript" src="/include/drag-drop.js"></script>
    <script language="javascript" type="text/javascript" src="homeauto.js"></script>
    <script language="javascript" type="text/javascript" src="lib/Devices/devices_ha.js"></script>
    <script language="javascript" type="text/javascript" src="lib/Devices/X10.js"></script>
    <script language="javascript" type="text/javascript" src="lib/Chart.bundle.js"></script>
    <script language="javascript" type="text/javascript" src="lib/Devices/MQTT.js"></script>
    <script language="javascript" type="text/javascript" src="lib/Devices/Common.js"></script>
    <script language="javascript" type="text/javascript" src="lib/calendar.js"></script>

    <script>
        window.onclick=function() {
        document.getElementById('ha_popup').innerHTML='';
    }
    </script>

    <link rel="stylesheet" type="text/css" href="/include/common_style.css" />
    <link rel="stylesheet" type="text/css" href="homeauto.css" />
    <link rel="stylesheet" href="sensors.css"/>
</head>
EOB
}
#================================================================================
sub displayStartBody {
print <<EOB;
<body class=\"noGlobalMenu\" onClick=\"document.getElementById('ha_popup').innerHTML='';\">
<span id=ha_popup class=ha_popup></span>
<div id=ha_groups class=ha_groups></div>
<div id=ha_controls class=ha_controls></div>
EOB
}
#================================================================================
sub displayEndBody {
print <<EOB;
</body>
EOB
}
#================================================================================
sub displayMap {
	print "<script type=\"text/javascript\" src=\"/include/jquery.js\"></script>\n";

	if ($input{tab} eq '') {$input{tab}=0;}

	$::session->setParam('mode','map');
	$::session->setParam('tab',$input{tab});

	my $mainTabs={};
	$mainTabs->{tabuid}=1;

	opendir(my $DIN,"tabs");
	my @tabs=grep{/\.tab$/} readdir($DIN);
	closedir($DIN);

	my $cnt=0;
	foreach my $t (@tabs) {
		my ($tt) = ($t =~ m/(.*)(\.tab$)/);
		set_webtab($mainTabs,$cnt++,$tt,$t);
	}	

	draw_webtabs($mainTabs,
	"#FFFFFF,#555566",
	"#AAAAFF,#666666",
	"#444444",
	$::session->url({tab=>undef}),"top",$input{tab},"100%");

	my $tabAction = $mainTabs->{$input{tab}};

	if ($tabAction=~m/\.tab$/) {
		open (my $IN,"<",'tabs/'.$tabAction);
		my $title=<$IN>;		chomp($title);
		my $backgroundImg=<$IN>;	chomp($backgroundImg);
		my $settings=<$IN>;		chomp($settings);

		$backgroundImg=uri_escape($backgroundImg);

		print "<div id=\"mainImage\" style=\"position:absolute;top:3em;bottom:0px;left:0px;right:0px;\">\n";
		print "</div>\n";
		print <<EOB;
<script language="javascript">
	function loadScalableClickableImage(elementID,image) {
		var e = \$('#'+elementID);
		e.css('background-image','url('+image+')');
		e.css('background-size',e.width()+'px '+e.height()+'px');
	}
	loadScalableClickableImage('mainImage','userTabs/$backgroundImg');
	\$( window ).resize(function() {
		loadScalableClickableImage('mainImage','actions/loadImage.cgi?file=images/$backgroundImg');
	});
</script>
EOB

	}
}
#================================================================================
sub displayLoadGroup {
	my $url=$::session->url({});

	print "<script language=\"javascript\">\n";
	print "\$('#ha_groups').load('$url&mode=groups&group=$input{group}');\n";
	print "</script>\n";
}
#================================================================================
sub displayDataLogTable {
	# my ($devices)=@_;
    
    my @dataTables = HomeAutomation::DataLogging::getDataTables();

    my $devices={};
    HomeAutomation::DataLogging::loadDevices(\@dataTables,$devices);

	print "<meta http-equiv=\"refresh\" content=\"60\">\n";

	my $data={};

	foreach my $section (keys %$devices) {
		displaySectionHeader($section);
		foreach my $device (keys %{$devices->{$section}}) {
			displaySensor($section,$device,$devices->{$section}->{$device});
		}
	}
}
#================================================================================
sub displayDataLogGraph {
	my ($devices)=@_;
    
    my @dataTables = HomeAutomation::DataLogging::getDataTables();

    my $devices={};
    HomeAutomation::DataLogging::loadDevices(\@dataTables,$devices);

    my $limiters = HomeAutomation::DataLogging::returnLimiters($devices,
        {startTime=>'',endTime=>'',resolution=>'','mode'=>$input{mode}}, \%input);
    
	print "<div style=\"menuBar\">$limiters</div>\n";
	print "<div class=\"pageTable\" style=\"margin-top:3em\">\n";
	print "<canvas id=\"dataGraph\" style=\"width:100%;height:100%\"></canvas>\n";
	print "</div>\n";

    my $start = $input{startTime};
    my $end = $input{endTime};
    my $resolution = 0+$input{resolution};
    my $graphData = HomeAutomation::DataLogging::returnGraphData($devices,$resolution,$start,$end);

print <<EOB;
<script>
function drawChart(id,dat) {
    var ctx = document.getElementById(id).getContext('2d');
    var myLine = new Chart(ctx,dat);
}
drawChart("dataGraph",$graphData);
</script>
EOB
}
#================================================================================
sub displaySectionHeader {
	my ($section)=@_;
	print "<div class=\"section\">$section</div>\n";
}
#================================================================================
sub displaySensor {
	my ($section,$id,$sensor)=@_;

	my $sensorLoad=HomeAutomation::DataLogging::returnSensorData($section,$id,$sensor);

	print "<div class=\"sensor\" id=\"$id\">$sensorLoad</div>";
}#================================================================================
#================================================================================
1;
