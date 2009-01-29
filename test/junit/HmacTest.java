package test.junit;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import junit.framework.TestCase;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.mailster.pop3.commands.auth.AuthCramMD5Command;
import org.mailster.pop3.commands.auth.AuthCramSHA1Command;
import org.mailster.util.ByteUtilities;

public class HmacTest extends TestCase 
{
	protected void setUp() throws Exception 
	{
		super.setUp();
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public void testHmacMD5() 
		throws Exception 
	{
		byte[] key = new byte[16];
		Arrays.fill(key, (byte) 0x0b);
		String hmac = AuthCramMD5Command.hmacMD5("Hi There".getBytes(), key);		
		assertEquals("9294727a3638bb1c13f48ef8158bfc9d", hmac);

		hmac = AuthCramMD5Command.hmacMD5("what do ya want for nothing?".getBytes(), "Jefe".getBytes());
		assertEquals("750c783e6ab0b503eaa86e310a5db738", hmac);
				
		byte[] text = new byte[50];
		Arrays.fill(key, (byte) 0xaa);
		Arrays.fill(text, (byte) 0xdd);
		hmac = AuthCramMD5Command.hmacMD5(text, key);
		assertEquals("56be34521d144c88dbb8c733f0e8b3f6", hmac);
	}
	
	public void testCramMD5PerRFC2195() 
		throws Exception 
	{
		String key = "tanstaaftanstaaf";
		String banner = "PDE4OTYuNjk3MTcwOTUyQHBvc3RvZmZpY2UucmVzdG9uLm1jaS5uZXQ+";
		
		String hmac = AuthCramMD5Command.hmacMD5(Base64.decode(banner), key.getBytes());
		
		String result = new String(Base64.encode(("tim "+hmac).getBytes()));
		assertEquals("dGltIGI5MTNhNjAyYzdlZGE3YTQ5NWI0ZTZlNzMzNGQzODkw", result);
	}
	
	public void testHmacSHA1() 
		throws Exception 
	{
		String key = "Jefe";
		String banner = "what do ya want for nothing?";
		
		String hmac = AuthCramSHA1Command.hmacSHA1(banner.getBytes(), key.getBytes());
		
		assertEquals("effcdf6ae5eb2fa2d27416d5f184df9c259a7c79", hmac);								
	}	
    
    public void testIntToByteConversions() 
        throws Exception 
    {
        SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
        int i = rnd.nextInt();
        System.out.println("i="+i);
        
        byte[] buf = new byte[4];
        buf[0]=(byte)((i & 0xff000000)>>>24);
        buf[1]=(byte)((i & 0x00ff0000)>>>16);
        buf[2]=(byte)((i & 0x0000ff00)>>>8);
        buf[3]=(byte)((i & 0x000000ff));
        System.out.println("buf="+Arrays.toString(buf));
        
        byte[] buf2 = new byte[4];
        ByteUtilities.intToNetworkByteOrder(i, buf2, 0, 4);
        System.out.println("buf2="+Arrays.toString(buf2));
        
        assertEquals(ByteUtilities.networkByteOrderToInt(buf2, 0, 4), i);
        assertTrue(Arrays.equals(buf, buf2));
        
        int x = ((buf[0] & 0xFF) << 24)
                | ((buf[1] & 0xFF) << 16)
                | ((buf[2] & 0xFF) << 8)
                | (buf[3] & 0xFF);
        
        assertEquals(i, x);                             
    }
}
