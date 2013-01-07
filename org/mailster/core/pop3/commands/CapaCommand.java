package org.mailster.core.pop3.commands;

import org.mailster.core.pop3.connection.AbstractPop3Connection;
import org.mailster.core.pop3.connection.AbstractPop3Handler;
import org.mailster.core.pop3.connection.Pop3State;

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
 * CapaCommand.java - The POP3 CAPA command (see RFC 2449).
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.5 $, $Date: 2008/12/06 13:57:16 $
 */
public class CapaCommand extends Pop3Command
{
    public boolean isValidForState(Pop3State state)
    {
        return true;
    }

    public void execute(AbstractPop3Handler handler, 
                        AbstractPop3Connection conn, 
                        String cmd)
    {
        //RFC 2449
        conn.println("+OK Capability list follows");
        
        for (String c:Pop3CommandRegistry.listCapabilities())
            conn.println(c);
        
        conn.println(".");
    }
}