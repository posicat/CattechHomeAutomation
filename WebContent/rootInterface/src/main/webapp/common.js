
function renderInterface(devices) {
	var menuInterface = $('#menuInferface');
	
	for (var i = 0; i < devices.length; i++) {
		
		var dev = devices[i];
		
		var blk = dev.deviceName + "["+dev.deviceMap_id+"]";
		
		
		if (dev.interfaceType == "group") {
			blk = "<a class=\"device\" href=\"/?group="+dev.deviceMap_id+"\" >" + blk + "</a>";
		}else {
			blk = "<div class=\"device\">" +blk +"</div>";	
		}
		
		var content = $(blk);

		content.css("width",dev.dx+"px");
		content.css("height",dev.dy+"px");
		content.css("left",dev.x+"px");
		content.css("top",dev.y+"px");

		menuInterface.append(content);
		
	}
}