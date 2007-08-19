package org.mailster.pop3;

import java.util.StringTokenizer;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.filter.SSLFilter.SSLFilterMessage;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.mailster.pop3.commands.ApopCommand;
import org.mailster.pop3.commands.MultiStatePop3Command;
import org.mailster.pop3.commands.Pop3Command;
import org.mailster.pop3.commands.Pop3CommandRegistry;
import org.mailster.pop3.commands.Pop3CommandState;
import org.mailster.pop3.connection.AbstractPop3Connection;
import org.mailster.pop3.connection.AbstractPop3Handler;
import org.mailster.pop3.connection.MinaPop3Connection;
import org.mailster.pop3.mailbox.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ---<br>
 * Mailster (C) 2007 De Oliveira Edouard
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
 * Pop3ProtocolHandler.java - Handles POP3 protocol communications.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class Pop3ProtocolHandler extends IoHandlerAdapter 
    implements AbstractPop3Handler
{
    // Session objects
    protected final static String CONNECTION = Pop3ProtocolHandler.class.getName()+".connection";
    private final static String PEER_IS_CLOSING_SESSION = Pop3ProtocolHandler.class.getName()+".peerIsClosingSession"; 
    private final static String USING_APOP_AUTH = Pop3ProtocolHandler.class.getName()+".usingApopAuth";
    private final static String SECURED_AUTH_REQUIRED = Pop3ProtocolHandler.class.getName()+".securedAuthRequired";
    private final static String LAST_INCOMPLETE_COMMAND = Pop3ProtocolHandler.class.getName()+".lastIncompleteCommand";
    
    // Default 10 Minutes Timeout by RFC recommendation (see RFC 1939);
    public final static int DEFAULT_TIMEOUT_SECONDS = 600;
    
    private static int timeout = DEFAULT_TIMEOUT_SECONDS;
    
    private static final Logger log = LoggerFactory.getLogger(Pop3ProtocolHandler.class);

    private UserManager userManager;
    
    // Defaults to maximum security.
    private boolean usingAPOPAuthMethod = true;
    private boolean secureAuthRequired = true;

    public Pop3ProtocolHandler(UserManager userManager)
    {
        super();
        this.userManager = userManager;
    }

    public void sessionCreated(IoSession session)
    {
        if (session.getTransportType() == TransportType.SOCKET)
        {
            SocketSessionConfig cfg = (SocketSessionConfig) session.getConfig();
            cfg.setReceiveBufferSize(1024);
            cfg.setTcpNoDelay(true);
        }

        session.setIdleTime(IdleStatus.READER_IDLE, timeout);

        // We're going to use SSL negotiation notification.
        session.setAttribute(SSLFilter.USE_NOTIFICATION);

        // Initialize POP3 protocol session attributes
        MinaPop3Connection conn = new MinaPop3Connection(session, userManager);
        session.setAttribute(CONNECTION, conn);        
        session.setAttribute(USING_APOP_AUTH, usingAPOPAuthMethod);
        session.setAttribute(SECURED_AUTH_REQUIRED, secureAuthRequired);
        session.setAttribute(PEER_IS_CLOSING_SESSION, Boolean.FALSE);        
        sendGreetings(conn);
    }

    public void sessionClosed(IoSession session) throws Exception
    {
        log.info("Session closed by peer");
        MinaPop3Connection conn = (MinaPop3Connection) session.getAttribute(CONNECTION);
        if (conn != null && conn.getState().isAuthenticated())
            conn.getState().getMailBox().releaseLock();
    }
    
    private void sendGreetings(MinaPop3Connection conn)
    {
        if (isUsingAPOPAuthMethod(conn))
        {
            conn.getState()
                    .setGeneratedAPOPBanner(ApopCommand
                            .generateBannerTimestamp());
            conn.println("+OK Mailster POP3 Server ready "
                    + conn.getState().getGeneratedAPOPBanner());
        }
        else
            conn.println("+OK Mailster POP3 Server ready ");
    }

    public void sessionIdle(IoSession session, IdleStatus status)
    {
        log.info("Session timed out");
        MinaPop3Connection conn = (MinaPop3Connection) session.getAttribute(CONNECTION);
        conn.println("421 Service shutting down and closing transmission channel");
        if (conn.getState().isAuthenticated())
            conn.getState().getMailBox().releaseLock();
        session.close();
    }

    public void exceptionCaught(IoSession session, Throwable cause)
    {
        log.error("Exception occured : {}", cause.getMessage());
        MinaPop3Connection conn = (MinaPop3Connection) session.getAttribute(CONNECTION);
        if (conn != null && conn.getState().isAuthenticated())
            conn.getState().getMailBox().releaseLock();
        session.close();
    }

    public void messageReceived(IoSession session, Object message)
            throws Exception
    {
        if (((Boolean)session.getAttribute(PEER_IS_CLOSING_SESSION)).booleanValue())
            session.close();
        
    	if (message instanceof SSLFilterMessage)
    	{
    		log.debug("SSL filter message -> {}", message);
    		return;
    	}
    	
        String request = (String) message;
        log.info("C: {}", request);
        
        if (request == null)
        {
            session.close();
            return;
        }
        
        MinaPop3Connection conn = (MinaPop3Connection) session.getAttribute(CONNECTION);        
        Pop3CommandState state = (Pop3CommandState) session.getAttribute(LAST_INCOMPLETE_COMMAND);
        Pop3Command command = null;
        
        if (state == null)
        {
	        String commandName = new StringTokenizer(request, " ").nextToken().toUpperCase();
	        command = Pop3CommandRegistry.getCommand(commandName);
	        
	        if (command == null)
	        {
	            conn.println("-ERR Command not recognized");
	            return;
	        }
	
	        if (!command.isValidForState(conn.getState()))
	        {
	            conn.println("-ERR Command not valid for this state");
	            return;
	        }
        }
        else
        	command = state.getCommand();
        
        if (command instanceof MultiStatePop3Command)
        {
            state = ((MultiStatePop3Command)command).execute(this, conn, request, state);
            session.setAttribute(LAST_INCOMPLETE_COMMAND, state);
        }
        else
        {
        	command.execute(this, conn, request);
        	session.setAttribute(LAST_INCOMPLETE_COMMAND, null);
        }
    }

    public boolean isUsingAPOPAuthMethod(AbstractPop3Connection conn)
    {
        MinaPop3Connection c = (MinaPop3Connection) conn;
        return ((Boolean) c.getSession().getAttribute(USING_APOP_AUTH)).booleanValue();
    }

	public boolean isSecureAuthRequired(AbstractPop3Connection conn) 
	{
		MinaPop3Connection c = (MinaPop3Connection) conn;
        return ((Boolean) c.getSession().getAttribute(SECURED_AUTH_REQUIRED)).booleanValue();
	}
	
    public void setUsingAPOPAuthMethod(boolean usingAPOPAuthMethod)
    {
        this.usingAPOPAuthMethod = usingAPOPAuthMethod;
    }
    
    public void setSecureAuthRequired(boolean secureAuthRequired)
    {
        this.secureAuthRequired = secureAuthRequired;
    }
    
    public void quit(AbstractPop3Connection conn)
    {
        MinaPop3Connection c = (MinaPop3Connection) conn;
        c.getSession().setAttribute(PEER_IS_CLOSING_SESSION, Boolean.TRUE);
    }

    /**
     * Get the reader timeout in seconds.
     * 
     * @return the timeout value
     */
    public static int getTimeout()
    {
        return timeout;
    }

    /**
     * Set the reader timeout for the new sessions.
     * 
     * @param timeout timeout in seconds
     */
    public static void setTimeout(int timeout)
    {
        Pop3ProtocolHandler.timeout = timeout;
    }
}
