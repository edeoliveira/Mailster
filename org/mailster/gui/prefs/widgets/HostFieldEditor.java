package org.mailster.gui.prefs.widgets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.util.StringUtilities;

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
 * HostFieldEditor.java - A <code>FieldEditor</code> to edit host/ip addresses.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Author$ / $Date$
 */
public class HostFieldEditor extends StringFieldEditor
{
    /**
     * The pattern to identify an IPv4 address. 
     */
    private final static String _255 = "(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))";
    private final static Pattern ipv4Pattern = Pattern.compile("^(?:"+_255+"\\.){3}"+_255+"$");
    
    /**
     * The pattern to identify an IPv6 address. 
     */
    private final static Pattern ipv6Pattern = Pattern.compile(
            "^((([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4})|" +
            "(([0-9A-Fa-f]{1,4}:){6}:[0-9A-Fa-f]{1,4})|" +
            "(([0-9A-Fa-f]{1,4}:){5}:([0-9A-Fa-f]{1,4}:)?[0-9A-Fa-f]{1,4})|" +
            "(([0-9A-Fa-f]{1,4}:){4}:([0-9A-Fa-f]{1,4}:){0,2}[0-9A-Fa-f]{1,4})|" +
            "(([0-9A-Fa-f]{1,4}:){3}:([0-9A-Fa-f]{1,4}:){0,3}[0-9A-Fa-f]{1,4})|" +
            "(([0-9A-Fa-f]{1,4}:){2}:([0-9A-Fa-f]{1,4}:){0,4}[0-9A-Fa-f]{1,4})|" +
            "(([0-9A-Fa-f]{1,4}:){6}((\\b((25[0-5])|(1\\d{2})|(2[0-4]\\d)|(\\d{1,2}))\\b)\\.){3}(\\b((25[0-5])|(1\\d{2})|(2[0-4]\\d)|(\\d{1,2}))\\b))|" +
            "(([0-9A-Fa-f]{1,4}:){0,5}:((\\b((25[0-5])|(1\\d{2})|(2[0-4]\\d)|(\\d{1,2}))\\b)\\.){3}(\\b((25[0-5])|(1\\d{2})|(2[0-4]\\d)|(\\d{1,2}))\\b))|" +
            "(::([0-9A-Fa-f]{1,4}:){0,5}((\\b((25[0-5])|(1\\d{2})|(2[0-4]\\d)|(\\d{1,2}))\\b)\\.){3}(\\b((25[0-5])|(1\\d{2})|(2[0-4]\\d)|(\\d{1,2}))\\b))|" +
            "([0-9A-Fa-f]{1,4}::([0-9A-Fa-f]{1,4}:){0,5}[0-9A-Fa-f]{1,4})|" +
            "(::([0-9A-Fa-f]{1,4}:){0,6}[0-9A-Fa-f]{1,4})|" +
            "(([0-9A-Fa-f]{1,4}:){1,7}:))$");
    
    /**
     * The pattern to identify a valid FQDN hostname.
     * See RFC 1035 # 2.3.1
     */
    private final static String labelDNS = "([a-zA-Z](?:[a-zA-Z0-9]|-[a-zA-Z0-9])*)";    
    private final static Pattern hostPattern = Pattern.compile("^(?:"+labelDNS+"(\\."+labelDNS+")*)$");
    
	/**
	 * <code>true</code> if only local addresses are valid.
	 */
	private boolean onlyLocalAddressAllowed;
    
    /**
     * Cached valid state.
     */
    private boolean isValid;    

	public HostFieldEditor(String name, String labelText, Composite parent)
    {
        super(name, labelText, parent);        
    }

    protected void refreshValidState()
    {
        isValid = true;
        final String host = getStringValue();
        
        if (!(isEmptyStringAllowed() && StringUtilities.isEmpty(host)))
        {
            if (ipv4Pattern.matcher(host).matches()
                    || ipv6Pattern.matcher(host).matches()
                    || hostPattern.matcher(host).matches())
            {
                if (isOnlyLocalAddressAllowed()&& !host.equals("localhost"))
                {
                    BusyIndicator.showWhile(SWTHelper.getDisplay(), new Runnable() {                            
                        public void run()
                        {
                            try
                            {
                                InetAddress addr = InetAddress.getByName(host);
                                if (addr == null || (!addr.isLoopbackAddress() 
                                        && !addr.equals(InetAddress.getLocalHost())))
                                {
                                    isValid = false;
                                    setErrorMessage(Messages.getString("notALocalAddressMessage"));
                                    showErrorMessage();
                                }
                            } 
                            catch (UnknownHostException e) 
                            {
                                isValid = false;
                                setErrorMessage(Messages.getString("invalidHostNameMessage"));
                                showErrorMessage();
                            }
                        }
                    });
                }
            }
            else
            {
                isValid = false;
                setErrorMessage(Messages.getString("invalidHostNameMessage"));
                showErrorMessage();
            }
        }
        
        if (isValid)
            clearErrorMessage(true);
    }
    
    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    public boolean isValid() 
    {
        return isValid;
    }
    
    /**
     * Checks whether the text input field contains a valid value or not.
     *
     * @return <code>true</code> if the field value is valid,
     *   and <code>false</code> if invalid
     */
    protected boolean checkState() 
    {
        boolean result = false;
        if (isEmptyStringAllowed())
            result = true;
        
        Text textField = getTextControl();
        
        if (textField == null) 
            result = false;
        else
        {
            String txt = textField.getText();
            result = (txt.trim().length() > 0) || isEmptyStringAllowed();
        }

        // call hook for subclasses
        result = result && doCheckState();

        if (!result)
        {
            showErrorMessage(JFaceResources
                    .getString("StringFieldEditor.errorMessage")); //$NON-NLS-1$
            showErrorMessage();
        }

        return result;
    }
    
    /**
     * Returns this field editor's text control.
     * <p>
     * The control is created if it does not yet exist
     * </p>
     *
     * @param parent the parent
     * @return the text control
     */
    public Text getTextControl(Composite parent) 
    {
    	//Enforce validation strategy
        setValidateStrategy(VALIDATE_ON_FOCUS_LOST);
        return super.getTextControl(parent);
    }
    
    /**
     * Informs this field editor's listener, if it has one, about a change
     * to the value (<code>VALUE</code> property) provided that the old and
     * new values are different.
     * <p>
     * This hook is <em>not</em> called when the text is initialized 
     * (or reset to the default value) from the preference store.
     * </p>
     */
    protected void valueChanged() 
    {
        clearErrorMessage(true);
        super.valueChanged();
    }    
    
    /**
     * Clears the error message from the message line.
     */
    protected void clearErrorMessage() 
    {
    	//This way super calls are not executed
        clearErrorMessage(false);
    }
    
    /**
     * Clears the error message from the message line.
     */
    protected void clearErrorMessage(boolean force) 
    {
        if (force && getPage() != null)
            getPage().setErrorMessage(null);
    }    
    
    public boolean isOnlyLocalAddressAllowed() 
    {
		return onlyLocalAddressAllowed;
	}

    public void setOnlyLocalAddressAllowed(boolean onlyLocalAddressAllowed) 
	{
		this.onlyLocalAddressAllowed = onlyLocalAddressAllowed;
	}    
}
