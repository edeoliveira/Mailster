/*
 * Dumbster - a dummy SMTP server
 * Copyright 2004 Jason Paul Kitchen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Mailster additions : (c) De Oliveira Edouard
 * Minor modifications as for example compliance with Java5.0 generics
 */
package com.dumbster.smtp;

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

/**
 * Dummy SMTP server for testing purposes.
 *
 * @todo constructor allowing user to pass preinitialized ServerSocket
 */
public class SimpleSmtpServer implements Runnable {
  /**
   * Stores all of the email received since this instance started up.
   */
  private List<SmtpMessage> receivedMail;

  /**
   * Defaut SMTP host is localhost
   */
  public static final String DEFAULT_SMTP_HOST = "localhost";  
  
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
  private String hostname = DEFAULT_SMTP_HOST;  
  
  /**
   * Timeout listening on server socket.
   */
  private static final int TIMEOUT = 500;

  /**
   * Constructor.
   * @param hostname host name
   * @param port port number
   */
  public SimpleSmtpServer(String hostname, int port) {
    receivedMail = new ArrayList<SmtpMessage>();
    this.hostname = hostname;
    this.port = port;
  }
  
  /**
   * Main loop of the SMTP server.
   */
  public void run() {    
    try {

      try {
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(hostname, port));
        serverSocket.setSoTimeout(TIMEOUT); // Block for maximum of 0.5 seconds
        stopped = false;
      }
      finally {
        synchronized (this) {
          // Notify when server socket has been created
          notifyAll();
        }
      }
      
      // Server: loop until stopped
      while (!isStopped()) {
        // Start server socket and listen for client connections
        Socket socket = null;
        try {
          socket = serverSocket.accept();
        } catch (Exception e) {
          if (socket != null) {
            socket.close();
          }
          continue; // Non-blocking socket timeout occurred: try accept() again
        }

        // Get the input and output streams
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream());

        synchronized (this) {
          /*
           * We synchronize over the handle method and the list update because the client call completes inside
           * the handle method and we have to prevent the client from reading the list until we've updated it.
           * For higher concurrency, we could just change handle to return void and update the list inside the method
           * to limit the duration that we hold the lock.
           */
          List<SmtpMessage> msgs = handleTransaction(out, input);
          receivedMail.addAll(msgs);
        }
        socket.close();
      }
    } catch (IOException ioex) {
      /** @todo Should throw an appropriate exception here. */
      throw new RuntimeException(ioex);
    } 
    finally { 
    	stop();
    }
  }

  /**
   * Check if the server has been placed in a stopped state. Allows another thread to
   * stop the server safely.
   * @return true if the server has been sent a stop signal, false otherwise
   */
  public synchronized boolean isStopped() {
    return stopped;
  }

  /**
   * Stops the server. Server is shutdown after processing of the current request is complete.
   */
  public synchronized void stop() {
    // Mark us closed
	  if (serverSocket != null)
	  {
		  try {
	      // Kick the server accept loop
	      serverSocket.close();
	    } catch (IOException e) {
	      // Ignore
	    }
	  }
    stopped = true;
  }

  /**
   * Handle an SMTP transaction, i.e. all activity between initial connect and QUIT command.
   *
   * @param out   output stream
   * @param input input stream
   * @return List of SmtpMessage
   * @throws IOException
   */
  private List<SmtpMessage> handleTransaction(PrintWriter out, BufferedReader input) throws IOException {
    // Initialize the state machine
    SmtpState smtpState = SmtpState.CONNECT;
    SmtpRequest previous = null;
    SmtpRequest smtpRequest = new SmtpRequest(SmtpActionType.CONNECT, "", smtpState);
    SmtpRequest request = null;

    // Execute the connection request
    SmtpResponse smtpResponse = smtpRequest.execute();

    // Send initial response
    sendResponse(out, smtpResponse);
    smtpState = smtpResponse.getNextState();

    List<SmtpMessage> msgList = new ArrayList<SmtpMessage>();
    SmtpMessage msg = new SmtpMessage();

    while (smtpState != SmtpState.CONNECT) {
      String line = input.readLine();

      if (line == null) {
        break;
      }
      
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

      // If message reception is complete save it
      if (request.getAction() != SmtpActionType.NOOP && smtpState == SmtpState.QUIT) {
        msgList.add(msg);
        msg = new SmtpMessage();
      }
    }

    return msgList;
  }

  /**
   * Send response to client.
   * @param out socket output stream
   * @param smtpResponse response object
   */
  private static void sendResponse(PrintWriter out, SmtpResponse smtpResponse) {
    if (smtpResponse.getCode() > 0) {
      int code = smtpResponse.getCode();
      String message = smtpResponse.getMessage();
      out.print(code + " " + message + "\r\n");
      out.flush();
    }
  }

  /**
   * Get email received by this instance since start up.
   * @return List of String
   */
  public synchronized Iterator getReceivedEmail() {
    return receivedMail.iterator();
  }

  public synchronized void copyReceivedEmailList(List<SmtpMessage> dest) {
    dest.clear();
    dest.addAll(receivedMail);        
  }
  
  /**
   * Get the number of messages received.
   * @return size of received email list
   */
  public synchronized int getReceivedEmailSize() {
    return receivedMail.size();
  }

  /**
   * Creates an instance of SimpleSmtpServer and starts it. Will listen on the default port.
   * @return a reference to the SMTP server
   */
  public static SimpleSmtpServer start() {
    return start(DEFAULT_SMTP_PORT);
  }

  /**
   * Creates an instance of SimpleSmtpServer and starts it.
   * @param hostname hostname the server should listen on
   * @param port port number the server should listen to
   * @return a reference to the SMTP server
   * @throws InterruptedException 
   */
  public static SimpleSmtpServer start(String hostname, int port){
    SimpleSmtpServer server = new SimpleSmtpServer(hostname, port);
    Thread t = new Thread(server);    
    t.setUncaughtExceptionHandler(Thread.currentThread().getUncaughtExceptionHandler());
    
    // Block until the server socket is created
    synchronized (server) {
        try {
            t.start();
			server.wait();
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
    }
    return server;
  }
  
  /**
   * Creates an instance of SimpleSmtpServer and starts it. Will listen on the
   * default hostname.
   * @param port port number the server should listen to
   * @return a reference to the SMTP server
   */
   public static SimpleSmtpServer start(int port) {
    return start(DEFAULT_SMTP_HOST, port);
   }  

}
