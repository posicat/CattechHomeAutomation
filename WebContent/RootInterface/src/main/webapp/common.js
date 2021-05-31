function renderInterface() {
	for (let i = 0; i < currentDevices.length; i++) {
		var dev = currentDevices[i];
		var id = "deviceID_" + dev.deviceMap_id;
	
		
		var deviceControl = "<div id=\""+id+"_status\" class=\"deviceStatus\"></div>";
		
		deviceControl += "<div class=\"deviceName\" title=\"" + "[" + dev.deviceMap_id + "]" + "\">" + dev.deviceName + "</div>";
	
		var setOnClick = true;
		var classIDEtc = "class=\"device\" id=\"" + id + "\" ";
	
		if (dev.interfaceType == "menuPage") {
			deviceControl = "<a " + classIDEtc + " href=\"/?menuPage=" + dev.deviceMap_id + "\" >" + deviceControl + "</a>";
			setOnClick = false;
		} else {
			deviceControl = "<div " + classIDEtc + " >" + deviceControl + "</div>";
		}
	
		// Convert strings into actual elements.
		deviceControl=$(deviceControl);
		
		deviceControl.css("background-image", "url(/deviceImages/" + dev.image + ")");
		deviceControl.css("background-repeat", "no-repeat");
		deviceControl.css("background-size", dev.dx + "px " + dev.dy + "px");
	
		deviceControl.css("width", dev.dx + "px");
		deviceControl.css("height", dev.dy + "px");
		deviceControl.css("left", dev.x + "px");
		deviceControl.css("top", dev.y + "px");
	
		$('#menuInferface').append(deviceControl);
	
		if (setOnClick) {
			$('#' + id).click(
					function(idx){  return function(){loadInterface(idx)}  }(i)
			);
		}
	}
}

function loadInterface(devIdx) {
	var dev = currentDevices[devIdx];
	var id = "deviceID_" + dev.deviceMap_id;

	$('#wholePage').show(10);
	$('#wholePage').click(function() {
		$('#wholePage').hide(10);
	});

	$('#controls').load("/deviceControlRenderer.jsp?type=" + dev.interfaceType + "&dIdx=" + devIdx);
	$('#controls').click(function(e) {
		e.stopImmediatePropagation();
	});
}

function processAction(devIdx, action) {
	var dev = currentDevices[devIdx];
	var id = "deviceID_" + dev.deviceMap_id;
	
	$('#'+id+"_status").load("/deviceControlAction.jsp?devID="+dev.deviceMap_id+"&action="+action);
}