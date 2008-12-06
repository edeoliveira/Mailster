package org.mailster.pop3.commands.auth.iofilter;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.mailster.mina.CumulativeIoFilter;
import org.mailster.mina.DataConsumer;
import org.mailster.mina.IoFilterCodec;
import org.mailster.mina.TextLineConsumer;
import org.mailster.pop3.commands.auth.AuthCramMD5Command;
import org.mailster.pop3.commands.auth.AuthDigestMD5Command;
import org.mailster.pop3.commands.auth.AuthException;
import org.mailster.util.ByteUtilities;
import org.mailster.util.md5.MD5;

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
 * AuthDigestMD5IoFilter.java - Handles AUTH DIGEST-MD5 command  integrity 
 * protection and encryption (see RFC 2831).
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class AuthDigestMD5IoFilter 
	extends CumulativeIoFilter
{
    private final static String CLIENT_INTEGRITY_KEY 	= AuthDigestMD5IoFilter.class.getName()+".Kic";
    private final static String SERVER_INTEGRITY_KEY 	= AuthDigestMD5IoFilter.class.getName()+".Kis";

    private final static String SEQUENCE_NUMBER 					= AuthDigestMD5IoFilter.class.getName()+".sequenceNumber";
    protected final static String PEER_SEQUENCE_NUMBER	= AuthDigestMD5IoFilter.class.getName()+".peerSequenceNumber";
    
    protected final static String ENCODING_CIPHER 	= AuthDigestMD5IoFilter.class.getName()+".encodingCipher";
    protected final static String DECODING_CIPHER 	= AuthDigestMD5IoFilter.class.getName()+".decodingCipher";
    
    public final static String DISABLE_FILTER_ONCE = AuthDigestMD5IoFilter.class.getName() + ".DisableFilterOnce";
    
    private final static LineDelimiter DELIMITER			= new LineDelimiter("\r\n");
    
    /* Mask used to check for parity adjustment */
    private static final byte[] PARITY_BIT_MASK = { (byte)0x80, (byte)0x40, (byte)0x20, (byte)0x10,
    																		(byte)0x08, (byte)0x04, (byte)0x02 };
    private static final BigInteger MASK = new BigInteger("7f", 16);
    
    public static enum CIPHER 
    { 
    	RC4, RC4_40, RC4_56, DES, DES3;
        
    	private static final String[] JCE_CIPHER_NAME = 
        {
        	"RC4",
        	"DES/CBC/NoPadding",
        	"DESede/CBC/NoPadding",    	
        };
    	
    	private static boolean[] supportedCiphers = new boolean[JCE_CIPHER_NAME.length];
    	
        static 
        {
        	for (int i = 0; i < JCE_CIPHER_NAME.length; i++) 
        	{
        	    try 
        	    {
    	    		// Checking whether the transformation is available from the
    	    		// current installed providers.
    	    		Cipher.getInstance(JCE_CIPHER_NAME[i], "BC");
    	    		supportedCiphers[i] = true;
        	    } 
        	    catch (Exception e) 
        	    {
        	    	// no implementation found for requested algorithm.
        	    }
        	}
        }
        
        public static boolean isSupported(CIPHER cipher)
        {
        	switch (cipher)
        	{
        		case RC4 : 
        		case RC4_40 : 
        		case RC4_56 : 
        			return supportedCiphers[0];
        		case DES : return supportedCiphers[1];
        		case DES3 : return supportedCiphers[2];
        		default :
        			return false;
        	}
        }
        
        public static CIPHER getByName(String alg)
        {
        	if (alg == null)
        		return null;
        	
        	if (alg.equals("rc4"))
        		return RC4;
        	if (alg.equals("rc4-40"))
        		return RC4_40;
        	if (alg.equals("rc4-56"))
        		return RC4_56;
        	if (alg.equals("des"))
        		return DES;
        	if (alg.equals("3des"))
        		return DES3;
        	
        	return null;
        }
        
        public String toString()
        {
        	switch (this)
        	{
        		case RC4 : return "rc4";
        		case RC4_40 : return "rc4-40";
        		case RC4_56 : return "rc4-56";
        		case DES : return "des";
        		case DES3 : return "3des";
        		default :
        			return null;
        	}
        }
    };
    
    static
    {
        // Disable native library loading
        MD5.initNativeLibrary(true);        
    }
    
    protected static MD5 md5 = new MD5();
    
    public static void computeIntegrityKeys(IoSession session, String encoding) 
    	throws UnsupportedEncodingException
    {
        byte[] kic = new byte[16];
        byte[] kis = new byte[16];
        
        byte[] A1 = (byte[]) session.getAttribute(AuthDigestMD5Command.A1);
        synchronized (md5)
        {
            md5.Init();
            md5.Update(A1);
            byte[] hA1 = md5.Final();
            
            md5.Init();
            md5.Update(hA1);
            md5.Update(
            	"Digest session key to client-to-server signing key magic constant".getBytes(encoding));
            kic = md5.Final();
            
            md5.Init();
            md5.Update(hA1);
            md5.Update(
            	"Digest session key to server-to-client signing key magic constant".getBytes(encoding));
            kis = md5.Final();
        }
        
        session.setAttribute(CLIENT_INTEGRITY_KEY, kic);
        session.setAttribute(SERVER_INTEGRITY_KEY, kis);
        session.setAttribute(SEQUENCE_NUMBER, new Integer(0));
        session.setAttribute(PEER_SEQUENCE_NUMBER, new Integer(0));
    }
    	
	/**	
	 * Generates client-server and server-client keys to encrypt and
	 * decrypt messages. Also generates IVs for DES ciphers.	 
	 */
    public static void computePrivacyKeys(IoSession session, String encoding, boolean isClientToServer) 
    	throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, 
    	InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException 
    {
	    byte[] ccmagic = "Digest H(A1) to client-to-server sealing key magic constant".getBytes(encoding);
	    byte[] scmagic = "Digest H(A1) to server-to-client sealing key magic constant".getBytes(encoding);
	    
	    CIPHER negotiatedCipher = (CIPHER) session.getAttribute(AuthDigestMD5Command.NEGOCIATED_CIPHER);
	    // Kcc = MD5{H(A1)[0..n], "Digest ... client-to-server"}
	    int n;
	    if (negotiatedCipher.equals(CIPHER.RC4_40))
			n = 5; // H(A1)[0..5]
		else if (negotiatedCipher.equals(CIPHER.RC4_56))
			n = 7; // H(A1)[0..7]
		else
			// des and 3des and rc4
			n = 16; // H(A1)[0..16]

	    byte[] A1 = (byte[]) session.getAttribute(AuthDigestMD5Command.A1);
	    byte[] hA1 = null;
	    synchronized (md5)
        {
            md5.Init();
            md5.Update(A1);
            hA1 = md5.Final();
        }
	    
	    // Both client-magic-keys and server-magic-keys are the same length
	    byte[] keyBuffer = new byte[n + ccmagic.length];
	    System.arraycopy(hA1, 0, keyBuffer, 0, n);   // H(A1)[0..n]

	    // Kcc: Key for encrypting messages from client->server
	    System.arraycopy(ccmagic, 0, keyBuffer, n, ccmagic.length);
	    byte[] kcc = null;
	    synchronized (md5)
        {
            md5.Init();
            md5.Update(keyBuffer);
            kcc = md5.Final();
        }

	    // Kcs: Key for decrypting messages from server->client
	    // No need to copy hA1 again since it hasn't changed
	    System.arraycopy(scmagic, 0, keyBuffer, n, scmagic.length);
	    byte[] kcs = null;
	    synchronized (md5)
        {
            md5.Init();
            md5.Update(keyBuffer);
            kcs = md5.Final();
        }

	    byte[] myKc;
	    byte[] peerKc;

	    if (isClientToServer) 
	    {
			myKc = kcc;
			peerKc = kcs;
		} 
	    else 
	    {
			myKc = kcs;
			peerKc = kcc;
		}

		SecretKey encKey;
		SecretKey decKey;
		Cipher encCipher = null;
		Cipher decCipher = null;

		// Initialize cipher objects
		if (CIPHER.RC4.equals(negotiatedCipher) ||
				CIPHER.RC4_40.equals(negotiatedCipher) ||
				CIPHER.RC4_56.equals(negotiatedCipher)) 
		{
			encCipher = Cipher.getInstance("RC4", "BC");
			decCipher = Cipher.getInstance("RC4", "BC");

			encKey = new SecretKeySpec(myKc, "RC4");
			decKey = new SecretKeySpec(peerKc, "RC4");

			encCipher.init(Cipher.ENCRYPT_MODE, encKey);
			decCipher.init(Cipher.DECRYPT_MODE, decKey);

		} 
		else 
		if ((CIPHER.DES.equals(negotiatedCipher))
				|| (CIPHER.DES3.equals(negotiatedCipher))) 
		{
			// DES or 3DES
			String cipherFullname;
			String cipherShortname;

			// Use "NoPadding" when specifying cipher names
			// RFC 2831 already defines padding rules for producing
			// 8-byte aligned blocks
			if (CIPHER.DES.equals(negotiatedCipher)) 
			{
				cipherFullname = "DES/CBC/NoPadding";
				cipherShortname = "des";
			} 
			else 
			{
				// 3DES
				cipherFullname = "DESede/CBC/NoPadding";
				cipherShortname = "desede";
			}

			encCipher = Cipher.getInstance(cipherFullname, "BC");
			decCipher = Cipher.getInstance(cipherFullname, "BC");

			encKey = makeDesKeys(myKc, cipherShortname);
			decKey = makeDesKeys(peerKc, cipherShortname);

			// Set up the DES IV, which is the last 8 bytes of Kcc/Kcs
			IvParameterSpec encIv = new IvParameterSpec(myKc, 8, 8);
			IvParameterSpec decIv = new IvParameterSpec(peerKc, 8, 8);

			// Initialize cipher objects
			encCipher.init(Cipher.ENCRYPT_MODE, encKey, encIv);
			decCipher.init(Cipher.DECRYPT_MODE, decKey, decIv);
		}
		
		session.setAttribute(ENCODING_CIPHER, encCipher);
		session.setAttribute(DECODING_CIPHER, decCipher);
	}

    /**
     * Sets the parity bit (0th bit) in each byte so that each byte
     * contains an odd number of 1's.
     */
    private static void setParityBit(byte[] key) 
    {
		for (int i = 0; i < key.length; i++) 
		{
		    int bitCount = 0;
		    for (int maskIndex = 0; maskIndex < PARITY_BIT_MASK.length; maskIndex++) 
		    {
		    	if ((key[i] & PARITY_BIT_MASK[maskIndex]) == PARITY_BIT_MASK[maskIndex]) 
		    		bitCount++;
		    }
		    
		    if ((bitCount & 0x01) == 1) 
				// Odd number of 1 bits in the top 7 bits. Set parity bit to 0
				key[i] = (byte)(key[i] & (byte)0xfe);
		    else 
				// Even number of 1 bits in the top 7 bits. Set parity bit to 1
				key[i] = (byte)(key[i] | 1);
		}
    }

    /**
     * Expands a 7-byte array into an 8-byte array that contains parity bits
     * The binary format of a cryptographic key is:
     *  
     *     (B1,B2,...,B7,P1,B8,...B14,P2,B15,...,B49,P7,B50,...,B56,P8)
     *      
     * where (B1,B2,...,B56) are the independent bits of a DES key and 
     * (PI,P2,...,P8) are reserved for parity bits computed on the preceding 
     * seven independent bits and set so that the parity of the octet is odd, 
     * i.e., there is an odd number of "1" bits in the octet.
     */
    private static byte[] addDesParity(byte[] input, int offset, int len) 
    {
		if (len != 7) 
		    throw new IllegalArgumentException("Invalid length of DES Key Value:" + len);

		byte[] raw = new byte[7];
		System.arraycopy(input, offset, raw, 0, len);

		byte[] result = new byte[8];
		BigInteger in = new BigInteger(raw);

		// Shift 7 bits each time into a byte
		for (int i=result.length-1; i>=0; i--) 
		{
		    result[i] = in.and(MASK).toByteArray()[0];
		    result[i] <<= 1;         // make room for parity bit
		    in = in.shiftRight(7);
		}
		setParityBit(result);
		return result;
    }
    
    /** 
     * Create parity-adjusted keys suitable for DES / DESede encryption. 
     * 
     * @param input A non-null byte array containing key material for 
     * DES / DESede.
     * @param desStrength A string specifying either a DES or a DESede key.
     *
     * @throws NoSuchAlgorithmException if the either the DES or DESede 
     * algorithms cannot be loaded by JCE.
     * @throws InvalidKeyException if an invalid array of bytes is used
     * as a key for DES or DESede.
     * @throws InvalidKeySpecException in an invalid parameter is passed
     * to either the DESKeySpec of the DESedeKeySpec constructors.
     * @throws NoSuchProviderException when provider cannot be found
     */
    public static SecretKey makeDesKeys(byte[] input, String desStrength)
		throws NoSuchAlgorithmException, InvalidKeyException, 
		InvalidKeySpecException, NoSuchProviderException 
	{
		// Generate first subkey using first 7 bytes
		byte[] subkey1 = addDesParity(input, 0, 7);
	
		KeySpec spec = null;
		SecretKeyFactory desFactory = SecretKeyFactory.getInstance(desStrength, "BC");
	
		if (desStrength.equals("des")) 
		    spec = new DESKeySpec(subkey1, 0);
		else 
		if (desStrength.equals("desede")) 
		{
		    // Generate second subkey using second 7 bytes
		    byte[] subkey2 = addDesParity(input, 7, 7);
	
		    // Construct 24-byte encryption-decryption-encryption sequence
		    byte[] ede = new byte[subkey1.length*2+subkey2.length];
		    System.arraycopy(subkey1, 0, ede, 0, subkey1.length);
		    System.arraycopy(subkey2, 0, ede, subkey1.length, subkey2.length);
		    System.arraycopy(subkey1, 0, ede, subkey1.length+subkey2.length, subkey1.length);
	
		    spec = new DESedeKeySpec(ede, 0);
		} 
		else
		    throw new IllegalArgumentException("Invalid DES strength:" + desStrength);
		
		return desFactory.generateSecret(spec);
    }

    public static byte[] computeHMACBlock(IoSession session, byte[] msg, boolean isClientToServer) 
		throws AuthException 
	{
		try 
		{
			String encoding = (String) session.getAttribute(AuthDigestMD5Command.ENCODING);
			
			if (session.getAttribute(CLIENT_INTEGRITY_KEY) == null)
				computeIntegrityKeys(session, encoding);
			
			byte[] ki = isClientToServer ? (byte[]) session
					.getAttribute(CLIENT_INTEGRITY_KEY) : (byte[]) session
					.getAttribute(SERVER_INTEGRITY_KEY);
			
			int seqNum = ((Integer) session.getAttribute(
					isClientToServer ? PEER_SEQUENCE_NUMBER : SEQUENCE_NUMBER))
					.intValue();
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(seqNum);
			bos.write(msg);
			byte[] hmac = AuthCramMD5Command.hmacMD5(bos.toByteArray(), ki)
					.getBytes(encoding);
			bos.close();
			
			return hmac;
		} 
		catch (Exception e) 
		{
			throw new AuthException(e.getMessage(), e);
		}
	}
    
    public static byte[] computeMACBlock(IoSession session, byte[] msg, boolean isClientToServer) 
    	throws AuthException 
    {
		byte[] hmac = computeHMACBlock(session, msg, isClientToServer);
		byte[] mac = new byte[16];
		ByteUtilities.intToNetworkByteOrder(1, mac, 10, 2);
		int seqNum = ((Integer) session.getAttribute(
				isClientToServer ? PEER_SEQUENCE_NUMBER : SEQUENCE_NUMBER))
				.intValue();
		System.arraycopy(hmac, 0, mac, 0, 10);
		ByteUtilities.intToNetworkByteOrder(seqNum, mac, 12, 4);
		
		if (!isClientToServer)
			session.setAttribute(SEQUENCE_NUMBER, new Integer(++seqNum));
		
		return mac;
    }

    public DataConsumer getDataConsumer(IoSession session)
    {
    	DataConsumer consumer = (DataConsumer) session.getAttribute(CONSUMER);
    	
    	synchronized (session)
    	{
	    	if (consumer == null)
	    	{
	    		String encoding = (String) session.getAttribute(AuthDigestMD5Command.ENCODING);
	    		IoFilterCodec codec ;
	    		
	    		if (session.containsAttribute(AuthDigestMD5Command.INTEGRITY_QOP))
	    			codec = new DigestMD5IntegrityIoFilterCodec(session);
	    		else
	    			codec = new DigestMD5PrivacyIoFilterCodec(session);
	    		
	    		consumer =	new TextLineConsumer(Charset.forName(encoding), DELIMITER, codec);
	
	    		// Define min. length to 18 i.e a 16 byte MAC block and the 2 byte delimiter
	    		((TextLineConsumer)consumer).setMinLineLength(18);
	    		
	    		session.setAttribute(CONSUMER, consumer);
	    	}
    	}    	
    	
    	return consumer;
    }
    
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) 
		throws Exception 
	{
        if (message instanceof ByteBuffer
        		&& (session.containsAttribute(AuthDigestMD5Command.INTEGRITY_QOP) ||
        				session.containsAttribute(AuthDigestMD5Command.PRIVACY_QOP)))
        	cumulateAndConsume(nextFilter, session, (ByteBuffer) message);
        else
        	nextFilter.messageReceived(session, message);
	}

	public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) 
		throws Exception 
	{
		Object message = writeRequest.getMessage();
		if (message instanceof ByteBuffer)			 
		{
			if (!((ByteBuffer) message).hasRemaining())
			{
				nextFilter.filterWrite(session, writeRequest);
				return;
			}
			
			boolean skipOnce = false;
			if (session.containsAttribute(DISABLE_FILTER_ONCE))
			{
				skipOnce = true;
				session.removeAttribute(DISABLE_FILTER_ONCE);
			}
			
			if (! skipOnce && (session.containsAttribute(AuthDigestMD5Command.INTEGRITY_QOP) ||
					session.containsAttribute(AuthDigestMD5Command.PRIVACY_QOP)))
				getDataConsumer(session).getCodec().wrap(nextFilter, writeRequest, (ByteBuffer) message);
			else
				nextFilter.filterWrite(session, writeRequest);
	    }		
        else
        	nextFilter.filterWrite(session, writeRequest);
	}
}