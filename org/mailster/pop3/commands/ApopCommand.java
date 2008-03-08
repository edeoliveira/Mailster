package org.mailster.pop3.commands;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.mailster.pop3.connection.AbstractPop3Connection;
import org.mailster.pop3.connection.AbstractPop3Handler;
import org.mailster.pop3.connection.Pop3State;
import org.mailster.server.MailsterConstants;
import org.mailster.util.StringUtilities;
import org.mailster.util.md5.MD5;

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
 * ApopCommand.java - The POP3 APOP command (see RFC 1939).
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class ApopCommand extends Pop3Command
{
    private MD5 md5 = new MD5();
    
    public boolean isValidForState(Pop3State state)
    {
        return !state.isAuthenticated();
    }

    /**
     * Get the JVM's process ID
     * 
     * @return the pid
     */
    private static String getPID()
    {
        String pidString = ManagementFactory.getRuntimeMXBean().getName();
        return pidString.substring(0, pidString.indexOf("@"));
    }

    /**
     * Returns a unique timestamp with the form
     * <Process-ID.thread-ID.clock@hostname> which is used by the client for the
     * authentication.
     * 
     * @return the timestamp banner
     */
    public static String generateBannerTimestamp()
    {
        String hostName = null;

        try
        {
            hostName = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)
        {
            hostName = "localhost";
        }

        return "<" + getPID() + "." + Thread.currentThread().getId() + "."
                + System.currentTimeMillis() + "@" + hostName + ">";
    }

    public void execute(AbstractPop3Handler handler, 
                        AbstractPop3Connection conn, 
                        String cmd) 
    {
        try
        {
            if (!handler.isUsingAPOPAuthMethod(conn))
                conn.println("-ERR APOP not authorized");
            else
            {
                String[] cmdLine = StringUtilities.split(cmd);
                if (cmdLine.length < 3)
                {
                    conn.println("-ERR Required syntax: APOP <name> <digest>");
                    return;
                }

                String username = cmdLine[1];
                Pop3State state = conn.getState();
                state.setUser(state.getUser(username));

                byte[] uniqueKey = (state.getGeneratedAPOPBanner() + state
                        .getUser().getPassword()).getBytes(MailsterConstants.DEFAULT_CHARSET_NAME);

                String hash = null;
                
                synchronized(md5)
                {
                    md5.Init();
                    md5.Update(uniqueKey);
                    hash = md5.asHex();
                }
                
                if (hash.equals(cmdLine[2]))
                	tryLockingMailbox(conn);
                else
                    conn.println("-ERR permission denied");
            }
        }
        catch (Exception ex)
        {
            // Shouldn't append cause we automatically create the mailbox.
            // RFC 1939 states it is a security threat to respond -ERR
            // as it is giving potential attackers clues about which names are
            // valid
            conn.println("-ERR " + ex.getMessage());
        }
    }
}