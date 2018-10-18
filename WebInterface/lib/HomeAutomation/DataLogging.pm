use strict;

package HomeAutomation::DataLogging;

use HomeAutomation::Database;
use Data::Dumper;
use Digest::MD5  qw(md5_hex);

my $LogDB = HomeAutomation::Database->new();
$LogDB->connectToSQLDatabase();

#================================================================================
sub loadDevices {
    my ($dataTables,$devices)=@_;
    
    foreach my $table (@$dataTables) {
        my $data={};
        $LogDB->{SH}->execute_raw_sql($data,"SELECT DISTINCT dm.deviceMap_id,deviceName FROM HomeAutomation_DataLogging.".$table." t ".
            " LEFT JOIN HomeAutomation.deviceMap dm ON dm.deviceMap_id=t.deviceMap_id ");
        while ($LogDB->{SH}->next_row($data)) {
            if ($data->{deviceName}) {
                $devices->{$table}->{$data->{deviceMap_id}}=$data->{deviceName};
            }
        }
    }
}
#================================================================================
sub returnSensorData {
	my ($section,$id,$sensor)=@_;

	my $ret="";

	$ret.="\t<div class=\"nodeName\">$sensor</div>\n";

	my $data={};
	$LogDB->{SH}->execute_raw_sql($data,"SELECT value,CONVERT_TZ(eventTime,'GMT','CST') FROM HomeAutomation_DataLogging.$section WHERE deviceMap_id=$id ORDER BY eventTime DESC LIMIT 1");
	if ($LogDB->{SH}->next_row($data)) {
		$ret.="\t\t<div>$data->{value}</div>\n";
		$ret.="\t<div style=\"font-size:60%;float:right;\">".ageFormat($data->{eventTime})."</div>\n";
	}

#	print "<span class=\"sensorNote\">$note</span>";
#	print "\t<div class=\"sensorName\"><a href=\"$url\">$data->{name}</a>";
#	print " <span class=\"subNode\">$data->{subNode}</span>";
#	print "</div>\n";

	return $ret;
}
#================================================================================
sub returnLimiters {
    my ($devices,$fields,$input)=@_;
    
    my $ret="";
    
    if ($$input{resolution} eq '') {$$input{resolution}=5;}
    if ($$input{startTime} eq '') {$$input{startTime}='';}
    if ($$input{endTime} eq '') {$$input{endTime}='';}
    
    $ret.="<span style=\"position:relative;float:right;padding-right:4em;\">\n";
    
    $ret.=$::session->form(($fields,{'_method'=>'get'}),)."\n";
    $ret.=" <span id=\"cal\"></span>\n";
    $ret.=" Resolution:<input type=text name=resolution value=\"$$input{resolution}\">\n";
    $ret.=" [From : <input type=text name=startTime value=\"$$input{startTime}\">".occCalendar("startTime");
    $ret.=" to <input type=text name=endTime value=\"$$input{endTime}\">\n".occCalendar("endTime");
    $ret.=" ] <input type=submit value=\"Go\">\n";
    $ret.="</form>\n";
    
    
    $ret.="</span>\n";
    
    return $ret;
}
#================================================================================
sub getDataTables {
    my $data={};
    
    my @dataTables;
    
    $LogDB->{SH}->execute_raw_sql($data,"SELECT dataTable FROM dataLogging");
    while ($LogDB->{SH}->next_row($data)) {
        push @dataTables,$data->{dataTable};
    }
    
    if ($::Gdebug) {print "Tables : " . join(',',@dataTables) . "<br>\n";}
    
    return @dataTables;
}

