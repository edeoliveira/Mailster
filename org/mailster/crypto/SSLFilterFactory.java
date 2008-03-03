package org.mailster.crypto;

import org.apache.mina.filter.SSLFilter;
import org.mailster.crypto.X509SecureSocketFactory.SSLProtocol;
import org.mailster.gui.crypto.SWTCertificateTrustCallBackHandler;
import org.subethamail.smtp.server.io.DummySSLSocketFactory;

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
 * SSLFilterFactory.java - The factory that generates a {@link SSLFilter}.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class SSLFilterFactory 
{
    public static SSLFilter createFilter(SSLProtocol protocol, boolean clientAuthNeeded)
    	throws RuntimeException
    {
    	SSLFilter sslFilter;
    	
    	try 
    	{
    		X509SecureSocketFactory ssf = X509SecureSocketFactory.getInstance(protocol, 
    				new SWTCertificateTrustCallBackHandler());
    		sslFilter = new SSLFilter(ssf.getContext());
		}
    	catch (Exception e) 
    	{			
			sslFilter = new SSLFilter((new DummySSLSocketFactory()).getSSLContext());
		}
    	
    	if (sslFilter == null)
    		throw new RuntimeException("SSLFilter creation failed");
    	
    	sslFilter.setNeedClientAuth(clientAuthNeeded);
    	
    	return sslFilter;
    }
}
