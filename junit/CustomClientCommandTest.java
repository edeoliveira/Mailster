package junit;

import junit.framework.TestCase;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;

import org.mailster.smtp.SimpleSmtpServer;
import org.mailster.smtp.SmtpMessage;

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
 * CustomClientCommandTest.java - Minimal client that tests smtp responses.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class CustomClientCommandTest extends TestCase 
{

    private static final String FROM_ADDRESS = "from-addr@localhost";
    private static final String HOST_NAME = "localhost";
    private static final String TO_ADDRESS = "to-addr@localhost";
    private static final int SMTP_PORT = 1001;

    private BufferedReader input;
    private PrintWriter output;
    private SimpleSmtpServer server;
    private Socket socket;

    protected void setUp() 
    	throws Exception 
    {
        super.setUp();
        server = new SimpleSmtpServer(SMTP_PORT);
        server.start();
        socket = new Socket(HOST_NAME, SMTP_PORT);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
    }

    protected void tearDown() 
    	throws Exception 
    {
        super.tearDown();
        socket.close();
        server.stop();
    }

    public void testBouncedMail() 
    	throws IOException 
    {
        assertConnect(input);
        sendExtendedHello(HOST_NAME, output, input);
        sendMailFrom("", output, input);
        sendReceiptTo(TO_ADDRESS, output, input);
        sendDataStart(output, input);
        output.println("");
        output.println("Body");
        sendDataEnd(output, input);
        sendQuit(output, input);

        assertEquals(1, server.getReceivedEmailSize());
        Iterator<SmtpMessage> emailIter = server.getReceivedEmail();
        SmtpMessage email = (SmtpMessage) emailIter.next();
        assertEquals("Body", email.getBody());
    }
    
    public void testMailFromAfterReset() 
    	throws IOException 
    {
        assertConnect(input);
        sendExtendedHello(HOST_NAME, output, input);
        sendMailFrom(FROM_ADDRESS, output, input);
        sendReceiptTo(TO_ADDRESS, output, input);
        sendReset(output, input);
        sendMailFrom(FROM_ADDRESS, output, input);
        sendReceiptTo(TO_ADDRESS, output, input);
        sendDataStart(output, input);
        output.println("");
        output.println("Body");
        sendDataEnd(output, input);
        sendQuit(output, input);

        assertEquals(1, server.getReceivedEmailSize());
        Iterator<SmtpMessage> emailIter = server.getReceivedEmail();
        SmtpMessage email = (SmtpMessage) emailIter.next();
        assertEquals("Body", email.getBody());
    }

    public void testMailFromWithInitialResetAfterHello() 
    	throws IOException 
    {
        assertConnect(input);
        sendExtendedHello(HOST_NAME, output, input);
        sendReset(output, input);
        sendMailFrom(FROM_ADDRESS, output, input);
        sendReceiptTo(TO_ADDRESS, output, input);
        sendDataStart(output, input);
        output.println("");
        output.println("Body");
        sendDataEnd(output, input);
        sendQuit(output, input);

        assertEquals(1, server.getReceivedEmailSize());
        Iterator<SmtpMessage> emailIter = server.getReceivedEmail();
        SmtpMessage email = (SmtpMessage) emailIter.next();
        assertEquals("Body", email.getBody());
    }
    
    public void testMailFromWithInitialReset() 
    	throws IOException 
    {
        assertConnect(input);
        sendReset(output, input);
        sendMailFrom(FROM_ADDRESS, output, input);
        sendReceiptTo(TO_ADDRESS, output, input);
        sendDataStart(output, input);
        output.println("");
        output.println("Body");
        sendDataEnd(output, input);
        sendQuit(output, input);

        assertEquals(1, server.getReceivedEmailSize());
        Iterator<SmtpMessage> emailIter = server.getReceivedEmail();
        SmtpMessage email = (SmtpMessage) emailIter.next();
        assertEquals("Body", email.getBody());
    }

    public void testMailFromWithNoHello() 
    	throws IOException 
    {
        assertConnect(input);
        sendMailFrom(FROM_ADDRESS, output, input);
        sendReceiptTo(TO_ADDRESS, output, input);
        sendDataStart(output, input);
        output.println("");
        output.println("Body");
        sendDataEnd(output, input);
        sendQuit(output, input);

        assertEquals(1, server.getReceivedEmailSize());
        Iterator<SmtpMessage> emailIter = server.getReceivedEmail();
        SmtpMessage email = (SmtpMessage) emailIter.next();
        assertEquals("Body", email.getBody());
    }

    private void assertConnect(BufferedReader input) 
    	throws IOException 
    {
        String response = input.readLine();
        assertTrue(response, response.startsWith("220 "));
    }

    private void sendDataEnd(PrintWriter output, BufferedReader input) 
    	throws IOException 
    {
        output.println(".");
        String response = input.readLine();
        assertTrue(response, response.startsWith("250 "));
    }

    private void sendDataStart(PrintWriter output, BufferedReader input) 
    	throws IOException 
    {
        output.println("DATA");
        String response = input.readLine();
        assertTrue(response, response.startsWith("354 "));
    }

    private void sendExtendedHello(String hostName, PrintWriter output, BufferedReader input) 
    	throws IOException 
    {
        output.println("EHLO " + hostName);
        String response = input.readLine();
        assertTrue(response, response.startsWith("250 "));
    }

    private void sendMailFrom(String fromAddress, PrintWriter output, BufferedReader input) 
    	throws IOException 
    {
        output.println("MAIL FROM:<" + fromAddress  + ">");
        String response = input.readLine();
        assertTrue(response, response.startsWith("250 "));
    }

    private void sendQuit(PrintWriter output, BufferedReader input) 
    	throws IOException 
    {
        output.println("QUIT");
        String response = input.readLine();
        assertTrue(response, response.startsWith("221 "));
    }

    private void sendReceiptTo(String toAddress, PrintWriter output, BufferedReader input) 
    	throws IOException 
    {
        output.println("RCPT TO:<" + toAddress  + ">");
        String response = input.readLine();
        assertTrue(response, response.startsWith("250 "));
    }

    private void sendReset(PrintWriter output, BufferedReader input) 
    	throws IOException 
    {
        output.println("RSET");
        String response = input.readLine();
        assertTrue(response, response.startsWith("250 "));
    }
}
