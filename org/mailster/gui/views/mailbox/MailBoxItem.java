package org.mailster.gui.views.mailbox;

import org.mailster.core.pop3.mailbox.StoredSmtpMessage;

/**
 * ---<br>
 * Mailster (C) 2007-2009 De Oliveira Edouard
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * <p>
 * See&nbsp; <a href="http://tedorg.free.fr/en/projects.php" target="_parent">Mailster Web Site</a>
 * <br>
 * ---
 * <p>
 * MailBoxItem.java - Decorates the <code>StoredSmtpMessage</code>object with properties used for
 * the <code>MailBoxView</code>.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.9 $, $Date: 2011/05/14 12:08:10 $
 */
public class MailBoxItem
{
	public static MailBoxItemCategories categories = new MailBoxItemCategories();
	
	private StoredSmtpMessage message;
	private String categoryLabel;
	private long category = -1;
	private boolean root;
	
	public MailBoxItem(String label)
	{
		// root nodes constructor
		this.root = true;
		categoryLabel = label;
		category = categories.getCategory(label);
	}

	public MailBoxItem(StoredSmtpMessage msg)
	{
		this.message = msg;
	}

	public static void computeCategories()
	{
		categories.rebuildCategories();
	}

	public boolean isRoot()
	{
		return root;
	}

	public boolean resetCategory()
	{
		String oldCategoryLabel = categoryLabel;
		categoryLabel = null;
		category = -1;

		return !getCategoryLabel().equals(oldCategoryLabel);
	}

	public long getCategory()
	{
		if (category == -1 && message != null)
		{
			long d = message.getMessageDate().getTime();
			category = categories.getCategory(d);
		}

		return category;
	}

	public String getCategoryLabel()
	{
		if (categoryLabel == null && message != null)
		{
			long d = message.getMessageDate().getTime();
			categoryLabel = categories.getCategoryLabel(d);
		}

		return categoryLabel;
	}

	public StoredSmtpMessage getMessage()
	{
		return message;
	}

	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (category ^ (category >>> 32));
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + (root ? 1231 : 1237);
		return result;
	}

	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MailBoxItem other = (MailBoxItem) obj;
		if (root != other.root)
			return false;
		if (category != other.category)
			return false;
		if (message == null)
		{
			if (other.message != null)
				return false;
		}
		else if (!message.equals(other.message))
			return false;
		return true;
	}

	public String toString()
	{
		if (message == null)
			return categoryLabel;
		else
			return message.getMessageSubject();
	}
}
