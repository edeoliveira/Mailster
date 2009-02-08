package test.junit;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import junit.framework.TestCase;

import org.mailster.message.SmtpMessage;
import org.mailster.server.MailsterSMTPServer;
import org.mailster.server.events.SMTPServerAdapter;
import org.mailster.server.events.SMTPServerEvent;

/**
 * Check a possible bug in Dumbster SMTP server 
 * when Transport.isConnected() is called.
 */
public class ServerTest extends TestCase
{
    private static final String HOST = "localhost";
    private static final int PORT = 2000;

    /**
     * Succeeds: one email recorded.
     */
    public void testSendWithoutConnectedCheck() throws Exception
    {
        MailsterSMTPServer server = new MailsterSMTPServer(PORT);
        final List<SmtpMessage> messages = new ArrayList<SmtpMessage>();

        server.addSMTPServerListener(new SMTPServerAdapter() {		
			public void emailReceived(SMTPServerEvent event) {
				messages.add(event.getMessage());
			}
		});
        server.start();
        sendEmail(false);
        Thread.sleep(500);
        assertEquals("# emails received", 1, messages.size());
        server.stop();
    }

    /**
     * Fails: two emails recorded. The second is completely blank. The javadocs
     * for Transport.isConnected() say it actually pings the server: 
     * @link http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/SMTPTransport.html#isConnected()
     */
    public void testSendWithConnectedCheck() throws Exception
    {
        MailsterSMTPServer server = new MailsterSMTPServer(PORT);
        final List<SmtpMessage> messages = new ArrayList<SmtpMessage>();

        server.addSMTPServerListener(new SMTPServerAdapter() {		
			public void emailReceived(SMTPServerEvent event) {
				messages.add(event.getMessage());
			}
		});
        server.start();
        sendEmail(true);
        server.stop();
        assertEquals("# emails received", 1, messages.size());
    }

    private void sendEmail(boolean checkState) throws Exception
    {
        Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", String.valueOf(PORT));
        Session session = Session.getInstance(props);
        Transport transport = session.getTransport("smtp");
        transport.connect(HOST, PORT, null, null);
        MimeMessage message = new MimeMessage(session);
        message.setContent("test body", "text/plain");
        message.setSubject("test subject");
        message.setFrom(new InternetAddress("from@example.com"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(
                "to@example.com"));
        transport.sendMessage(message, message.getAllRecipients());
        if (checkState)
            transport.isConnected();
        transport.close();
    }
}