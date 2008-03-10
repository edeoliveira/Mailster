package org.mailster.pop3.mailbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.mailster.server.MailsterConstants;
import org.mailster.smtp.SmtpMessage;
import org.mailster.util.AbstractReloadableSoftReference;
import org.mailster.util.FileUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * SoftSmtpMessageReference.java - Provides a reloadable soft reference
 * for a {@link SmtpMessage}.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class SoftSmtpMessageReference 
	extends AbstractReloadableSoftReference<Long, SmtpMessage> 
{
	private static final Logger log = LoggerFactory.getLogger(SoftSmtpMessageReference.class);

	private final static String TEMP_DIRECTORY = MailsterConstants.USER_DIR+File.separator+"tmp"+File.separator;
	private String tmpFileName;
    
	static
	{
		final File dir = new File(TEMP_DIRECTORY);
		
		// remove residual files.
		FileUtilities.deleteDirectory(dir);
		
		dir.mkdir();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() 
			{
				// remove temporary files
				FileUtilities.deleteDirectory(dir);
			}		
		}));
	}
	
	public SoftSmtpMessageReference(Long key, SmtpMessage object) 
	{
		super(key, object);
		this.tmpFileName = TEMP_DIRECTORY+key+".obj";
		store(object);
	}
	
	public SmtpMessage reload()
	{
		long started = System.currentTimeMillis();
		log.debug("Reloading Object-id[{}] ...", getKey());
		ObjectInputStream oIn = null;
		try 
		{
			ZipInputStream zip = new ZipInputStream(new FileInputStream(this.tmpFileName));
			zip.getNextEntry();			
			oIn = new ObjectInputStream(zip);
			Object obj = oIn.readObject();
			zip.closeEntry();
			
			if (obj != null && obj instanceof SmtpMessage)
				return (SmtpMessage) obj;
		} 
		catch (Exception ex) 
		{
			log.debug("Unable to reload the passivated object", ex);
		}
		finally
		{
			if (oIn != null)
			{
				try 
				{
					oIn.close();
				} 
				catch (IOException e) {}
			}
			long elapsed = System.currentTimeMillis() - started;
			log.debug("Object-id[{0}] reloaded in {1} ms", new Object[] {getKey(), elapsed});
		}
		
		return null;
	}

	public void store(SmtpMessage object) 
	{
		ObjectOutputStream oOut = null;
		try 
		{
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(this.tmpFileName));
			zip.putNextEntry(new ZipEntry(getKey()+".eml"));			
			oOut = new ObjectOutputStream(zip);
			oOut.writeObject(object);
			oOut.flush();
			zip.closeEntry();
		}
		catch (Exception ex) 
		{
			log.debug("Unable to store the soft referenced object", ex);
		}		
		finally
		{
			if (oOut != null)
			{
				try 
				{
					oOut.close();
				} 
				catch (IOException e) {}
			}
		}
	}
	
	public void delete()
	{
		(new File(this.tmpFileName)).delete();
	}
}