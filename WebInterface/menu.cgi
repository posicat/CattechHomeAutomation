#!/usr/bin/perl
#use strict;
use Cattech::HTMLHelper;
use URI::Escape;
require "cgi-lib.pm";
require "lib/sql-lib.pl";
require "lib/debug.pl";

$|=1;

$::Gdebug=0;

$cgi_lib'maxdata = 500000; # ~50k for images
ReadParse(*input);

my $base_url="./index.cgi?d=$::Gdebug";
foreach $f (edit)
{
	if ($input{$f} ne "") {$base_url.="&$f=$input{$f}"}
}

print "Content-type:text/html\n\n";
print Cattech::HTMLHelper::browserSpecificDoctype();



$DBH = sql_connect("localhost","homeauto","homeauto","6C5a3PtSqtNHAACD");

$QH = execute_query($DBH,"select * from settings");
while ( %x = %{$QH->fetchrow_hashref()} ){$settings{$x{var}}=$x{val};}

$ocll="onClick=\"load_link(this);return false\";";

if ($input{mode} eq "")
{
	print <<EOB;
<html>
<head>
	<title>Home Automation</title>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
        <link rel="stylesheet" type="text/css" href="/include/common_style.css" />
        <link rel="stylesheet" type="text/css" href="homeauto.css" />
	<script type="text/javascript" src="/include/jquery.js"></script>
        <script language="javascript" type="text/javascript" src="/include/override_getelementbyid.js"></script>
        <script language="javascript" type="text/javascript" src="/include/getElementsByClassName.js"></script>
        <script language="javascript" type="text/javascript" src="/include/drag-drop.js"></script>
        <script language="javascript" type="text/javascript" src="homeauto.js"></script>
	<script>
		window.onclick=function() {
			document.getElementById('ha_popup').innerHTML='';
		}
	</script>
</head>
<body class=\"noGlobalMenu\" onClick=\"document.getElementById('ha_popup').innerHTML='';\">
<span id=ha_popup class=ha_popup></span>
<div id=ha_groups class=ha_groups></div>
<div id=ha_controls class=ha_controls></div>

<script language="javascript">
	\$('#ha_groups').load('$base_url&mode=groups&group=$input{group}');
</script>
</body>
EOB

}

if ($input{mode} eq "groups")
{
	$grp=0+$input{group}; 	# 0 default, otherwise numeric

	$QH = execute_query($DBH,"select * from groups where (grpID = $grp)");
	%Gx = %{$QH->fetchrow_hashref()};

		print "<div class=titlebar>$Gx{name}</div>\n";
	if ($grp != 0)	{
		print "<a class=\"groupitem\" href=\"$base_url&mode=groups&group=$Gx{parentGrpID}\" target=\"ha_groups\" $ocll>";
		print "&bull;&bull;";
		print "</a>\n";
	}
	
	print "<div class=indent1>\n";
	$QH = execute_query($DBH,"select * from groups where (parentGrpID = $grp)");
	while ( %Cx = %{$QH->fetchrow_hashref()} )
	{
		print "<a class=\"groupitem\" href=\"$base_url&mode=groups&group=$Cx{grpID}\" target=\"ha_groups\" $ocll>";
		print "$Cx{name}\n";
		print "</a>\n";
	}
	print "</div>\n";

	print "<script>\n";
	print "	\$('#ha_controls').load('$base_url&mode=controls&group=$grp');\n";
	print "</script>\n";

}
if ($input{mode} eq "controls")
{
	$grp=0+$input{group}; 	# 0 default, otherwise numeric

	$QH = execute_query($DBH,"select * from modules");
	while ( %mod = %{$QH->fetchrow_hashref()} )
	{
		$module{$mod{modID}}=$mod{path}."?";
	}

	if ($input{edit})
	{
		print "<a style=\"position:relative;z-index:200;background-color:#666666;\" href=\"button_edit.cgi?group=$input{group}\">Edit Buttons</a>\n";
	}


	$script="";
	$QH = execute_query($DBH,"select * from controls where (GrpID = $grp)");
	while ( %ctl = %{$QH->fetchrow_hashref()} ) {
		$id	="ctl_$ctl{ctlID}";
		$m	=$module{$ctl{modID}};

		my $style ="position:absolute;";
		$style.="top:$ctl{y}px;left:$ctl{x}px;";
		$style.="width:$ctl{dx}px;height:$ctl{dy}px;";
		$style.="max-width:$ctl{dx}px;max-height:$ctl{dy}px;";

		print "<div style=\"$style\" class=\"controlitem\" id=\"$id\">\n";
		print "$ctl{name}\n";
		print "</div>\n";
		$scr .= "\$('#$id').load('$m$ctl{options}&name=".uri_escape($ctl{name})."&id=$ctl{ctlID}');\n";
	}
	print "<script>$scr</script>";
}

