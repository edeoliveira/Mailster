package org.mailster.core.pop3.commands.auth;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

import org.apache.mina.core.session.IoSession;
import org.bouncycastle.util.encoders.Base64;
import org.mailster.core.pop3.commands.Pop3CommandState;
import org.mailster.core.pop3.commands.auth.iofilter.AuthDigestMD5IoFilter;
import org.mailster.core.pop3.commands.auth.iofilter.AuthDigestMD5IoFilter.CIPHER;
import org.mailster.core.pop3.connection.AbstractPop3Connection;
import org.mailster.core.pop3.connection.AbstractPop3Handler;
import org.mailster.core.pop3.connection.MinaPop3Connection;
import org.mailster.core.pop3.connection.Pop3State;
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
 * AuthDigestMD5Command.java - A class supporting the POP3 AUTH DIGEST-MD5 command 
 * (see RFC 2831).
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.10 $, $Date: 2009/02/03 00:29:30 $
 */
public class AuthDigestMD5Command extends AuthAlgorithmCommand 
{    
    private final static int DEFAULT_MAXBUF = 65536;
    
	// Session attributes
    private final static String DIRECTIVES_MAP 			= AuthDigestMD5Command.class.getName()+".directivesMap";
    public final static String ENCODING 							= AuthDigestMD5Command.class.getName()+".encoding";
    public final static String A1 										= AuthDigestMD5Command.class.getName()+".A1";
    public final static String INTEGRITY_QOP 				= AuthDigestMD5Command.class.getName()+".integrityQop";
    public final static String PRIVACY_QOP 					= AuthDigestMD5Command.class.getName()+".privacyQop";
    public final static String CLIENT_MAXBUF 				= AuthDigestMD5Command.class.getName()+".clientMaxBuf";
    public final static String NEGOCIATED_CIPHER 		= AuthDigestMD5Command.class.getName()+".negociatedCipher";

    // States
    private final static int GET_CLIENT_ACK_STATE 												= 2;
    private final static int GET_CLIENT_ACK_WITH_INTEGRITY_QOP_STATE 	= 3;
    private final static int GET_CLIENT_ACK_WITH_PRIVACY_QOP_STATE		= 4;
    
    private static SecureRandom rnd;
    
    static
    {
        // Disable native library loading
        MD5.initNativeLibrary(true);
        
        // Initialize secure random generator 
        try 
        {
			rnd = SecureRandom.getInstance("SHA1PRNG");
		} 
        catch (NoSuchAlgorithmException e) 
        {
			throw new RuntimeException(e);
		}
    }
    
    private static MD5 md5 = new MD5();
    
    private final static String[] SUPPORTED_QOPS = new String[] {"auth", "auth-int", "auth-conf"};
    
	private String getDirectiveValue(HashMap<String, String> directivesMap, 
			String directive, boolean mandatory) 
		throws AuthException
	{
		String value = directivesMap.get(directive);
		if (mandatory && value == null)
			throw new AuthException("\""+directive+"\" mandatory directive is missing");
		
		return value;
	}
	
    private void computeA1(IoSession session, HashMap<String, String> map, String pwd, String encoding) 
    	throws AuthException, UnsupportedEncodingException
    {
        // Build A1
    	boolean useUTF8 = "utf8".equals(encoding);
    	
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtilities.stringTo_8859_1(getDirectiveValue(map, "username", true), useUTF8)).append(':');
        
        String realm = StringUtilities.stringTo_8859_1(getDirectiveValue(map, "realm", false), useUTF8); 
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
        sb.append(':').append(getDirectiveValue(map, "nonce", true));
        sb.append(':').append(getDirectiveValue(map, "cnonce", true));
        
        String authzid = getDirectiveValue(map, "authzid", false);
        if (authzid != null)
            sb.append(':').append(authzid);

        byte[] step2 = sb.toString().getBytes(encoding);
        
        byte[] a1 = new byte[step1.length+step2.length];
        System.arraycopy(step1, 0, a1, 0, step1.length);
        System.arraycopy(step2, 0, a1, step1.length, step2.length);
        
