package test.examples;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.mailster.crypto.MailsterKeyStoreFactory;
import org.mailster.service.MailsterConstants;
import org.mailster.util.DateUtilities;

public class SendMultipleMail 
{
	private static GregorianCalendar gc = new GregorianCalendar();
	private static String[] addresses = new String[] {"Ted <ted@home.com>", "John Doe <john.doe@fake.com>", " LatchKey <latchy@google.com>",
																									"Flora <littleflower@HOTMAIL.fr>", "Jacko <hugues.jacques@HOTmaiL.FR>", "zero_187@google.com"};
	
	public static void main(String[] args) throws Exception 
	{
		// create the base for our message
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", "localhost");
		Session session = Session.getInstance(props);

		for (int i=0;i<200;i++)
		{
			MimeMessage baseMsg = new MimeMessage(session);
			MimeBodyPart bp1 = new MimeBodyPart();
			bp1.setHeader("Content-Type", "text/plain");
			bp1.setContent("Hello World !!!", "text/plain; charset=\"ISO-8859-1\"");
	
			// Attach the file
			MimeBodyPart bp2 = new MimeBodyPart();
			FileDataSource fileAttachment = new FileDataSource(MailsterConstants.USER_DIR+"/changelog.htm");
			DataHandler dh = new DataHandler(fileAttachment);
			bp2.setDataHandler(dh);
			bp2.setFileName(fileAttachment.getName());
			bp2.setHeader("Content-Type", "text/html");
	
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(bp1);
			multipart.addBodyPart(bp2);
	
	        MimeBodyPart finalPart = new MimeBodyPart();
	        finalPart.setContent(multipart);
	        
			baseMsg.setFrom(getRandomEmailAddress());
			baseMsg.setRecipient(Message.RecipientType.TO, getRandomEmailAddress());
			
			boolean signed = rndBoolean();
			if (signed)		
			{
				baseMsg.setHeader("Content-Type", "multipart/signed");
				baseMsg.setSubject("secured mail oO [signed]");
				baseMsg.setContent(signMessage(finalPart));
			}
			else
			{
				baseMsg.setSubject("i love this subject");
				baseMsg.setContent(multipart);
			}

			synchronized(DateUtilities.rfc822DateFormatter)
			{
				baseMsg.setHeader("Date", DateUtilities.rfc822DateFormatter.format(getRandomDate()));
			}
	        baseMsg.saveChanges();
	        
	        Transport.send(baseMsg);
		}
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
	
	private static Date getRandomDate()
	{
		gc.clear();
		gc.set(2008, rnd(11), rnd(28), rnd(23), rnd(59), rnd(59));
		return gc.getTime();
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
