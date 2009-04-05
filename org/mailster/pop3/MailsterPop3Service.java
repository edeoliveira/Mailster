package org.mailster.pop3;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.message.SmtpMessage;
import org.mailster.pop3.mailbox.MailBox;
import org.mailster.pop3.mailbox.Pop3User;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.pop3.mailbox.UserManager;
import org.mailster.server.MailsterConstants;
import org.mailster.util.StringUtilities;
import org.mailster.util.ThreadFactoryUtilitity;
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
 * MailsterPop3Service.java - The POP3 service controller.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class MailsterPop3Service
{
    /**
     * This is the official POP3 port number.
     */
    public static final int POP3_PORT = 110;
    
    private static final Logger log = 
    	LoggerFactory.getLogger(MailsterPop3Service.class);
    
    private SocketAcceptor acceptor;
    private ExecutorService executor;
    private InetSocketAddress iSocketAddr;
    private SocketSessionConfig config;
    private Pop3ProtocolHandler handler;
    
    private UserManager userManager = new UserManager();    

    private String host;
    private int port = POP3_PORT;
    
    private boolean debugEnabled;
    
    public MailsterPop3Service() throws Exception
    {        
        initService();
    }
    
    private void initService() throws Exception
    {
        IoBuffer.setUseDirectBuffer(false);

        acceptor = new NioSocketAcceptor(
        		Runtime.getRuntime().availableProcessors() + 1);
        config = acceptor.getSessionConfig();
        config.setReuseAddress(true);
        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();

        chain.addLast("codec", new ProtocolCodecFilter(
        		new TextLineCodecFactory(
                		MailsterConstants.DEFAULT_CHARSET, 
                		LineDelimiter.CRLF, 
                		LineDelimiter.CRLF)));

		executor = Executors.newCachedThreadPool(
				ThreadFactoryUtilitity.createFactory("POP3 Thread"));
		chain.addLast("threadPool", new ExecutorFilter(executor));
		
        handler = new Pop3ProtocolHandler(userManager);
    }
    
    public void startService(boolean debug) throws IOException
    {
    	this.debugEnabled = debug;
        if (debugEnabled)
        	acceptor.getFilterChain().
        		addBefore("codec", "logger", new LoggingFilter());

        if (host == null)
            iSocketAddr = new InetSocketAddress(port);
        else
            iSocketAddr = new InetSocketAddress(InetAddress.getByName(host), port);
        
		acceptor.setHandler(handler);
        acceptor.bind(iSocketAddr);        
    }
    
    public String getOutputDirectory()
    {
        return ConfigurationManager.CONFIG_STORE.
            getString(ConfigurationManager.DEFAULT_ENCLOSURES_DIRECTORY_KEY);
    }
    
    public void setUsingAPOPAuthMethod(boolean usingAPOPAuthMethod)
    {
        handler.setUsingAPOPAuthMethod(usingAPOPAuthMethod);
    }

    public void setSecureAuthRequired(boolean secureAuthRequired)
    {
        handler.setSecureAuthRequired(secureAuthRequired);
    }
    
    public void stopService() throws IOException
    {
    	if (debugEnabled)
    		acceptor.getFilterChain().remove("logger");
    	
    	try { 
			acceptor.unbind(); 
		} catch (Exception e) { e.printStackTrace(); }
		
		try { 
			executor.shutdown(); 
		} catch (Exception e) { e.printStackTrace(); }
    }

    public void shutdownService() throws IOException
    {
        try
        {
            stopService();
        }
        catch (IOException e)
        {
        	throw e;
        }
        finally
        {
            executor.shutdown();
        }
    }

    public void removeAllMessages()
    {
    	userManager.getMailBoxManager().removeAllMessages();
    }    
    
    public void removeMessage(StoredSmtpMessage msg)
    {
    	userManager.getMailBoxManager().removeMessage(msg);
    }
    
    public StoredSmtpMessage storeMessage(SmtpMessage msg)
    {
        for (String recipient : msg.getRecipients())
        {
        	if (recipient.indexOf('<') > -1)
        		recipient = recipient.substring(recipient.indexOf('<') + 1, recipient.indexOf('>'));
        	
            log.debug("Storing new message in mailbox of recipient <{}>", recipient);
            Pop3User user = userManager.getUserByEmail(recipient);
            MailBox mbox = userManager.getMailBoxManager().getMailBoxByUser(user);
            if (mbox != null)
            	mbox.storeMessage(msg);
        }
        
        return userManager.getMailBoxManager().addMessageToSpecialAccount(msg);
    }

    public UserManager getUserManager()
    {
        return userManager;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public int getPort()
    {
        return port;
    }

    /**
     * Returns the port on which the server is listening ONLY if acceptor
     * is currently bound otherwise it returns null.
     * 
     * @return the listening port
     */
    public Integer getListeningPort()
    {
        return acceptor.isActive() ? 
        		new Integer(iSocketAddr.getPort()) : null;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = StringUtilities.isEmpty(host) ? null : host;
    }
}