package org.mailster.subethasmtp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.server.AbstractMessageHandler;

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
 * MailsterMessageHandler.java - Class which implements the {@link MessageHandler}
 * interface.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class MailsterMessageHandler
	extends AbstractMessageHandler
{
	private List<String> recipients = new ArrayList<String>();
	private String from;
	
	public MailsterMessageHandler(MessageContext ctx, AuthenticationHandler authHandler)
	{
		super(ctx, authHandler);
	}
	
	public void from(String from) throws RejectException
	{
		this.from = from;
	}
	
	public void recipient(String recipient) throws RejectException
	{
		recipients.add(recipient);
	}
	
	public void resetMessageState()
	{
		this.from = null;
		this.recipients.clear();
	}
	
	public void data(InputStream data) throws TooMuchDataException, IOException
	{
		if (recipients.size() > 0)
		{
			SubEthaSmtpServer server = (SubEthaSmtpServer) getListeners().iterator().next();
			server.deliver(this.from, recipients, data);
		}
	}
}