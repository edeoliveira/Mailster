package org.mailster.gui.views.mailbox;

import java.util.List;

import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
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
 * MailBoxTableInterface.java - API used by the <code>MailBoxView</code> to synchronize the tables.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.9 $, $Date: 2011/05/14 12:08:10 $
 */
public interface MailBoxTableInterface
{
	public Shell getShell();
	public boolean focus();
	
	public List<StoredSmtpMessage> getSelection();
	public void setSelection(List<StoredSmtpMessage> selected);
	public void selectAll();
	
	public void addTableListener(int eventType, Listener listener);
	public void removeTableListener(int eventType, Listener listener);
	
	public void setTableRedraw(boolean redraw);
	public void refreshViewer(Object elt, boolean updateLabels);
}
