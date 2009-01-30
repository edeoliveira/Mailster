package org.mailster.crypto.smime;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

import javax.mail.internet.MimeBodyPart;

import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.mail.smime.SMIMEEnveloped;
import org.bouncycastle.mail.smime.SMIMEUtil;
import org.mailster.message.SmtpMessage;
import org.mailster.message.SmtpMessagePart;

/**
 * ---<br>
 * Mailster (C) 2007-2009 De Oliveira Edouard
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 * <p>
 * See&nbsp; <a href="http://tedorg.free.fr/en/projects.php" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * SmimeUtilities.java - A set of utilities methods to S/MIME functions.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class SmimeUtilities
{
    
    /**
     * Build a path using the given root as the trust anchor, and the passed
     * in end constraints and certificate store.
     * <p>
     * Note: the path is built with revocation checking turned off.
     */
    private static PKIXCertPathBuilderResult buildPath(X509Certificate rootCert,
                                                      X509CertSelector endConstraints,
                                                      CertStore certsAndCRLs)
        throws Exception
    {
        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", "BC");
        PKIXBuilderParameters buildParams = new PKIXBuilderParameters(
                Collections.singleton(new TrustAnchor(rootCert, null)), endConstraints);
        
        buildParams.addCertStore(certsAndCRLs);
        buildParams.setRevocationEnabled(false);
        
        return (PKIXCertPathBuilderResult)builder.build(buildParams);
    }
    
    /**
     * Return a boolean array representing a <code>KeyUsage</code> 
     * with the digitalSignature bit set.
     */
    private static boolean[] getKeyUsageForSignature()
    {
        boolean[] val = new boolean[9];
        val[0] = true;
        return val;
    }
    
    /**
     * Take a CMS SignedData message and a trust anchor and determine if
     * the message is signed with a valid signature from a end entity
     * certificate recognized by the trust anchor rootCert.
     */
    public static boolean isValid(CMSSignedData signedData,
                                  X509Certificate rootCert)
        throws Exception
    {
        CertStore certsAndCRLs = signedData.getCertificatesAndCRLs("Collection", "BC");
        SignerInformationStore signers = signedData.getSignerInfos();
        Iterator<?> it = signers.getSigners().iterator();

        while (it.hasNext())
        {
            SignerInformation signer = (SignerInformation)it.next();
            X509CertSelector signerConstraints = signer.getSID();
            
            signerConstraints.setKeyUsage(getKeyUsageForSignature());            
            PKIXCertPathBuilderResult result = buildPath(rootCert, signer.getSID(), certsAndCRLs);

            if (signer.verify(result.getPublicKey(), "BC"))
            	return true;
        }
        
        return false;
    }
    
    public static MimeBodyPart decryptMimeBodyPart(SmtpMessage msg, 
									SmtpMessagePart part, KeyStore ks, char[] password)
	throws Exception
	{
    	return decryptMimeBodyPart(part.asMimeBodyPart(msg.getBodyCharset()), ks, password);
	}
	
    public static MimeBodyPart decryptMimeBodyPart(MimeBodyPart mbp, 
    								KeyStore ks, char[] password)
    	throws Exception
    {
    	SMIMEEnveloped enveloped = new SMIMEEnveloped(mbp);
    	Enumeration<String> aliases = ks.aliases();
    	
    	while (aliases.hasMoreElements())
    	{
    		String alias = aliases.nextElement();
    		if (ks.isKeyEntry(alias))
    		{    			
    			MimeBodyPart decrypted = decryptEnvelope(enveloped, 
    					ks.getKey(alias, password), (X509Certificate) ks.getCertificate(alias));
    			if (decrypted != null)
    				return decrypted;
    		}    			
    	}
    	
    	return null;
    }
    
    /**
     * Try to decrypt the provided envelope with the provided certificate 
     * and private key. 
     */
    public static MimeBodyPart decryptEnvelope(SMIMEEnveloped enveloped, 
    							Key key, X509Certificate cert)
    	throws Exception
    {
    	 // look for our recipient identifier
        RecipientId recId = new RecipientId();
        recId.setSerialNumber(cert.getSerialNumber());
        recId.setIssuer(cert.getIssuerX500Principal().getEncoded());

        RecipientInformationStore recipients = enveloped.getRecipientInfos();
        RecipientInformation recipient = recipients.get(recId);

        // decryption step
    	if (recipient != null)
    		return SMIMEUtil.toMimeBodyPart(recipient.getContent(key, "BC"));
    	else
    		return null;
    }
    
    /**
     * Returns <code>true</code> if the message is S/MIME signed.
     */
    public static boolean isSignedMessage(SmtpMessage msg)
    {
    	SmtpMessagePart parentPart = msg.getInternalParts();
    	for (SmtpMessagePart p : parentPart.getAttachedFiles())
    	{
    		if (p.getFileName().endsWith("p7s") &&
    				("application/pkcs7-signature".equals(p.getContentType()) ||
    						"signed-data".equals(p.getHeaders().getHeaderValue("smime-type"))))
    			return true;
    	}
    	
    	return false;
    }
    
    /**
     * Returns <code>true</code> if the message contains S/MIME encrypted data.
     */
    public static boolean containsEnvelopedData(SmtpMessage msg)
    {
    	SmtpMessagePart parentPart = msg.getInternalParts();
    	for (SmtpMessagePart p : parentPart.getAttachedFiles())
    	{
    		if (p.getFileName().endsWith("p7m") &&
    				("application/pkcs7-mime".equals(p.getContentType()) ||
    						"enveloped-data".equals(p.getHeaders().getHeaderValue("smime-type"))))
    			return true;
    	}
    	
    	return false;
    }

    /**
     * Returns <code>true</code> if the part data is S/MIME encrypted.
     */
    public static boolean isEnvelopedData(SmtpMessagePart part)
    {
		return (part.getFileName() != null && part.getFileName().endsWith("p7m") &&
				("application/pkcs7-mime".equals(part.getContentType()) ||
						"enveloped-data".equals(part.getHeaders().getHeaderValue("smime-type"))));
    }    
}
