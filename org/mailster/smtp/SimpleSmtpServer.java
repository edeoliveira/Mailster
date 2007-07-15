/*
 * Dumbster - a dummy SMTP server Copyright 2004 Jason Paul Kitchen Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

/*
 * Mailster additions : (c) De Oliveira Edouard Minor modifications as for
 * example compliance with Java5.0 generics
 */
package org.mailster.smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mailster.smtp.events.SMTPServerEvent;
import org.mailster.smtp.events.SMTPServerListener;
import org.mailster.util.StringUtilities;

/**
 * Dummy SMTP server for testing purposes.
 */
public class SimpleSmtpServer implements Runnable
{
	/**
	 * A watchdog thread that makes sure that connections don't go stale. It
	 * prevents someone from opening up MAX_CONNECTIONS to the server and
	 * holding onto them for more than 1 minute.
	 */
	private class Watchdog extends Thread
	{
		private SimpleSmtpServer server;
		private boolean run = true;

		public Watchdog(SimpleSmtpServer server)
		{
			super(Watchdog.class.getName());
			this.server = server;
			setPriority(Thread.MAX_PRIORITY / 3);
		}

		public void quit()
		{
			this.run = false;
		}

		public void run()
		{
			while (this.run)
			{
				try
				{
					synchronized(this)
					{
						if ((server.getLastActiveTime() + server.getConnectionTimeout()) 
							< System.currentTimeMillis())
							server.timeout();
					}
				
					// go to sleep for 10 seconds.
					sleep(1000 * 10);
				}
				catch (Exception e)
				{
					// ignore
				}
			}
		}
	}
	
	/**
	 * A thread watchdog.
	 */
	private Watchdog watchdog;
	
	/**
	 * Last active time.
	 */
	private long lastActiveTime;

	/**
	 * The default timeout.
	 */
	public static final int DEFAULT_TIMEOUT = 300000;
	
	/**
	 * The timeout before closing connection. Set to 5 minutes.
	 */
	private long connectionTimeout = DEFAULT_TIMEOUT;
	
	/**
	 * Tells if session timed out.
	 */
	private boolean timedOut = false;
	
	/**
	 * Current socket output <code>PrintWriter</code>.
	 * *
	 */
	private PrintWriter out;
	
    /**
     * Output client/server commands for debugging. Off by default.
     */
    private boolean debug = false;

    /**
     * Activates/deactivates internal storage of received mails. Activated 
     * by default.
     */
    private boolean internalStoreActivated = true;
    
    /**
     * Stores all of the email received since this instance started up.
     */
    private List<SmtpMessage> receivedMail;

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

    /**
     * Indicates whether this server is stopped or not.
     */
    private volatile boolean stopped = true;

    /**
     * Handle to the server socket this server listens to.
     */
    private ServerSocket serverSocket;

    /**
     * Port the server listens on - set to the default SMTP port initially.
     */
    private int port = DEFAULT_SMTP_PORT;

    /**
     * Hostname the server listens on
     */
    private String hostName = DEFAULT_SMTP_HOST;

    /**
     * Blocks listening on server socket for 500 ms.
     */
    private static final int SOCKET_SO_TIMEOUT = 500;

    /**
     * Charset used when reading input on sockets.
     */
    protected static final String DEFAULT_CHARSET = "ISO-8859-1";

    /**
     * The listeners list.
     */
    private SMTPServerListener[] serverListeners = new SMTPServerListener[0];

    /**
     * Creates an instance of SimpleSmtpServer. Server will listen on the
     * default host:port. Debug mode is off by default.
     */
    public SimpleSmtpServer()
    {
        this(DEFAULT_SMTP_HOST, DEFAULT_SMTP_PORT, false);
    }

    /**
     * Creates an instance of SimpleSmtpServer. Server will listen on the
     * default host:port.
     * 
     * @param debug if true debug mode is enabled
     */
    public SimpleSmtpServer(boolean debug)
    {
        this(DEFAULT_SMTP_HOST, DEFAULT_SMTP_PORT, debug);
    }

    /**
     * Creates an instance of SimpleSmtpServer. Server will listen on the
     * default host. Debug mode is off by default.
     * 
     * @param port port number the server should listen to
     */
    public SimpleSmtpServer(int port)
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
    public SimpleSmtpServer(int port, boolean debug)
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
    public SimpleSmtpServer(String hostname, int port, boolean debug)
    {
        this.hostName = hostname;
        this.port = port;
        this.debug = debug;
    }

    public void addSMTPServerListener(SMTPServerListener listener)
    {
        if (listener == null)
            throw new IllegalArgumentException("Argument is null");

        // add to array
        SMTPServerListener[] newListeners = new SMTPServerListener[serverListeners.length + 1];
        System.arraycopy(serverListeners, 0, newListeners, 0,
                serverListeners.length);
        serverListeners = newListeners;
        serverListeners[serverListeners.length - 1] = listener;
    }

