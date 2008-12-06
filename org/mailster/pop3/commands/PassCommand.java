package org.mailster.pop3.commands;

import org.mailster.pop3.connection.AbstractPop3Connection;
import org.mailster.pop3.connection.AbstractPop3Handler;
import org.mailster.pop3.connection.Pop3State;
import org.mailster.pop3.mailbox.Pop3User;
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
 * See&nbsp; <a href="http://mailster.sourceforge.net" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * PassCommand.java - The POP3 PASS command (see RFC 1939).
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class PassCommand extends Pop3Command
{
    public boolean isValidForState(Pop3State state)
    {
        return !state.isAuthenticated();
    }

    public void execute(AbstractPop3Handler handler, 
                        AbstractPop3Connection conn, 
                        String cmd)
    {
        if (handler.isSecureAuthRequired(conn))
        {
            conn.println("-ERR USER/PASS method not authorized. Please use a secure authentication instead");
            return;
        }
        
        Pop3State state = conn.getState();
        Pop3User user = state.getUser();
        if (user == null)
        {
            conn.println("-ERR USER required");
            return;
        }

        String[] args = StringUtilities.split(cmd);
        if (args.length < 2)
        {
            conn.println("-ERR Required syntax: PASS <username>");
            return;
        }

        try
        {
            if (state.authenticate(args[1]))
            	tryLockingMailbox(conn);
            else
                conn.println("-ERR Authentication failed");
        }
        catch (Exception e)
        {
            conn.println("-ERR Authentication failed: " + e.getMessage());
        }
    }
}