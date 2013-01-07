package org.mailster.core.pop3.mailbox;

import java.util.Hashtable;
import java.util.Iterator;

import org.mailster.core.mail.SmtpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * MailBoxManager.java - Manages the mailboxes.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.9 $, $Date: 2009/03/24 16:52:51 $
 */
public class MailBoxManager
{
    private static final Logger log = LoggerFactory.getLogger(MailBoxManager.class);
	
    /**
     * This is a special mailbox account name used to retrieve all the received
     * emails. This is purposedly a valid forged email address in order that
     * email clients won't reject it.
     */
    public final static String POP3_SPECIAL_ACCOUNT_LOGIN = "pop3.all@Mailster.host.org";

    private Hashtable<String, MailBox> mailBoxes = new Hashtable<String, MailBox>();
    private MailBox pop3SpecialAccountMailbox;
    private String pop3SpecialAccountLogin = POP3_SPECIAL_ACCOUNT_LOGIN; 
    
    protected MailBoxManager()
    {
        pop3SpecialAccountMailbox = new MailBox(new Pop3User(POP3_SPECIAL_ACCOUNT_LOGIN));
        mailBoxes.put(POP3_SPECIAL_ACCOUNT_LOGIN, pop3SpecialAccountMailbox);
    }

    public StoredSmtpMessage addMessageToSpecialAccount(SmtpMessage msg)
    {
    	boolean acquired = pop3SpecialAccountMailbox.tryAcquireLock(3, 300);
    	if (acquired)
    	{
    		StoredSmtpMessage m = pop3SpecialAccountMailbox.storeMessage(msg);
    		pop3SpecialAccountMailbox.releaseLock();
    		
    		return m;
    	}
    	
    	return null;
    }
    
    public void removeAllMessagesFromSpecialAccount()
    {
    	boolean acquired = pop3SpecialAccountMailbox.tryAcquireLock(3, 300);
    	if (acquired)
    	{
    		pop3SpecialAccountMailbox.removeAllMessages();
    		pop3SpecialAccountMailbox.releaseLock();
    	}
    }
    
    protected void removeMessageFromSpecialAccount(StoredSmtpMessage msg) 
    {
    	boolean acquired = pop3SpecialAccountMailbox.tryAcquireLock(3, 300);
    	if (acquired)
    	{
    		pop3SpecialAccountMailbox.removeMessage(msg);
    		pop3SpecialAccountMailbox.releaseLock();
    	}
    }

    public void removeAllMessages()
    {
    	synchronized (this) 
    	{
        	Iterator<MailBox> it = mailBoxes.values().iterator();
        	while (it.hasNext())
        	{
        		MailBox m = it.next();
        		try
        		{
        			m.acquireLock();
        			m.removeAllMessages();
        			m.releaseLock();
        		}
        		catch (Exception ex)
        		{
       				log.debug("Error acquiring lock on mailbox", ex);
        		}
        	}			
		}
    }
    
    public void removeMessage(StoredSmtpMessage msg)
    {
    	removeMessageFromSpecialAccount(msg);
        for (String recipient : msg.getMessage().getRecipients())
        {
        	if (recipient.indexOf('<') > -1)
        		recipient = recipient.substring(recipient.indexOf('<') + 1, recipient.indexOf('>'));
        	
            MailBox mbox = getMailBoxByUser(new Pop3User(recipient));            
			boolean acquired = mbox.tryAcquireLock(3, 300);
			if (acquired)
			{
				mbox.removeMessage(msg);
				mbox.releaseLock();
			}
        }
	}
    
    public MailBox getMailBoxByUser(Pop3User user)
    {
        if (user == null || "".equals(user.getEmail()))
            return null;

        synchronized (this)
        {
        	if (pop3SpecialAccountLogin.equals(user.getEmail()))
        		return pop3SpecialAccountMailbox;
        	
        	if (!mailBoxes.containsKey(user.getEmail()))
            {
                MailBox m = new MailBox(user);
                mailBoxes.put(user.getEmail(), m);
                return m;
            }
        }
        
        return mailBoxes.get(user.getEmail());
    }

    public void clear()
    {
    	synchronized (this)
    	{
    		mailBoxes.clear();
    	}
    }

	public void setPop3SpecialAccountLogin(String pop3SpecialAccountLogin) 
	{
		synchronized (this) 
		{
			this.pop3SpecialAccountLogin = pop3SpecialAccountLogin;	
		}
	}
}
