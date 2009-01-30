package org.mailster.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.write.WriteRequest;

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
 * IoFilterCodec.java - This class is the codec used by the {@link DataConsumer} 
 * that consumes the data cumulated by the {@link CumulativeIoFilter}.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public interface IoFilterCodec 
{
	/**
	 * Unwraps the data from the {@link IoBuffer} and writes it to the 
	 * {@link NextFilter} filter.
	 */    
	public void unwrap(NextFilter nextFilter, IoBuffer buf) 
		throws Exception;
	
	/**
	 * Wraps the data of the {@link IoBuffer} and writes it to the 
	 * {@link NextFilter} filter.
	 */
	public void wrap(NextFilter nextFilter, WriteRequest writeRequest, IoBuffer buf) 
		throws Exception;
}
