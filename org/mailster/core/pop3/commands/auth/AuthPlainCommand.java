package org.mailster.core.pop3.commands.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.util.encoders.Base64;
import org.mailster.core.pop3.commands.Pop3CommandState;
import org.mailster.core.pop3.connection.AbstractPop3Connection;
import org.mailster.core.pop3.connection.AbstractPop3Handler;
import org.mailster.core.pop3.connection.Pop3State;
import org.mailster.core.smtp.MailsterConstants;
import org.mailster.util.StringUtilities;

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
 * AuthPlainCommand.java - The POP3 AUTH PLAIN command (see RFC 2595).
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.7 $, $Date: 2009/02/03 00:56:00 $
 */
public class AuthPlainCommand extends AuthAlgorithmCommand
{
    private String[] parse(String encodedString) 
    	throws IOException
    {
    	byte[] decoded = Base64.decode(encodedString.getBytes(MailsterConstants.DEFAULT_CHARSET_NAME));
    	List<String> l = new ArrayList<String>();

    	int pos=0;
    	for (int i=0,max=decoded.length;i<max;i++)
    	{
    		if (decoded[i] == 0)
    		{
    			if (i != pos)
    				l.add(new String(decoded,pos,i-pos));
    			
    			pos = i+1;
    		}
    	}
    	l.add(new String(decoded,pos,decoded.length-pos));
    	return l.toArray(new String[0]);
    }
    
    public Pop3CommandState checkClientResponse(AbstractPop3Handler handler, 
									            AbstractPop3Connection conn, 
									            String authString) 
    	throws IOException
    {
    	String[] args = parse(authString);
    	String username = args.length == 3 ? args[1] : args[0];
    	String pwd = args.length == 3 ? args[2] : args[1];
    	
        Pop3State state = conn.getState();
        state.setUser(state.getUser(username));

        if (state.getUser().getPassword().equals(pwd))
        	tryLockingMailbox(conn);
        else
            conn.println("-ERR permission denied");
        
        return null;
    }
    
    public Pop3CommandState challengeClient(AbstractPop3Handler handler, 
								            AbstractPop3Connection conn, 
								            String cmd) 
    	throws Exception
    {
    	String[] cmdLine = StringUtilities.split(cmd);
    	if (cmdLine.length < 3)
        {
        	conn.println("+");
            return new Pop3CommandState(this, CHECK_RESPONSE_TO_CHALLENGE_STATE);
        }
    	else
    		return checkClientResponse(handler, conn, cmdLine[2]);
    }
}