package org.mailster.pop3.commands.auth.iofilter;

import java.util.Arrays;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoFilter.NextFilter;
import org.apache.mina.common.IoFilter.WriteRequest;
import org.apache.mina.common.support.BaseIoSession;
import org.mailster.mina.IoFilterCodec;
import org.mailster.pop3.commands.auth.AuthDigestMD5Command;
import org.mailster.pop3.commands.auth.AuthException;
import org.mailster.util.ByteUtilities;
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
 * See&nbsp; <a href="http://mailster.sourceforge.net" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * DigestMD5IntegrityIoFilterCodec.java - This class wraps and unwraps data when 
 * SASL DIGEST-MD5 has negotiated integrity protection. It forwards the message 
 * if successfull, otherwise it forwards an empty buffer.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class DigestMD5IntegrityIoFilterCodec implements IoFilterCodec 
{
    private static final Logger log = LoggerFactory.getLogger(DigestMD5IntegrityIoFilterCodec.class);
    public final static byte[] LINE_TERMINATOR = new byte[] {'\r', '\n'};
    
    protected final IoSession session;

    public DigestMD5IntegrityIoFilterCodec(IoSession session)
    {
        this.session = session;
    }
    
    public boolean isValidMAC(byte[] mac, byte[] computedMac)
    	throws AuthException
    {
    	if (mac.length != 16 || computedMac.length != 16)
    		throw new IllegalArgumentException("Mac arrays must be 16 byte wide");
    		
    	if (ByteUtilities.networkByteOrderToInt(computedMac, 10, 2) != 1)
	    	throw new AuthException("Wrong message type");

	    int peerSeq = ((Integer) session.getAttribute(AuthDigestMD5IoFilter.PEER_SEQUENCE_NUMBER)).intValue();
	    if (peerSeq != ByteUtilities.networkByteOrderToInt(computedMac, 12, 4))
	    	throw new AuthException("Wrong sequence number");
    	
	    if (Arrays.equals(mac, computedMac))
		    session.setAttribute(AuthDigestMD5IoFilter.PEER_SEQUENCE_NUMBER, new Integer(++peerSeq));
	    else
	    	throw new AuthException("Integrity protection checking failed");
	    
	    return true;
    }
    
    public void unwrap(NextFilter nextFilter, ByteBuffer buf) 
	{    	
    	try 
    	{        		 
    		byte[] msg = new byte[buf.limit()];
    		buf.get(msg);
    		int size = msg.length-16;
    	    
    		if (size < 0)
    	    	throw new AuthException("Missing or incomplete mac block");
    	    
    	    byte[] originalMessage = new byte[size];
    	    System.arraycopy(msg, 0, originalMessage, 0, size);
    	    
    	    byte[] mac = new byte[16];
    	    System.arraycopy(msg, size, mac, 0, 16);
    	    
    	    if (isValidMAC(mac, AuthDigestMD5IoFilter.computeMACBlock(session, originalMessage, true)))
    	    {
    		    buf.position(size);
    		    buf.put(LINE_TERMINATOR);
    		    buf.flip();

    		    nextFilter.messageReceived(session, buf);
    	    }
		} 
    	catch (Exception ex) 
    	{
    		log.debug(ex.getMessage());
    		nextFilter.messageReceived(session, "\r\n");
		}
    	
        if ( session instanceof BaseIoSession )
            ( ( BaseIoSession ) session ).increaseReadMessages();
	}
    
    public void wrap(NextFilter nextFilter, WriteRequest writeRequest, ByteBuffer buf)
    	throws AuthException
    {
		int endOriginalMessage = buf.limit() - DigestMD5IntegrityIoFilterCodec.LINE_TERMINATOR.length;
		byte[] originalMessage = new byte[endOriginalMessage]; 
		buf.get(originalMessage);				
		buf.put(AuthDigestMD5IoFilter.computeMACBlock(session, originalMessage, false));
		buf.put(DigestMD5IntegrityIoFilterCodec.LINE_TERMINATOR);				
		buf.flip();
		
		if (buf.limit() > ((Integer)session.getAttribute(AuthDigestMD5Command.CLIENT_MAXBUF)).intValue())
			throw new AuthException("Data exceeds client maxbuf capabilities");

		nextFilter.filterWrite(session, 
        		new WriteRequest(buf, writeRequest.getFuture()));
    }
}