#================================================================================
sub getGraphDataQuery {
	my ($minuteGap,$start,$end,$devices)=@_;

	if ($::Gdebug) {print "Devices : " . Dumper($devices) . "<br>\n";}

	my $unions = '';
	my @d=();

	my $tableNum=0;
	foreach my $section (keys %{$devices}) {
		$tableNum++;
		if ($tableNum>1) {
			$unions .= "UNION\n";
		}
		$unions .= "SELECT *,'$section' AS tbl FROM HomeAutomation_DataLogging.".$section. " \n";

		push @d,keys( %{$devices->{$section}} );
	}

	my $where;

	if ($start ne '') {
        $start = $LogDB->{SH}->human_date_to_sql($start);
		if ($where ne '') {$where.=" AND "};
		$where .=" eventTime >= '$start'";
	}
	if ($end ne '') {
        $end =$LogDB->{SH}->human_date_to_sql($end);
        if ($where ne '') {$where.=" AND "};
		$where .=" eventTime <= '$end'";
	}

	if ($where ne '') { 
		$where = "WHERE ".$where; 
	}

	# Miliseconds * 60 = Minutes;
	my $chunk = 60*$minuteGap;

	my $fullQuery = "SELECT tbl,FLOOR(UNIX_TIMESTAMP(eventTime)/($chunk)) grp ,avg(value) aValue ,min(eventTime) eventTime,deviceMap_id FROM (\n" 
	. $unions
	. " ) AS data $where GROUP BY grp,tbl,deviceMap_id\n";

	if ($::Gdebug) {print "Query : " . $fullQuery . "<br>\n";}

	return $fullQuery;
}
#================================================================================
sub returnGraphData {
	my ($devices,$resolution,$start,$end)=@_;

	my $query = getGraphDataQuery($resolution,$start,$end,$devices);

	my $data={};
    my $nodes={};
	$LogDB->{SH}->execute_raw_sql($data,$query);
	my $dataSet={};
    my @dates = ();
	while ($LogDB->{SH}->next_row($data)) {
        my $node = "$data->{tbl}.$data->{deviceMap_id}";
		my $time = $data->{eventTime};
        $time=~s/:\d\d$//;
        push @dates,$time;
		$dataSet->{$time}->{$node} = sprintf("%.2f",$data->{aValue});
        $nodes->{$node}++;
	}	

	if ($::Gdebug) {print "Graph Data : <pre>" . Dumper($dataSet) . "</pre><br>\n";}

	my $gData = {};

	foreach my $time (sort @dates) {
		foreach my $node (keys %$nodes) {
			push @{$gData->{$node}},$dataSet->{$time}->{$node};
		}
	}
	if ($::Gdebug) {print "Graph Data : <pre>" . Dumper($gData) . "</pre><br>\n";}


	my $graphData={};
	$graphData->{type}='line';
	$graphData->{options}->{showLines}='true';
    $graphData->{options}->{spanGaps}='true';
    $graphData->{options}->{animation}->{duration}=0;
    $graphData->{options}->{'scales'}->{'yAxes'} = [{'ticks'=>{'beginAtZero'=>'true'}}];

	$graphData->{data}->{labels}=\@dates;
	foreach my $node (keys %$gData) {
        my $nodeColor=nodeColor($node);
		push @{$graphData->{data}->{datasets}},{ 
            'label'=>$node,
            'data'=>$gData->{$node},
            'borderColor'=>"$nodeColor",
            'fill'=>'false',
            'borderWidth'=>'1',
            'pointRadius'=>'2',
#            ''=>'',
        };
    }

	if ($::Gdebug) {print "Graph Data : <pre>" . Dumper($graphData) . "</pre><br>\n";}
    
	return JSON->new->utf8->canonical->indent->encode($graphData);	
}
#================================================================================
sub nodeColor {
    my ($nodeName) = @_;
    my $md5_hash = md5_hex( $nodeName );
    
    my $col="rgb(75, 192, 192)";
    $col="#".substr($md5_hash,3,6);
    
    return($col);
}
#================================================================================
#================================================================================
sub ageFormat {
	my ($time) = @_;

	my ($h,$m,$s)=($time=~m/(\d+):(\d+):(\d)/);


	#print "[$h , $m , $s]\n";

	my $ago='';
	my $style='';

	if ($h > 0) {
		$ago.=0+$h." hrs ";
		$style="background-color:#400;";
	}elsif ($m > 0) {
		$ago.=0+$m." min ";

		if ($m > 10) {
			$style="background-color:#440;";
		}
		if ($m > 30) {
			$style="background-color:#400;";
		}

	}elsif ($s > 0) {
		$ago.=0+$s." sec ";
	}

	my $ret = "<span style=\"$style\">";
	if ($ago ne '') {
		$ret .= "$ago ago";
	}
	$ret .= "</span>\n";

	return $ret;
}
#================================================================================
sub occCalendar {
    my ($name)=@_;
    
    return "<img style=\"border:0px;vertical-align:middle\" src=\"images/calendar.gif\" onclick=\"return(popup_calendar(addForm.$name,document.getElementById('cal')))\">\n";

}
#================================================================================
1;