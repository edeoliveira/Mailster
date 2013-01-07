package org.mailster.gui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.mailster.gui.Messages;

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
 * MemoryProgressBar.java - A custom widget that displays available memory.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.8 $, $Date: 2009/04/10 00:05:10 $
 */
public class MemoryProgressBar 
	extends ProgressBar 
{
	// Default timeout.
	public final static int DEFAULT_TIMEOUT = 1000;
	
	public MemoryProgressBar(Composite parent, int style) 
	{
		this(parent, style, DEFAULT_TIMEOUT);
		setToolTipText(Messages.getString("MailsterSWT.memory.progressBar.tooltip")); //$NON-NLS-1$
	}
	
	public MemoryProgressBar(Composite parent, int style, int timeout) 
	{
		super(parent, style);
		
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent evt) 
			{
				GC gc = evt.gc;
				Color foreGround = gc.getForeground();
				
				gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
				
				long maxMemory = Runtime.getRuntime().maxMemory();
				int used = Math.round(((maxMemory-Runtime.getRuntime().freeMemory()) * 100) 
						/ maxMemory);
				
				String title = used+" %";
				Point size = getSize();
				
				gc.drawString(title, 
						(size.x - gc.getFontMetrics().getAverageCharWidth()*title.length()) /2, 
						(size.y-gc.getFontMetrics().getHeight()) / 2, true);
				
				gc.setForeground(foreGround);
			}		
		});
		
		addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event evt) 
			{
				System.gc();
			}		
		});
		
		startRefreshThread(timeout);
	}

	private void startRefreshThread(final long timeout)
	{
		Thread t = new Thread("Memory data collector") 
		{
			public void run() 
			{
				while (!isDisposed())
				{
					try 
					{
						Thread.sleep (timeout);
					} 
					catch (Throwable th) {}
					
					if (!isDisposed()) {
						getDisplay().asyncExec(new Runnable() {
							public void run() 
							{
								long maxMemory = Runtime.getRuntime().maxMemory();
								int used = Math.round(((maxMemory-Runtime.getRuntime().freeMemory()) * 100) 
										/ maxMemory);
								
								if (!isDisposed()) {
									setSelection(used);
									redraw();
								}
							}
						});
					}	
				}
			}
		};
		t.setDaemon(true);
		t.start();		
	}
	
    protected void checkSubclass()
    {
        // Override SWT subclassing protection
        return;
    }
}
