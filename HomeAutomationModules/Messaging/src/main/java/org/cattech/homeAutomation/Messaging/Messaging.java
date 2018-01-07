package org.cattech.homeAutomation.Messaging;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONObject;

public class Messaging extends HomeAutomationModule {
	static Logger log = Logger.getLogger(Messaging.class.getName());

	public Messaging(ChannelController controller) {
		super(controller);
	}

	@Override
	public String getModuleChannelName() {
		return "Messaging";
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		if (null != incoming.getWrapper()) {
			boolean handled = false;
			if (incoming.getData().has("user")) {
				// Sending a message to a user
				String user = incoming.getData().getString("user");
				String sendVia = incoming.getData().getString("sendVia");

				List<URI> msgPaths = lookupUserMessagePath(user, sendVia);

				for (URI pth : msgPaths) {
					sendMessageViaPath(pth, incoming.getData());
				}

				handled = true;
			}
			if (incoming.getData().has("device")) {
				// Sending a message to a user
				String device = incoming.getData().getString("device");
				handled = true;
			}

			if (!handled) {
				log.debug("Messaging request type was not recognized : " + incoming);
			}

		}
	}

	private void sendMessageViaPath(URI pth, JSONObject data) {
		log.debug("Sending message to " + pth);
		log.debug("Message contents " + data);

		if ("mailto".equals(pth.getScheme())) {
			Session mailSession = Session.getInstance(configuration.getProps());
//			mailSession.setDebug(true);
			MimeMessage message = new MimeMessage(mailSession);

			try {
				message.setFrom(configuration.getProps().getProperty("mail.from"));
				String[] pthParts = pth.toString().split(":");
				log.info("Destination paths : "+pthParts[1]);
				message.setRecipient(Message.RecipientType.TO, new InternetAddress(pthParts[1]));
				
				if (data.has("subject")) {
					message.setHeader("Subject", data.getString("subject"));
				}else {
					message.setHeader("Subject", "HomeAutomation");
				}
				
				if (data.has("body")) {
					message.setText(data.getString("body"));
				}else {
					message.setText("");
				}
				Transport.send(message);
			} catch (MessagingException e) {
				log.error("Error creating message.", e);
			}
		}
	}

	private ArrayList<URI> lookupUserMessagePath(String user, String sendVia) {
		Connection conn = getHomeAutomationDBConnection();
		PreparedStatement stmt;
		ResultSet rs;

		String query = "SELECT uri FROM messagePath m " + " LEFT JOIN user u ON u.user_id=m.id "
				+ " WHERE m.channel_type='user' AND u.name=? AND sendVia=?";
		log.debug("SQL : " + query);

		ArrayList<URI> result = new ArrayList<URI>();

		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, user);
			stmt.setString(2, sendVia);
			rs = stmt.executeQuery();
			while (rs.next()) {
				try {
					result.add(new URI(rs.getString("uri")));
				} catch (URISyntaxException e) {
					log.error("URI format error", e);
				}
			}
			log.debug("URL for " + user + " via " + sendVia + " was " + result);
		} catch (SQLException e) {
			log.error("Could not look up message path for " + user + " via " + sendVia, e);
		}
		closeNoThrow(conn);
		return result;
	}

}
