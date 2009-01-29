package org.mailster.service.smtp;

import java.io.InputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.mailster.dumbster.SmtpRequest;
import org.mailster.dumbster.SmtpResponse;
import org.mailster.dumbster.SmtpState;
import org.mailster.service.smtp.parser.SmtpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * SmtpMessageFactory.java - This class creates a {@link SmtpMessage} from an 
 * {@link InputStream}. 
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class SmtpMessageFactory
{
	private static Logger log = LoggerFactory.getLogger(SmtpMessageFactory.class);
	
	public final static LineDelimiter DELIMITER = new LineDelimiter("\r\n");
	
    private ByteBuffer delimBuf;
    private Charset charset;
    private CharsetDecoder decoder;
    
    /**
     * Decoding vars.
     */
    private SmtpRequest previous = null;
    private SmtpRequest request = null;
    private SmtpState smtpState = SmtpState.DATA_HDR;

    /**
     * Creates a new instance with the current default {@link Charset}, 
     * the {@link SmtpMessageFactory#DELIMITER} delimiter.
     */
    public SmtpMessageFactory()
    {
        this(Charset.defaultCharset(), DELIMITER);
    }

    /**
     * Creates a new instance with the specified <tt>charset</tt> 
     * and the {@link SmtpMessageFactory#DELIMITER} delimiter.
     */
    public SmtpMessageFactory(Charset charset)
    {
        this(charset, DELIMITER);
    }  
    
    /**
     * Creates a new instance with the specified <tt>charset</tt> and
     * the specified <tt>delimiter</tt>.
     */
    public SmtpMessageFactory(Charset charset, LineDelimiter delimiter)
    {
        if(charset == null)
        {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
        
        if (delimiter == null)
        {
            throw new NullPointerException("delimiter");
        }
        
        // Convert delimiter to ByteBuffer.
        delimBuf = ByteBuffer.allocate(delimiter.getValue().length()).setAutoExpand(true);
        try 
        {
			delimBuf.putString(delimiter.getValue(), charset.newEncoder());
		} 
        catch (CharacterCodingException e) 
        {
			throw new RuntimeException(e);
		}
        delimBuf.flip();
    }

    private void reset()
    {
    	previous = null;
        request = null;
        smtpState = SmtpState.DATA_HDR;
        decoder = charset.newDecoder();
    }
    
    public SmtpMessage asSmtpMessage(InputStream data)
    	throws Exception
    {
    	SmtpMessage msg = new SmtpMessage();            	
    	reset();
    	
    	byte[] b = new byte[1024];
		ByteBuffer buf = ByteBuffer.allocate(1024).setAutoExpand(true);		
		boolean decoded = false;
		int nb = 0;
		
		while ((nb = data.read(b)) > -1)
		{
			if (buf.position() != 0)
				buf.compact();
			
			buf.put(b, 0, nb);
			buf.flip();
			decoded = consume(msg, buf);
		}
		
		if (decoded && (buf.position() == buf.limit()) && !"\n".equals(previous.getParams()))
		{
			request = SmtpRequest.createRequest("", SmtpState.DATA_BODY, previous);
			msg.store(request.execute(), request.getParams());
		}
		
		if (buf.remaining()>0)
			updateMessage(msg, buf);
		
		buf.release();
		
    	return msg;
    }
    
    /**
     * Stores the data in the {@link SmtpMessage} body.
     */
    private void updateMessage(SmtpMessage msg, ByteBuffer in)
    	throws Exception
    {
        String line = in.getString(decoder);
        
        if (line.endsWith("\r"))
        	log.debug("[CONSUME] "+line.substring(0, line.length()-1));
        else		
        	log.debug("[CONSUME] "+line);
        
        previous = request;
        request = SmtpRequest.createRequest(line, smtpState, previous);
        SmtpResponse response = request.execute();
        smtpState = response.getNextState();
        
        msg.store(response, request.getParams());
    }
    
    private boolean consume(SmtpMessage msg, ByteBuffer in) 
    	throws Exception
    {        
        // Try to find a match
        int oldPos = in.position();
        int oldLimit = in.limit();
        int matchCount = 0;
        boolean decoded = false;
        
        while(in.hasRemaining())
        {
            byte b = in.get();
            if(delimBuf.get(matchCount) == b)
            {
                matchCount ++;
                if(matchCount == delimBuf.limit())
                {
                    // Found a match.
                    int pos = in.position();
                    in.position(oldPos);                    
                    in.limit(pos - matchCount);
                    
                    updateMessage(msg, in);
                    decoded = true;
                    
                    in.limit(oldLimit);
                    in.position(pos);
                    oldPos = pos;
                    matchCount = 0;
                }
            }
            else
            {
            	in.position(in.position()-matchCount);
                matchCount = 0;
            }
        }
        
        // Let remainder in buf.
        in.position(oldPos);
        
        return decoded;
    }
}