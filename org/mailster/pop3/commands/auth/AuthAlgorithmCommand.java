package org.mailster.pop3.commands.auth;

import org.mailster.pop3.commands.MultiStatePop3Command;
import org.mailster.pop3.commands.Pop3CommandState;
import org.mailster.pop3.connection.AbstractPop3Connection;
import org.mailster.pop3.connection.AbstractPop3Handler;
import org.mailster.pop3.connection.Pop3State;

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
 * AuthAlgorithmCommand.java - An abstract class for the different AUTH algorithms 
 * implementations.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public abstract class AuthAlgorithmCommand extends MultiStatePop3Command 
{
    /**
     * Checking client response to challenge state.
     */
    public final static int CHECK_RESPONSE_TO_CHALLENGE_STATE = 1;
    	
    public boolean isValidForState(Pop3State state)
    {
        return !state.isAuthenticated();
    }

    public abstract Pop3CommandState checkClientResponse(AbstractPop3Handler handler, 
								            												AbstractPop3Connection conn, 
								        													String cmd) 
		throws Exception;
    
    public abstract Pop3CommandState challengeClient(AbstractPop3Handler handler, 
																		            AbstractPop3Connection conn, 
																		            String cmd) 
		throws Exception;
	
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
			
				if (state.getNextState() == CHECK_RESPONSE_TO_CHALLENGE_STATE)
					return checkClientResponse(handler, conn, cmd);
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
	
	public boolean isSecuredAuthenticationMethod()
	{
		return false;
	}
}
