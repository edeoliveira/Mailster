package test.junit;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import junit.framework.TestCase;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.mailster.crypto.MailsterKeyStoreFactory;
import org.mailster.message.SmtpMessage;
import org.mailster.server.MailsterSMTPServer;
import org.mailster.server.events.SMTPServerAdapter;
import org.mailster.server.events.SMTPServerEvent;
import org.mailster.service.MailsterConstants;

public class EncryptedMailTest extends TestCase
{
	  private static GregorianCalendar gc = new GregorianCalendar();
	  private static final int SMTP_PORT = 1082;
	  
	  private MailsterSMTPServer server;
	  private List<SmtpMessage> messages = new ArrayList<SmtpMessage>();

	  public EncryptedMailTest(String s) 
	  {
		  super(s);
	  }

	  protected void setUp() 
	  	throws Exception 
	  {
		  super.setUp();
		  server = new MailsterSMTPServer(SMTP_PORT);			
		  server.addSMTPServerListener(new SMTPServerAdapter() {		
			public void emailReceived(SMTPServerEvent event) {
				messages.add(event.getMessage());
			}
		  });

	      server.start();
	  }

	  protected void tearDown() 
	  	throws Exception 
	  {
	      super.tearDown();
	      server.stop();
	  }
	
	public static void main(String[] args) throws Exception 
	{
		sendMail(25);
	}
	
	public void testSendEncodedMessage() 
	{
		try 
		{
			sendMail(SMTP_PORT);			
			assertEquals(2, messages.size());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			fail("Unexpected exception: " + e);
		}		
	}
	
	private static void sendMail(int port) throws Exception
	{
		// Open the key store
		KeyStore ks = MailsterKeyStoreFactory.getInstance().getKeyStore();
		Enumeration<String> e = ks.aliases();
		String keyAlias = null;
		String alias = null;
		while (e.hasMoreElements()) 
		{
			alias = (String) e.nextElement();
			if (ks.isKeyEntry(alias))
				keyAlias = alias;
		}
		
		if (keyAlias == null) 
		{
			System.err.println("can't find a private key!");
			System.exit(0);
		}
		Certificate[] chain = ks.getCertificateChain(keyAlias);

		// create the generator for creating an smime/encrypted message
		SMIMEEnvelopedGenerator gen = new SMIMEEnvelopedGenerator();
		gen.addKeyTransRecipient((X509Certificate) chain[0]);

		// Generate the encrypted bodyparts
		// CHANGES FROM HERE
		// create the base for our message
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", "localhost");
		props.setProperty("mail.smtp.port", String.valueOf(port));		
		Session session = Session.getInstance(props);

		MimeMessage baseMsg = new MimeMessage(session);
		MimeBodyPart bp1 = new MimeBodyPart();
		bp1.setHeader("Content-Type", "text/plain");
		bp1.setContent("Hello World!!!", "text/plain; charset=\"ISO-8859-1\"");
		// Attach the file
		MimeBodyPart bp2 = new MimeBodyPart();
		FileDataSource fileAttachment = new FileDataSource(MailsterConstants.USER_DIR+"/pom.xml");
		DataHandler dh = new DataHandler(fileAttachment);
		bp2.setDataHandler(dh);
		bp2.setFileName(fileAttachment.getName());
		bp2.setHeader("Content-Type", "application/pdf");

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(bp1);
		multipart.addBodyPart(bp2);

        MimeBodyPart finalPart = new MimeBodyPart();
        finalPart.setContent(multipart);
        
		baseMsg.setHeader("Content-Type", "multipart/signed");
		baseMsg.setFrom(new InternetAddress("Ted <ted@home.com>"));
		baseMsg.setRecipient(Message.RecipientType.TO, new InternetAddress(
				"John Doe <john.doe@fake.com>"));
		baseMsg.setSubject("Test Signed Message");
		baseMsg.setContent(signMessage(finalPart));
		baseMsg.setSentDate(getRandomDate());
        baseMsg.saveChanges();
        
        Transport.send(baseMsg);
        
        baseMsg.setRecipient(Message.RecipientType.TO, new InternetAddress(
		"Mickael Fake <mickael.fake@gmail.com>"));
        baseMsg.setHeader("Content-Type", null);
        baseMsg.setSubject("Example Encrypted Message II with very big subject line included " +
        		"in the header to test text wrapping method.");
		baseMsg.setContent(cryptMessage(finalPart));
		baseMsg.setSentDate(getRandomDate());
        baseMsg.saveChanges();
        Transport.send(baseMsg);		
	}
    
	private static int rnd(int limit)
	{
		return (int) Math.round(Math.random()*limit);
	}
	
	private static Date getRandomDate()
	{
		gc.clear();
		gc.set(2008, rnd(11), rnd(28), rnd(23), rnd(59), rnd(59));
		return gc.getTime();
	}
	
    private static MimeMultipart cryptMessage(MimeBodyPart mbp)
	    throws Exception
	{
		// Open the key store
		char[] pwd = "password".toCharArray();
		
		KeyStore ks = MailsterKeyStoreFactory.loadKeyStore("PKCS12", "clients.p12", pwd);    	
    	SMIMEEnvelopedGenerator  gen = new SMIMEEnvelopedGenerator();
        
        gen.addKeyTransRecipient((X509Certificate) ks.getCertificate(MailsterKeyStoreFactory.TED_CERT_ALIAS));

        // generate the enveloped message
        MimeBodyPart envPart = gen.generate(mbp, SMIMEEnvelopedGenerator.AES256_CBC, "BC");
        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(envPart);
        
        return multipart;
	}
    
    private static MimeMultipart signMessage(MimeBodyPart mbp)
            throws Exception
    {
        // Open the key store
    	char[] pwd = "password".toCharArray();
    	
        KeyStore ks = MailsterKeyStoreFactory.loadKeyStore("PKCS12", "clients.p12", pwd);
        String alias = MailsterKeyStoreFactory.TED_CERT_ALIAS;
        Certificate[] chain = ks.getCertificateChain(alias);
        PrivateKey privateKey = (PrivateKey) ks.getKey(alias, pwd);

        ArrayList<Certificate> certList = new ArrayList<Certificate>();

        for (int i = 0; i < chain.length; i++)
            certList.add(chain[i]);

        CertStore certs = CertStore.getInstance("Collection",
                new CollectionCertStoreParameters(certList), "BC");

        ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
        SMIMECapabilityVector caps = new SMIMECapabilityVector();

        caps.addCapability(SMIMECapability.dES_EDE3_CBC);
        caps.addCapability(SMIMECapability.rC2_CBC, 128);
        caps.addCapability(SMIMECapability.dES_CBC);
        caps.addCapability(SMIMECapability.aES256_CBC);

        signedAttrs.add(new SMIMECapabilitiesAttribute(caps));

        SMIMESignedGenerator gen = new SMIMESignedGenerator();

        gen.addSigner(privateKey, (X509Certificate) chain[0],
                SMIMESignedGenerator.DIGEST_SHA1, 
                new AttributeTable(signedAttrs), null);

        gen.addCertificatesAndCRLs(certs);

        return gen.generate(mbp, "BC");
    }    
}