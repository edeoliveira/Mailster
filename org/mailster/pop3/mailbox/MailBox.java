package org.mailster.pop3.mailbox;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import javax.mail.Flags.Flag;

import org.mailster.message.SmtpMessage;
import org.mailster.util.StreamUtilities;
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
 * MailBox.java - In-memory store of a user mails .
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class MailBox
{
    private static final Logger log = LoggerFactory.getLogger(MailBox.class);
    
    private final Semaphore available = new Semaphore(1);
    private ConcurrentHashMap<Long, StoredSmtpMessage> mails = new ConcurrentHashMap<Long, StoredSmtpMessage>();
    private String mailBoxID;
    private String email;
    
    // Unique counter ID for the mailbox
    private long counter = 1;

    public MailBox(Pop3User user)
    {
        this.mailBoxID = user.getQualifiedMailboxID();
        this.email = user.getEmail();
    }

    public String toString()
    {
        return getEmail()+" ("+getMessageCount()+")";
    }
    
    public String getEmail()
    {
        return email;
    }
    
    public boolean tryAcquireLock(int nbOfRetries, long delayInMs)
    {
        if (nbOfRetries<1 || delayInMs<0)
            throw new IllegalArgumentException("Number of retries must be >=1 and delayInMs must be > 0");
        
        boolean acquired = false;
        for (int i=0;!acquired && i<nbOfRetries;i++)
        {
            if (i>0)
            {
                try
                {
                    Thread.sleep(delayInMs);
                }
                catch (InterruptedException e) {}
            }
            log.debug("Try n°={} to acquire lock on mailbox of {}", i+1, getEmail());
            acquired = available.tryAcquire();
        }
        
        log.debug("Lock acquired {}",acquired);
        return acquired;
    }
    
    public boolean tryAcquireLock()
    {
        return available.tryAcquire();
    }

    public void releaseLock()
    {
        available.release();
    }
    
    public void removeMessage(StoredSmtpMessage msg)
    {
   		mails.remove(msg.getId());
    }
    
    public void removeAllMessages()
    {
   		mails.clear();
    }
    
    /**
     * Stores a mail in the mailbox.
     * 
     * @param message the message to be stored
     * @return the stored object
     */
    public StoredSmtpMessage storeMessage(SmtpMessage message)
    {
    	StoredSmtpMessage stored = null;
    	
    	synchronized (mails) 
    	{
			Long id = new Long(counter);
			stored = new StoredSmtpMessage(message, id);
	        stored.setMailBox(this);
	    	mails.put(id, stored);
	    	counter++;
    	}
    	
        return stored;
    }
    
    /**
     * Removes everything from the to-be-deleted set
     */
    public void reset()
    {
        for (StoredSmtpMessage msg : mails.values())
            msg.getFlags().remove(Flag.DELETED);
    }

    /**
     * Returns the list of mails that are not marked for deletion.
     * 
     * @return the list
     */
    public List<StoredSmtpMessage> getNonDeletedMessages()
    {
        List<StoredSmtpMessage> l = new ArrayList<StoredSmtpMessage>();

        for (StoredSmtpMessage msg : mails.values())
        {
            if (!msg.getFlags().contains(Flag.DELETED))
                l.add(msg);
        }

        return l;
    }

    /**
     * Returns mail size by its ID
     */
    public long getMessageSize(Long id)
    {
        if (mails.containsKey(id))
            return mails.get(id).getMessageSize();
        else
            return 0;
    }

    /**
     * Get mail from the mailbox by its ID
     */
    public StoredSmtpMessage getMessage(Long id)
    {
        return mails.get(id);
    }

    /**
     * Returns the mailbox unique-id This implementation returns the hexed
     * hashcode of the mailbox concatened with the message hexed hashcode
     * separated by the ':' char.
     */
    public String getMessageUniqueID(Long id)
    {
        return Integer.toHexString(hashCode()) + ":"
                + Integer.toHexString(mails.get(id).hashCode());
    }

    /**
     * Deletes mails marked for deletion.
     * 
     * @throws Exception
     */
    public void deleteMarked() throws Exception
    {
        for (StoredSmtpMessage msg : mails.values())
        {
            if (msg.getFlags().contains(Flag.DELETED))
                mails.remove(msg);
        }
    }

    /**
     * Returns the number of emails in the mailbox
     */
    public long getMessageCount()
    {
        return mails.values().size();
    }

    /**
     * Returns the size in bytes of the emails in the mailbox
     */
    public long getMailBoxByteSize()
    {
        int total = 0;
        for (StoredSmtpMessage msg : mails.values())
            total += msg.getMessage().toString().length();
        
        return total;
    }

    /**
     * Writes mailbox to mbox format
     */
    public void writeMailBoxToFile(String outputDirectory)
    {
        try
        {
            PrintWriter out = new PrintWriter(new FileWriter(
            		outputDirectory + File.separator + mailBoxID + ".mbox", 
            		false));
            
            for (StoredSmtpMessage msg : mails.values())
            {
                if (!msg.getFlags().contains(Flag.DELETED))
                    StreamUtilities.writeMessageToMBoxRDFormat(msg, out);
            }
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
