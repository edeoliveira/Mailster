package org.mailster.pop3.connection.ssl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.mailster.util.StreamWriterUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ---<br>
 * Mailster (C) 2007 De Oliveira Edouard
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
 * See&nbsp; <a href="http://mailster.sourceforge.net" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * X509SecureSocketFactory.java - A full featured SSL socket factory that uses
 * X509v3 certificates.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class X509SecureSocketFactory implements X509TrustManager
{
	/** 
	 * Log object for this class. 
	 */
    private static final Logger LOG = LoggerFactory.getLogger(X509SecureSocketFactory.class);
    
    private final static transient String PUBLIC_KEY_CERT_ALIAS = "publicKeyCertificateAlias";

    private final static transient char[] keyStorePass = 
    	new char[] { 'p', 'a', 's', 's', 'w', 'o', 'r', 'd' };

    // all the entries have the same password
    private final static transient char[] entryPass = 
    	new char[] { 'e', 'n', 't', 'r', 'y', 'p', 'a', 's', 's' };

    private MessageDigest digester;

    // file name for key pairs store
    private final static String KEYPAIR_STORE_FILE = "Mailster.jks";
    // file name for remote certificats
    private final static String TRUSTED_STORE_FILE = "trustedKS.jks";

    // store to keep secrets
    private transient KeyStore keyPairKS = KeyStore.getInstance("JKS");
    // store for remotes
    private transient KeyStore trustedKS = KeyStore.getInstance("JKS");

    // certificates
    private transient TrustManagerFactory tmf = TrustManagerFactory
            .getInstance("SunX509");
    private transient KeyManagerFactory kmf = KeyManagerFactory
            .getInstance("SunX509");
    private transient SSLContext context = SSLContext.getInstance("SSLv3");
    private transient X509TrustManager trustManager;

    private static X509SecureSocketFactory _instance;
    
    private X509SecureSocketFactory() throws Exception
    {
        if (Security.getProvider("BC") == null)
            Security.addProvider(new BouncyCastleProvider());
        
        digester = MessageDigest.getInstance("MD5", "BC");
        
        try
        {
            InputStream fis = new FileInputStream(StreamWriterUtilities.USER_DIR+"/"+KEYPAIR_STORE_FILE);
            keyPairKS.load(fis, keyStorePass);
            LOG.debug("Successfully loaded certificate {}",
                    toFingerprint(keyPairKS.getCertificate(PUBLIC_KEY_CERT_ALIAS)));
        }
        catch (FileNotFoundException e)
        {
            keyPairKS.load(null, null);
            KeyPairGenerator KPGen = KeyPairGenerator.getInstance("RSA");
            KPGen.initialize(1024);
            KeyPair KPair = KPGen.generateKeyPair();

            X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
            v3CertGen.setSerialNumber(BigInteger.valueOf(1));

            String DN = "CN=DE OLIVEIRA Edouard, OU=Mailster, O=SOHO, L=Paris, C=FR";
            X509Principal issuerPrincipal = new X509Principal(DN);
            v3CertGen.setIssuerDN(issuerPrincipal);

            // Five years validity
            long delta = 1000L * 60L * 60L * 24L * 365L * 5L;
            long time = System.currentTimeMillis();
            v3CertGen.setNotBefore(new Date(time - delta));
            v3CertGen.setNotAfter(new Date(time + delta));
            X509Principal subjectPrincipal = new X509Principal(DN);
            v3CertGen.setSubjectDN(subjectPrincipal);
            v3CertGen.setPublicKey(KPair.getPublic());
            v3CertGen.setSignatureAlgorithm("MD5WithRSAEncryption");
            X509Certificate publicKeyCertificate = v3CertGen.generate(KPair.getPrivate());
            keyPairKS.setKeyEntry(PUBLIC_KEY_CERT_ALIAS, KPair.getPrivate(),
                    entryPass, new Certificate[] { publicKeyCertificate });
            keyPairKS.store(new FileOutputStream(KEYPAIR_STORE_FILE), keyStorePass);
            
            LOG.debug("New certificate successfully created {}", toFingerprint(publicKeyCertificate));
        }
        kmf.init(keyPairKS, entryPass);

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(StreamWriterUtilities.USER_DIR+"/"+TRUSTED_STORE_FILE);
        }
        catch (FileNotFoundException e) {}

        trustedKS.load(fis, keyStorePass);
        updateTrustedManager();
        context.init(kmf.getKeyManagers(), new TrustManager[] { this }, null);
    }
    
    public static synchronized X509SecureSocketFactory getInstance() 
    	throws Exception
    {
    	if (_instance == null)
    		_instance = new X509SecureSocketFactory();
    		
    	return _instance;
    }
    
    private void updateTrustedManager() throws KeyStoreException
    {
        tmf.init(trustedKS);
        trustManager = (X509TrustManager) tmf.getTrustManagers()[0];
    }

    private String toFingerprint(Certificate cer)
            throws CertificateEncodingException
    {
        byte[] bytes = digester.digest(cer.getEncoded());
        StringBuilder fingerprint = new StringBuilder("MD5: ");
        for (int i = 0; i < bytes.length; i++)
            fingerprint.append(Integer.toHexString(bytes[i] & 0xFF));
        return fingerprint.toString();
    }

    private String getCertificateUniqueID(X509Certificate x509)
    {
    	boolean[] id = x509.getSubjectUniqueID();
    	if (id == null)
    		return CertUtilities.getDN(x509)+x509.hashCode();
    	else
    	{
    		StringBuilder idString = new StringBuilder(id.length);
    		for (int i=0;i<id.length;i++)
    			idString.append(id[i] ? '1' : '0');
    			
    		return idString.toString();
    	}
    }
    
    private void tryTrustManagerUpdate(X509Certificate[] cer, String str)
            throws IOException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException
    {
        LOG.debug("Received certificate {}\n{}",cer[0],toFingerprint(cer[0]));

        //TOSEE Trust management
        /*System.out.println("Do you want to trust on it? y/n...");
        if (System.in.read() == 'n')
            System.exit(0);*/

        trustedKS.setCertificateEntry(getCertificateUniqueID(cer[0]), cer[0]);
        trustedKS.store(new FileOutputStream(TRUSTED_STORE_FILE), keyStorePass);
        updateTrustedManager();
    }

    public void checkClientTrusted(X509Certificate[] cer, String str)
            throws CertificateException
    {
        try
        {
            trustManager.checkClientTrusted(cer, str);
        }
        catch (CertificateException e)
        {
            if (e instanceof CertificateEncodingException
                    || e instanceof CertificateParsingException
                    || e instanceof CertificateNotYetValidException
                    || e instanceof CertificateExpiredException)
                throw e;
            try
            {
                tryTrustManagerUpdate(cer, str);
                trustManager.checkClientTrusted(cer, str);
            }
            catch (KeyStoreException ex)
            {
                throw new CertificateException(
                        "Impossible to add a certificate to the store.");
            }
            catch (IOException ex)
            {
                throw new CertificateException(
                        "Impossible to add a certificate to the store.");
            }
            catch (NoSuchAlgorithmException ex)
            {
                throw new CertificateException(
                        "Impossible to add a certificate to the store.");
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                System.exit(-1);
            }
        }
    }

    public void checkServerTrusted(X509Certificate[] cer, String str)
            throws CertificateException
    {
        try
        {
            trustManager.checkServerTrusted(cer, str);
        }
        catch (CertificateException e)
        {
            if (e instanceof CertificateEncodingException
                    || e instanceof CertificateParsingException
                    || e instanceof CertificateNotYetValidException
                    || e instanceof CertificateExpiredException)
                throw e;
            try
            {
                tryTrustManagerUpdate(cer, str);
                trustManager.checkServerTrusted(cer, str);
            }
            catch (KeyStoreException ex)
            {
                throw new CertificateException(
                        "Unable to add a certificate to the store.");
            }
            catch (IOException ex)
            {
                throw new CertificateException(
                        "Unable to add a certificate to the store.");
            }
            catch (NoSuchAlgorithmException ex)
            {
                throw new CertificateException(
                        "Unable to add a certificate to the store.");
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                System.exit(-1);
            }
        }
    }

    public X509Certificate[] getAcceptedIssuers()
    {
        return trustManager.getAcceptedIssuers();
    }

    public SSLContext getContext()
    {
        return context;
    }
}
