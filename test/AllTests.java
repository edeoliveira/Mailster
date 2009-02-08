package test;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.junit.CustomClientCommandTest;
import test.junit.ServerTest;
import test.junit.EncryptedMailTest;
import test.junit.HmacTest;
import test.junit.Pop3DigestMD5Test;
import test.junit.SmtpServerTest;

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
 * AllTests.java - Launches all test tests.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class AllTests 
{
	public static Test suite() 
	{
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ServerTest.class);
		suite.addTestSuite(CustomClientCommandTest.class);
		suite.addTestSuite(SmtpServerTest.class);
		suite.addTestSuite(HmacTest.class);
		suite.addTestSuite(Pop3DigestMD5Test.class);
		suite.addTestSuite(EncryptedMailTest.class);
		return suite;
	}

	public static void main(String args[]) 
	{
		TestRunner.run(suite());
	}
}
