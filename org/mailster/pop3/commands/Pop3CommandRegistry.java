package org.mailster.pop3.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mailster.pop3.commands.auth.AuthCommand;
import org.mailster.pop3.commands.auth.AuthCramMD5Command;
import org.mailster.pop3.commands.auth.AuthCramSHA1Command;
import org.mailster.pop3.commands.auth.AuthDigestMD5Command;
import org.mailster.pop3.commands.auth.AuthLoginCommand;
import org.mailster.pop3.commands.auth.AuthPlainCommand;
import org.mailster.util.md5.MD5;

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
 * Pop3CommandRegistry.java - The POP3 command registry maps a command line to
 * the command class that actually handles the command.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class Pop3CommandRegistry
{
    static
    {
        // Disable native library loading
        MD5.initNativeLibrary(true);
    }
    
    private final static Map<String, Pop3Command> commands = new HashMap<String, Pop3Command>();
    private final static Object[][] COMMANDS = new Object[][] {
            { "QUIT", QuitCommand.class, Boolean.FALSE }, 
            { "STAT", StatCommand.class, Boolean.FALSE },
            { "APOP", ApopCommand.class, Boolean.FALSE },
            { "USER", UserCommand.class, Boolean.TRUE },
            { "PASS", PassCommand.class, Boolean.FALSE },   
            { "LIST", ListCommand.class, Boolean.FALSE },
            { "UIDL", UidlCommand.class, Boolean.TRUE },
            { "TOP", TopCommand.class, Boolean.TRUE },
            { "RETR", RetrCommand.class, Boolean.FALSE },
            { "DELE", DeleCommand.class, Boolean.FALSE },
            { "NOOP", NoopCommand.class, Boolean.FALSE },
            { "RSET", RsetCommand.class, Boolean.FALSE },
            { "CAPA", CapaCommand.class, Boolean.FALSE }, 
            { "IMPLEMENTATION", ImplCommand.class, Boolean.TRUE },
            { "STLS", StartTLSCommand.class, Boolean.TRUE }, 
            { "AUTH", AuthCommand.class, Boolean.TRUE }
    };

    private static void load() throws Exception
    {
        for (int i = 0; i < COMMANDS.length; i++)
        {
            String name = COMMANDS[i][0].toString();

            Class<?> type = (Class<?>) COMMANDS[i][1];
            Pop3Command command = (Pop3Command) type.newInstance();
            commands.put(name, command);
        }
        
        AuthCommand.register("DIGEST-MD5", new AuthDigestMD5Command());
        AuthCommand.register("CRAM-MD5", new AuthCramMD5Command());
        AuthCommand.register("CRAM-SHA1", new AuthCramSHA1Command());
        AuthCommand.register("PLAIN", new AuthPlainCommand());
        AuthCommand.register("LOGIN", new AuthLoginCommand());
    }

    protected static List<String> listCapabilities()
    {
        List<String> capabilities = new ArrayList<String>(COMMANDS.length);
        for (int i = 0; i < COMMANDS.length; i++)
        {
            if (((Boolean)COMMANDS[i][2]).booleanValue())
            {
            	Pop3Command cmd = getCommand((String)COMMANDS[i][0]);
            	if (cmd instanceof CapabilitiesInterface)
            	{
            		String capa = ((CapabilitiesInterface)cmd).getCapability();
            		if (capa != null)
            			capabilities.add(capa);
            	}
            	else
            		capabilities.add(COMMANDS[i][0].toString());
            }
        }
        
        return capabilities;
    }

    public static Pop3Command getCommand(String name)
    {
    	synchronized (commands)
    	{
	        if (commands.size() == 0)
	        {
	            try
	            {
	                load();
	            }
	            catch (Exception e)
	            {
	                throw new RuntimeException(e);
	            }
	        }
    	}
        return (Pop3Command) commands.get(name);
    }
}