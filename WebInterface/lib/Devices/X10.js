
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
}

