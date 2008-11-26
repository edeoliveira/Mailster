package org.mailster.subethasmtp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.mailster.crypto.SSLFilterFactory;
import org.mailster.crypto.X509SecureSocketFactory.SSLProtocol;
import org.mailster.server.MailsterConstants;
import org.mailster.smtp.SmtpMessage;
import org.mailster.smtp.events.SMTPServerEvent;
import org.mailster.smtp.events.SMTPServerListener;
import org.mailster.util.StringUtilities;
import org.subethamail.smtp.MessageListener;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.command.StartTLSCommand;
import org.subethamail.smtp.server.MessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

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
 * SimpleSmtpServer.java - The smtp server based on SubEthaSMTP.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */

public class SubEthaSmtpServer implements MessageListener
{
	/**
	 * The default timeout.
	 */
	public final static int DEFAULT_TIMEOUT = 300000;

    /**
     * Defaut SMTP host is null to listen on all subnets. If a hostname is
     * specified, then server socket will ONLY listen on this particular ip
     * address this is not the preferred default behaviour.
     * 
     * @see java.net.InetSocketAddress
     */
    public static final String DEFAULT_SMTP_HOST = null;

    /**
     * Default SMTP port is 25.
     */
    public static final int DEFAULT_SMTP_PORT = 25;
    
    /*** Vars ***/
	
	/**
	 * The timeout before closing connection. Set to 5 minutes.
	 */
	private int connectionTimeout = DEFAULT_TIMEOUT;    
    
    /**
     * Output client/server commands for debugging. Off by default.
     */
    private boolean debug = false;
    
    /**
     * Port the server listens on - set to the default SMTP port initially.
     */
    private int port = DEFAULT_SMTP_PORT;

    /**
     * Hostname the server listens on.
     */
    private String hostName = DEFAULT_SMTP_HOST;
    
    /**
     * The SubethaSmtp server.
     */
    private SMTPServer server;

    /**
     * Task event dispatched when an email has been received.
     */    
    class EmailReceivedTask implements Runnable {
    	private SmtpMessage msg;
    	
        public void run()
        {
            SMTPServerEvent e = new SMTPServerEvent(this);
            e.setMessage(msg);
            List<SMTPServerListener> list = copyListeners();
            for (SMTPServerListener l : list)
            	l.emailReceived(e);
        }
        
        public void setMessage(SmtpMessage msg)
        {
        	this.msg = msg;
        }
    }

    /**
     * Task event dispatched when server state has been updated.
     */
    class ServerStateUpdatedTask implements Runnable {
        public void run()
        {
            SMTPServerEvent e = new SMTPServerEvent(this);
            List<SMTPServerListener> list = copyListeners();
            for (SMTPServerListener l : list)
            {
                if (isStopped())
                	l.stopped(e);
                else
                	l.started(e);
            }
        }
    }    
    
    /**
     * The list of smtp server events listeners.
     */
    private Collection<SMTPServerListener> externalListeners = 
    	new ArrayList<SMTPServerListener>();
    
    /**
     * The event dispatcher executor.
     */
    private Executor eventDispatcher = Executors.newFixedThreadPool(4);
    
    /**
     * The email received event task.
     */
    private EmailReceivedTask emailTask = new EmailReceivedTask();

    /**
     * The server state updated event task.
     */    
    private ServerStateUpdatedTask serverStateTask = new ServerStateUpdatedTask();
    
    /**
     * Creates an instance of SimpleSmtpServer. Server will listen on the
     * default host:port. Debug mode is off by default.
     */
    public SubEthaSmtpServer()
    {
        this(DEFAULT_SMTP_HOST, DEFAULT_SMTP_PORT, false);
    }

    /**
     * Creates an instance of SimpleSmtpServer. Server will listen on the
     * default host:port.
     * 
     * @param debug if true debug mode is enabled
     */
    public SubEthaSmtpServer(boolean debug)
    {
        this(DEFAULT_SMTP_HOST, DEFAULT_SMTP_PORT, debug);
    }

