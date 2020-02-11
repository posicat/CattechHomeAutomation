var Common = {
	appliance_onoff: function(elem,deviceData) {
		var html='';

		html += "<div style=\"text-align:center;\">" + deviceData.type + " " + 
			deviceData.house + deviceData.unit +
			"</div>";

		html +="<table width=100% align=center class=\"subDeviceControl\"><tr align=center valign=center>\n";
		html +="<td>"+Common._onoff(deviceData)+"</td>";
		html +="</tr></table>\n";


		Common._displayPopup(elem,html);
	},
	lamp_dimbri: function(elem,deviceData) {
		var html='';

		html += "<div style=\"text-align:center;\">" + deviceData.data.deviceName + "</div>";

		html +="<table width=100% align=center class=\"subDeviceControl\"><tr align=center valign=center>\n";
		html +="<td>"+Common._onoff(deviceData)+"</td>";
		html +="<td>"+Common._dimbri(deviceData)+"</td>";
		html +="</tr></table>\n";


		Common._displayPopup(elem,html);
	},
	lamp_onoff: function(elem,deviceData) {
		var html='';

		html += "<div style=\"text-align:center;\">" + deviceData.data.deviceName + "</div>";

		html +="<table width=100% align=center class=\"subDeviceControl\"><tr align=center valign=center>\n";
		html +="<td>"+Common._onoff(deviceData)+"</td>";
		html +="</tr></table>\n";


		Common._displayPopup(elem,html);
	},
	_dimbri: function(hap) {
		var db = "";

		for (s of [-100,-50,-25,0,25,50,100]) {
			var size = parseInt(Math.log(Math.abs(s*5)));
			var icon = (s>0?"+":"-").repeat(size);
			var action = (s>0?"bright ":"dim ")+size;

			var occb=devices_ha.addOnClickTransmitHAP(hap,{"action":action});
			db+='<button '+occb+'>'+icon+'</button><br>';
		}

		return db;
	},
	_onoff: function(hap) {
		var oo = "";

		var occb=devices_ha.addOnClickTransmitHAP(hap,{"action":"on"});
		oo+='<button style="height:5em;width:100%;" '+occb+'>ON</button><br>';

		occb=devices_ha.addOnClickTransmitHAP(hap,{"action":"off"});
		oo+='<button style="height:5em;width:100%;" '+occb+'>OFF</button><br>';

		return oo;
	},
	_displayPopup : function(elem,html) {
			var p = $('#ha_popup');
			var coord = elem.offset();
			p.html(html);
			p.css({top: coord.top, left: coord.left});
			p.show();
			
	}
}

