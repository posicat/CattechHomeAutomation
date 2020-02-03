var m_names = new Array(
	"January ", 
	"February", 
	"March", 
	"April", 
	"May", 
	"June", 
	"July", 
	"August", 
	"September", 
	"October", 
	"November",
	"December");

var cspan;
var cal_field;
var indate = new Date();
		indate.setHours(0);
		indate.setMinutes(0);
		indate.setSeconds(0);
		indate.setMilliseconds(0);
var today  = new Date();
		today.setHours(0);
		today.setMinutes(0);
		today.setSeconds(0);
		today.setMilliseconds(0);

function update_calendar(m,y)
{
	cspan.innerHTML=populate_calendar(m,y);
	return false;
}
function return_date(m,d,y)
{
	cal_field.value=m+"/"+d+"/"+y;
	cspan.innerHTML="";
	return false;
}
function popup_calendar(field,set_span)
{
	cal_field=field;
	cspan=set_span;
	var m="";
	var d="";
	var y="";

	if (field.value != "")
	{
		var s=field.value.split('/');
		m=s[0]-1;
		d=s[1];
		y=s[2];
	}

	if ( cspan.innerHTML != "" )
	{
		cspan.innerHTML = "";
	}else{
		indate.setFullYear(y,m,d);
		update_calendar(m,y);
	}
	return false;
}
function populate_calendar(month,year)
{
	var workdate=new Date();
	workdate.setTime(0);
	workdate.setDate(today.getDate()+0);
	workdate.setHours(0);
	workdate.setMinutes(0);
	workdate.setSeconds(0);
	workdate.setMilliseconds(0);

	year=parseInt(year);
	month=parseInt(month);

	if (isNaN(month)) { month=today.getMonth(); }
	if (isNaN(year) ) { year =today.getYear()+1900; }


	var controls="";

	while(month > 11) {year++; month-=12;}
	while(month < 0)  {year--; month+=12;}

	// Month and year, forward and back
	var mb=month-1;
	var mf=month+1;
	var yb=year-1;
	var yf=year+1


	controls+="<div>\n";
	controls+="  <a href=# onClick='return(update_calendar("+mb+","+year+"))'><img src=\"images/left-arrow.gif\" border=0></a>\n";
	controls+="  <div style=\"width:4em;text-align:center;display:inline-block;\">"+m_names[month]+"</div>\n";
	controls+="  <a href=# onClick='return(update_calendar("+mf+","+year+"))'><img src=\"images/right-arrow.gif\" border=0></a>";
	controls+="  <a href=# onClick='return(update_calendar("+month+","+yb+"))'><img src=\"images/left-arrow.gif\" border=0></a>\n";
	controls+="  "+year+"\n";
	controls+="  <a href=# onClick='return(update_calendar("+month+","+yf+"))'><img src=\"images/right-arrow.gif\" border=0></a>\n";
	controls+="</div>\n";

	var result="";

	result+="<style>\n";
	result+=".caltable {width:100%;border-spacing:0px;border-collapse:collapse;}\n";
	result+=".caltable td {border:1px solid black}\n";
	result+=".caltable a {text-decoration:none;}\n";
	result+=".calspan {"+
		"z-index:10;"+
		"position:absolute;"+
		"background:grey;"+
		"color:black;"+
		"border:3px inset grey;"+
	"}";
	result+="</style>\n";

	result+="<span class=calspan>\n\n";
	result+=controls;
	result+="\n\n<table class=caltable align=center>";
	result+="<tr>";
	result+="<th>S</th>";
	result+="<th>M</th>";
	result+="<th>T</th>";
	result+="<th>W</th>";
	result+="<th>T</th>";
	result+="<th>F</th>";
	result+="<th>S</th>";
	result+="</tr>\n";

	workdate.setFullYear(year,month,1);

	dow = workdate.getDay();
	day = workdate.getDate();
	mon = workdate.getMonth();

	var rows=0;

	if (dow > 0) {	result+="<tr align=center><td colspan="+dow+"></td>\n";}
	blank="&nbsp;<div class=backdoor><br></div>";

	while (mon == month)
	{
		if (dow == 0) { result+="<tr align=center>\n";}	

		color="#555555";

		if ( (dow == 0) || (dow == 6) ) { color="#444444"; }

		if (today.getTime() ==workdate.getTime()) {color="#999966"; }
		if (indate.getTime()==workdate.getTime()) {color="#6666FF"; }

		result+= "  <td bgcolor="+color+"><a href=# onClick=\"return(return_date("+(month+1)+","+day+","+year+"));\">"+day+"</a></td>\n";

		workdate.setDate(workdate.getDate()+1);
		dow = workdate.getDay();
		day = workdate.getDate();
		mon = workdate.getMonth();

		if (dow == 0) { result+="</tr>\n";  rows++;}	
	}
	if (dow == 0) {rows--;}

	result+="</tr>";
	result+="</table>";
	result+=controls;

	result+="</span>";

	return(result);
}

