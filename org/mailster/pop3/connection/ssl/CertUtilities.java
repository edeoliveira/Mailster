package org.mailster.pop3.connection.ssl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 * 
 * This software is released under the LGPL which is available at
 * http://www.gnu.org/copyleft/lesser.html This file has been used and modified.
 * 
 * Original code can be found on :
 * http://svn.apache.org/viewvc/jakarta/commons/proper/httpclient/
 * 		trunk/src/contrib/org/apache/commons/httpclient/contrib/ssl/
 * 		StrictSSLProtocolSocketFactory.java?view=markup
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 */
public class CertUtilities
{
	/** 
	 * Log object for this class. 
	 */
    private static final Logger LOG = LoggerFactory.getLogger(CertUtilities.class);
    
    /**
     * Returns the X.500 distinguished name. It internally uses toString()
     * instead of the getName() method. 
     * 
     * For example, getName() gives me this:
     * 1.2.840.113549.1.9.1=#16166a756c6975736461766965734063756362632e636f6d 
     * whereas toString() gives me this: 
     * EMAILADDRESS=juliusdavies@cucbc.com
     * 
     * @param cert  a X.500 certificate.
     * @return the value of the Subject DN.
     */
    public static String getDN(X509Certificate cert)
    {
        return cert.getSubjectDN().toString();
    }
    
    /**
     * @see CertUtilities#getDN(X509Certificate cert)
     */
    public static String getDN(java.security.cert.X509Certificate cert)
    {
        return cert.getSubjectDN().toString();
    }
    
    /**
     * @see CertUtilities#getCN(String dn)
     */    
    public static String getCN(X509Certificate cert)
    {
        return getCN(getDN(cert));
    }

    /**
     * Parses a X.500 distinguished name for the value of the 
     * "Common Name" field.
     * This is done a bit sloppy right now and should probably be done a bit
     * more according to <code>RFC 2253</code>.
     *
     * @param dn  a X.500 distinguished name.
     * @return the value of the "Common Name" field.
     */
    public static String getCN(String dn) 
    {
        int i = dn.indexOf("CN=");
        if (i == -1)
            return null;

        //get the remaining DN without CN=
        dn = dn.substring(i + 3);  

        char[] dncs = dn.toCharArray();
        for (i = 0; i < dncs.length; i++) 
        {
            if (dncs[i] == ','  && i > 0 && dncs[i - 1] != '\\')
                break;
        }
        
        return dn.substring(0, i);
    }
    
    /**
     * Describe <code>verifyHostname</code> method here.
     *
     * @param socket a <code>SSLSocket</code> value
     * @exception SSLPeerUnverifiedException  If there are problems obtaining
     * the server certificates from the SSL session, or the server certificates 
     * does not have a "Common Name" or if it does not match.
     * 
     * @exception UnknownHostException  If we are not able to resolve
     * the SSL sessions returned server host name. 
     */
    public static void verifyHostname(SSLSocket socket) 
        throws SSLPeerUnverifiedException, UnknownHostException {

        SSLSession session = socket.getSession();
        String hostname = session.getPeerHost();
        
        try 
        {
            InetAddress.getByName(hostname);
        } 
        catch (UnknownHostException uhe) 
        {
            throw new UnknownHostException("Could not resolve SSL session server hostname: " + hostname);
        }
        
        X509Certificate[] certs = session.getPeerCertificateChain();
        if (certs == null || certs.length == 0) 
            throw new SSLPeerUnverifiedException("No server certificates found!");
        
        //get the servers DN in its string representation
        X509Certificate x509 = (X509Certificate) certs[0];
        String dn = getDN(x509);

        //might be useful to print out all certificates we receive from the
        //server, in case one has to debug a problem with the installed certs.
        if (LOG.isDebugEnabled()) 
        {
            LOG.debug("Server certificate chain:");
            for (int i = 0; i < certs.length; i++)
                LOG.debug("X509Certificate[" + i + "]=" + certs[i]);            
        }
        
        //get the common name from the first cert
        String cn = getCN(dn);
        if (cn == null)
            throw new SSLPeerUnverifiedException("Certificate doesn't contain CN: " + dn);
        
        // I'm okay with being case-insensitive when comparing the host we used
        // to establish the socket to the hostname in the certificate.
        // Don't trim the CN, though.
        cn = cn.toLowerCase();
        hostname = hostname.trim().toLowerCase();
        boolean doWildcard = false;
        
        if (cn.startsWith("*."))
        {
            // The CN better have at least two dots if it wants wildcard action,
            // but can't be [*.co.uk] or [*.co.jp] or [*.org.uk], etc...
            String withoutCountryCode = "";
            if (cn.length() >= 7 && cn.length() <= 9)
                withoutCountryCode = cn.substring(2, cn.length() - 2);

            doWildcard = cn.lastIndexOf('.') >= 0
                    && !"ac.".equals(withoutCountryCode)
                    && !"co.".equals(withoutCountryCode)
                    && !"com.".equals(withoutCountryCode)
                    && !"ed.".equals(withoutCountryCode)
                    && !"edu.".equals(withoutCountryCode)
                    && !"go.".equals(withoutCountryCode)
                    && !"gouv.".equals(withoutCountryCode)
                    && !"gov.".equals(withoutCountryCode)
                    && !"info.".equals(withoutCountryCode)
                    && !"lg.".equals(withoutCountryCode)
                    && !"ne.".equals(withoutCountryCode)
                    && !"net.".equals(withoutCountryCode)
                    && !"or.".equals(withoutCountryCode)
                    && !"org.".equals(withoutCountryCode);

            // The [*.co.uk] problem is an interesting one. Should we just
            // hope that CA's would never foolishly allow such a
            // certificate to happen?
        }

        boolean match;
        if (doWildcard)
            match = hostname.endsWith(cn.substring(1));
        else
            match = hostname.equals(cn);

        if (match) 
        {
            LOG.debug("Target hostname valid : {}", cn);
        } 
        else 
        {
            throw new SSLPeerUnverifiedException("HTTPS hostname invalid: <"
                    + hostname + "> != <" + cn + ">");
        }
    }    
}
