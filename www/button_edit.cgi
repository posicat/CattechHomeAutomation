#!/usr/bin/perl
$|=1;
require "cgi-lib.pm";
require "lib/sql-lib.pl";
require "lib/debug.pl";

$Gdebug=0;

$cgi_lib'maxdata = 500000; # ~50k for images
my %input;
ReadParse(\%input);

$base_url="button_edit.cgi?d=$Gdebug";
foreach $f (edit,group)
{
	if ($input{$f} ne "") {$base_url.="&$f=$input{$f}";}
}

print "Content-type:text/html\n\n";

$DBH = sql_connect("localhost","homeauto","homeauto","6C5a3PtSqtNHAACD");

$QH = execute_query($DBH,"select * from settings");
while ( %x = %{$QH->fetchrow_hashref()} ){$settings{$x{var}}=$x{val};}

$ocll="onClick=\"load_link(this);return false\";";

print <<EOB;
<html>
<head>
	<title>Home Automation</title>

        <link rel="stylesheet" type="text/css" href="/include/common_style.css" />
        <link rel="stylesheet" type="text/css" href="homeauto.css" />

        <script language="javascript" type="text/javascript" src="/include/xmlHttp.js"></script>
        <script language="javascript" type="text/javascript" src="/include/override_getelementbyid.js"></script>
        <script language="javascript" type="text/javascript" src="/include/getElementsByClassName.js"></script>
        <script language="javascript" type="text/javascript" src="/include/drag-drop.js"></script>
        <script language="javascript" type="text/javascript" src="homeauto.js"></script>
	<script>
	function save_positions()
	{
		// alert(allDragObjects);

		var res="$base_url&mode=savecoords";

		for (var i=0;i<allDragObjects.length;i++)
		{
			o=allDragObjects[i];
			res+="&id_"+o.id+"="+o.offsetLeft+","+o.offsetTop;
		}

		window.location.href=res;

	}
	</script>
</head>
<body onClick=\"document.getElementById('ha_popup').innerHTML='';\">
<div id="ha_popup"></div>

EOB

if ($input{mode} eq "savecoords")
{
	foreach $k (grep {/^id_/} keys (%input))
	{
		($x,$y)=split(",",$input{$k});
		($j,$id)=split("_",$k);
		$SQL = "UPDATE controls SET x=$x,y=$y WHERE (ctlID=$id);";
		$QH = execute_query($DBH,$SQL);
		print "SQL : $SQL ($QH)<br>\n";
	}
}



print "<a style=\"position:relative;z-index:100;background-color:#777777;\" href=# onClick=\"save_positions();\">Save</a>\n";

$QH = execute_query($DBH,"select * from controls where (GrpID = $input{group})");
while ( %ctl = %{$QH->fetchrow_hashref()} )
{
	$m	=$module{$ctl{modID}};

	$style="position:absolute;top:$ctl{y};left:$ctl{x};width:$ctl{dx};height:$ctl{dy};max-height:$ctl{dy};";
	print "<div style=\"$style;background-color:black\" class=\"draggable controlitem\" id=\"$ctl{ctlID}\">\n";
	print "$ctl{name}\n";
	print "</div>\n";
}

print <<EOB;
<script>init_draggable();</script>
</body>
</html>
EOB
