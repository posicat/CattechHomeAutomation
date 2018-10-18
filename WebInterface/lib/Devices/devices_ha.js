devices_ha = {
	HAPCounter : 10000,
	HAPs : {},
	ActionCounter : 10000,
	Actions : {},
    protocols : ["Common"],

	addHAP : function(hap) {
		hap._idx = devices_ha.HAPCounter++;
		devices_ha.HAPs[hap._idx]=hap;
		return hap._idx;
	},
	addAction : function(action) { 
		actionIDX = devices_ha.ActionCounter++;
		devices_ha.Actions[actionIDX]=action;
		return actionIDX;
	},
	renderDeviceConfig : function(elemID,hapIDX) {
		var elem=$('#'+elemID); // Find element
		var hap = devices_ha.HAPs[hapIDX];  // Lookup hap

		elem.html("<div>"+JSON.stringify(hap)+"</div>");

		// Attempt to get the manager class for this HAP's interface
		var manager = window[hap.data.protocol];

		if (typeof manager !== 'undefined') {
			manager.renderConfig(elem,hap);
		}else{
			// Fallback to a generic HAP config
			console.log("No configs for [] defaulting to generic send");
			devices_ha._genericRenderConfig(elem,hap);
		}
	},

	_genericRenderConfig : function(elem,hap) {
		var html='';
		var formID = 'formCfg'+(hap._idx);
		var hapID = 'hapCfg'+(hap._idx);
		var protID = 'protCfg'+(hap._idx);

		html += '<form id="'+formID+'">';
		html += '<div id="'+protID+'"></div>';
		html += '<textarea id="'+hapID+'" name="hap" rows="5" cols="15"></textarea>';
		html += '</form>';

		elem.html(html);
		document.getElementById(hapID).value=JSON.stringify(hap);

		devices_ha.pullDownDevieSelect(protID);
	},

	pullDownDevieSelect: function(elemID) {
		var elem=$('#'+elemID);
		var html = "";
		html += "<select name=\"deviceList\">\n";
		for (var p of devices_ha.protocols) {
			html += '<option value="'+p+'">'+p+'</option>';
		}
		html += "</select>\n";

		elem.html(html);
	},

	renderDeviceControl : function(elemID,hapIDX) {
		var elem=$('#'+elemID); // Find element
		var hap = devices_ha.HAPs[hapIDX];  // Lookup hap

		//elem.html("<div>"+JSON.stringify(hap)+"</div>");
		if (hap !== undefined) {
			var control = Common[hap.data.interfaceType];

			if (typeof control !== 'undefined') {
				control(elem,hap);
			}else{
				console.log("No controls for [Common."+hap.data.interfaceType+"] defaulting to generic send");
				devices_ha._genericRenderControl(elem,hap);
			}
		}
	},

	_genericRenderControl : function(elem,hap) {
		var hapID = 'hapCtl'+(hap._idx);
		var occb=devices_ha.addOnClickTransmitHAP(hap,{data:{action:'send'}});

		var html='';
		html += '<span id="'+hapID+'"></span>';
		html +='<button style="height:5em;width:100%;" '+occb+'>SEND</button><br>';

		elem.html(html);
		document.getElementById(hapID).value=JSON.stringify(hap);
	},

	renderIcon : function(elemID,hapIDX) {
		var elem=$('#'+elemID); // Find element
		var hap = devices_ha.HAPs[hapIDX];  // Lookup hap

		var html='';
		var iconID = 'icon'+(hap._idx);
		var oc = devices_ha.addOnClickControlPopup(iconID, hap);

		html += '<div class="ha_icon" '+oc+' id="'+iconID+'">\n';
		html += '	<div class="title">'+hap.data.deviceName+'</div>\n';
		html += '	<img class="ha_image" src="./images/devices/'+hap.data.image+'" width=100 height=100>\n';
		html += '	<div class="footer"></div>\n';
		html += '</div>\n';

		elem.html(html);
	},
	updateHAPFromControl : function(hapIDX) {
		var hap = devices_ha.HAPs[hapIDX];  // Lookup hap
		var formID = 'formCfg'+(hap._idx);

		var form=$('#'+formID);
		var formData = $(form).serializeArray();

		console.log("PreMerged HAP");
		console.log(hap);
		console.log("formData");
		console.log(formData);

		hap=devices_ha.mergePackets(hap,formData);
	},

	mergePackets : function(a, b){
		var c = {};

		for(var p in a) {
			if (a.hasOwnProperty(p)) {
				c[p]= a[p];
			}
		}
		for(var p in b) {
			if (b.hasOwnProperty(p)) {
				if (c.hasOwnProperty(p)) {
					if (typeof c[p] === 'object' || typeof c[p] === 'object') {
						c[p] = devices_ha.mergePackets(c[p],b[p]);
					}
				}else{
					c[p]= b[p];
				}
			}
		}
		// alert(
			// JSON.stringify(a)+"\n\n"+
			// JSON.stringify(b)+"\n\n"+
			// JSON.stringify(c)
		// );

		return c;
	},

	addOnClickControlPopup: function(elemID,hap) {
		return 'onClick="devices_ha._onClickControlPopup(event,\''+elemID+'\','+hap._idx+');"';
	},

	_onClickControlPopup : function(event,elemID,hapIDX) {
		event.stopPropagation();
		var elem=$('#'+elemID); // Find element
		var hap = devices_ha.HAPs[hapIDX];  // Lookup hap
		devices_ha.renderDeviceControl(elemID,hapIDX);
	},

	addOnClickTransmitHAP : function(hap,action) {
		var actionIDX = devices_ha.addAction({"data":action});
		return 'onClick="devices_ha._onClickTransmitHAP(event,'+hap._idx+','+actionIDX+');"';
	},

	_onClickTransmitHAP(event,hapIDX,actionIDX) {
		//event.stopPropagation();
		var hap = devices_ha.HAPs[hapIDX];  // Lookup hap

		hap=devices_ha.mergePackets(hap,devices_ha.Actions[actionIDX]);

		var dev = hap['data']['device'];
		var ctl="DeviceCommandHandler";
		if (! dev) {
			dev = hap['data']['nativeDevice'];
			ctl = dev['channelController'];
		}

		if (dev) {
			hap.destination = [ctl];
			hap.source = 'transient.webpage';

			var url = "http://pawz.cattech.org/pw/homeAutomation/dispatch.cgi?mode=send&packet="+encodeURIComponent(JSON.stringify(hap));
			//alert(JSON.stringify(hap));
			$.get(url);
		}else{
			console.log("No device name found");
		}
	},
}

