package org.mailster.pop3.commands.auth;

import java.util.HashMap;
import java.util.TreeSet;

import org.mailster.pop3.commands.CapabilitiesInterface;
import org.mailster.pop3.commands.MultiStatePop3Command;
import org.mailster.pop3.commands.Pop3CommandState;
import org.mailster.pop3.connection.AbstractPop3Connection;
import org.mailster.pop3.connection.AbstractPop3Handler;
import org.mailster.pop3.connection.Pop3State;
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
 * AuthCommand.java - The POP3 AUTH command (see RFC 1734).
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class AuthCommand extends MultiStatePop3Command
	implements CapabilitiesInterface
{
	private static HashMap<String, AuthAlgorithmCommand> algorithms = 
		new HashMap<String, AuthAlgorithmCommand>();
    
	public static void register(String algorithm, AuthAlgorithmCommand cmd)
	{
		algorithms.put(algorithm, cmd);
	}
	
	public static void unregister(String algorithm)
	{
		algorithms.remove(algorithm);
	}
	
    public boolean isValidForState(Pop3State state)
    {
        return !state.isAuthenticated();
    }
    
    public Pop3CommandState execute(AbstractPop3Handler handler, 
																            AbstractPop3Connection conn, 
																            String cmd, 
																            Pop3CommandState state)
    {
        String[] cmdLine = StringUtilities.split(cmd);
        
        if (cmdLine.length < 2)
        {
    		conn.println("-ERR Required syntax: AUTH <authentication type>");
    		return null;
        }
        
        AuthAlgorithmCommand pop3Cmd = algorithms.get(cmdLine[1]);
        if (pop3Cmd == null)
        {
            conn.println("-ERR Unimplemented authentication type");
            return null;
        }
        else
        if (handler.isSecureAuthRequired(conn) && 
        		!pop3Cmd.isSecuredAuthenticationMethod())
        {
        	conn.println("-ERR Secure authentication type required");
        	return null;
        }
        
        return pop3Cmd.execute(handler, conn, cmd, state);
    }
    
    public String getCapability()
    {
    	if (algorithms.isEmpty())
    		return null;
    	
    	StringBuilder sb = new StringBuilder("SASL");
    	TreeSet<String> sortedKeys = new TreeSet<String>();
    	sortedKeys.addAll(algorithms.keySet());
    	for (String alg : sortedKeys)
    		sb.append(' ').append(alg);
    	
        return sb.toString();
    }
}