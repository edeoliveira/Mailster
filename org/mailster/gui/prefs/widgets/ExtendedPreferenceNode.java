/*******************************************************************************
 * Copyright notice                                                            *
 *                                                                             *
 * Copyright (c) 2005-2006 Feed'n Read Development Team                        *
 * http://sourceforge.net/fnr                                                  *
 *                                                                             *
 * All rights reserved.                                                        *
 *                                                                             *
 * This program and the accompanying materials are made available under the    *
 * terms of the Common Public License v1.0 which accompanies this distribution,*
 * and is available at                                                         *
 * http://www.eclipse.org/legal/cpl-v10.html                                   *
 *                                                                             *
 * A copy is found in the file cpl-v10.html and important notices to the       *
 * license from the team is found in the textfile LICENSE.txt distributed      *
 * in this package.                                                            *
 *                                                                             *
 * This copyright notice MUST APPEAR in all copies of the file.                *
 *                                                                             *
 * Contributors:                                                               *
 *    Feed'n Read - initial API and implementation                             *
 *                  (smachhau@users.sourceforge.net)                           *
 *******************************************************************************/
package org.mailster.gui.prefs.widgets;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.jface.viewers.TreeViewer;

/**
 * Extends {@link PreferenceNode} to support easy handling of different icons in
 * the {@link TreeViewer} and {@link PreferencePage} of a JFace
 * {@link PreferenceDialog}. This class changes the original behaviour to that
 * effect that the icon of a {@link PreferencePage} stays untouched and has to
 * be set in the implementing class itself.
 * 
 * @author <a href="mailto:Sebastian.Machhausen@gmail.com">Sebastian Machhausen</a>
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 */
public class ExtendedPreferenceNode extends PreferenceNode
{

    /**
     * The name of the class that implements the <code>PreferencePage</code>
     * bound to this <code>ExtendedPreferenceNode</code>
     */
    private String classname;

    /**
     * Creates an <code>ExtendedPreferenceNode</code> with the given id. The
     * new node has nosubnodes.
     * 
     * @param id the node id
     */
    public ExtendedPreferenceNode(String id)
    {
        super(id);
    }

    /**
     * Creates an <code>ExtendedPreferenceNode</code> with the given id,
     * label, and image, and lazily-loaded preference page. The preference node
     * assumes (sole) responsibility for disposing of the image; this will
     * happen when the node is disposed.
     * 
     * @param id the node id
     * @param label the label used to display the node in the preference
     *            dialog's tree
     * @param image the image displayed left of the label in the preference
     *            dialog's tree, or <code>null</code> if none
     * @param className the class name of the preference page; this class must
     *            implement <code>IPreferencePage</code>
     */
    public ExtendedPreferenceNode(String id, String label,
            ImageDescriptor image, String className)
    {
        super(id, label, image, className);
        this.classname = className;
    }

    /**
     * Creates an <code>ExtendedPreferenceNode</code> with the given id and
     * preference page. The title of the preference page is used for the node
     * label. The node will not have an image.
     * 
     * @param id the node id
     * @param preferencePage the preference page
     */
    public ExtendedPreferenceNode(String id, IPreferencePage preferencePage)
    {
        super(id, preferencePage);
    }

    /**
     * Creates the <code>PreferencePage</code> according to the settings in
     * this <code>ExtendedPreferenceNode</code>.
     */
    public void createPage()
    {
        setPage((IPreferencePage) this.createObject(this.classname));
        getPage().setTitle(this.getLabelText());
    }

    /**
     * Creates a new instance of the given class <code>className</code>.
     * 
     * @param className
     * @return new Object or <code>null</code> in case of failures
     */
    private Object createObject(String className)
    {
        Assert.isNotNull(className);
        try
        {
            Class<?> cl = Class.forName(className);
            if (cl != null)
            {
                return (cl.newInstance());
            }
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }
        catch (InstantiationException e)
        {
            return null;
        }
        catch (IllegalAccessException e)
        {
            return null;
        }
        catch (NoSuchMethodError e)
        {
            return null;
        }
        return null;
    }

}
