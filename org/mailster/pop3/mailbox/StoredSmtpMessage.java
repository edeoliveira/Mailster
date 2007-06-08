package org.mailster.pop3.mailbox;

import java.util.Date;

import javax.mail.Flags;

import org.mailster.smtp.SmtpMessage;

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
 * StoredSmtpMessage.java - Decorates the <code>SmtpMessage</code>object with
 * properties used for the POP3 protocol and for the storage in a 
 * <code>MailBox</code>.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class StoredSmtpMessage
{
    private MailBox mailBox;
    
    private SmtpMessage message;
    private Date internalDate = new Date();
    private Flags flags = new Flags();
    private Long id;
    
    private boolean checked;
    
    public boolean isChecked()
    {
        return checked;
    }

    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }

    public StoredSmtpMessage(SmtpMessage message, Long id)
    {
        this.message = message;
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public Flags getFlags()
    {
        return flags;
    }

    public SmtpMessage getMessage()
    {
        return message;
    }

    public Date getInternalDate()
    {
        return internalDate;
    }
    
    public boolean isSeen()
    {
        return flags.contains(Flags.Flag.SEEN);
    }
    
    public void setSeen()
    {
        flags.add(Flags.Flag.SEEN);
    }

    public MailBox getMailBox()
    {
        return mailBox;
    }

    protected void setMailBox(MailBox mailBox)
    {
        this.mailBox = mailBox;
    }
}
