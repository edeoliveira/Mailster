package org.mailster.gui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.mailster.gui.SWTHelper;
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
 * GIFAnimator.java - Class which allows to animate a GIF89a image drawing it
 * on a {@link Control} object.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.2 $, $Date: 2009/05/17 11:14:25 $
 */
public class GIFAnimator extends Thread
{
	private static final Logger LOG = LoggerFactory.getLogger(GIFAnimator.class);
	
	private ImageLoader loader;
	private ImageData[] imageDataArray;
	private Image image;
	private boolean useGIFBackground;
	
	private Display display;
	private GC gc;
	private Color bgColor;
	
	private boolean paused = true;
	private boolean quit = false;
	
	private int offsetX = 0;
	private int offsetY = 0;
	
	public GIFAnimator(String threadName, 
			String resourceFileName, Control ctrl, boolean useGIFBackground)
	{
		super(threadName);
		
		this.useGIFBackground = useGIFBackground;
		gc = new GC(ctrl);
		display = ctrl.getDisplay();
		bgColor = ctrl.getBackground();
		loader = new ImageLoader();
		imageDataArray = loader.load(SWTHelper.getResourceAsStream(resourceFileName));
		
		if (imageDataArray.length <= 1)
			throw new IllegalStateException("The animated file has only one frame ...");
	}
	
	public void startAnimation()
	{
		setPaused(false);
	}
	
	public void stopAnimation()
	{
		setPaused(true);
	}
	
	private void setPaused(boolean paused)
	{
		this.paused = paused;
	}
	
	public void dispose()
	{
	    this.quit = true;
	}
	
	public void run() 
	{
		// Create an off-screen image to draw on, 
		// and fill it with the shell background. 
		Image offScreenImage = new Image(display, loader.logicalScreenWidth, loader.logicalScreenHeight);
		GC offScreenImageGC = new GC(offScreenImage);
		offScreenImageGC.setBackground(bgColor);
		offScreenImageGC.fillRectangle(0, 0, loader.logicalScreenWidth, loader.logicalScreenHeight);
			
		try 
		{
			// Create the first image and draw it on the off-screen image.
			int imageDataIndex = 0;	
			ImageData imageData = imageDataArray[imageDataIndex];
			
			if (image != null && !image.isDisposed()) 
				image.dispose();
			
			image = new Image(display, imageData);
			offScreenImageGC.drawImage(
				image,
				0,
				0,
				imageData.width,
				imageData.height,
				imageData.x,
				imageData.y,
				imageData.width,
				imageData.height);

			gc.drawImage(offScreenImage, offsetX, offsetY);
			
			// Now loop through the images, creating and drawing each one
			// on the off-screen image before drawing it on the shell.
			int repeatCount = loader.repeatCount;
			while (loader.repeatCount == 0 || repeatCount > 0) 
			{
			    if (quit)
			       	break;
			    	
				if (!paused)
				{
					switch (imageData.disposalMethod) 
					{
						case SWT.DM_FILL_BACKGROUND:
							// Fill with the background color before drawing.
							Color bgColor = this.bgColor;
							if (useGIFBackground && loader.backgroundPixel != -1) 
								bgColor = new Color(display, imageData.palette.getRGB(loader.backgroundPixel));
		
							offScreenImageGC.setBackground(bgColor);
							offScreenImageGC.fillRectangle(imageData.x, imageData.y, imageData.width, imageData.height);
							if (bgColor != null)
								bgColor.dispose();
							break;
						case SWT.DM_FILL_PREVIOUS:
							// Restore the previous image before drawing.
							offScreenImageGC.drawImage(
								image,
								0,
								0,
								imageData.width,
								imageData.height,
								imageData.x,
								imageData.y,
								imageData.width,
								imageData.height);
							break;
					}
										
					imageDataIndex = (imageDataIndex + 1) % imageDataArray.length;
					imageData = imageDataArray[imageDataIndex];
					image.dispose();
					image = new Image(display, imageData);
					offScreenImageGC.drawImage(
						image,
						0,
						0,
						imageData.width,
						imageData.height,
						imageData.x,
						imageData.y,
						imageData.width,
						imageData.height);
					
					// If we have just drawn the last image, 
					// decrement the repeat count and start again.
					if (imageDataIndex == imageDataArray.length - 1) 
						repeatCount--;
				
					// Draw the off-screen image.
					gc.drawImage(offScreenImage, offsetX, offsetY);
				}
								
				// Sleep for the specified delay time 
				// (adding commonly-used slow-down fudge factors).
				try 
				{
					int ms = imageData.delayTime * 10;
					if (ms < 20) ms += 30;
					if (ms < 30) ms += 10;
					Thread.sleep(ms);
				} 
				catch (InterruptedException e) {}
			}
		} 
		catch (SWTException ex) 
		{
			LOG.debug("There was an error animating the GIF on thread {}", 
					Thread.currentThread().getName(), ex);
		} 
		finally 
		{
			if (offScreenImage != null && !offScreenImage.isDisposed()) 
				offScreenImage.dispose();
			
			if (offScreenImageGC != null && !offScreenImageGC.isDisposed()) 
				offScreenImageGC.dispose();
			
			if (image != null && !image.isDisposed()) 
				image.dispose();
		}
	}

	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}

	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}
}
