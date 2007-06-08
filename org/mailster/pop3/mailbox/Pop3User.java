package org.mailster.pop3.mailbox;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
 * Pop3User.java - A POP3 user.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class Pop3User
{
    private String email;
    private String password;

    protected Pop3User(String email)
    {
        this(email, UserManager.DEFAULT_PASSWORD);
    }

    protected Pop3User(String email, String password)
    {
        this.email = email;
        this.password = password;
    }

    public String getEmail()
    {
        return email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean authenticate(String pass)
    {
        return password.equals(pass);
    }

    public String getQualifiedMailboxID()
    {
        try
        {
            return email+".Mailster@"+InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)
        {
            return email+".Mailster@localhost";
        }
    }

    public int hashCode()
    {
        return email.hashCode();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Pop3User) || (null == o))
            return false;
        
        Pop3User that = (Pop3User) o;
        return this.email.equals(that.email);
    }
}
