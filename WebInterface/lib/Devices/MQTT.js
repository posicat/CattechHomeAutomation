
var device_x10 = {

	renderConfig: function(elem,deviceData) {
		var html='';

		html += '<form>';

		html += '<input type="hidden" name="protocol" value="x10">';
		html += '<input type="hidden" name="controlChannel" value="x10Controller">';

		html += 'House Code : <select name="house" id="' + elem.id + '_house">';

		for (s of 'ABCDEFGHIJKLMNOP'.split('')) {
			var selected = (deviceData.house == s) ? 'SELECTED' : '';
			html+='<option value="'+s+'" '+selected+'>'+s+'</option>\n';
		}
		html += "</select><br>\n";

		html += 'Unit Code : <select name="unit" id="' + elem.id + '_unit">';

		for (var s=1;s<=16;s++) {
			var selected = (deviceData.unit == s) ? 'SELECTED' : '';
			html+='<option value="'+s+'" '+selected+'>'+s+'</option>\n';
		}
		html += "</select><br>\n";

		html += 'Unit Type : <select name="type" id="' + elem.id + '_type">';

		for (s of ['Lamp','Appliance']) {
			var selected = (deviceData.type == s) ? 'SELECTED' : '';
			html+='<option value="'+s+'" '+selected+'>'+s+'</option>\n';
		}
		html += "</select>" +
			"</form>";

		elem.html(html);
	},
	renderControl: function(elem,deviceData) {
		var html='';

		html += "<div style=\"text-align:center;\">" + deviceData.type + " " + 
			deviceData.house + deviceData.unit +
			"</div>";

		html +="<table width=100% align=center class=\"subDeviceControl\"><tr align=center valign=center>\n";
		html +="<td>"+device_x10.onOff(deviceData)+"</td>";
		if (deviceData.type == "Lamp") {
			html +="<td>"+device_x10.dimBright(deviceData)+"</td>";
		}
		html +="</tr></table>\n";


		elem.html(html);
	},
	dimBright: function(deviceData) {
		var db = "";

		for (s of [-16,-8,-2,2,8,16]) {
			var icon = (s>0?"+":"-").repeat(Math.abs(s/2));
			var action = (s>0?"bright ":"dim ")+Math.abs(s);

			var occb=devices_ha.addOnClickCallback(deviceData,{action:action});
			db+='<button '+occb+'>'+icon+'</button><br>';
		}

		return db;
	},
	onOff: function(deviceData) {
		var oo = "";

		var occb=devices_ha.addOnClickCallback(deviceData,{action:'on'});
		oo+='<button style="height:5em;width:100%;" '+occb+'>ON</button><br>';

		occb=devices_ha.addOnClickCallback(deviceData,{action:'off'});
		oo+='<button style="height:5em;width:100%;" '+occb+'>OFF</button><br>';

		return oo;
	}
}