    /**
     * Creates an instance of SimpleSmtpServer. Server will listen on the
     * default host. Debug mode is off by default.
     * 
     * @param port port number the server should listen to
     */
    public SubEthaSmtpServer(int port)
    {
        this(DEFAULT_SMTP_HOST, port, false);
    }

    /**
     * Creates an instance of SimpleSmtpServer. Server will listen on the
     * default host.
     * 
     * @param port port number the server should listen to
     * @param debug if true debug mode is enabled
     */
    public SubEthaSmtpServer(int port, boolean debug)
    {
        this(DEFAULT_SMTP_HOST, port, debug);
    }

    /**
     * Creates an instance of SimpleSmtpServer.
     * 
     * @param hostname hostname the server should listen on
     * @param port port number the server should listen to
     * @param debug if true debug mode is enabled
     */
    public SubEthaSmtpServer(String hostname, int port, boolean debug)
    {
        this.hostName = hostname;
        this.port = port;
        this.debug = debug;
        
        ArrayList<MessageListener> l = new ArrayList<MessageListener>(1);
        l.add(this);
   		server = new SMTPServer(l);
   		((MessageListenerAdapter)server.getMessageHandlerFactory())
   			.setMessageHandlerImpl(MailsterMessageHandler.class);
   		server.setHostName(hostName);
   		server.setPort(port);
   		server.setConnectionTimeout((int) connectionTimeout);   		
   		
   		server.setMaxConnections(-1);
   		server.setMaxRecipients(-1);
    }

    public static void setupSSLParameters(SSLProtocol protocol, boolean clientAuthNeeded)
    {
    	StartTLSCommand.setSSLFilter(SSLFilterFactory.createFilter(protocol, clientAuthNeeded));
    }

	/**
     * Starts an instance of SimpleSmtpServer.
     */
    public void start()
    {
   		server.start();
   		fireServerStateUpdated();
    }
    
    /**
     * Stops the server. Server is shut down after processing of the current
     * request is complete.
     */
    public void stop()
    {
        server.stop();
        fireServerStateUpdated();
    }

    public synchronized void addSMTPServerListener(SMTPServerListener listener)
    {
    	externalListeners.add(listener);
    }
    
    public synchronized void removeSMTPServerListener(SMTPServerListener listener)
    {
    	externalListeners.remove(listener);
    }
    
    /**
     * Check if the server has been placed in a stopped state. 
     * 
     * @return true if the server is stopped, false otherwise
     */
    public boolean isStopped()
    {
    	return server == null ||  !server.isRunning();
    }
	
	public void deliver(String from, List<String> recipients, InputStream data)
			throws TooMuchDataException, IOException 
	{		
		SmtpMessageFactory factory = 
			new SmtpMessageFactory(MailsterConstants.DEFAULT_CHARSET);
		
		try 
		{
			SmtpMessage msg = factory.asSmtpMessage(data);
			msg.addRecipients(recipients);
			fireMessageReceived(msg);
		} 
		catch (Exception e) 
		{
			throw new IOException(e.getLocalizedMessage());
		}
	}
    
	private synchronized ArrayList<SMTPServerListener> copyListeners() 
	{
	    return new ArrayList<SMTPServerListener>(externalListeners);
	}
	
    private void fireMessageReceived(final SmtpMessage msg)
    {
    	emailTask.setMessage(msg);
    	eventDispatcher.execute(emailTask);
    }

    private void fireServerStateUpdated()
    {
    	eventDispatcher.execute(serverStateTask);    	
    }	
	
    public boolean isDebug()
    {
        return debug;
    }

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }
    
    public void setHostName(String hostName)
    {
        this.hostName = StringUtilities.isEmpty(hostName) ? null : hostName;
    }

    public int getPort()
    {
        return port;
    }
    
    public void setPort(int port)
    {
        this.port = port;
    }
    
	public int getConnectionTimeout() 
	{
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) 
	{
		this.connectionTimeout = connectionTimeout * 1000;
	}

	public boolean accept(String from, String recipient) 
	{
		// unused
		return true;
	}

	public void deliver(String from, String recipient, InputStream data)
			throws TooMuchDataException, IOException 
	{
		// unused
	}
}