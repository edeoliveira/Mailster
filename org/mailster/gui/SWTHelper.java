package org.mailster.gui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
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
 * SWTHelper.java - Enter your Comment HERE.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class SWTHelper
{
    /**
     * The directory in which images and icons are stored.
     */
    public final static String IMAGES_DIRECTORY = "/org/mailster/gui/resources/images/"; //$NON-NLS-1$

    /**
     * The temporary directory path.
     */
    private final String tempDir = MailUtilities.tempDirectory.replace(
            File.separatorChar, '/')
            + "/";

    /**
     * The resource tracker.
     */
    private Map<String, Resource> resourceTracker = new HashMap<String, Resource>(
            20);

    /**
     * Loads an image from the application's image directory and register it
     * with a resource tracker for later disposal by the
     * <code>disposeAll()</code> function.
     * 
     * @param fileName the image file name
     * @return the image object
     */
    public Image loadImage(String fileName)
    {
        if (resourceTracker.get(fileName) == null)
        {
            Image i = new Image(Display.getCurrent(), MailsterSWT.class
                    .getResourceAsStream(IMAGES_DIRECTORY + fileName));

            resourceTracker.put(fileName, i);
            return i;
        }

        Object o = resourceTracker.get(fileName);
        if (o == null || !(o instanceof Image))
            return null;
        else
            return (Image) resourceTracker.get(fileName);
    }

    /**
     * Dispose all registered OS resources.
     */
    public void disposeAll()
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
    public ToolItem createToolItem(ToolBar parent, int type, String text,
            String toolTipText, String imageName, boolean createHotImage)
    {
        Display display = Display.getCurrent();
        ToolItem item = new ToolItem(parent, type);
        item.setText(text);
        Image image = loadImage(imageName);
        item.setToolTipText(toolTipText);

        if (createHotImage)
        {
            Image grayed = new Image(display, image, SWT.IMAGE_GRAY);
            resourceTracker.put(imageName + "_SWT_GRAY_IMAGE", grayed);
            item.setImage(grayed);
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
    public Color[] getGradientColors(int steps, Color start, Color end)
    {
        Color[] colors = new Color[steps];
        int rStep = Math.abs(start.getRed() - end.getRed()) / steps;
        int gStep = Math.abs(start.getGreen() - end.getGreen()) / steps;
        int bStep = Math.abs(start.getBlue() - end.getBlue()) / steps;

        colors[0] = start;
        colors[steps - 1] = end;
        Display display = Display.getCurrent();

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
    public Color createColor(int r, int g, int b)
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
    public String getImageURL(String fileName)
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
    private boolean writeImageToTempDirectory(String fileName)
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
}
