package org.mailster.core.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;

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
 * CumulativeIoFilter.java - A IoFilter that cumulates data into internal buffer 
 * until there's enough data for the decoder to decode it. 
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.7 $, $Date: 2009/05/17 20:56:35 $
 */
public abstract class CumulativeIoFilter extends IoFilterAdapter
{
    private final static String BUFFER					= CumulativeIoFilter.class.getName() + ".Buffer";
    protected final static String CONSUMER		= CumulativeIoFilter.class.getName() + ".Consumer";
    
    public abstract DataConsumer getDataConsumer(IoSession session);
    
    /**
     * Cumulates content of <tt>in</tt> into internal buffer and forwards
     * decoding requests to 
     * {@link DataConsumer#consume(org.apache.mina.core.filterchain.IoFilter.NextFilter, IoBuffer)}.
     * repeatedly until it returns <tt>false</tt>. The cumulative buffer is 
     * compacted after decoding ends.
     * 
     * @throws IllegalStateException if your <tt>doDecode()</tt> returned
     *                               <tt>true</tt> not consuming the cumulative buffer.
     */
    public void cumulateAndConsume(NextFilter nextFilter, IoSession session, IoBuffer in) 
    	throws Exception
    {
        boolean usingSessionBuffer = true;
        IoBuffer buf = ( IoBuffer ) session.getAttribute( BUFFER );
        // If we have a session buffer, append data to that; otherwise
        // use the buffer read from the network directly.
        if( buf != null )
        {
            buf.put( in );
            buf.flip();
        }
        else
        {
            buf = in;
            usingSessionBuffer = false;
        }
        
        DataConsumer consumer = getDataConsumer( session );
        
        if (consumer == null)
        {
        	throw new IllegalArgumentException( "consumer can't be null" );
        }
        
        while (true)
        {
            int oldPos = buf.position();
            boolean consumed = consumer.consume( nextFilter, buf );
            if( consumed )
            {
                if( buf.position() == oldPos )
                    throw new IllegalStateException(
                            "cumulateAndConsume() can't return true when buffer is not consumed." );
                
                if( !buf.hasRemaining() )
                    break;
            }
            else
                break;
        }
        
        // if there is any data left that cannot be decoded, we store
        // it in a buffer in the session and next time this decoder is
        // invoked the session buffer gets appended to
        if ( buf.hasRemaining() )
        {
            if ( usingSessionBuffer )
                buf.compact();
            else
                storeRemainingInSession( buf, session );
        }
        else
        {
            if ( usingSessionBuffer )
                removeSessionBuffer( session );
        }
    }
    
    private void removeSessionBuffer( IoSession session )
    {        
    	IoBuffer buf = ( IoBuffer ) session.removeAttribute( BUFFER );
        if( buf != null )
            buf.free();
    }
    
    private void storeRemainingInSession( IoBuffer buf, IoSession session )
    {
    	IoBuffer remainingBuf = IoBuffer.allocate( buf.capacity() );
        remainingBuf.setAutoExpand( true );
        remainingBuf.put( buf );
        session.setAttribute( BUFFER, remainingBuf );
    }
}
