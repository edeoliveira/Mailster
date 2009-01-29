package org.mailster.service.smtp.events;

import java.util.EventObject;

import org.mailster.service.smtp.parser.SmtpMessage;


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
 * SMTPServerEvent.java - Enter your Comment HERE.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class SMTPServerEvent extends EventObject
{
    private static final long serialVersionUID = 6285711085999392526L;

    private SmtpMessage message;
    
    /**
     * Constructs a new instance of this class.
     * 
     * @param source the object which fired the event
     */
    public SMTPServerEvent(Object source)
    {
        super(source);
    }

    public Object getSource()
    {
        return super.getSource();
    }

    public String toString()
    {
        return super.toString();
    }

    public SmtpMessage getMessage()
    {
        return message;
    }

    public void setMessage(SmtpMessage message)
    {
        this.message = message;
    }
}
