var statusTimer;

function load_link(link) {
	$("#"+link.target).load(link.href);
	return false;		
}

function findPos(obj) {
	var curleft = curtop = 0;
	if (obj.offsetParent) {
		do {
			curleft += obj.offsetLeft;
			curtop += obj.offsetTop;
		} while (obj = obj.offsetParent);
	}
	return {x:curleft,y:curtop};
}

function move_object_here(o,moveID){
	var c=findPos(o);

	var po = document.getElementById(moveID);
	po.style.top=c.y+'px';
	po.style.left=c.x+'px';
	//po.style.position="absolute";
}

function getStatusInABit(seconds,type) {
	clearTimeout(statusTimer);
	statusTimer = setTimeout(function(){ getStatus(type) }, seconds*1000);
}

function getStatus(type) {
	$.getJSON('./dispatch.cgi?data={}&type='+type+'&mode=status',function(data){updateStatus(data,type);})
  .fail(function(jqXHR, textStatus, errorThrown) {
    console.log( "error "+textStatus );
  });
}

function updateStatus(Status,type) {
	console.log('updateStatus : ' + Status);
	var msg ='';
	var name,dat,val;
	$('.x10_title').each(function(i, obj) {

		name = obj.attributes.name.value;
		if (name) {
			dat = name.match(/x10_([A-P])([0-9]+)/);
		}
		if (dat) {
			val = Status[dat[1]][dat[2]];
		}
		if (val) {
			var dimbri = val.split(',');
			var cl = "x10_title ";
			if (dimbri[0] == '?') { cl +="X10_UNK ";}
			if (dimbri[0] == '*') { cl +="X10_ON ";}
			if (dimbri[0] == '.') { cl +="X10_OFF ";}
			if (dimbri[0] == 'x') { cl +="X10_DIM ";}

			obj.className=cl;

			msg += dat + "=" + cl + "\n";
		}
	});
	console.log('updateStatus : ' + msg);
	getStatusInABit(10);
}
