package junit;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class AllTests 
{
	public static Test suite() 
	{
		TestSuite suite = new TestSuite();
		suite.addTestSuite(DumbsterTest.class);
		suite.addTestSuite(CustomClientCommandTest.class);
		suite.addTestSuite(SimpleSmtpServerTest.class);
		suite.addTestSuite(SmtpRequestTest.class);
		suite.addTestSuite(HmacTest.class);
		suite.addTestSuite(Pop3DigestMD5Test.class);
		suite.addTestSuite(SendEncryptedMail.class);
		return suite;
	}

	public static void main(String args[]) 
	{
		TestRunner.run(suite());
	}
}
