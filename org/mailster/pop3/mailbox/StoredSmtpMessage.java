package org.mailster.pop3.mailbox;

import java.text.ParseException;
import java.util.Date;

import javax.mail.Flags;

import org.mailster.message.SmtpMessage;
import org.mailster.util.DateUtilities;

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
 * StoredSmtpMessage.java - Decorates the <code>SmtpMessage</code>object with
 * properties used for the POP3 protocol and for the storage in a 
 * <code>MailBox</code>.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class StoredSmtpMessage
{
    private MailBox mailBox;
    
    private SoftSmtpMessageReference message;
    private Date internalDate = new Date();    
    private Flags flags = new Flags();
    private Long id;
    private boolean checked;
    
    // Cached data to prevent from parsing the date multiple times.
    private Date messageDate;
    
    // Cached data, allows to gc the message.
    private String _msgId;
    private String _msgTo;
    private String _msgSubject;
    private int _msgSize;
    private int _msgAttachedFilesCount;

    public StoredSmtpMessage(SmtpMessage msg, Long id)
    {
    	updateCache(msg);
	    
		this.message = new SoftSmtpMessageReference(id, msg);
        this.id = id;
    }

    protected void updateCache(SmtpMessage msg)
    {
    	try 
		{
    		synchronized(DateUtilities.rfc822DateFormatter)
    		{
    			messageDate = DateUtilities.rfc822DateFormatter.parse(msg.getDate());
    		}
		} 
		catch (ParseException e) 
		{
			messageDate = new Date();
		}
		
		_msgId = msg.getMessageID();
	    _msgTo = msg.getTo();
	    _msgSubject = msg.getSubject();
	    _msgSize = msg.getSize();
	    _msgAttachedFilesCount = msg.getInternalParts().getAttachedFiles().length; 
    }
    
    public Date getMessageDate()
    {
    	return messageDate;
    }
    
    public boolean isPassivated()
    {
    	return message.get() == null;
    }
    
    public String getMessageId()
    {
    	SmtpMessage msg = message.get();
    	if (msg != null)
    		return msg.getMessageID();
    	else
    		return _msgId;
    }

    public String getMessageTo()
    {
    	SmtpMessage msg = message.get();
    	if (msg != null)
    		return msg.getTo();
    	else
    		return _msgTo;
    }
    
    public String getMessageSubject()
    {
    	SmtpMessage msg = message.get();
    	if (msg != null)
    		return msg.getSubject();
    	else
    		return _msgSubject;
    }
    
    public int getMessageSize()
    {
    	SmtpMessage msg = message.get();
    	if (msg != null)
    		return msg.getSize();
    	else
    		return _msgSize;
    }
    
    public int getAttachedFilesCount()
    {
    	SmtpMessage msg = message.get();
    	if (msg != null)
    		return msg.getInternalParts().getAttachedFiles().length;
    	else
    		return _msgAttachedFilesCount;
    }    
    
    public boolean isChecked()
    {
        return checked;
    }

    public void setChecked(boolean checked)
    {
        this.checked = checked;
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
        return message.getReference();
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

	protected void finalize() throws Throwable 
	{
		super.finalize();
		message.delete();
	}
}
