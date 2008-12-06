package org.mailster.util;

import java.util.concurrent.ThreadFactory;

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
 * ThreadFactoryUtilitity.java - Creates thread factories that customize thread names.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class ThreadFactoryUtilitity
{
	public static ThreadFactory createFactory(final String threadName)
	{
		return
			new ThreadFactory() 
			{
				int sequence;
				
				public Thread newThread(Runnable r) 
				{					
					sequence += 1;
					StringBuilder sb = new StringBuilder();
					sb.append('[').append(Thread.currentThread().getThreadGroup().getName()).append("] ");
					sb.append(threadName).append(" - ").append(sequence);
					return new Thread(r, sb.toString());
				}			
			};
	}
}
