package org.mailster.pop3.commands.auth;

import java.io.UnsupportedEncodingException;

import org.bouncycastle.util.encoders.Base64;
import org.mailster.pop3.commands.Pop3CommandState;
import org.mailster.pop3.connection.AbstractPop3Connection;
import org.mailster.pop3.connection.AbstractPop3Handler;
import org.mailster.pop3.connection.Pop3State;
import org.mailster.server.MailsterConstants;
import org.mailster.util.StringUtilities;

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
 * AuthCramCommand.java - An abstract class supporting the POP3 AUTH CRAM command 
 * (see RFCs 1734 & 2195). Implementations use different hash functions.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public abstract class AuthCramCommand extends AuthAlgorithmCommand
{
    public abstract String hmacHash(byte[] text, byte[] secret) throws Exception;
    
    public Pop3CommandState challengeClient(AbstractPop3Handler handler, 
								            AbstractPop3Connection conn, 
								            String cmd) 
    	throws UnsupportedEncodingException
    {
        conn.println("+ "+
        		new String(Base64.encode(conn.getState().getGeneratedAPOPBanner().
        							getBytes(MailsterConstants.DEFAULT_CHARSET_NAME)), 
        							MailsterConstants.DEFAULT_CHARSET_NAME));
        
        return new Pop3CommandState(this, CHECK_RESPONSE_TO_CHALLENGE_STATE);
    }
    
    public Pop3CommandState checkClientResponse(AbstractPop3Handler handler, 
									            AbstractPop3Connection conn, 
									            String cmd) 
    	throws Exception
    {
    	String[] cmdLine = StringUtilities.split(new String(Base64.decode(cmd), 
    			MailsterConstants.DEFAULT_CHARSET_NAME));
        String username = cmdLine[0];
        String hmac = cmdLine[1];
        Pop3State state = conn.getState();
        state.setUser(state.getUser(username));

        if (hmacHash(state.getGeneratedAPOPBanner().getBytes(MailsterConstants.DEFAULT_CHARSET_NAME), 
        		state.getUser().getPassword().getBytes(MailsterConstants.DEFAULT_CHARSET_NAME)).equals(hmac))
        	tryLockingMailbox(conn);
        else
            conn.println("-ERR permission denied");
        
        return null;
    }
    
	public boolean isSecuredAuthenticationMethod()
	{
		return true;
	}
}