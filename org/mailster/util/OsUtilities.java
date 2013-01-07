package org.mailster.util;

import java.io.IOException;

/**
 * ---<br>
 * Mailster (C) 2007-2009 De Oliveira Edouard
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * <p>
 * See&nbsp; <a href="http://tedorg.free.fr/en/projects.php" target="_parent">Mailster Web Site</a>
 * <br>
 * ---
 * <p>
 * OsUtilities.java - Various methods to help in OS specific functions...
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class OsUtilities
{
	public static String getOsName()
	{
		return System.getProperty("os.name", "unknown");
	}

	public static void showDirectory(String dir)
		throws IOException
	{
		if (isWindows())
			Runtime.getRuntime().exec("explorer " + dir);
		else if (isMac())
			Runtime.getRuntime().exec("open " + dir);
		else if (isLinux())
		{
			String sess = System.getProperty("DESKTOP_SESSION");
			if (sess != null)
			{
				sess = sess.toLowerCase();
				if (sess.contains("gnome"))
					Runtime.getRuntime().exec("gnome-open " + dir);
				else if (sess.contains("kde"))
					Runtime.getRuntime().exec("kde-open " + dir);
			}
		}
	}

	public static String platform()
	{
		String osname = System.getProperty("os.name", "generic").toLowerCase();
		if (osname.startsWith("windows"))
		{
			return "win32";
		}
		else if (osname.startsWith("linux"))
		{
			return "linux";
		}
		else if (osname.startsWith("sunos"))
		{
			return "solaris";
		}
		else if (osname.startsWith("mac") || osname.startsWith("darwin"))
		{
			return "mac";
		}
		else
			return "generic";
	}

	public static boolean isWindows()
	{
		return (getOsName().toLowerCase().indexOf("windows") >= 0);
	}

	public static boolean isLinux()
	{
		return getOsName().toLowerCase().indexOf("linux") >= 0;
	}

	public static boolean isUnix()
	{
		final String os = getOsName().toLowerCase();

		if ((os.indexOf("nix") >= 0) || (os.indexOf("nux") >= 0) || (os.indexOf("sunos") >= 0))
			return true;

		if (isMac() && (System.getProperty("os.version", "").startsWith("10.")))
			return true;

		return false;
	}

	public static boolean isMac()
	{
		final String os = getOsName().toLowerCase();
		return os.startsWith("mac") || os.startsWith("darwin");
	}

	public static boolean isSolaris()
	{
		final String os = getOsName().toLowerCase();
		return os.indexOf("sunos") >= 0;
	}
}
