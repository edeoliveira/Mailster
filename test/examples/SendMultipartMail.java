package test.examples;

import static org.mailster.service.MailsterConstants.USER_DIR;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMultipartMail
{
	public void testAttachedFiles() throws MessagingException {
		// create the base for our message
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", "localhost");
		Session session = Session.getInstance(props);

		for (int i=0;i<2;i++)
		{
			MimeMessage baseMsg = new MimeMessage(session);
			MimeBodyPart bp1 = new MimeBodyPart();
			bp1.setHeader("Content-Type", "text/plain");
			bp1.setText("Hello World !!!");
	
			// Attach the file
			MimeBodyPart bp2 = new MimeBodyPart();
			FileDataSource fileAttachment = new FileDataSource(USER_DIR+"/changelog.htm");
			DataHandler dh = new DataHandler(fileAttachment);
			bp2.setDataHandler(dh);
			bp2.setFileName(fileAttachment.getName());
			bp2.setHeader("Content-Type", "text/html");
	
			Multipart multipart = new MimeMultipart();
			if (i==0)
				multipart.addBodyPart(bp1);
			multipart.addBodyPart(bp2);
	
	        MimeBodyPart finalPart = new MimeBodyPart();
	        finalPart.setContent(multipart);
	        
			baseMsg.setFrom(new InternetAddress("Ted <ted@home.com>"));
			baseMsg.setRecipient(Message.RecipientType.TO, new InternetAddress("My_dest <dest@home.com>"));
			
			baseMsg.setSubject("Message "+i);
			baseMsg.setContent(multipart);
			baseMsg.setSentDate(new Date());
	        baseMsg.saveChanges();	        
	        
	        Transport.send(baseMsg);
		}
	}
}
