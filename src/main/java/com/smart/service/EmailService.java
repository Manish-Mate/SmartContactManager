package com.smart.service;

import java.util.Properties;

import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to) {
		boolean f= false;
		String host = "smtp.gmail.com";
		String from="manish20comp@student.mes.ac.in";
		Properties properties = System.getProperties();
		System.out.println(properties);

//		Setting important info to the properties object
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

//		step 1: to get the session Object.class.

		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
				// TODO Auto-generated method stub
				return new jakarta.mail.PasswordAuthentication("manish20comp@student.mes.ac.in", "Manish@777");
			}

		});

		session.setDebug(true);
//		compose the message

		MimeMessage msg = new MimeMessage(session);

		try {

			msg.setFrom(from);
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			msg.setSubject(subject);
//			msg.setText(message);
			msg.setContent(message, "text/html");

			// send the message using transport class

			Transport.send(msg);

			System.out.println("Send success.....");
			f=true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("mail not sent");
		}
		return f;
	}
}
