/*
 * Copyright (c) 2006 Wael Chatila / Icegreen Technologies. All Rights Reserved.
 * This software is released under the LGPL which is available at
 * http://www.gnu.org/copyleft/lesser.html This file has been used and modified.
 * Original file can be found on http://foedus.sourceforge.net
 */
package org.mailster.pop3.connection;

import org.mailster.pop3.mailbox.MailBox;
import org.mailster.pop3.mailbox.Pop3User;
import org.mailster.pop3.mailbox.UserManager;

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
 * Pop3State.java - Stores the state of a POP3 connection.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class Pop3State
{
    private UserManager _users;
    
    private Pop3User user;
    private MailBox inbox;
    private String generatedAPOPBanner;
    private boolean authenticated;

    public String getGeneratedAPOPBanner()
    {
        return generatedAPOPBanner;
    }

    public void setGeneratedAPOPBanner(String generatedAPOPBanner)
    {
        this.generatedAPOPBanner = generatedAPOPBanner;
    }

    public Pop3State(UserManager users)
    {
        _users = users;
    }

    public Pop3User getUser()
    {
        return user;
    }

    public Pop3User getUser(String arg)
    {
        return _users.getUser(arg);
    }

    public void setUser(Pop3User user)
    {
        this.user = user;
    }

    public boolean isAuthenticated()
    {
        return authenticated;
    }

    public boolean authenticate(String pass) throws IllegalStateException
    {
        if (user == null)
            throw new IllegalStateException("No user selected");

        boolean authenticated = user.authenticate(pass);
        if (authenticated)
            setAuthenticated();
        
        return authenticated;
    }
    
    public void setAuthenticated()
    {
        authenticated = true;
    }
    
    public MailBox getMailBox()
    {
        if (authenticated)
        {
            if (inbox == null)
                inbox = _users.getMailBoxManager().getMailBoxByUser(user);
            
            return inbox;
        }
        else
            return null;
    }
    
    public void reset()
    {
        user = null;
        inbox = null;
        authenticated = false;
    }
}