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

import org.eclipse.jface.preference.FieldEditor;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.mailster.gui.utils.LayoutUtils;

/**
 * Allows editing of a numeric value in a <code>Spinner</code> component.
 * 
 * @author <a href="mailto:Sebastian.Machhausen@gmail.com">Sebastian Machhausen</a>
 */
public class SpinnerFieldEditor extends FieldEditor
{

    /**
     * The Spinner control to edit the numeric value 
     */
    private Spinner spinnerCtrl;
    
    /** 
     * The number of digit places (default is 0) 
     */
    private int digits = 0;
    
    /** 
     * The cached old editor value 
     */
    private int oldValue;

    /**
     * Creates a new <code>SpinnerFieldEditor</code> instance.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    public SpinnerFieldEditor(String name, String labelText, Composite parent)
    {
        super(name, labelText, parent);
    }

    /**
     * Creates a new <code>SpinnerFieldEditor</code> instance.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     * @param digits the number of decimal places; The digit setting is used to
     *            allow for floating point values in the receiver.
     */
    public SpinnerFieldEditor(String name, String labelText, Composite parent,
            int digits)
    {
        super(name, labelText, parent);
        this.digits = digits;
    }

    /**
     * Informs this field editor's listener, if it has one, about a change to
     * the value (<code>VALUE</code> property) provided that the old and new
     * values are different. This hook is <em>not</em> called when the value
     * is initialized (or reset to the default value) from the preference store.
     */
    protected void valueChanged()
    {
        this.setPresentsDefaultValue(false);

        int newValue = this.spinnerCtrl.getSelection();
        if (newValue != oldValue)
        {
            this.fireValueChanged(VALUE, new Integer(oldValue), new Integer(
                    newValue));
            oldValue = newValue;
        }
    }

    /**
     * Adjusts the horizontal span of this <code>SpinnerFieldEditor</code>'s
     * basic controls. The number of columns will always be equal to or greater
     * than the value returned by this editor's <code>getNumberOfControls</code>
     * method.
     * 
     * @param numColumns the number of columns
     * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
     */
    protected void adjustForNumColumns(int numColumns)
    {
        GridData gd = (GridData) this.spinnerCtrl.getLayoutData();
        gd.horizontalSpan = numColumns - 1;
        gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
    }

