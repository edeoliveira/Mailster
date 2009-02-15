package org.mailster.pop3.commands;

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
 * Pop3Command.java - Abstract class that defines what a POP3 command class has to 
 * implement to be registered in the command registry.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public abstract class Pop3Command
{
    /**
     * Checks if command is valid for the specified POP3 state.
     * 
     * @param state the POP3 state
     * 
     * @return true, if is valid for state
     */
    public abstract boolean isValidForState(Pop3State state);

    /**
     * Executes the command.
     * 
     * @param handler a pop3 handler
     * @param conn a pop3 connection
     * @param cmd the command line sent by the client
     */
    public abstract void execute(AbstractPop3Handler handler, AbstractPop3Connection conn,
            String cmd);
    
    /**
     * Try to lock a mailbox after a successfull login.
     * 
     * @param conn the connection
     */
    public void tryLockingMailbox(AbstractPop3Connection conn)
    {
    	conn.getState().setAuthenticated();
        boolean locked = conn.getState().getMailBox().tryAcquireLock(3, 300);
        if (locked)
            conn.println("+OK maildrop locked and ready");
        else
            conn.println("-ERR maildrop is already locked");
    }
}