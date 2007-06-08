package org.mailster.gui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.mailster.MailsterSWT;
import org.mailster.util.MailUtilities;

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
 * SWTHelper.java - A helper class that handles all the OS associated resources.
 * Resources are cached and the helper provides a <code>disposeAll()</code> method
 * that allows to free all system resources acquired till there. The class also
 * register a JVM shutdown hook to ensure it won't be forgotten.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class SWTHelper
{
    static
    {
        // Automatic disposal of OS resources
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run()
            {
                disposeAll();
            }
        }));
    }
    
    /**
     * The directory in which images and icons are stored.
     */
    public final static String IMAGES_DIRECTORY = "/org/mailster/gui/resources/images/"; //$NON-NLS-1$
    
    /**
     * The suffix added to the key name of the gray version of a image.
     */
    public final static String GRAY_IMAGE_SUFFIX = "_SWT_GRAY_IMAGE";

    /**
     * The temporary directory path.
     */
    private final static String tempDir = MailUtilities.tempDirectory.replace(
            File.separatorChar, '/')
            + "/";

    /** 
     * The system font. 
     * 
     * Note: This font should not be freed because it's allocated by the 
     * system itself.
     */
    public final static Font SYSTEM_FONT = Display.getDefault().getSystemFont();
    
    /**
     * The system font in bold style.
     */ 
    public final static Font SYSTEM_FONT_BOLD = makeFontBold(SYSTEM_FONT);
    
    /**
     * The resource tracker.
     */
    private static Map<String, Resource> resourceTracker = new HashMap<String, Resource>(
            20);

    /**
     * Makes the specified <code>Font</code> bold.
     * 
     * @param sourceFont the source <code>Font</code> object
     * @return Font the <code>Font</code> in Bold
     */
    public static Font makeFontBold(Font sourceFont) 
    {
        FontData[] fontData = sourceFont.getFontData();

        for (int i = 0; i < fontData.length; i++)
            fontData[i].setStyle(SWT.BOLD);
        
        return new Font(Display.getDefault(), fontData);
    }
    
    private SWTHelper()
    {
        super();
    }
    
    /**
     * Loads an image from the application's image directory and register it
     * with a resource tracker for later disposal by the
     * <code>disposeAll()</code> function.
     * 
     * @param fileName the image file name
     * @return the image object
     */
    public static Image loadImage(String fileName)
    {
        Object o = resourceTracker.get(fileName);
        
        if (o == null)
        {
            Image i = new Image(getDisplay(), MailsterSWT.class
                    .getResourceAsStream(IMAGES_DIRECTORY + fileName));

            resourceTracker.put(fileName, i);
            return i;
        }

        if (o == null || !(o instanceof Image))
            return null;
        else
            return (Image) o;
    }
    
    /**
     * Loads an image from the application's image directory, then computes the
     * correszponding gray image and finally register the grayed image with a 
     * resource tracker for later disposal by the <code>disposeAll()</code> 
     * function.
     * 
     * @param fileName the image file name
     * @return the image object
     */
    public static Image loadGrayImage(String fileName)
    {
        String key = fileName + GRAY_IMAGE_SUFFIX;
        Object o = resourceTracker.get(key);
        
        if (o == null)
        {
            Image i = loadImage(fileName);
            if (i != null)
            {
                Image gray = new Image(getDisplay(), i, SWT.IMAGE_GRAY);
                resourceTracker.put(fileName + GRAY_IMAGE_SUFFIX, gray);
                return gray;
            }
        }

        if (o == null || !(o instanceof Image))
            return null;
        else
            return (Image) o;
    }

    /**
     * Dispose all registered OS resources.
     */
    public static void disposeAll()
    {
        Iterator<Resource> it = resourceTracker.values().iterator();
        while (it.hasNext())
            it.next().dispose();
    }

    /**
     * Helper function to create a tool item. The behaviour of the toolitem is
     * set to use a grayed image when not hovered and to use the normal image
     * when mouse is over the toolitem. Image objects are registered in the
     * resource tracker.
     * 
     * @return ToolItem
     */
    public static ToolItem createToolItem(ToolBar parent, int type, String text,
            String toolTipText, String imageName, boolean createHotImage)
    {
        ToolItem item = new ToolItem(parent, type);
        item.setText(text);
        Image image = loadImage(imageName);
        item.setToolTipText(toolTipText);

        if (createHotImage)
        {            
            item.setImage(loadGrayImage(imageName));
            item.setHotImage(image);
        }
        else
            item.setImage(image);

        return item;
    }

    /**
     * Computes the gradient between two colors. Color objects are registered in
     * the resource tracker.
     * 
     * @param steps the number of steps for the color transition in the gradient
     * @param start the color at the start of the gradient
     * @param end the color at the end of the gradient
     * @return an array of the colors for the gradient
     */
    public static Color[] getGradientColors(int steps, Color start, Color end)
    {
        Color[] colors = new Color[steps];
        int rStep = Math.abs(start.getRed() - end.getRed()) / steps;
        int gStep = Math.abs(start.getGreen() - end.getGreen()) / steps;
        int bStep = Math.abs(start.getBlue() - end.getBlue()) / steps;

        colors[0] = start;
        colors[steps - 1] = end;
        Display display = getDisplay();

        resourceTracker.put(start.getRGB().toString(), start);
        resourceTracker.put(end.getRGB().toString(), end);

        for (int i = 1, max = steps - 1; i < max; i++)
        {
            colors[i] = new Color(display, end.getRed() >= start.getRed()
                    ? colors[i - 1].getRed() + rStep
                    : colors[i - 1].getRed() - rStep, end.getGreen() >= start
                    .getGreen()
                    ? colors[i - 1].getGreen() + gStep
                    : colors[i - 1].getGreen() - gStep, end.getBlue() >= start
                    .getBlue()
                    ? colors[i - 1].getBlue() + bStep
                    : colors[i - 1].getBlue() - bStep);

            resourceTracker.put(colors[i].getRGB().toString(), colors[i]);
        }
        return colors;
    }

    /**
     * Creates a Color object and register in the resource tracker for later
     * disposal.
     * 
     * @param r the red composant of the color
     * @param g the green composant of the color
     * @param b the blue composant of the color
     * @return the generated color object
     */
    public static Color createColor(int r, int g, int b)
    {
        Color c = new Color(Display.getDefault(), r, g, b);
        resourceTracker.put(c.getRGB().toString(), c);
        return c;
    }

    /**
     * Returns the file URL of the image on the local temporary directory. If
     * image isn't already created it creates it before.
     * 
     * @param fileName the image file name
     * @return the url if file exists or "file:///" if file couldn't be written
     *         to temporary directory
     */
    public static String getImageURL(String fileName)
    {
        // Generate files in temporary directory if needed
        boolean exists = (new File(tempDir + fileName)).exists();
        if (!exists)
            exists = writeImageToTempDirectory(fileName);

        if (!exists)
            return "file:///";
        else
            return "file:///" + tempDir + fileName;
    }

    /**
     * Writes the resource image to the temporary directory.
     * 
     * @param fileName the image file name
     * @return true if write succeeded
     */
    private static boolean writeImageToTempDirectory(String fileName)
    {
        try
        {
            BufferedInputStream bin = new BufferedInputStream(SWTHelper.class
                    .getResourceAsStream(IMAGES_DIRECTORY + fileName));

            MailUtilities.outputStreamToFile(tempDir, fileName, bin);
            bin.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    
    /**
     * Try to return the current display. If no display is tied to current 
     * thread then it returns the default Display a.k.a the first created
     * as described in Display.getDefault().
     * 
     * @return the current or default display
     */
    public static Display getDisplay() 
    {
        Display display = Display.getCurrent();
        
        //may be null if outside the UI thread
        if (display == null)
           display = Display.getDefault();
        
        return display;     
     }
}
