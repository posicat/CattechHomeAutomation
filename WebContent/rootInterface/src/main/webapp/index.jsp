<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.webjars.org/tags" prefix="wj"%>

<jsp:useBean id="homeAuto" class="org.cattech.HomeAutomation.servlets.HomeAutoServlet" scope="page" />
<%	homeAuto.loadCurrentState(request,response);%>

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
		var currentDevices = <%=homeAuto.getJSONListOfDevicesForCurrentGroup().toString()%>;
		
		$( document ).ready(function() {
			renderInterface(currentDevices);
		});
	</script>
</head>
<body>
	<div class="groupName" id="groupName"><%=homeAuto.getGroupName() %></div>
	<div class="menuInterface" id="menuInferface"></div>
</body>
</html>