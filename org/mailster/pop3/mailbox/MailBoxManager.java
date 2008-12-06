package org.mailster.pop3.mailbox;

import java.util.Hashtable;

import org.mailster.smtp.SmtpMessage;

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
 * @version $Revision$, $Date$
 */
public class MailBoxManager
{
    /**
     * This is a special mailbox account name used to retrieve all the received
     * emails. This is intentionnaly a valid forged email address in order that
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
    	return pop3SpecialAccountMailbox.storeMessage(msg);
    }
    
    public void removeAllMessagesFromSpecialAccount()
    {
    	pop3SpecialAccountMailbox.removeAllMessages();
    }
    
    public void removeMessageFromSpecialAccount(StoredSmtpMessage msg) 
    {
        pop3SpecialAccountMailbox.removeMessage(msg);
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
        mailBoxes.clear();
    }

	public void setPop3SpecialAccountLogin(String pop3SpecialAccountLogin) 
	{
		this.pop3SpecialAccountLogin = pop3SpecialAccountLogin;
	}
}