    public void removeSMTPServerListener(SMTPServerListener listener)
    {
        if (listener == null)
            throw new IllegalArgumentException("Argument is null");

        if (serverListeners.length == 0)
            return;
        int index = -1;
        for (int i = 0; i < serverListeners.length; i++)
        {
            if (listener == serverListeners[i])
            {
                index = i;
                break;
            }
        }
        if (index == -1)
            return;
        if (serverListeners.length == 1)
        {
            serverListeners = new SMTPServerListener[0];
            return;
        }
        SMTPServerListener[] newTabListeners = new SMTPServerListener[serverListeners.length - 1];
        System.arraycopy(serverListeners, 0, newTabListeners, 0, index);
        System.arraycopy(serverListeners, index + 1, newTabListeners, index,
                serverListeners.length - index - 1);
        serverListeners = newTabListeners;
    }

    public void fireMessageReceived(final SmtpMessage msg)
    {
        Thread thread = new Thread() {
            public void run()
            {
                SMTPServerEvent e = new SMTPServerEvent(this);
                e.setMessage(msg);
                for (int i = 0; i < serverListeners.length; i++)
                    serverListeners[i].emailReceived(e);
            }
        };
        thread.start();
    }

    public void fireServerStateUpdated()
    {
        Thread thread = new Thread() {
            public void run()
            {
                SMTPServerEvent e = new SMTPServerEvent(this);
                for (int i = 0; i < serverListeners.length; i++)
                {
                    if (isStopped())
                        serverListeners[i].stopped(e);
                    else
                        serverListeners[i].started(e);
                }
            }
        };
        thread.start();
    }

