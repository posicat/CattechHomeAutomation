package org.cattech.homeAutomation.WebInterfaceModule;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cattech.homeAutomation.homeAutomationContext.HomeAutomationContextListener;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class AboutPage extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		HashMap<String,String> manifest = (HashMap<String, String>) request.getServletContext().getAttribute(HomeAutomationContextListener.SERVER_MANIFEST);

		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter();

		out.println("<h1>Cattech Home Automation Webserver</h1>");
		out.println("Build : " + manifest.getOrDefault("Implementation-Title","") + " " + manifest.getOrDefault("Implementation-Version",""));

		baseRequest.setHandled(true);
	}

}
