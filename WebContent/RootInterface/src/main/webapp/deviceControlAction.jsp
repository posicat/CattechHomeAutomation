<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<jsp:useBean id="devAction" class="org.cattech.HomeAutomation.RootInterface.servlets.DeviceControlAction" scope="page" />

<% 
	devAction.setupServletState(request,response); 

	devAction.processDeviceAction();
%>

<%= devAction.displayStatus() %> 