    /**
     * Main loop of the SMTP server.
     */
    public void run()
    {
    	if (receivedMail == null && isInternalStoreActivated())        
    		receivedMail = new ArrayList<SmtpMessage>();

        try
        {
            try
            {
                serverSocket = new ServerSocket();
                if (hostName != null)
                    serverSocket.bind(new InetSocketAddress(hostName, port));
                else
                    serverSocket.bind(new InetSocketAddress(port));
                serverSocket.setSoTimeout(SOCKET_SO_TIMEOUT); // Block for maximum of 0.5 seconds
                stopped = false;
            }
            finally
            {
                synchronized (this)
                {
                    // Notify when server socket has been created
                    notifyAll();
                }
            }
            fireServerStateUpdated();
            
            watchdog = new Watchdog(this);
            watchdog.start();
            
            // Server: loop until stopped
            while (!isStopped())
            {
                try
                {
                    // Start server socket and listen for client connections
                    Socket socket = null;
                    try
                    {
                        socket = serverSocket.accept();
                    }
                    catch (Exception e)
                    {
                        if (socket != null)
                        {
                            socket.close();
                        }
                        continue; // Non-blocking socket timeout occurred: try
                        // accept() again
                    }
    
                    // Get the input and output streams
                    BufferedReader input = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(),
                                    DEFAULT_CHARSET));
                    PrintWriter out = new PrintWriter(socket.getOutputStream());
    
                    synchronized (this)
                    {
                        /*
                         * We synchronize over the handle method and the list update
                         * because the client call completes inside the handle
                         * method and we have to prevent the client from reading the
                         * list until we've updated it. For higher concurrency, we
                         * could just change handle to return void and update the
                         * list inside the method to limit the duration that we hold
                         * the lock.
                         */
                    	List<SmtpMessage> msgs = handleTransaction(out, input);
                        if (isInternalStoreActivated())
                        	receivedMail.addAll(msgs);                        
                    }
                    socket.close();
                }
                catch (IOException ioex)
                {
                    // Do not kill server if client fails
                    ioex.printStackTrace();                    
                }
            }
        }
        catch (IOException ioex)
        {
            // Server did not start
            throw new RuntimeException(ioex);
        }        
        finally
        {
            stop();
            if (watchdog != null)
            	watchdog.quit();
        }
    }

    /**
     * Check if the server has been placed in a stopped state. Allows another
     * thread to stop the server safely.
     * 
     * @return true if the server has been sent a stop signal, false otherwise
     */
    public synchronized boolean isStopped()
    {
        return stopped;
    }

    /**
     * Stops the server. Server is shutdown after processing of the current
     * request is complete.
     */
    public void stop()
    {
        synchronized (this)
        {
            // Mark us closed
            if (serverSocket != null)
            {
                try
                {
                    // Kick the server accept loop
                    serverSocket.close();
                }
                catch (IOException e)
                {
                    // Ignore
                }
            }
            stopped = true;
        }

        fireServerStateUpdated();
    }

    protected void resetTimeout()
    {
    	synchronized(watchdog)
    	{
    		lastActiveTime = System.currentTimeMillis();
    		timedOut = false;
    	}
    }
    
    protected void timeout()
    {
    	timedOut = true;
    	if (out != null)
    		sendResponse(out, new SmtpResponse(421, "Timeout waiting for data from client",SmtpState.QUIT));
    }
    
    /**
     * Handle an SMTP transaction, i.e. all activity between initial connect and
     * QUIT command.
     * 
     * @param out output stream
     * @param input input stream
     * @return List of SmtpMessage
     * @throws IOException
     */
    private List<SmtpMessage> handleTransaction(PrintWriter out,
            BufferedReader input) throws IOException
    {
        // Initialize the state machine
        SmtpState smtpState = SmtpState.CONNECT;
        SmtpRequest previous = null;
        SmtpRequest smtpRequest = new SmtpRequest(SmtpActionType.CONNECT, "",
                smtpState);
        SmtpRequest request = null;

        // Execute the connection request
        SmtpResponse smtpResponse = smtpRequest.execute();

        // Send initial response
        sendResponse(out, smtpResponse);
        smtpState = smtpResponse.getNextState();
        this.out = out;
        resetTimeout();
        
        List<SmtpMessage> msgList = new ArrayList<SmtpMessage>();
        SmtpMessage msg = new SmtpMessage();

        while (smtpState != SmtpState.CONNECT)
        {
            String line = input.readLine();            
            
            if (timedOut)
            	break;
            else
            	resetTimeout();
            
            if (line == null)
                break;

            if (debug)
                System.err.println("C: " + line);

            // Create request from client input and current state
            previous = request;
            request = SmtpRequest.createRequest(line, smtpState, previous);
            // Execute request and create response object
            SmtpResponse response = request.execute();
            // Move to next internal state
            smtpState = response.getNextState();
            // Send reponse to client
            sendResponse(out, response);

            // Store input in message
            String params = request.getParams();
            msg.store(response, params);

            // Store enveloppe recipients to expose BCC recipients
            if (SmtpActionType.RCPT == request.getAction())
                msg.addRecipient(params);

            // If message reception is complete save it
            if (request.getAction() != SmtpActionType.NOOP
                    && smtpState == SmtpState.QUIT)
            {
                msgList.add(msg);
                fireMessageReceived(msg);
                msg = new SmtpMessage();
            }
        }

        return msgList;
    }

    /**
     * Send response to client.
     * 
     * @param out socket output stream
     * @param smtpResponse response object
     */
    private void sendResponse(PrintWriter out, SmtpResponse smtpResponse)
    {
        if (smtpResponse.getCode() > 0)
        {
            int code = smtpResponse.getCode();
            String message = smtpResponse.getMessage();
            String response = code + " " + message;
            out.print(response + "\r\n");
            out.flush();

            if (debug)
                System.err.println("S: " + response);
        }
    }

    /**
     * Get email received by this instance since start up.
     * 
     * @return List of String
     */
    public synchronized Iterator<SmtpMessage> getReceivedEmail()
    {
    	if (!isInternalStoreActivated())
    		throw new IllegalStateException("Internal store not activated !");
    	
        return receivedMail.iterator();
    }
    
    public synchronized void clearQueue()
    {
    	if (!isInternalStoreActivated())
    		throw new IllegalStateException("Internal store not activated !");
    	
        receivedMail.clear();
    }

    /**
     * Get the number of messages received.
     * 
     * @return size of received email list
     */
    public synchronized int getReceivedEmailSize()
    {
    	if (!isInternalStoreActivated())
    		throw new IllegalStateException("Internal store not activated !");
    	
        return receivedMail.size();
    }

    /**
     * Starts an instance of SimpleSmtpServer.
     */
    public void start()
    {
        Thread t = new Thread(this);
        t.setUncaughtExceptionHandler(Thread.currentThread()
                .getUncaughtExceptionHandler());

        // Block until the server socket is created
        synchronized (this)
        {
            try
            {
                t.start();
                wait();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public boolean isDebug()
    {
        return debug;
    }

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }
    
    public boolean isInternalStoreActivated() 
    {
		return internalStoreActivated;
	}
    
    public void setInternalStoreActivated(boolean internalStoreActivated) 
    {
		this.internalStoreActivated = internalStoreActivated;
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

	public long getConnectionTimeout() 
	{
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) 
	{
		this.connectionTimeout = connectionTimeout * 1000;
	}

	protected long getLastActiveTime() 
	{
		return lastActiveTime;
	}
}
