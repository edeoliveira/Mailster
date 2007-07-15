package org.mailster.pop3.commands;

import org.mailster.pop3.connection.AbstractPop3Connection;
import org.mailster.pop3.connection.AbstractPop3Handler;
import org.mailster.pop3.connection.Pop3State;
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
 * StartTLSCommand.java - The POP3 STARTTLS command (see RFC 2595).
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class StartTLSCommand implements Pop3Command
{
    public boolean isValidForState(Pop3State state)
    {
        return !state.isAuthenticated();
    }

    public void execute(AbstractPop3Handler handler, AbstractPop3Connection conn,
            String cmd)
    {
        try
        {
            String[] cmdLine = StringUtilities.split(cmd);
            if (cmdLine.length > 1)
            {
                conn.println("-ERR Required syntax: STLS");
                return;
            }

            if (conn.isTLSConnection())
            {
                conn.println("-ERR Command not permitted when TLS is already active");
                return;
            }

            conn.startTLS("+OK Begin TLS negotiation");
            conn.getState().reset(); // clean state
        }
        catch (Exception ex)
        {
            conn.println("-ERR " + ex.getMessage());
        }
    }
}