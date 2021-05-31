<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.webjars.org/tags" prefix="wj"%>

<jsp:useBean id="homeAuto" class="org.cattech.HomeAutomation.RootInterface.servlets.HomeAutoServlet" scope="page" />
<% homeAuto.setupServletState(request,response); %>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Cattech Home Automation</title>
	<script type='text/javascript' src='<wj:locate path="jquery.min.js" relativeTo="META-INF/resources"/>'></script>
	<script type='text/javascript' src='<wj:locate path="jquery-ui.min.js" relativeTo="META-INF/resources"/>'></script>
	<script type='text/javascript' src='/common.js'></script>
	<link rel="stylesheet" type="text/css" href="/common.css" />
	<script>
		var currentDevices = <%=homeAuto.getJSONListOfDevicesForCurrentMenuPage().toString()%>;
		
		$( document ).ready(function() {
			renderInterface();
		});
	</script>
</head>
<body>
	<div class="wholePage" id="wholePage">
		<div class="controls" id="controls">CONTROLS</div>
	</div>
	<div class="groupName" id="groupName"><%=homeAuto.getMenuPage()%></div>
	<div class="menuInterface" id="menuInferface"></div>
</body>
</html>