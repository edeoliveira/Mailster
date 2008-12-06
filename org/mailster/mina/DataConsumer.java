package org.mailster.mina;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoFilter.NextFilter;

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
 * DataConsumer.java - An interface that defines the data consumer of a 
 * {@link CumulativeIoFilter}. A consumer is used to delimit messages 
 * before forwarding them to the {@link IoFilterCodec} codec.
 * 
 * Example :
 * 
 *   {@link TextLineConsumer} - messages are delimited by the \r\n sequence.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public interface DataConsumer 
{
	/**
	 * Consumes data and sends it to the {@link IoFilterCodec#unwrap(
	 * org.apache.mina.common.IoFilter.NextFilter, ByteBuffer)} method.
	 */
	public boolean consume( NextFilter nextFilter, ByteBuffer in )
		throws Exception;
	
	/**
	 * Returns the {@link IoFilterCodec} codec to which data is sent
	 * when consumed.
	 */
	public IoFilterCodec getCodec();
}
