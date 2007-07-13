package org.mailster.pop3.connection;

import java.io.IOException;

import org.apache.mina.common.IoSession;
import org.apache.mina.filter.SSLFilter;
import org.mailster.pop3.connection.ssl.X509SecureSocketFactory;
import org.mailster.pop3.mailbox.UserManager;
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
 * MinaPop3Connection.java - Provides a MINA implementation of AbstractPop3Connection.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class MinaPop3Connection implements AbstractPop3Connection
{
    private final static String lineSeparator = "\r\n";
    private final static Logger log = LoggerFactory.getLogger(MinaPop3Connection.class);

    private static SSLFilter sslFilter;
    
    private IoSession session;
    private Pop3State state;

    static
    {
    	try
        {
        	X509SecureSocketFactory ssf = X509SecureSocketFactory.getInstance();
            sslFilter = new SSLFilter(ssf.getContext());
        }
        catch (Exception e)
        {                
            e.printStackTrace();
        }
    }
    
    public MinaPop3Connection(IoSession session, UserManager userManager)
    {
        this.session = session;
        this.state = new Pop3State(userManager);
    }

    public IoSession getSession()
    {
        return session;
    }

    public Pop3State getState()
    {
        return state;
    }

    public void println(String line)
    {
        if (line == null)
            return;
        
        StringBuilder sb = new StringBuilder(line.length()
                + lineSeparator.length());
        sb.append(line);
        sb.append(lineSeparator);
        log.info("S: " + line);
        session.write(sb.toString());
    }

    public void startTLS(String response) throws IOException
    {
        // Insert SSLFilter to get ready for handshaking
        session.getFilterChain().addFirst("SSLfilter", sslFilter);

        // Disable encryption temporarilly.
        // This attribute will be removed by SSLFilter
        // inside the Session.write() call below.
        session.setAttribute(SSLFilter.DISABLE_ENCRYPTION_ONCE, Boolean.TRUE);

        // Write StartTLSResponse which won't be encrypted.
        println(response);
        
        // Now DISABLE_ENCRYPTION_ONCE attribute is cleared.
        assert session.getAttribute(SSLFilter.DISABLE_ENCRYPTION_ONCE) == null;
    }

    public boolean isTLSConnection()
    {
        return sslFilter.isSSLStarted(session);
    }
}
