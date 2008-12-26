package org.mailster.crypto;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
 * DummySSLSocketFactory.java - A dummy SSL socket factory with a {@link TrustManager}
 * that always replies ok to whatever demand is done.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class DummySSLSocketFactory extends SSLSocketFactory
{
    public class DummyTrustManager implements X509TrustManager
    {
        public void checkClientTrusted(X509Certificate[] cert, String authType)
        {
        }

        public void checkServerTrusted(X509Certificate[] cert, String authType)
        {
        }

        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[0];
        }
    }

    private SSLSocketFactory factory;
    private SSLContext sslcontext;

    public DummySSLSocketFactory()
    {
        try
        {
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null,
                    new TrustManager[] { new DummyTrustManager() }, new SecureRandom());
            factory = (SSLSocketFactory) sslcontext.getSocketFactory();
        }
        catch (Exception ex)
        {
            // ignore
        }
    }

    public static SocketFactory getDefault()
    {
        return new DummySSLSocketFactory();
    }

    public Socket createSocket(Socket socket, String s, int i, boolean flag)
            throws IOException
    {
        return factory.createSocket(socket, s, i, flag);
    }

    public Socket createSocket(InetAddress inaddr, int i, InetAddress inaddr1,
            int j) throws IOException
    {
        return factory.createSocket(inaddr, i, inaddr1, j);
    }

    public Socket createSocket(InetAddress inaddr, int i) throws IOException
    {
        return factory.createSocket(inaddr, i);
    }

    public Socket createSocket(String s, int i, InetAddress inaddr, int j)
            throws IOException
    {
        return factory.createSocket(s, i, inaddr, j);
    }

    public Socket createSocket(String s, int i) throws IOException
    {
        return factory.createSocket(s, i);
    }

    public String[] getDefaultCipherSuites()
    {
        return factory.getDefaultCipherSuites();
    }

    public Socket createSocket() throws IOException
    {
        return factory.createSocket();
    }

    public String[] getSupportedCipherSuites()
    {
        return factory.getSupportedCipherSuites();
    }

    public SSLContext getSSLContext()
    {
        return sslcontext;
    }
}