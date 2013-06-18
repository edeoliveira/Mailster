package org.mailster.core.pop3.commands;

import javax.mail.Flags;

import org.mailster.core.pop3.connection.AbstractPop3Connection;
import org.mailster.core.pop3.connection.AbstractPop3Handler;
import org.mailster.core.pop3.connection.Pop3State;
import org.mailster.core.pop3.mailbox.MailBox;
import org.mailster.core.pop3.mailbox.StoredSmtpMessage;
import org.mailster.util.StreamUtilities;
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
 * TopCommand.java - The POP3 TOP command (see RFC 1939).
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.7 $, $Date: 2008/12/06 13:57:16 $
 */
public class TopCommand extends Pop3Command
{
    public boolean isValidForState(Pop3State state)
    {
        return state.isAuthenticated();
    }

    public void execute(AbstractPop3Handler handler, 
                        AbstractPop3Connection conn, 
                        String cmd)
    {
        try
        {
            MailBox inbox = conn.getState().getMailBox();
            String[] cmdLine = StringUtilities.split(cmd);
            if (cmdLine.length < 3)
            {
                conn.println("-ERR Required syntax: TOP <id> <lineCount>");
                return;
            }

            StoredSmtpMessage msg = inbox.getMessage(new Long(cmdLine[1]));

            if (msg == null)
            {
                conn.println("-ERR no such message");
                return;
            }

            if (msg.getFlags().contains(Flags.Flag.DELETED))
            {
                conn.println("-ERR message marked as deleted");
                return;
            }

            int numLines = Integer.parseInt(cmdLine[2]);
            if (numLines < 0)
            {
                conn.println("-ERR lineCount should not be negative");
                return;
            }

            conn.println("+OK");
            StreamUtilities.write(msg.getMessage().toString(), conn,
                    numLines);
            conn.println(".");
        }
        catch (Exception e)
        {
            conn.println("-ERR " + e.getMessage());
        }
    }
}