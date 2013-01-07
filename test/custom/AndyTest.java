package test.custom;


public class AndyTest 
{
	public static void main(String[] args) 
		throws Exception 
	{
		Client c = new Client("localhost", 25);
		c.setEcho(true);
		c.readResponse();
		exec(c, "HELO pipo");
		exec(c, "MAIL FROM: andy pl <andy@qwerty.pl>");
		exec(c, "RCPT TO: andy pl <andy@qwerty.pl>");
		exec(c, "DATA");
		exec(c, "From: andy@qwerty.pl>\r\n" +
        				"Content-Type: text/plain; charset=utf-8\r\n" +
        				"MIME-Version: 1.0\r\n" +
        				"Content-Transfer-Encoding: 8bit\r\n" +
        				"X-Mailer: PHP/5.2.6\r\n\r\n.");
		exec(c, "QUIT");
	}
	
	private static void exec(Client c, String cmd) throws Exception
	{
		c.send(cmd);
		c.readResponse();
	}
}
