package org.mailster.crypto;

import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Semaphore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

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
    static 
    {
        if (Security.getProvider("BC") == null)
            Security.addProvider(new BouncyCastleProvider());
    }
    
    public enum SSLProtocol 
    {
        SSL, TLS;

        public String toString() 
        {
        	if (SSL.equals(this))
        		return "SSLv3";
        	else
    		if (TLS.equals(this))
        		return "TLSv1";
    		else
    			throw new AssertionError("Unknown protocol : " + this);
        }
    }
    
    private TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    private KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");    
    private SSLContext context;
    private KeyStore trustedKS;
    private KeyStore temporaryTrustedKS;
    
    private static X509SecureSocketFactory _instance;
    
    private SSLProtocol selectedProtocol;
    private UICertificateTrustCallBackHandler trustCallBackHandler;
    
    /**
     * The default X509TrustManager returned by SunX509.  We'll delegate
     * decisions to it, and fall back to the logic in this class if the
     * default X509TrustManager doesn't trust it.
     */
    private X509TrustManager sunJSSEX509TrustManager;
    
    /**
     * Temporary trusted certificates are hold in this TrustManager.
     */
    private X509TrustManager tmpTrustManager;
    
    private X509SecureSocketFactory(String protocol) 
        throws Exception
    {
    	trustedKS = MailsterKeyStoreFactory.getInstance().getKeyStore();
        temporaryTrustedKS = MailsterKeyStoreFactory.loadKeyStore(
                "PKCS12", null, MailsterKeyStoreFactory.KEYSTORE_PASSWORD);
        
        kmf.init(trustedKS, MailsterKeyStoreFactory.KEYSTORE_PASSWORD);
        sunJSSEX509TrustManager = initTrustManager(trustedKS);
        tmpTrustManager = initTrustManager(temporaryTrustedKS);
        
        context = SSLContext.getInstance(protocol);
        context.init(kmf.getKeyManagers(), new TrustManager[] {this}, null);
    }
    
    public static synchronized X509SecureSocketFactory getInstance(SSLProtocol protocol, 
    		UICertificateTrustCallBackHandler handler) 
    	throws Exception
    {
    	if (_instance == null)
        {
    	    if (protocol == null)
                _instance = new X509SecureSocketFactory(SSLProtocol.SSL.toString());
            else
                _instance = new X509SecureSocketFactory(protocol.toString());
    	    
    	    _instance.selectedProtocol = protocol;
    	    _instance.trustCallBackHandler = handler;
        }
    		
    	return _instance;
    }
    
    public static X509SecureSocketFactory getInstance() 
    {
    	return _instance;
    }    
    
    public static synchronized void reload()
		throws Exception
    {
    	if (_instance.selectedProtocol == null)
            _instance = new X509SecureSocketFactory(SSLProtocol.SSL.toString());
        else
            _instance = new X509SecureSocketFactory(_instance.selectedProtocol.toString());
    }
    
    private X509TrustManager initTrustManager(KeyStore ks) 
    	throws KeyStoreException 
    {
        tmf.init(ks);    	
        TrustManager[] tms = tmf.getTrustManagers();

        // Iterate over the returned trust managers looking for an instance 
        // of X509TrustManager.  If found, use that as our "default" trust manager.
        for (int i = 0; i < tms.length; i++) 
        {
            if (tms[i] instanceof X509TrustManager) 
                return (X509TrustManager) tms[i];
        }
        
        return null;
    }

    private String getCertificateUniqueID(X509Certificate x509)
    {
    	return CertificateUtilities.getCN(x509.getSubjectDN().getName())
               +"_"
               +x509.hashCode();
    }
    
    private X509TrustManager updateAndCheckTrustManager(KeyStore ks,
                                                        X509Certificate[] chain, 
                                                        OutputStream out,
                                                        char[] pwd,
                                                        String authType, 
                                                        boolean isServerCheck) 
        throws Exception
    {
        synchronized(ks)
        {
            ks.setCertificateEntry(getCertificateUniqueID(chain[0]), chain[0]);
            if (out != null)
                ks.store(out, pwd);
            
            X509TrustManager tm = initTrustManager(ks);
            
            if (isServerCheck)
                tm.checkServerTrusted(chain, authType);
            else
                tm.checkClientTrusted(chain, authType);
        
            return tm;
        }
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException
    {
    	try
        {
        	sunJSSEX509TrustManager.checkClientTrusted(chain, authType);
        }
        catch (CertificateException e) 
        {
            try
            {
                tmpTrustManager.checkClientTrusted(chain, authType);
            }
            catch (CertificateException cex) 
            {
                checkUserDecision(chain, authType, false, e);
            }
        }
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException
    {
    	try
        {
        	sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
        }
        catch (CertificateException e) 
        {
            try
            {
                tmpTrustManager.checkServerTrusted(chain, authType);
            }
            catch (CertificateException cex) 
            {
                checkUserDecision(chain, authType, true, e);
            }            
        }
    }

    public void checkUserDecision(X509Certificate[] chain, 
                                    String authType, 
                                    boolean isServerCheck, 
                                    CertificateException e)
        throws CertificateException
    {
        Semaphore sem = new Semaphore(1);
        trustCallBackHandler.setCallBackParameters(sem, chain);
        trustCallBackHandler.run();
        
        try
        {
            sem.acquire();

            if (trustCallBackHandler.isCancelled())
                throw e;
            else
            {
                switch (trustCallBackHandler.getReturnCode())
                {
                    case UICertificateTrustCallBackHandler.REJECT_CERTIFICATE_CHAIN :
                        throw e;
                    case UICertificateTrustCallBackHandler.TEMPORARY_ACCEPT_CERTIFICATE_CHAIN :
                        tmpTrustManager = updateAndCheckTrustManager(temporaryTrustedKS, chain, null, 
                            MailsterKeyStoreFactory.KEYSTORE_PASSWORD, authType, isServerCheck);
                        break;
                    case UICertificateTrustCallBackHandler.ACCEPT_CERTIFICATE_CHAIN :
                        OutputStream os = MailsterKeyStoreFactory.getInstance().getKeyStoreOutputStream();
                        sunJSSEX509TrustManager = updateAndCheckTrustManager(trustedKS, chain, os,
                                MailsterKeyStoreFactory.KEYSTORE_PASSWORD, authType, isServerCheck);
                        os.close();
                }
            }            
        }
        catch (Exception ex) 
        {
            throw e;
        }
    }
    
    public X509Certificate[] getAcceptedIssuers()
    {
        return new X509Certificate[0];
    }

    public SSLContext getContext()
    {
        return context;
    }

	public UICertificateTrustCallBackHandler getTrustCallBackHandler() 
	{
		return trustCallBackHandler;
	}

	public void setTrustCallBackHandler(
			UICertificateTrustCallBackHandler trustCallBackHandler) 
	{
		this.trustCallBackHandler = trustCallBackHandler;
	}
}
