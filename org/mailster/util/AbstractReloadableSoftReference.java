package org.mailster.util;

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
 * See&nbsp; <a href="http://mailster.sourceforge.net" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * AbstractReloadableSoftReference.java - Provides a reloadable soft reference
 * for an object.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public abstract class AbstractReloadableSoftReference<E, T> 
{	
	private SoftReference<T> ref;
	private E key;

	public AbstractReloadableSoftReference(E key, T object) 
	{
		this.key = key;
		this.ref = new SoftReference<T>(object);		
	}
	
	public abstract void store(T object);
	
	public abstract T reload();
	
	public abstract void delete();
	
	/**
	 * This method may return null if object has been gc'ed.
	 * @return the object.
	 */
	public T get()
	{
		return this.ref.get();
	}
	
	public synchronized T getReference() 
	{
	    T object = this.ref.get();
	    if (object == null)
	    {
			object = reload();
			this.ref = new SoftReference<T>(object); 
	    }
	    
	    return object;
	}
	
	public E getKey()
	{
		return this.key;
	}
}