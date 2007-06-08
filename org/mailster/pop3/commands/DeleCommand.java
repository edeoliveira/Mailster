package org.mailster.pop3.commands;

import javax.mail.Flags;

import org.mailster.pop3.connection.AbstractPop3Connection;
import org.mailster.pop3.connection.AbstractPop3Handler;
import org.mailster.pop3.connection.Pop3State;
import org.mailster.pop3.mailbox.MailBox;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
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
 * DeleCommand.java - The POP3 DELE command (see RFC 1939).
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class DeleCommand implements Pop3Command
{
    public boolean isValidForState(Pop3State state)
    {
        return state.isAuthenticated();
    }

    public void execute(AbstractPop3Handler handler, AbstractPop3Connection conn, String cmd)
    {
        try
        {
            MailBox inbox = conn.getState().getMailBox();
            String[] cmdLine = StringUtilities.split(cmd);
            if (cmdLine.length < 2)
            {
                conn.println("-ERR Required syntax: DELE <id>");
                return;
            }

            StoredSmtpMessage msg = inbox.getMessage(new Long(cmdLine[1]));

            if (msg == null)
            {
                conn.println("-ERR no such message");
                return;
            }

            Flags flags = msg.getFlags();

            if (flags.contains(Flags.Flag.DELETED))
            {
                conn.println("-ERR message already deleted");
                return;
            }

            flags.add(Flags.Flag.DELETED);
            conn.println("+OK message scheduled for deletion");
        }
        catch (Exception e)
        {
            conn.println("-ERR " + e.getMessage());
        }
    }
}