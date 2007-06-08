package org.mailster.pop3.mailbox;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
 * UserManager.java - Manages the POP3 users.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class UserManager
{
    public final static String DEFAULT_PASSWORD = "pwd";
    
    private Map<String, Pop3User> _users = Collections
            .synchronizedMap(new HashMap<String, Pop3User>());
    
    private MailBoxManager mailBoxManager = new MailBoxManager();

    public UserManager()
    {
    }

    public Pop3User getUser(String login)
    {
        return createUserIfNecessary(login);
    }
    
    public Pop3User getUserByEmail(String email)
    {
        // Consider email as login
        return getUser(email);
    }

    private Pop3User createUserIfNecessary(String login)
    {
        Pop3User user = (Pop3User) _users.get(login);
        if (user == null)
        {
            user = new Pop3User(login);
            _users.put(login, user);            
        }
        
        return user;
    }

    public void clear()
    {
        _users.clear();
    }

    public MailBoxManager getMailBoxManager()
    {
        return mailBoxManager;
    }
}