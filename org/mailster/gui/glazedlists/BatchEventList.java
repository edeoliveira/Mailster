package org.mailster.gui.glazedlists;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;

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
 * See&nbsp; <a href="http://mailster.sourceforge.net" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * BatchEventList.java - Allow to do batch changes to a given source list.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class BatchEventList<S, E> extends TransformedList<S, E> 
{
	public BatchEventList(EventList<S> source) 
	{
		super(source);
		source.addListEventListener(this);
	}

	public void listChanged(ListEvent<S> e) 
	{
		updates.forwardEvent(e);
	}

	protected boolean isWritable() 
	{
		return true;
	}

	public void beginBatch() 
	{
		updates.beginEvent(true);
	}

	public void commitBatch() 
	{
		updates.commitEvent();
	}
}