    /**
     * Fills this <code>SpinnerFieldEditor</code>'s basic controls into the
     * given parent.
     * 
     * @param parent the composite used as a parent for the basic controls; the
     *            parent's layout must be a <code>GridLayout</code>
     * @param numColumns the number of columns
     * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite,
     *      int)
     */
    protected void doFillIntoGrid(Composite parent, int numColumns)
    {
        Label lbl = getLabelControl(parent);
        lbl.setLayoutData(
        	LayoutUtils.createGridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

        this.spinnerCtrl = new Spinner(parent, SWT.BORDER);
        this.spinnerCtrl.setDigits(this.digits);
        this.spinnerCtrl.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                valueChanged();
            }

            public void widgetSelected(SelectionEvent e)
            {
                valueChanged();
            }
        });

        this.spinnerCtrl.setDigits(this.digits);
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns - 1;
        gd.horizontalAlignment = GridData.FILL_HORIZONTAL;
        gd.grabExcessHorizontalSpace = true;
        this.spinnerCtrl.setLayoutData(gd);
    }

    /**
     * Initializes this <code>SpinnerFieldEditor</code> with the preference
     * value from the preference store.
     * 
     * @see org.eclipse.jface.preference.FieldEditor#doLoad()
     */
    protected void doLoad()
    {
        if (this.spinnerCtrl != null)
        {
            int value = this.getPreferenceStore().getInt(
                    this.getPreferenceName());
            this.spinnerCtrl.setSelection(value);
            this.oldValue = value;
        }
    }

    /**
     * Initializes this <code>SpinnerFieldEditor</code> with the default
     * preference value from the preference store.
     * 
     * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
     */
    protected void doLoadDefault()
    {
        if (this.spinnerCtrl != null)
        {
            int defaultValue = this.getPreferenceStore().getDefaultInt(
                    this.getPreferenceName());
            this.spinnerCtrl.setSelection(defaultValue);
        }
        this.valueChanged();
    }

    /**
     * Stores the preference value from this <code>SpinnerFieldEditor</code>
     * into the preference store.
     * 
     * @see org.eclipse.jface.preference.FieldEditor#doStore()
     */
    protected void doStore()
    {
        this.getPreferenceStore().setValue(this.getPreferenceName(),
                spinnerCtrl.getSelection());
    }

    /**
     * Returns the number of controls in this <code>SpinnerFieldEditor</code>.
     * Returns <code>1</code> as the <code>Spinner</code> is the only
     * control.
     * 
     * @return <code>1</code>
     * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
     */
    public int getNumberOfControls()
    {
        return 2;
    }

    /**
     * Sets the amount that this <code>SpinnerFieldEditor</code>'s value will
     * be modified by when the up/down arrows are pressed to the argument, which
     * must be at least one.
     * 
     * @param increment the new increment (must be greater than zero)
     */
    public void setIncrement(int increment)
    {
        this.spinnerCtrl.setIncrement(increment);
    }

    /**
     * Gets the amount that this <code>SpinnerFieldEditor</code>'s value will
     * be modified by when the up/down arrows are pressed to the argument.
     * 
     * @return the increment value
     */
    public int getIncrement()
    {
        return (this.spinnerCtrl.getIncrement());
    }

    /**
     * Sets the amount that this <code>SpinnerFieldEditor</code>'s position
     * will be modified by when the page up/down keys are pressed to the
     * argument, which must be at least one.
     * 
     * @param increment the new page increment (must be greater than zero)
     */
    public void setPageIncrement(int increment)
    {
        this.spinnerCtrl.setPageIncrement(increment);
    }

    /**
     * Gets the amount that this <code>SpinnerFieldEditor</code>'s value will
     * be modified by when the page up/down arrows are pressed to the argument.
     * 
     * @return the page increment value
     */
    public int getPageIncrement()
    {
        return (this.spinnerCtrl.getPageIncrement());
    }

    /**
     * Sets the maximum value that this this <code>SpinnerFieldEditor</code>
     * will allow. This new value will be ignored if it is not greater than this
     * <code>SpinnerFieldEditor</code>'s current minimum value. If the new
     * maximum is applied then this this <code>SpinnerFieldEditor</code>'s
     * selection value will be adjusted if necessary to fall within its new
     * range.
     * 
     * @param maximum the new maximum, which must be greater than the current
     *            minimum
     */
    public void setMaximum(int maximum)
    {
        this.spinnerCtrl.setMaximum(maximum);
    }
    
    /**
     * Gets the maximum value that this this <code>SpinnerFieldEditor</code>
     * allows.
     * 
     * @return the maximum value
     */
    public int getMaximum()
    {
        return (this.spinnerCtrl.getMaximum());
    }

    /**
     * Sets the minimum value that this <code>SpinnerFieldEditor</code> will
     * allow. This new value will be ignored if it is negative or is not less
     * than this <code>SpinnerFieldEditor</code>'s current maximum value. If
     * the new minimum is applied then this <code>SpinnerFieldEditor</code>'s
     * selection value will be adjusted if necessary to fall within its new
     * range.
     * 
     * @param minimum the new minimum, which must be nonnegative and less than
     *            the current maximum
     */
    public void setMinimum(int minimum)
    {
        this.spinnerCtrl.setMinimum(minimum);
    }

    /**
     * Gets the minimum value that this this <code>SpinnerFieldEditor</code>
     * allows.
     * 
     * @return the minimum value
     */
    public int getMinimum()
    {
        return (this.spinnerCtrl.getMinimum());
    }

    /**
     * Returns this <code>SpinnerFieldEditor</code>'s current value as an
     * integer.
     * 
     * @return the value
     */
    public int getIntValue()
    {
        return (this.spinnerCtrl.getSelection());
    }

    /**
     * Sets the enabled status of this <code>SpinnerFieldEditor</code>.
     * 
     * @param enabled <code>true</code> to enable, <code>false</code> to
     *            disable this <code>SpinnerFieldEditor</code>
     * @param parent the parent <code>Composite</code>
     * @see org.eclipse.jface.preference.FieldEditor#setEnabled(boolean,
     *      Composite)
     */
    public void setEnabled(boolean enabled, Composite parent)
    {
        super.setEnabled(enabled, parent);
        this.spinnerCtrl.setEnabled(enabled);
    }
}
