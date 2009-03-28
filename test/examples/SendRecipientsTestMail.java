package test.examples;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.mailster.util.DateUtilities;

public class SendRecipientsTestMail 
{
	private static String[] addresses = new String[] {
		"Ted <ted@home.com>", 
		"John Doe <john.doe@fake.com>", 
		"LatchKey <latchy@google.com>",
		"Flora <littleflower@HOTMAIL.fr>", 
		"Jacko <hugues.jacques@HOTmaiL.FR>", 
		"zero_187@google.com",
		"nico <NicoBling@hotMail.com>",
		"carlita@htomail.it",
		"kirby.plazza@marvel.com",
		"b.obama@whitehouse.us",
		"Da hard dude<ironman@avengers.wld>"};
	
	public static void main(String[] args) throws Exception 
	{
		// create the base for our message
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", "localhost");
		Session session = Session.getInstance(props);

		MimeMessage baseMsg = new MimeMessage(session);
		InternetAddress from = getRandomEmailAddress();
		baseMsg.setFrom(from);
		baseMsg.addRecipient(Message.RecipientType.BCC, from);
		
		System.out.println("from:"+ from);
		
		for (int i=0;i<addresses.length;i++)
		{
			InternetAddress addr = getRandomEmailAddress();
			if (rndBoolean()) 
			{
				baseMsg.addRecipient(Message.RecipientType.TO, addr);
				System.out.println("to:"+ addr);
			}
			else
			{
				baseMsg.addRecipient(Message.RecipientType.CC, addr);
				System.out.println("cc:"+ addr);
			}
		}
		
		baseMsg.setSubject("Test recipients mail");
		baseMsg.setContent("Test content ë", "text/plain; charset=\"ISO-8859-1\"");
		baseMsg.setHeader("Date", DateUtilities.rfc822DateFormatter.format(new Date()));
		
        baseMsg.saveChanges();
        Transport.send(baseMsg);
	}
    
	private static boolean rndBoolean()
	{
		return Math.random() >= 0.5;
	}
	
	private static int rnd(int limit)
	{
		return (int) Math.round(Math.random()*limit);
	}
	
	private static InternetAddress getRandomEmailAddress() 
		throws AddressException
	{
		return new InternetAddress(addresses[rnd(addresses.length-1)]);
	}
}
