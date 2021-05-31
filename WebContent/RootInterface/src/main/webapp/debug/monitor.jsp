<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.webjars.org/tags" prefix="wj"%>

<jsp:useBean id="homeAuto" class="org.cattech.HomeAutomation.RootInterface.servlets.HomeAutoServlet" scope="page" />
<% homeAuto.setupServletState(request, response); %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Cattech Home Automation - Message Debug Monitor</title>
<script type='text/javascript'
	src='<wj:locate path="jquery.min.js" relativeTo="META-INF/resources"/>'></script>
<script type='text/javascript'
	src='<wj:locate path="jquery-ui.min.js" relativeTo="META-INF/resources"/>'></script>
<script type='text/javascript' src='/common.js'></script>
<link rel="stylesheet" type="text/css" href="/common.css" />
<script>
	var l = window.location;
	var pushSocket = new WebSocket("ws://" + l.hostname + ":" + l.port + "/ws/debugMonitor");

	pushSocket.onmessage = function(event) {
		$('#monitor').append(event.data);
	}

	pushSocket.onopen = function(event) {
		//send empty message to initialize socket connnection
		pushSocket.send("");
	};

	pushSocket.onclose = function(event) {
		//send empty message to initialize socket connnection
		alert("Socket Closed by Server");
	};

	function sendMessage(message) {
		pushSocket.send(message);
		return false;
	}
</script>
<style>
body {
	background: #554459;
	margin: 0px;
	padding-bottom: 2em;
	color: #FFFFFF;
	min-height: 100%;
}
.monitorBox {
	width: 100vw;
	height: 95vh;
}
.monitor {
}
</style>
</head>
<body>
	<div class="wholePage" id="wholePage">
		<div class="controls" id="controls"></div>
	</div>
	<div class="monitorBox">
		<div id="monitor" class="monitor"></div>
	</div>
	<div style="position: absolute; bottom: 0px;">
		<form onSubmit="return sendMessage($('#sendPacket')[0].value);">
			<input type=text id="sendPacket">
			<input type="submit" value="send">
		</form>
	</div>
</body>
</html>