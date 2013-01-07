package test.examples;

import java.io.ByteArrayInputStream;
import java.net.Inet4Address;

import org.columba.ristretto.message.Address;
import org.columba.ristretto.smtp.SMTPProtocol;

public class SendAndyTest 
{	
	public static void main(String[] args) throws Exception 
	{
	/*	Properties props = new Properties();
		props.setProperty("mail.smtp.host", "localhost");
		Session session = Session.getInstance(props);

		MimeMessage baseMsg = new MimeMessage(session);
		
		baseMsg.setHeader("Content-Type", "text/plain; charset=utf-8");
		new InternetAddress("andy@qwerty.pl>");
		baseMsg.setHeader("From", "andy@qwerty.pl>");
		baseMsg.setHeader("To", "<andy@qwerty.pl>");
		baseMsg.setHeader("MIME-Version", "1.0");
		baseMsg.setHeader("Content-Transfer-Encoding", "8bit");
		baseMsg.setHeader("X-Mailer", "PHP/5.2.6");
		baseMsg.setText("");
		
        baseMsg.saveChanges();
        Transport.send(baseMsg);*/

		SMTPProtocol smtp = new SMTPProtocol("localhost");
		smtp.openPort();
		smtp.ehlo(Inet4Address.getLocalHost());
		smtp.mail(Address.parse("andy@qwerty.pl>"));
		smtp.rcpt(Address.parse("<andy@qwerty.pl>"));
		
		String data = "From: andy@qwerty.pl>\r\n" +
				"Content-Type: text/plain; charset=utf-8\r\n" +
				"MIME-Version: 1.0\r\n" +
				"Content-Transfer-Encoding: 8bit\r\n" +
				"X-Mailer: PHP/5.2.6\r\n\r\n";
		
		smtp.data(new ByteArrayInputStream(data.getBytes()));
		smtp.quit();		
	}    
}
