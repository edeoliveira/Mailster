package org.mailster.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.security.auth.x500.X500PrivateCredential;

import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.prefs.store.MailsterPrefStore;
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
 * MailsterKeyStoreFactory.java - The factory that generates the certificates
 * used in Mailster.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class MailsterKeyStoreFactory 
{
	/** 
	 * Log object for this class. 
	 */
    private static final Logger LOG = LoggerFactory.getLogger(MailsterKeyStoreFactory.class);

	private final static String KEYSTORE_FILENAME 			= "Mailster.p12";
	private final static String SSL_CERT_FILENAME 				= "ssl_server.crt";
	private final static String CLI_KEYSTORE_FILENAME 	= "clients.p12";

	private final static String KEYSTORE_FULL_PATH 			= getFullPath(KEYSTORE_FILENAME);
	private final static String SSL_CERT_FULL_PATH 			= getFullPath(SSL_CERT_FILENAME);
	private final static String CLI_KEYSTORE_FULL_PATH 	= getFullPath(CLI_KEYSTORE_FILENAME);
	
    private final static String DN_ORGANISATION                = "O=Mailster.org";
    private final static String DN_ORGANISATION_UNIT     = "OU=http://mailster.sourceforge.net";
    private final static String DN_COUNTRY                      		= "C=FR";
    private final static String DN_ROOT                         			= DN_ORGANISATION+", "+
												                                                                  DN_ORGANISATION_UNIT+", "+
												                                                                  DN_COUNTRY;
	
    private static final String ROOT_CA_ALIAS 					= "root";
    private static final String INTERMEDIATE_CA_ALIAS 	= "intermediate_CA";
    private static final String MAILSTER_SSL_ALIAS 			= "ssl_cert";
    private static final String DUMMY_SSL_CLIENT_ALIAS	= "ssl_dummy_client_cert";
    
    //TOSEE
    public static final String TED_CERT_ALIAS                   	= "ted_cert";
	
	protected final static char[] KEYSTORE_PASSWORD 	= new char[] {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
	
	private static MailsterKeyStoreFactory _instance;
	private KeyStore store;
	private PKIXParameters params;
	
	private Set<TrustAnchor> sessionAnchors = new HashSet<TrustAnchor>();
	
	static 
	{
		if (Security.getProvider("BC") == null)
            Security.addProvider(new BouncyCastleProvider());		
	}

	private MailsterKeyStoreFactory()
	{
		loadDefaultKeyStore();
	}
	
	public synchronized static MailsterKeyStoreFactory getInstance()
	{
		if (_instance == null)
			_instance = new MailsterKeyStoreFactory();
		
		return _instance;
	}
	
	private static String getFullPath(String fileName)
	{
		return StreamWriterUtilities.USER_DIR+"/"+fileName;
	}
	
	public static void regenerate()
		throws Exception
	{
		LOG.info("Regenerating Mailster certificates ...");
		
		// Delete previous files
		(new File(KEYSTORE_FULL_PATH)).delete();
		(new File(SSL_CERT_FULL_PATH)).delete();
		(new File(CLI_KEYSTORE_FULL_PATH)).delete();
		
		getInstance().loadDefaultKeyStore();
		X509SecureSocketFactory.reload();
	}
	
	public static void main(String args[])
	{
		try 
		{
			regenerate();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		System.exit(0);
	}
	
	private synchronized KeyStore loadDefaultKeyStore()
	{
		try
        {
			store = KeyStore.getInstance("PKCS12", "BC");
            InputStream fis = new FileInputStream(KEYSTORE_FULL_PATH);
            store.load(fis, KEYSTORE_PASSWORD);
            fis.close();
            LOG.debug("Key store "+KEYSTORE_FULL_PATH+" successfuly loaded");
        }
        catch (Exception ex)
        {
        	createDefaultKeyStore();
        }
        
        return store;
	}
	
	private int getCryptoStrength()
	{
		return ((MailsterPrefStore) ConfigurationManager.CONFIG_STORE).getInt(
				ConfigurationManager.CRYPTO_STRENGTH_KEY);
	}
	
	private KeyStore createDefaultKeyStore()
	{
		try
        {               
			LOG.info("Creating main key store ...");
			int keySize = getCryptoStrength();
            X500PrivateCredential rootCredential = CertificateUtilities.createRootCredential(keySize,
                    "CN=Mailster AUTHORITY, "+DN_ROOT, ROOT_CA_ALIAS);                    
            
            X500PrivateCredential interCredential = 
                CertificateUtilities.createIntermediateCredential(keySize,rootCredential.getPrivateKey(), 
                        rootCredential.getCertificate(), DN_ROOT, INTERMEDIATE_CA_ALIAS);
            
            final String DN = "EmailAddress=ted@home.com, CN=DE OLIVEIRA Edouard, " +DN_ROOT;
            
            X500PrivateCredential endCredential =
                CertificateUtilities.createEntityCredential(keySize, interCredential.getPrivateKey(), 
                        interCredential.getCertificate(), TED_CERT_ALIAS, DN, true); 
            
            // Generate store
            store.load(null, null);
            store.setCertificateEntry(rootCredential.getAlias(), rootCredential.getCertificate());
            store.setCertificateEntry(interCredential.getAlias(), interCredential.getCertificate());
            
            sessionAnchors.add(new TrustAnchor(rootCredential.getCertificate(), null));
            sessionAnchors.add(new TrustAnchor(interCredential.getCertificate(), null));
            importJDKTrustedCertificates(store);
            
            KeyStore clientCerts = loadKeyStore("PKCS12", null, KEYSTORE_PASSWORD);
            clientCerts.setKeyEntry(endCredential.getAlias(), 
                                    endCredential.getPrivateKey(), 
                                    KEYSTORE_PASSWORD, 
                                    new Certificate[] {endCredential.getCertificate(), 
                                                        interCredential.getCertificate(), 
                                                        rootCredential.getCertificate()});
            
            generateDummySSLClientCertificate(clientCerts);
            generateSSLServerCertificate(store, rootCredential);
            
            FileOutputStream fos = new FileOutputStream(CLI_KEYSTORE_FULL_PATH); 
            clientCerts.store(fos, KEYSTORE_PASSWORD);
            fos.close();
            
            fos = new FileOutputStream(KEYSTORE_FULL_PATH);
            store.store(fos, KEYSTORE_PASSWORD);
            fos.close();
            
            LOG.debug("Key store {} successfuly created", KEYSTORE_FULL_PATH);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        
		return store;
	}
	
	private void generateSSLServerCertificate(KeyStore store, X500PrivateCredential rootCredential) 
		throws Exception
	{
		LOG.info("Generating SSL server certificate ...");
        KeyPair pair = CertificateUtilities.generateRSAKeyPair(getCryptoStrength());
        String DN = "CN=localhost, " +DN_ROOT;        
        X509V3CertificateGenerator v3CertGen = CertificateUtilities.initCertificateGenerator(
                pair, rootCredential.getCertificate().getSubjectX500Principal().getName(), 
                DN, false, CertificateUtilities.DEFAULT_VALIDITY_PERIOD);
        
    	v3CertGen.addExtension(X509Extensions.BasicConstraints, 
                true, new BasicConstraints(false));
    	
        v3CertGen.addExtension(MiscObjectIdentifiers.netscapeCertType, 
                false, new NetscapeCertType(NetscapeCertType.sslServer | NetscapeCertType.sslClient));

        // Firefox 2 disallows these extensions in an SSL server cert. IE7 doesn't care.
        // v3CertGen.addExtension(X509Extensions.KeyUsage, 
        //        true, new KeyUsage(KeyUsage.dataEncipherment | KeyUsage.keyAgreement | KeyUsage.keyEncipherment));
        
        Vector<KeyPurposeId> typicalSSLServerExtendedKeyUsages = new Vector<KeyPurposeId>();
        
        typicalSSLServerExtendedKeyUsages.add(KeyPurposeId.id_kp_serverAuth);
        typicalSSLServerExtendedKeyUsages.add(KeyPurposeId.id_kp_clientAuth);
        
        v3CertGen.addExtension(
                X509Extensions.ExtendedKeyUsage,
                false,
                new ExtendedKeyUsage(typicalSSLServerExtendedKeyUsages));
        
        X509Certificate publicKeyCertificate = v3CertGen.generate(pair.getPrivate());
        store.setKeyEntry(MAILSTER_SSL_ALIAS, pair.getPrivate(),
        		KEYSTORE_PASSWORD, new Certificate[] { publicKeyCertificate, rootCredential.getCertificate() });
        CertificateUtilities.exportCertificate(publicKeyCertificate, SSL_CERT_FULL_PATH, false);
	}
    
    private void generateDummySSLClientCertificate(KeyStore ks) 
        throws Exception
    {
    	LOG.info("Generating a Dummy SSL client certificate ...");
        KeyPair pair = CertificateUtilities.generateRSAKeyPair(getCryptoStrength());
        String DN = "CN=SSL dummy client cert, O=Dummy org., C=FR";        
        X509V3CertificateGenerator v3CertGen = CertificateUtilities.initCertificateGenerator(
                pair, DN, DN, true, CertificateUtilities.DEFAULT_VALIDITY_PERIOD);
        
        v3CertGen.addExtension(X509Extensions.BasicConstraints, 
                true, new BasicConstraints(false));
        
        v3CertGen.addExtension(MiscObjectIdentifiers.netscapeCertType, 
                false, new NetscapeCertType(NetscapeCertType.sslClient));
        
        v3CertGen.addExtension(
                X509Extensions.ExtendedKeyUsage,
                false,
                new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth));
        
        X509Certificate cert = v3CertGen.generate(pair.getPrivate());
        ks.setKeyEntry(DUMMY_SSL_CLIENT_ALIAS, pair.getPrivate(), KEYSTORE_PASSWORD, 
                new Certificate[] {cert});
    }    
	
	private void importJDKTrustedCertificates(KeyStore ks)
	{
		try        
        {
            KeyStore caCerts = KeyStore.getInstance("JKS");
            caCerts.load(
                    new FileInputStream(System.getProperty("java.home") + "/lib/security/cacerts"),
                    "changeit".toCharArray());

            Enumeration<String> e = caCerts.aliases();
            while (e.hasMoreElements())
            {
            	String alias = e.nextElement();
            	if (caCerts.isCertificateEntry(alias))
            	{
            		X509Certificate c = (X509Certificate) caCerts.getCertificate(alias);
            		ks.setCertificateEntry(alias, c);
            		sessionAnchors.add(new TrustAnchor(c, null));
            	}
            }
        }
        catch (Exception ex)
        {
            LOG.debug("JRE CA certificate file not found");
        }        
	}
	
	protected synchronized PKIXParameters getPKIXParameters(char[] password) 
		throws KeyStoreException, InvalidAlgorithmParameterException
	{
		if (params == null)
		{
			params = new PKIXParameters(store);
	        params.setRevocationEnabled(false);
		}
        
        return params;
	}
	
	public X509Certificate getRootCertificate() 
		throws KeyStoreException
	{
		if (store == null)
			throw new KeyStoreException("Store not loaded");
		
		return (X509Certificate) store.getCertificate(ROOT_CA_ALIAS);
	}
	
	public Certificate[] getCertificateChain(String alias)
		throws KeyStoreException	
	{
		if (store == null)
			throw new KeyStoreException("Store not loaded");
		
		return store.getCertificateChain(alias);
	}
	
	public KeyStore getKeyStore()
	{
		return store;
	}
	
	public InputStream getKeyStoreInputStream() 
		throws FileNotFoundException
	{
		return new FileInputStream(KEYSTORE_FULL_PATH);
	}
	
	public OutputStream getKeyStoreOutputStream() 
		throws FileNotFoundException
	{
		return new FileOutputStream(KEYSTORE_FULL_PATH);
	}
	
	public static KeyStore loadKeyStore(String storeType, String storePath, char pwd[]) 
	{
		try 
		{
			KeyStore store = KeyStore.getInstance(storeType, "BC");
			
			if (storePath == null)
				store.load(null, pwd);
			else
			{
				FileInputStream fis = new FileInputStream(storePath);
				store.load(fis, pwd);
				fis.close();
			}
			
			return store;
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			return null;
		}
	}
}
