package test.junit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.sasl.SaslException;

import junit.framework.TestCase;

import org.bouncycastle.util.encoders.Base64;
import org.columba.ristretto.pop3.POP3Protocol;
import org.mailster.pop3.MailsterPop3Service;
import org.mailster.pop3.commands.auth.AuthCramMD5Command;
import org.mailster.pop3.commands.auth.AuthException;
import org.mailster.pop3.commands.auth.iofilter.AuthDigestMD5IoFilter;
import org.mailster.pop3.commands.auth.iofilter.AuthDigestMD5IoFilter.CIPHER;
import org.mailster.pop3.mailbox.UserManager;
import org.mailster.util.ByteUtilities;
import org.mailster.util.StringUtilities;
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
 * Pop3DigestMD5Test.java - Client that tests POP3 Digest-MD5 authentication 
 * mechanism.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class Pop3DigestMD5Test extends TestCase 
{
    private static final String HOST_NAME = "localhost";
    private static final String USER = "ted";
    private static final String PWD = UserManager.DEFAULT_PASSWORD;
    private static final int POP3_PORT = 110;
    private static final int CLIENT_MAXBUF = 2000;
    
    protected static final byte[] messageType = new byte[2];
    protected static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    
    static
    {
    	ByteUtilities.intToNetworkByteOrder(1, messageType, 0, 2);
    }
    
    private static MD5 md5 = new MD5();
    
    private BufferedReader input;
    private PrintWriter output;
    private MailsterPop3Service service;
    private Socket socket;
    
    private String encoding = "UTF-8";
    private byte[] A1;
    
    private boolean integrityModeEnabled;
    private boolean privacyModeEnabled;
    
    private byte[] kic;
    private byte[] kis;
	private Cipher encCipher;
	private Cipher decCipher;
    private int seqNum;
    private int peerSeqNum;
    
    public Pop3DigestMD5Test(String name) 
    {
        super(name);
    }

    protected void setUp() throws Exception 
    {
        super.setUp();
        service = new MailsterPop3Service();
        service.startService(false);
        socket = new Socket(HOST_NAME, POP3_PORT);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), false);
    }

    protected void tearDown() throws Exception 
    {
        super.tearDown();
        socket.close();
        service.stopService();
    }

    public void testPrivacyWith3Des() throws Exception 
    {
        assertConnect();
        sendAuthDigestMD5("auth-conf", CIPHER.DES3);
        sendNoop();
        sendCapa();        
        sendQuit();
    }

    public void testPrivacyWithDes() throws Exception 
    {
        assertConnect();
        sendAuthDigestMD5("auth-conf", CIPHER.DES);
        sendNoop();
        sendCapa();
        sendQuit();
    }

    public void testPrivacyWithRC4() throws Exception 
    {
        assertConnect();
        sendAuthDigestMD5("auth-conf", CIPHER.RC4);
        sendNoop();
        sendCapa();        
        sendQuit();
    }

    public void testPrivacyWithRC4_40() throws Exception 
    {
        assertConnect();
        sendAuthDigestMD5("auth-conf", CIPHER.RC4_40);
        sendNoop();
        sendCapa();        
        sendQuit();
    }

    public void testPrivacyWithRC4_56() throws Exception 
    {
        assertConnect();
        sendAuthDigestMD5("auth-conf", CIPHER.RC4_56);
        sendNoop();
        sendCapa();        
        sendQuit();
    }

    public void testIntegrity() throws Exception 
    {
        assertConnect();
        sendAuthDigestMD5("auth-int", null);
        sendNoop();
        sendCapa();        
        sendQuit();
    }
    
    public void testAuth() throws Exception 
    {
        assertConnect();
        sendAuthDigestMD5("auth", null);
        sendNoop();
        sendCapa();        
        sendQuit();
    }
    
    public void testDigestMD5AuthWithRistretto() throws Exception
    {
  	  POP3Protocol pop3 = new POP3Protocol("localhost");
  	  pop3.openPort();
  	  pop3.capa();
  	  pop3.auth("DIGEST-MD5", "joe", PWD.toCharArray());
  	  pop3.noop();
  	  pop3.capa();
  	  pop3.quit();
    }

    public void testCramMD5AuthWithRistretto() throws Exception
    {
  	  POP3Protocol pop3 = new POP3Protocol("localhost");
  	  pop3.openPort();
  	  pop3.capa();
  	  pop3.auth("CRAM-MD5", "ted", PWD.toCharArray());
  	  pop3.noop();
  	  pop3.capa();
  	  pop3.quit();
    }
    
    private void assertConnect() throws Exception 
    {
        String response = readLine();
        assertTrue(response, response.startsWith("+OK "));
    }
	
	private String computeResponseValue(HashMap<String, String> map, String pwd, String encoding, boolean isResponseAuth) 
		throws AuthException, UnsupportedEncodingException
	{
		// Build A1
		boolean useUTF8 = "utf8".equals(encoding);
		
	    StringBuilder sb = new StringBuilder();
	    sb.append(StringUtilities.stringTo_8859_1(map.get("username"), useUTF8)).append(':');
	    
	    String realm = StringUtilities.stringTo_8859_1(map.get("realm"), useUTF8); 
	    if (realm != null)
	        sb.append(realm);
	    
	    sb.append(':').append(StringUtilities.stringTo_8859_1(pwd, useUTF8));
	    
	    byte[] step1;
	    synchronized (md5)
	    {
	        md5.Init();
	        md5.Update(sb.toString());
	        step1 = md5.Final();
	    }
	    
	    sb = new StringBuilder();
	    sb.append(':').append(map.get("nonce"));
	    sb.append(':').append(map.get("cnonce"));
	    
	    String authzid = map.get("authzid");
	    if (authzid != null)
	        sb.append(':').append(authzid);
	
	    byte[] step2 = sb.toString().getBytes(encoding);
	    
	    A1 = new byte[step1.length+step2.length];
	    System.arraycopy(step1, 0, A1, 0, step1.length);
	    System.arraycopy(step2, 0, A1, step1.length, step2.length);
	    
	    // Build A2
	    sb = new StringBuilder();
		String qop = map.get("qop");
		if (!isResponseAuth)
            sb.append("AUTHENTICATE");
        
        sb.append(':');
        sb.append(map.get("digest-uri"));
		if (qop.equals("auth-int") || qop.equals("auth-conf"))
			sb.append(":00000000000000000000000000000000");
		
		String A2 = sb.toString();
		
	    sb = new StringBuilder();
		sb.append(map.get("nonce")).append(':');
		sb.append(map.get("nc")).append(':');
		sb.append(map.get("cnonce")).append(':');
		sb.append(map.get("qop")).append(':');
		
		synchronized (md5)
		{
			md5.Init();
			md5.Update(A2);					
			sb.append(md5.asHex());
			
			md5.Init();
			md5.Update(A1);
			String hexA1 = md5.asHex();
			
			md5.Init();
			md5.Update(hexA1);
			md5.Update(":");
			md5.Update(sb.toString());
			return md5.asHex();
		}
	}
	
    private void sendAuthDigestMD5(String selectedQop, CIPHER cipher) throws Exception
    {
    	println("AUTH DIGEST-MD5");
    	byte[] decoded = Base64.decode(readLine().substring(2));
    	HashMap<String, String> map = StringUtilities.parseDirectives(decoded);
    	StringBuilder sb = new StringBuilder();
    	sb.append("username=\"").append(USER).append("\",");
    	sb.append("nonce=\"").append(map.get("nonce")).append("\",");
    	sb.append("cnonce=\"").append("hjds54s4dJZI").append("\",");
    	sb.append("realm=\"").append(map.get("realm")).append("\",");
    	sb.append("charset=").append(encoding).append(",");
   		sb.append("qop=\"").append(selectedQop);
    	sb.append("\",");
    	sb.append("maxbuf=").append(CLIENT_MAXBUF);
    	
    	if (selectedQop.equals("auth-conf"))
    		sb.append(",cipher=").append(cipher);
    	
    	sb.append(",nc=00000001,");
    	sb.append("digest-uri=\"pop3/").append(map.get("realm")).append("\"");
    	HashMap<String, String> clientMap = StringUtilities.parseDirectives(sb.toString().getBytes(encoding));
    	sb.append(",response=").append(computeResponseValue(clientMap, PWD, encoding, false));
    	
    	println(new String(Base64.encode(sb.toString().getBytes(encoding)), encoding));
    	String response = new String(Base64.decode(readLine().substring(2).getBytes(encoding)), encoding);
    	
    	assertTrue(response, response.startsWith("rspauth"));
    	String computed = computeResponseValue(clientMap, PWD, encoding, true);
    	assertTrue(computed, computed.equals(response.substring(8)));
    	println("");
    	response = readLine();
    	assertTrue(response, response.startsWith("+OK "));
    	integrityModeEnabled = selectedQop.equals("auth-int");
    	privacyModeEnabled = selectedQop.equals("auth-conf");
    	
    	if (privacyModeEnabled)
    		computePrivacyKeys(cipher, encoding, true);
    }
    
    private void sendCapa() throws Exception 
    {
        println("CAPA");
        String response = readLine();        
        assertTrue(response, response.startsWith("+OK "));
        
        do
        {
        	response = readLine();
        }
        while(response == null || !".".equals(response));
    }

    private void sendNoop() throws Exception 
    {
        println("NOOP");
        String response = readLine();        
        assertTrue(response, response.startsWith("+OK "));
    }
    
    private void sendQuit() throws Exception 
    {
        println("QUIT");
        String response = readLine();
        assertTrue(response, response.startsWith("+OK "));
        integrityModeEnabled = false;
        privacyModeEnabled = false;
    }
    
	/**	
	 * Generates client-server and server-client keys to encrypt and
	 * decrypt messages. Also generates IVs for DES ciphers.	 
	 */
    private  void computePrivacyKeys(CIPHER negotiatedCipher, String encoding, boolean isClientToServer) 
    	throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, 
    	InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException 
    {
	    byte[] ccmagic = "Digest H(A1) to client-to-server sealing key magic constant".getBytes(encoding);
	    byte[] scmagic = "Digest H(A1) to server-to-client sealing key magic constant".getBytes(encoding);
	    
	    // Kcc = MD5{H(A1)[0..n], "Digest ... client-to-server"}
	    int n;
	    if (negotiatedCipher.equals(CIPHER.RC4_40))
			n = 5; // H(A1)[0..5]
		else if (negotiatedCipher.equals(CIPHER.RC4_56))
			n = 7; // H(A1)[0..7]
		else
			// des and 3des and rc4
			n = 16; // H(A1)[0..16]

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

		// Initialize cipher objects
		if (negotiatedCipher.equals(CIPHER.RC4) ||
			negotiatedCipher.equals(CIPHER.RC4_40) ||
			negotiatedCipher.equals(CIPHER.RC4_56)) 
		{
			encCipher = Cipher.getInstance("RC4", "BC");
			decCipher = Cipher.getInstance("RC4", "BC");

			encKey = new SecretKeySpec(myKc, "RC4");
			decKey = new SecretKeySpec(peerKc, "RC4");

			encCipher.init(Cipher.ENCRYPT_MODE, encKey);
			decCipher.init(Cipher.DECRYPT_MODE, decKey);

		} 
		else if ((negotiatedCipher.equals(CIPHER.DES))
				|| (negotiatedCipher.equals(CIPHER.DES3))) 
		{
			// DES or 3DES
			String cipherFullname, cipherShortname;

			// Use "NoPadding" when specifying cipher names
			// RFC 2831 already defines padding rules for producing
			// 8-byte aligned blocks
			if (negotiatedCipher.equals(CIPHER.DES)) 
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

			encKey = AuthDigestMD5IoFilter.makeDesKeys(myKc, cipherShortname);
			decKey = AuthDigestMD5IoFilter.makeDesKeys(peerKc, cipherShortname);

			// Set up the DES IV, which is the last 8 bytes of Kcc/Kcs
			IvParameterSpec encIv = new IvParameterSpec(myKc, 8, 8);
			IvParameterSpec decIv = new IvParameterSpec(peerKc, 8, 8);

			// Initialize cipher objects
			encCipher.init(Cipher.ENCRYPT_MODE, encKey, encIv);
			decCipher.init(Cipher.DECRYPT_MODE, decKey, decIv);
		}
	}
    
    private void computeIntegrityKeys() throws UnsupportedEncodingException
    {
        kic = new byte[16];
        kis = new byte[16];
        
        synchronized (md5)
        {
            md5.Init();
            md5.Update(A1);
            byte[] hA1 = md5.Final();
            
            md5.Init();
            md5.Update(hA1);
            md5.Update("Digest session key to client-to-server signing key magic constant".getBytes(encoding));
            kic = md5.Final();
            
            md5.Init();
            md5.Update(hA1);
            md5.Update("Digest session key to server-to-client signing key magic constant".getBytes(encoding));
            kis = md5.Final();
        }
        seqNum = 0;
    }
    
    private byte[] computeMACBlock(byte[] msg, boolean isClientToServer) 
        throws Exception
    {
    	int seq = isClientToServer ? seqNum : peerSeqNum;
    	if (kic == null)
    		computeIntegrityKeys();
    	
        byte[] ki = isClientToServer ? kic : kis;

        byte[] mac = new byte[16];
        ByteUtilities.intToNetworkByteOrder(1, mac, 10, 2);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(seq);
        bos.write(msg);
        byte[] hmac = AuthCramMD5Command.hmacMD5(bos.toByteArray(), ki).getBytes(encoding);
        bos.close();
        System.arraycopy(hmac, 0, mac, 0, 10);
        
        ByteUtilities.intToNetworkByteOrder(seq, mac, 12, 4);        
        if (isClientToServer)
        	seqNum++;
        else
        	peerSeqNum++;
        
        return mac;
    }
    
    private String readLine() throws Exception
    {
    	if (integrityModeEnabled || privacyModeEnabled)
    	{
    		InputStream in = socket.getInputStream();
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		int b = 0;
    		int last = 0;
    		while (true)
    		{
    			b = in.read();
    			if (b == -1)
    				throw new Exception("Session closed by peer");
    			if (last == '\r')
    			{
    				if (b == '\n')
    					break;
    				else
    					bos.write(last);
    			}    				
    			if (b !='\r')
    				bos.write(b);
    			last = b;
    		}
    		byte[] msg = bos.toByteArray();
    		bos.close();

	    	if (privacyModeEnabled)
	    	{    		
	    		int len = msg.length;
	    	    byte[] encryptedMsg = new byte[len - 6];
	    	    byte[] msgType = new byte[2];
	    	    byte[] seqNum = new byte[4];
	    	
	    	    // Get cipherMsg; msgType; sequenceNum
	    	    System.arraycopy(msg, 0, encryptedMsg, 0, encryptedMsg.length);
	    	    System.arraycopy(msg, encryptedMsg.length, msgType, 0, 2);
	    	    System.arraycopy(msg, encryptedMsg.length+2, seqNum, 0, 4);
	    	
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
	    	    	throw new SaslException("Illegal block sizes used with chosen cipher", e);
	    	    }
	    	
	    	    byte[] msgWithoutPadding = new byte[decryptedMsg.length - 10];
	    	    byte[] mac = new byte[10];
	    	    int msgLength = msgWithoutPadding.length;
	    		    
	    	    System.arraycopy(decryptedMsg, 0, msgWithoutPadding, 0, msgLength);
	    	    System.arraycopy(decryptedMsg, msgLength, mac, 0, 10);
	    	
	    	    int blockSize = decCipher.getBlockSize();
	    	    
	    	    if (blockSize > 1) 
	    	    {
	    			// get value of last octet of the byte array 
	    			msgLength -= (int)msgWithoutPadding[msgWithoutPadding.length - 1];
	    			if (msgLength < 0) 
	    			    //  Discard message and do not increment sequence number
	    				throw new SaslException("Decryption failed");
	    	    }
	    		
	    	    byte[] originalMsg = new byte[msgLength];
	    	    System.arraycopy(msgWithoutPadding, 0, originalMsg, 0, msgLength);
	    	        	    
	    	    // Re-calculate MAC to ensure integrity
	    	    byte[] expectedMac = computeMACBlock(originalMsg, false);
	    	    byte[] fullMac = new byte[16];
	    	    System.arraycopy(mac, 0, fullMac, 0, 10);
	    	    System.arraycopy(msgType, 0, fullMac, 10, 2);
	    	    System.arraycopy(seqNum, 0, fullMac, 12, 4);
	    	    
	    	    assertTrue(Arrays.equals(fullMac, expectedMac));
	    		return new String(originalMsg, encoding);
	    	}
	    	
	    	if (integrityModeEnabled)
	    	{
	            int size = msg.length-16;
	            if (size < 0)
	            	throw new SaslException("Corrupted MAC block");
	            
	            byte[] originalMessage = new byte[size];
	            System.arraycopy(msg, 0, originalMessage, 0, size);
	            
	            byte[] mac = new byte[16];
	            System.arraycopy(msg, size, mac, 0, 16);
	            byte[] computedMac = computeMACBlock(originalMessage, false);
	            assertTrue(Arrays.equals(mac, computedMac));
	            return new String(originalMessage, encoding);
	    	}
    	}
    	
    	return input.readLine();
    }
    
    private void println(String s) throws Exception
    {
    	if (privacyModeEnabled)
    	{
    		OutputStream os = socket.getOutputStream();
    		byte[] msg = s.getBytes(encoding);
    	
    	    // HMAC(Ki, {SeqNum, msg})[0..9]
    	    byte[] mac = computeMACBlock(msg, true);
    	
    	    // Calculate padding
    	    int bs = encCipher.getBlockSize();
    	    byte[] padding;

    	    if (bs > 1 ) 
    	    {
    			int pad = bs - ((msg.length + 10) % bs); // add 10 for HMAC[0..9]
    			padding = new byte[pad];
    			for (int i=0; i < pad; i++)
    			    padding[i] = (byte)pad;
    	    } 
    	    else
    	    	padding = EMPTY_BYTE_ARRAY;
    	
    	    byte[] toBeEncrypted = new byte[msg.length+padding.length+10];
    	
    	    // {msg, pad, HMAC(Ki, {SeqNum, msg}[0..9])}
    	    System.arraycopy(msg, 0, toBeEncrypted, 0, msg.length);
    	    System.arraycopy(padding, 0, toBeEncrypted, msg.length, padding.length);
    	    System.arraycopy(mac, 0, toBeEncrypted, msg.length+padding.length, 10);
    	
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
    	    	throw new SaslException("Invalid block size for cipher", e);
    	    }
    	    
    	    os.write(cipherBlock);
    	    os.write(mac, 10, 6); // messageType & sequenceNum	    
    	    os.write("\r\n".getBytes(encoding));
    		os.flush();   		
    	}
    	else
    	if (integrityModeEnabled)
    	{
    		OutputStream os = socket.getOutputStream();
    		byte[] mac = computeMACBlock(s.getBytes(encoding), true);
    		os.write(s.getBytes(encoding));
    		os.write(mac);
    		os.write("\r\n".getBytes(encoding));
    		os.flush();
    	}
    	else
    	{
    		output.print(s+"\r\n");
    		output.flush();
    	}
    }
}