        session.setAttribute(A1, a1);
    }
    
	private String computeResponseValue(IoSession session, HashMap<String, String> map, boolean isResponseAuth) 
		throws AuthException
	{
	    // Build A2
        StringBuilder sb = new StringBuilder();
		String qop = getDirectiveValue(map, "qop", true);
		if (!isResponseAuth)
            sb.append("AUTHENTICATE");
        
        sb.append(':');        
		sb.append(getDirectiveValue(map, "digest-uri", true));
		if ("auth-int".equals(qop) || "auth-conf".equals(qop))
			sb.append(":00000000000000000000000000000000");
		
		String A2 = sb.toString();
		
        sb = new StringBuilder();
		sb.append(getDirectiveValue(map, "nonce", true)).append(':');
		sb.append(getDirectiveValue(map, "nc", true)).append(':');
		sb.append(getDirectiveValue(map, "cnonce", true)).append(':');
		sb.append(getDirectiveValue(map, "qop", true)).append(':');
		
		synchronized (md5)
		{
			md5.Init();
			md5.Update(A2);					
			sb.append(md5.asHex());
			
			md5.Init();
			md5.Update((byte[]) session.getAttribute(A1));
			String hexA1 = md5.asHex();
			
			md5.Init();
			md5.Update(hexA1);
			md5.Update(":");
			md5.Update(sb.toString());
			return md5.asHex();
		}
	}
	
	public boolean isSecuredAuthenticationMethod()
	{
		return true;
	}
	
    public Pop3CommandState challengeClient(AbstractPop3Handler handler, 
														            AbstractPop3Connection conn, 
														            String cmd) 
		throws Exception
	{
        byte[] nonce = new byte[20];
        rnd.nextBytes(nonce);
        String realm = InetAddress.getLocalHost().getCanonicalHostName();
        String serverNonce = new String(Base64.encode(nonce), "utf8");
        
        StringBuilder sb = new StringBuilder();
        sb.append("realm=\"").append(realm);
        sb.append("\",nonce=\"");
        sb.append(serverNonce);
        sb.append("\",qop=\"auth, auth-int");
        
        boolean foundAvailableCipher = false;
        for (CIPHER cipher : CIPHER.values())
        {
        	if (CIPHER.isSupported(cipher))
        	{
        		if (foundAvailableCipher)
        			sb.append(", ");
        		else
        		{
        			sb.append(", auth-conf\",cipher=\"");
        			foundAvailableCipher=true;
        		}
        		sb.append(cipher.toString());        		
        	}
        }
       	sb.append("\",");        
        sb.append("algorithm=md5-sess,charset=utf-8");

        HashMap<String, Object> sessionMap = new HashMap<String, Object>();
        sessionMap.put("realm", realm);
        sessionMap.put("nonce", serverNonce);
        sessionMap.put("qop", SUPPORTED_QOPS);
        
        IoSession session = ((MinaPop3Connection) conn).getSession();
        session.setAttribute(DIRECTIVES_MAP, sessionMap);
        
        byte[] challengeBytes = sb.toString().getBytes("utf8");
        if (challengeBytes.length > 2048)
            throw new AuthException("Server response size exceeds 2048 bytes");

        String challenge = new String(Base64.encode(challengeBytes), "utf8");
        
		conn.println("+ "+challenge.replaceAll("\n","").toString());
		
		return new Pop3CommandState(this, CHECK_RESPONSE_TO_CHALLENGE_STATE);
	}
    
    @SuppressWarnings("unchecked")
	public Pop3CommandState checkClientResponse(AbstractPop3Handler handler,
												AbstractPop3Connection conn, 
												String cmd) 
    	throws Exception 
	{
    	String decoded = new String(Base64.decode(cmd.getBytes("utf8")), "utf8");
    	if (decoded.getBytes("utf8").length > 4096)
			throw new AuthException("Digest-response size exceeds 4096 bytes");
    	
    	HashMap<String, String> map = StringUtilities.parseDirectives(decoded.getBytes("utf8"));
        
        if (!getDirectiveValue(map, "nc", true).equals("00000001"))
            throw new AuthException("Nonce-count value is wrong");
        
        IoSession session = ((MinaPop3Connection) conn).getSession();
        
        HashMap<String, String> sessionMap = (HashMap<String, String>)
           	session.getAttribute(DIRECTIVES_MAP);
        
        if (!getDirectiveValue(sessionMap, "realm", true).equals(getDirectiveValue(map, "realm", true)) ||
            !getDirectiveValue(sessionMap, "nonce", true).equals(getDirectiveValue(map, "nonce", true)))                    
            throw new AuthException("Negociation failed");
        
        HashMap<String, String[]> sMap = (HashMap<String, String[]>)
       		session.getAttribute(DIRECTIVES_MAP);
        String qop = getDirectiveValue(map, "qop", true);
        boolean matched = false;
        for(String s : sMap.get("qop"))
        {
        	if (s.equals(qop))
        	{
        		matched = true;
        		break;
        	}
        }
        
        if (!matched)
            throw new AuthException("Unsupported qop");
        
        if (qop.equals("auth-conf"))
        	session.setAttribute(NEGOCIATED_CIPHER, 
        			CIPHER.getByName(getDirectiveValue(map, "cipher", true)));
        
        Pop3State state = conn.getState();
        state.setUser(state.getUser(getDirectiveValue(map, "username", true)));
        String charset = getDirectiveValue(map, "charset", false);
        charset = charset == null || !"utf8".equals(charset) ? "8859_1" : "utf8";
        session.setAttribute(ENCODING, charset);
        
        String maxbuf = getDirectiveValue(map, "maxbuf", false);
        int clientMaxBufSize = maxbuf == null ? DEFAULT_MAXBUF : Integer.parseInt(maxbuf);
        if (clientMaxBufSize<16 || clientMaxBufSize>DEFAULT_MAXBUF)
        	throw new AuthException("Wrong maxbuf value");
        else
        	session.setAttribute(CLIENT_MAXBUF, clientMaxBufSize);
        
		String response = getDirectiveValue(map, "response", true);		
		computeA1(session, map, conn.getState().getUser().getPassword(), charset);
        
		if (!response.equals(computeResponseValue(session, map, false)))
			throw new AuthException("Response check failed");
        else
        {
        	String responseAuth = "rspauth=" + computeResponseValue(session, map, true);
            conn.println("+ "+new String(Base64.encode(responseAuth.getBytes(charset)), charset));

            if (qop.equals("auth-int"))
            	return new Pop3CommandState(this, GET_CLIENT_ACK_WITH_INTEGRITY_QOP_STATE);
            else
        	if (qop.equals("auth-conf"))
            	return new Pop3CommandState(this, GET_CLIENT_ACK_WITH_PRIVACY_QOP_STATE);
        }

		return new Pop3CommandState(this, GET_CLIENT_ACK_STATE);
	}
    
	public Pop3CommandState execute(AbstractPop3Handler handler,
														AbstractPop3Connection conn, 
														String cmd, 
														Pop3CommandState state) 
	{
		try
		{
			if (state == null || state.isInitialState())
				return challengeClient(handler, conn, cmd);
			else
			{
				if (cmd.equals("*"))
				{
					conn.println("-ERR AUTH command aborted by client");
					return null;
				}
			
				if (state.getNextState() == CHECK_RESPONSE_TO_CHALLENGE_STATE)
					return checkClientResponse(handler, conn, cmd);
				else
				if (state.getNextState() >= GET_CLIENT_ACK_STATE && "".equals(cmd))
				{
		            IoSession session = ((MinaPop3Connection) conn).getSession();		            
		            session.getFilterChain().addBefore("codec", "digestMD5Filter", new AuthDigestMD5IoFilter());
		            
					if (state.getNextState() == GET_CLIENT_ACK_WITH_INTEGRITY_QOP_STATE)
						session.setAttribute(INTEGRITY_QOP);
					else
					if (state.getNextState() == GET_CLIENT_ACK_WITH_PRIVACY_QOP_STATE)
						session.setAttribute(PRIVACY_QOP);
					
					session.setAttribute(AuthDigestMD5IoFilter.DISABLE_FILTER_ONCE);					
					tryLockingMailbox(conn);
					return null;
				}
				else
					conn.println("-ERR bad internal state");
			}
		}
		catch (Exception ex)
		{
			conn.println("-ERR " + ex.getMessage());
		}

		return null;
	}
}
