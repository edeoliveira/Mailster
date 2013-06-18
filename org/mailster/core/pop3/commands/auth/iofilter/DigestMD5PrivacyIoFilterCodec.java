package org.mailster.core.pop3.commands.auth.iofilter;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.mailster.core.pop3.commands.auth.AuthDigestMD5Command;
import org.mailster.core.pop3.commands.auth.AuthException;
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
 * DigestMD5PrivacyIoFilterCodec.java - This class wraps and unwraps data when 
 * SASL DIGEST-MD5 has negotiated privacy protection. It forwards the message 
 * if successfull, otherwise it forwards an empty buffer.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.4 $, $Date: 2009/01/30 01:32:30 $
 */
public class DigestMD5PrivacyIoFilterCodec extends DigestMD5IntegrityIoFilterCodec 
{
	private static final Logger log = LoggerFactory.getLogger(DigestMD5PrivacyIoFilterCodec.class);
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
	
	private Cipher encCipher;
	private Cipher decCipher;
	
	public DigestMD5PrivacyIoFilterCodec(IoSession session) 
	{
		super(session);
		String encoding = (String) session.getAttribute(AuthDigestMD5Command.ENCODING);
		try 
		{
			AuthDigestMD5IoFilter.computePrivacyKeys(session, encoding, false);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		encCipher = (Cipher) session.getAttribute(AuthDigestMD5IoFilter.ENCODING_CIPHER);
		decCipher = (Cipher) session.getAttribute(AuthDigestMD5IoFilter.DECODING_CIPHER);
	}
	
	public void unwrap(NextFilter nextFilter, IoBuffer buf) 
	{
		try
		{
			int len = buf.remaining();
		    if (len == 0)
		    	throw new AuthException("Decryption failed");
		    
		    byte[] encryptedMsg = new byte[len - 6];
		    byte[] msgType = new byte[2];
		    byte[] seqNum = new byte[4];
		
		    // Get cipherMsg; msgType; sequenceNum
		    buf.get(encryptedMsg);
		    buf.get(msgType);
		    buf.get(seqNum);
		
		    // Decrypt message - CIPHER(Kc, {msg, pad, HMAC(Ki, {SeqNum, msg}[0..9])})
		    byte[] decryptedMsg;
		
		    try 
		    {
				// Do CBC (chaining) across packets
				decryptedMsg = decCipher.update(encryptedMsg);
			
				// update() can return null
				if (decryptedMsg == null)			     
				    throw new IllegalBlockSizeException(""+encryptedMsg.length);
		    } 
		    catch (IllegalBlockSizeException e) 
		    {
		    	throw new AuthException("Illegal block sizes used with chosen cipher", e);
		    }
		
		    byte[] msgWithPadding = new byte[decryptedMsg.length - 10];
		    byte[] mac = new byte[10];
			    
		    System.arraycopy(decryptedMsg, 0, msgWithPadding, 0, msgWithPadding.length);
		    System.arraycopy(decryptedMsg, msgWithPadding.length, mac, 0, 10);
		
		    int msgLength = msgWithPadding.length;
		    int blockSize = decCipher.getBlockSize();
		    
		    if (blockSize > 1) 
		    {
				// get value of last octet of the byte array 
				msgLength -= (int)msgWithPadding[msgWithPadding.length - 1];
				if (msgLength < 0) 
				    //  Discard message and do not increment sequence number
					throw new AuthException("Decryption failed");
		    }
			
		    byte[] msg = new byte[msgLength];
		    System.arraycopy(msgWithPadding, 0, msg, 0, msgLength);
		    
		    // Re-calculate MAC to ensure integrity
		    byte[] expectedMac = AuthDigestMD5IoFilter.computeMACBlock(session, msg, true);
		    
		    byte[] fullMac = new byte[16];
		    System.arraycopy(mac, 0, fullMac, 0, 10);
		    System.arraycopy(msgType, 0, fullMac, 10, 2);
		    System.arraycopy(seqNum, 0, fullMac, 12, 4);
		    
		    if (isValidMAC(fullMac, expectedMac))
		    {
		    	IoBuffer out = IoBuffer.allocate(msgLength+LINE_TERMINATOR.length);
				out.put(msg);
				out.put(LINE_TERMINATOR);
			    out.flip();
	
			    nextFilter.messageReceived(session, out);
		    }
		}
    	catch (Exception ex) 
    	{
    		log.debug(ex.getMessage());
    		nextFilter.messageReceived(session, "\r\n");
		}

        if ( session instanceof AbstractIoSession )
            ( ( AbstractIoSession ) session ).
            	increaseReadMessages(System.currentTimeMillis());    	
	}
	
	public void wrap(NextFilter nextFilter, WriteRequest writeRequest, IoBuffer buf)
	    throws AuthException 
	{
		int start = buf.position();
		int len = buf.remaining()-LINE_TERMINATOR.length;
	    if (len == 0)
	    	throw new AuthException("Decryption failed");
	
	    // HMAC(Ki, {SeqNum, msg})[0..9]
	    byte[] originalMessage = new byte[len];
	    buf.get(originalMessage);    
	    byte[] mac = AuthDigestMD5IoFilter.computeMACBlock(session, originalMessage, false);
	
	    // Calculate padding
	    int bs = encCipher.getBlockSize();
	    byte[] padding;

	    if (bs > 1 ) 
	    {
			int pad = bs - ((len + 10) % bs); // add 10 for HMAC[0..9]
			padding = new byte[pad];
			for (int i=0; i < pad; i++)
			    padding[i] = (byte)pad;
	    } 
	    else
	    	padding = EMPTY_BYTE_ARRAY;
	
	    byte[] toBeEncrypted = new byte[len+padding.length+10];
	
	    // {msg, pad, HMAC(Ki, {SeqNum, msg}[0..9])}
	    System.arraycopy(originalMessage, start, toBeEncrypted, 0, len);
	    System.arraycopy(padding, 0, toBeEncrypted, len, padding.length);
	    System.arraycopy(mac, 0, toBeEncrypted, len+padding.length, 10);
	
	    // CIPHER(Kc, {msg, pad, HMAC(Ki, {SeqNum, msg}[0..9])})
	    byte[] cipherBlock;
	    try 
	    {
			// Do CBC (chaining) across packets
			cipherBlock = encCipher.update(toBeEncrypted);
		
			// update() can return null 
			if (cipherBlock == null)			    
			    throw new IllegalBlockSizeException(""+toBeEncrypted.length);
	    } 
	    catch (IllegalBlockSizeException e) 
	    {
	    	throw new AuthException("Invalid block size for cipher", e);
	    }
	    
	    IoBuffer out = IoBuffer.allocate(
	    		cipherBlock.length+2+4+LINE_TERMINATOR.length);
	    out.put(cipherBlock);
	    out.put(mac, 10, 6); // messageType & sequenceNum
	    out.put(LINE_TERMINATOR);
	    out.flip();
	    
		if (out.limit() > ((Integer)session.getAttribute(AuthDigestMD5Command.CLIENT_MAXBUF)).intValue())
			throw new AuthException("Data exceeds client maxbuf capability");

		nextFilter.filterWrite(session, 
        		new DefaultWriteRequest(out, writeRequest.getFuture()));
	}
}