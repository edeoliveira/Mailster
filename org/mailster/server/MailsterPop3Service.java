package org.mailster.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoAcceptorConfig;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.pop3.Pop3ProtocolHandler;
import org.mailster.pop3.mailbox.MailBox;
import org.mailster.pop3.mailbox.Pop3User;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.pop3.mailbox.UserManager;
import org.mailster.smtp.SmtpMessage;
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
    implements Pop3Service
{
    /**
     * This is the official POP3 port number.
     */
    public static final int POP3_PORT = 110;
    
    private static final Logger log = LoggerFactory.getLogger(MailsterPop3Service.class);
    
    private SocketAcceptor acceptor;
    private ExecutorService executor;
    private ExecutorService acceptorThreadPool;
    private InetSocketAddress iSocketAddr;
    private IoAcceptorConfig config;
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
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

        acceptorThreadPool = Executors.newCachedThreadPool(
        		ThreadFactoryUtilitity.createFactory("POP3 SocketAcceptor Thread")); 
        acceptor = new SocketAcceptor(
        		Runtime.getRuntime().availableProcessors() + 1, acceptorThreadPool);
        config = new SocketAcceptorConfig();
        config.setThreadModel(ThreadModel.MANUAL);
        ((SocketAcceptorConfig) config).setReuseAddress(true);
        DefaultIoFilterChainBuilder chain = config.getFilterChain();

        chain.addLast("codec", new ProtocolCodecFilter(
                new InetTextCodecFactory(MailsterConstants.DEFAULT_CHARSET)));

		executor = Executors.newCachedThreadPool(
				ThreadFactoryUtilitity.createFactory("POP3 Thread"));
		chain.addLast("threadPool", new ExecutorFilter(executor));
		
        handler = new Pop3ProtocolHandler(userManager);
    }
    
    public void startService(boolean debug) throws IOException
    {
    	this.debugEnabled = debug;
        if (debugEnabled)
        	config.getFilterChain().addBefore("codec", "logger", new LoggingFilter());

        if (host == null)
            iSocketAddr = new InetSocketAddress(port);
        else
            iSocketAddr = new InetSocketAddress(InetAddress.getByName(host), port);
        
        acceptor.bind(iSocketAddr, handler, config);        
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
    		config.getFilterChain().remove("logger");
    	
        acceptor.unbindAll();
        executor.shutdown();
        acceptorThreadPool.shutdown();
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
    	// Note that we only delete the messages from the special mailbox as
    	// other mailboxes are only accessed by POP3.
    	userManager.getMailBoxManager().removeAllMessagesFromSpecialAccount();
    }    
    
    public void removeMessage(StoredSmtpMessage msg)
    {
    	// Note that we only delete the message from the special mailbox as
    	// other mailboxes are only accessed by POP3.
    	userManager.getMailBoxManager().removeMessageFromSpecialAccount(msg);
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
        return acceptor.isManaged(iSocketAddr) ? new Integer(iSocketAddr.getPort()) : null;
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