package junit;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.mailster.smtp.SimpleSmtpServer;

import junit.framework.TestCase;

/**
 * Demonstrate possible bug in Dumbster SMTP server 
 * when Transport.isConnected() is called.
 */
public class DumbsterTest extends TestCase
{
    private static final String HOST = "localhost";
    private static final int PORT = 1000;

    /**
     * Succeeds: one email recorded.
     */
    public void testSendWithoutConnectedCheck() throws Exception
    {
        SimpleSmtpServer server = new SimpleSmtpServer(PORT);
        server.start();
        sendEmail(false);
        server.stop();
        assertEquals("# emails received", 1, server.getReceivedEmailSize());
    }

    /**
     * Fails: two emails recorded. The second is completely blank. The javadocs
     * for Transport.isConnected() say it actually pings the server: 
     * @link http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/SMTPTransport.html#isConnected()
     */
    public void testSendWithConnectedCheck() throws Exception
    {
        SimpleSmtpServer server = new SimpleSmtpServer(PORT);
        server.start();
        sendEmail(true);
        server.stop();
        assertEquals("# emails received", 1, server.getReceivedEmailSize());
    }

    private void sendEmail(boolean checkState) throws Exception
    {
        Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", String.valueOf(PORT));
        Session session = Session.getDefaultInstance(props, null);
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