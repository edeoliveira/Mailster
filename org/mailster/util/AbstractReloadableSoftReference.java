package org.mailster.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

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
 * AbstractReloadableSoftReference.java - Provides a reloadable soft reference
 * for an object.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.3 $, $Date: 2008/12/06 13:57:17 $
 */
public abstract class AbstractReloadableSoftReference<T> extends SoftReference<T>
{	
	public AbstractReloadableSoftReference(T referent, ReferenceQueue<? super T> q)
	{
		super(referent, q);
	}

	public abstract void store(T object);
	
	public abstract T reload();
	
	public abstract void delete();
}