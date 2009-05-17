package org.mailster.mina;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.BufferDataException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;

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
 * TextLineConsumer.java - This class consumes a {@link IoBuffer} extracting a 
 * \r\n terminated line and forwards decoded lines to a {@link IoFilterCodec} codec 
 * for further decoding. 
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class TextLineConsumer implements DataConsumer
{
    private IoBuffer delimBuf;
    private int maxLineLength 	= 1024;
    private int minLineLength 	= 0;
    private IoFilterCodec codec;

    /**
     * Creates a new instance with the current default {@link Charset}, 
     * the {@link LineDelimiter#DEFAULT} delimiter and the specified
     * {@link IoFilterCodec}.
     */
    public TextLineConsumer( IoFilterCodec codec )
    {
        this( Charset.defaultCharset(), LineDelimiter.DEFAULT , codec);
    }

    /**
     * Creates a new instance with the specified <tt>charset</tt>
     * the {@link LineDelimiter#DEFAULT} delimiter and the specified
     * {@link IoFilterCodec}.
     */
    public TextLineConsumer( Charset charset, IoFilterCodec codec )
    {
        this( charset, LineDelimiter.DEFAULT , codec );
    }

    /**
     * Creates a new instance with the specified <tt>charset</tt>
     * the specified <tt>delimiter</tt> and the specified
     * {@link IoFilterCodec}. Delimiter
     * {@link LineDelimiter#AUTO} is not allowed.
     */
    public TextLineConsumer( Charset charset, LineDelimiter delimiter, IoFilterCodec codec )
    {
        if( charset == null )
        {
            throw new NullPointerException( "charset" );
        }
        if( delimiter == null )
        {
            throw new NullPointerException( "delimiter" );
        }
        if( delimiter == LineDelimiter.AUTO )
        {
        	throw new IllegalArgumentException("LineDelimiter.AUTO is not allowed");
        }

        this.codec = codec;
        
        // Convert delimiter to ByteBuffer.
        delimBuf = IoBuffer.allocate( 2 ).setAutoExpand( true );
        try 
        {
			delimBuf.putString( delimiter.getValue(), charset.newEncoder() );
		} 
        catch (CharacterCodingException e) 
        {
			throw new RuntimeException(e);
		}
        delimBuf.flip();
    }
    
    /**
     * Returns the associated codec.
     */
	public IoFilterCodec getCodec() 
	{
		return codec;
	}
	
    /**
     * Returns the allowed minimum size of the line to be decoded.
     * The default value is <tt>0</tt>.
     */
    public int getMinLineLength()
    {
        return minLineLength;
    }
    
    /**
     * Sets the allowed minimum size of the line to be decoded.
     * If the size of the line is smaller than this value, the
     * {@link #consume(NextFilter, IoBuffer)} 
     * will return false immediately. The default value is <tt>0</tt>.
     */
    public void setMinLineLength( int minLineLength )
    {
        if( minLineLength <= 0 )
        {
            throw new IllegalArgumentException( "minLineLength: " + minLineLength );
        }

        this.minLineLength = minLineLength;
    }
    
    /**
     * Returns the allowed maximum size of the line to be decoded.
     * The default value is <tt>1024</tt> (1KB).
     */
    public int getMaxLineLength()
    {
        return maxLineLength;
    }

    /**
     * Sets the allowed maximum size of the line to be decoded.
     * If the size of the line to be decoded exceeds this value, the
     * decoder will throw a {@link BufferDataException}. The default
     * value is <tt>1024</tt> (1KB).
     */
    public void setMaxLineLength( int maxLineLength )
    {
        if( maxLineLength <= 0 )
        {
            throw new IllegalArgumentException( "maxLineLength: " + maxLineLength );
        }

        this.maxLineLength = maxLineLength;
    }    

    public boolean consume( NextFilter nextFilter, IoBuffer in ) 
    	throws Exception
    {        
    	if (in.remaining() < minLineLength)
    	{
    		return false;
    	}
    	
        // Try to find a match
        int oldPos = in.position();
        int oldLimit = in.limit();
        
        int matchCount = 0;
        boolean decoded = false;
        
        while( in.hasRemaining() )
        {
            byte b = in.get();
            if( delimBuf.get( matchCount ) == b )
            {
                matchCount ++;
                if( matchCount == delimBuf.limit() )
                {
                    // Found a match.
                    int pos = in.position();
                    in.position( oldPos );
                    
                    if( ( pos - oldPos ) > maxLineLength )
                    {
                        throw new BufferDataException( "Line is too long" );
                    }
                    
                    in.limit(pos - matchCount);
                    codec.unwrap( nextFilter, in.duplicate() );
                    decoded = true;
                    
                    in.limit( oldLimit );
                    in.position( pos );
                    oldPos = pos;
                    matchCount = 0;
                }
            }
            else
            {
            	in.position(Math.max(0, in.position() - matchCount));
                matchCount = 0;
            }
        }
        
        // Let remainder in buf.
        in.position( oldPos );
           
        return decoded;
    }
}