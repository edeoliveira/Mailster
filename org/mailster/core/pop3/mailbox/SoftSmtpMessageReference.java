package org.mailster.core.pop3.mailbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.ReferenceQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.mailster.core.mail.SmtpMessage;
import org.mailster.core.smtp.MailsterConstants;
import org.mailster.util.AbstractReloadableSoftReference;
import org.mailster.util.FileUtilities;
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
 * SoftSmtpMessageReference.java - Provides a reloadable soft reference
 * for a {@link SmtpMessage}.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.11 $, $Date: 2011/05/14 12:08:10 $
 */
public class SoftSmtpMessageReference
	extends AbstractReloadableSoftReference<SmtpMessage> 
{
	private static ReferenceQueue<SmtpMessage> refQueue = new ReferenceQueue<SmtpMessage>();

	private static  class Remover
		extends Thread
	{
		public Remover()
		{
			setDaemon(true);
		}

		public void run()
		{
			try
			{
				while (true)
				{
					SoftSmtpMessageReference ref = (SoftSmtpMessageReference) refQueue.remove();
					ref.store(ref._msg);
					ref._msg = null;
				}
			} catch (InterruptedException e) {
				LOG.debug("Soft references thread remover error", e);
			}
		}
	}

	static
	{
		new Remover().start();
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(SoftSmtpMessageReference.class);

	private final static String TEMP_DIRECTORY = MailsterConstants.USER_DIR+File.separator+"tmp"+File.separator;
	private String tmpFileName;
	
	private Long key;
    private SmtpMessage _msg;
    
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
		super(new SmtpMessage(), refQueue);
		this._msg = object;
		this.key = key;
		this.tmpFileName = TEMP_DIRECTORY+key+".obj";
	}
	
	@Override
	public SmtpMessage get()
	{
		SmtpMessage m = super.get();
		if (m != null)
			return _msg;
		else
			return null;
	}

	public Long getKey()
	{
		return this.key;
	}
	
	public SmtpMessage reload()
	{
		long started = System.currentTimeMillis();
		LOG.debug("Reloading Object[id:{}] ...", key);
		ObjectInputStream oIn = null;
		try 
		{
			ZipInputStream zip = new ZipInputStream(new FileInputStream(tmpFileName));
			zip.getNextEntry();			
			oIn = new ObjectInputStream(zip);
			Object obj = oIn.readObject();
			zip.closeEntry();
			
			if (obj != null && obj instanceof SmtpMessage)
				return (SmtpMessage) obj;
		} 
		catch (Exception ex) 
		{
			LOG.debug("Unable to reload the passivated object", ex);
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
			LOG.debug("Object[id:{}] reloaded in {} ms", new Object[] {key, elapsed});
		}
		
		return null;
	}

	public void store(SmtpMessage object) 
	{
		ObjectOutputStream oOut = null;
		try 
		{
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(tmpFileName));
			zip.putNextEntry(new ZipEntry(key+".eml"));
			oOut = new ObjectOutputStream(zip);
			oOut.writeObject(object);
			oOut.flush();
			zip.closeEntry();
		}
		catch (Exception ex) 
		{
			LOG.debug("Unable to store the soft referenced object", ex);
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
	    File f = new File(this.tmpFileName);
	    if (!f.delete())
		f.deleteOnExit();
	}
}