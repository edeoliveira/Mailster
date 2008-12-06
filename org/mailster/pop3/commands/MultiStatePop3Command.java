package org.mailster.pop3.commands;

import org.mailster.pop3.connection.AbstractPop3Connection;
import org.mailster.pop3.connection.AbstractPop3Handler;


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
 * MultiStatePop3Command.java - Abstract class that defines a POP3 command that 
 * has multiple states.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public abstract class MultiStatePop3Command extends Pop3Command
{
    /**
     * Executes the command. If the state returned is different from <code>
     * Pop3CommandState.COMPLETED_STATE</code> or <code>
     * Pop3CommandState.ERROR_STATE</code>, it means that execution of the command 
     * needs some additional request/response steps to complete its execution.
     * 
     * @param handler a pop3 handler
     * @param conn a pop3 connection
     * @param cmd the command line sent by the client
     * @param state the state of the command execution, if null it's considered to
     * be in the initial state.
     * 
     * @return a <code>Pop3CommandState</code> as the next state of the command
     */
    public abstract Pop3CommandState execute(AbstractPop3Handler handler, 
    										 AbstractPop3Connection conn,
    										 String cmd, 
    										 Pop3CommandState state);
    
    public void execute(AbstractPop3Handler handler, 
    					AbstractPop3Connection conn,
    					String cmd)
    {
        execute(handler, conn, cmd, null);
    }    
}
