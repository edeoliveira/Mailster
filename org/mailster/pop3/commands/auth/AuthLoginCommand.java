package org.mailster.pop3.commands.auth;

import java.io.IOException;

import org.bouncycastle.util.encoders.Base64;
import org.mailster.pop3.commands.Pop3CommandState;
import org.mailster.pop3.connection.AbstractPop3Connection;
import org.mailster.pop3.connection.AbstractPop3Handler;
import org.mailster.pop3.connection.Pop3State;
import org.mailster.server.Pop3Service;

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
 * AuthLoginCommand.java - The POP3 AUTH LOGIN command.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class AuthLoginCommand extends AuthAlgorithmCommand
{
	/**
	 * Second step of AUTH LOGIN negociation : ask for the password.
	 */
	private final static int ASK_PASSWORD_STATE = 2;
    
    public Pop3CommandState checkClientResponse(AbstractPop3Handler handler, 
																            AbstractPop3Connection conn, 
																            String encodedPassword) 
    	throws IOException
    {
    	String pwd = new String(Base64.decode(
    			encodedPassword.getBytes(Pop3Service.CHARSET_NAME)), Pop3Service.CHARSET_NAME);
    	
        Pop3State state = conn.getState();

        if (state.getUser().getPassword().equals(pwd))
        {
            state.setAuthenticated();
            boolean locked = state.getMailBox().tryAcquireLock(3, 100);
            if (locked)
                conn.println("+OK maildrop locked and ready");
            else
                conn.println("-ERR maildrop is already locked");
        }
        else
            conn.println("-ERR permission denied");
        
        return null;
    }
    
    public Pop3CommandState challengeClient(AbstractPop3Handler handler, 
								            AbstractPop3Connection conn, 
								            String cmd) 
    	throws Exception
    {
    	conn.println("+ VXNlcm5hbWU6"); // Base64 encoding of String 'Username:'
    	return new Pop3CommandState(this, ASK_PASSWORD_STATE);
    }
    
    public Pop3CommandState execute(AbstractPop3Handler handler, 
											            AbstractPop3Connection conn, 
											            String cmd, 
											            Pop3CommandState state)
	{
		try
		{
			if (state == null || state.isInitialState())
				return challengeClient(handler, conn, cmd);
			else
			{
				if (cmd.equals("*"))
				{
					conn.println("-ERR AUTH command aborted by client");
					return null;
				}
			
				if (state.getNextState() == ASK_PASSWORD_STATE)
				{
					String username = new String(Base64.decode(
							cmd.getBytes(Pop3Service.CHARSET_NAME)), Pop3Service.CHARSET_NAME);
					Pop3State pop3State = conn.getState();
					pop3State.setUser(pop3State.getUser(username));
					
					conn.println("+ UGFzc3dvcmQ6"); // Base64 encoding of String 'Password:'
					return new Pop3CommandState(this, CHECK_RESPONSE_TO_CHALLENGE_STATE);
				}
				else
				if (state.getNextState() == CHECK_RESPONSE_TO_CHALLENGE_STATE)
					checkClientResponse(handler, conn, cmd);
				else
					conn.println("-ERR bad internal state");
			}
		}
		catch (Exception ex)
		{
			conn.println("-ERR " + ex.getMessage());
		}
		return null;
	}    
}