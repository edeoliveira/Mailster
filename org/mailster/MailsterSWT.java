package org.mailster;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.time.DateFormatUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.mailster.crypto.X509SecureSocketFactory.SSLProtocol;
import org.mailster.gui.AboutDialog;
import org.mailster.gui.MemoryProgressBar;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.ConfigurationDialog;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.prefs.store.MailsterPrefStore;
import org.mailster.gui.pshelf.PShelf;
import org.mailster.gui.pshelf.PShelfItem;
import org.mailster.gui.pshelf.PaletteShelfRenderer;
import org.mailster.gui.utils.DialogUtils;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.gui.views.FilterTreeView;
import org.mailster.gui.views.ImportExportUtilities;
import org.mailster.gui.views.MailView;
import org.mailster.gui.views.OutLineView;
import org.mailster.message.SmtpHeadersInterface;
import org.mailster.pop3.MailsterPop3Service;
import org.mailster.pop3.Pop3ProtocolHandler;
import org.mailster.pop3.connection.MinaPop3Connection;
import org.mailster.pop3.mailbox.MailBoxManager;
import org.mailster.pop3.mailbox.UserManager;
import org.mailster.server.MailsterSMTPServer;
import org.mailster.server.MailsterSmtpService;
import org.mailster.server.events.SMTPServerAdapter;
import org.mailster.server.events.SMTPServerEvent;
import org.mailster.util.DateUtilities;
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
 * MailsterSWT.java - The main Mailster class.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class MailsterSWT
{
    /** 
     * Log object for this class. 
     */
    private static final Logger LOG = LoggerFactory.getLogger(MailsterSWT.class);
    
    private final static Image trayImage = SWTHelper.loadImage("mail_earth.gif"); //$NON-NLS-1$
    private final static Image trayRunningImage = SWTHelper.loadImage("mail_earth_running.gif"); //$NON-NLS-1$
    private final static Image stopImage = SWTHelper.loadImage("stop.gif"); //$NON-NLS-1$
    private final static Image startImage = SWTHelper.loadImage("start.gif"); //$NON-NLS-1$
    private final static Image debugImage = SWTHelper.loadImage("startdebug.gif"); //$NON-NLS-1$

    // Visual components
    private Shell sShell;
    private TrayItem trayItem;
    private MailView mailView;
    private OutLineView outlineView;
    private FilterTreeView treeView;
    
    private SashForm filterViewDivider;

    private MenuItem serverStartMenuItem;
    private MenuItem serverDebugMenuItem;
    private MenuItem serverStopMenuItem;
    private ToolItem serverStartToolItem;
    private ToolItem serverDebugToolItem;
    private ToolItem serverStopToolItem;

    private Text filterTextField;
    
    private MailsterSmtpService smtpService;
    
    private static MailsterSWT _instance;

    public static MailsterSWT getInstance()
    {
        return _instance;
    }
    
    public MailView getMailView()
    {
        return mailView;
    }
        
    private void createPShelfAndMailView(Composite parent)
    {
        filterViewDivider = new SashForm(parent, SWT.NONE);
        filterViewDivider.setOrientation(SWT.HORIZONTAL);
        filterViewDivider.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        
        Composite back = new Composite(filterViewDivider, SWT.BORDER);
        back.setLayout(LayoutUtils.createGridLayout(1, false, 0, 0, 0, 0, 0, 0, 0, 0));
        back.setLayoutData(LayoutUtils.createGridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
        
        PShelf shelf = new PShelf(back, SWT.NONE);
        shelf.setBackground(SWTHelper.createColor(128, 173, 249));        
        ((PaletteShelfRenderer) shelf.getRenderer()).setShadeColor(SWTHelper.createColor(12, 97, 232));
        
        shelf.setLayoutData(LayoutUtils.createGridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
        
        PShelfItem treeItem = new PShelfItem(shelf,SWT.NONE);
        treeItem.setText(Messages.getString("MailsterSWT.expandbar.treeView")); //$NON-NLS-1$
        treeItem.setImage(SWTHelper.loadImage("filter.gif")); //$NON-NLS-1$
        treeItem.getBody().setLayout(LayoutUtils.createGridLayout(1, false, 1, 1, 1, 1, 0, 0, 0, 0));
        
        treeView = new FilterTreeView(treeItem.getBody(), true);
        treeView.setLayoutData(LayoutUtils.createGridData(
                GridData.FILL, GridData.FILL, true, true, 1, 1));
        
        PShelfItem outlineItem = new PShelfItem(shelf,SWT.NONE);
        outlineItem.setText(Messages.getString("MailsterSWT.expandbar.outlineView")); //$NON-NLS-1$
        outlineItem.setImage(SWTHelper.loadImage("outline.gif")); //$NON-NLS-1$
        outlineItem.getBody().setLayout(LayoutUtils.createGridLayout(1, false, 1, 1, 1, 1, 0, 0, 0, 0));
        
        outlineView = new OutLineView(outlineItem.getBody(), true);        
        outlineView.setLayoutData(LayoutUtils.createGridData(
                GridData.FILL, GridData.FILL, true, true, 1, 1));
        
        PShelfItem aboutItem = new PShelfItem(shelf,SWT.NONE);
        aboutItem.setText(Messages.getString("MailsterSWT.expandbar.about")); //$NON-NLS-1$
        aboutItem.setImage(trayImage);
        aboutItem.getBody().setLayout(new GridLayout());
        aboutItem.getBody().setBackground(SWTHelper.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        aboutItem.getBody().setBackgroundMode(SWT.INHERIT_FORCE);
        
        Link l = new Link(aboutItem.getBody(), SWT.NONE);
        l.setText(ConfigurationManager.MAILSTER_VERSION + "\n\n" //$NON-NLS-1$
                    + ConfigurationManager.MAILSTER_COPYRIGHT);
        l.setToolTipText(ConfigurationManager.MAILSTER_HOMEPAGE);
        l.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event)
            {
                getMailView().showURL(event.text, true);
            }
        });
        
        mailView = new MailView(filterViewDivider, treeView);
        filterViewDivider.setWeights(new int[] { 29, 71 });
    }

    private void createShellToolBars()
    {
        final CoolBar coolBar = new CoolBar(sShell, SWT.FLAT);
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.BEGINNING;
        coolBar.setLayoutData(gridData);
        ToolBar toolBar = new ToolBar(coolBar, SWT.NONE);

        serverStartToolItem = new ToolItem(toolBar, SWT.PUSH);
        serverStartToolItem.setImage(startImage);
        serverStartToolItem.setToolTipText(Messages
                .getString("MailsterSWT.start.label")); //$NON-NLS-1$

        serverDebugToolItem = new ToolItem(toolBar, SWT.PUSH);
        serverDebugToolItem.setImage(debugImage);
        serverDebugToolItem.setToolTipText(Messages
                .getString("MailsterSWT.debug.label")); //$NON-NLS-1$

        serverStopToolItem = new ToolItem(toolBar, SWT.PUSH);
        serverStopToolItem.setImage(stopImage);
        serverStopToolItem.setEnabled(false);
        serverStopToolItem.setToolTipText(Messages
                .getString("MailsterSWT.stop.label")); //$NON-NLS-1$        

        ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);
        item.setWidth(item.getWidth()*2);
        
        final ToolItem configToolItem = new ToolItem(toolBar, SWT.PUSH);
        configToolItem.setImage(SWTHelper.loadImage("config.gif")); //$NON-NLS-1$
        configToolItem.setToolTipText(Messages
                .getString("MailsterSWT.config.tooltip")); //$NON-NLS-1$  
        
        item = new ToolItem(toolBar, SWT.SEPARATOR);
        item.setWidth(item.getWidth()*2);
        
        final ToolItem homeToolItem = new ToolItem(toolBar, SWT.PUSH);
        homeToolItem.setImage(SWTHelper.loadImage("home.gif")); //$NON-NLS-1$
        homeToolItem.setToolTipText(Messages
                .getString("MailView.home.page.tooltip")); //$NON-NLS-1$  
                
        final ToolItem changelogToolItem = new ToolItem(toolBar, SWT.PUSH);
        final Image changeLogImage = SWTHelper.loadImage("changelog.gif"); //$NON-NLS-1$
        changelogToolItem.setImage(changeLogImage);
        changelogToolItem.setToolTipText(Messages
                .getString("MailsterSWT.changelog.tooltip")); //$NON-NLS-1$ 
        
        final ToolItem versionCheckToolItem = new ToolItem(toolBar, SWT.PUSH);
        versionCheckToolItem.setImage(SWTHelper.loadImage("versioncheck.gif")); //$NON-NLS-1$
        versionCheckToolItem.setToolTipText(Messages
                .getString("MailsterSWT.versioncheck.tooltip")); //$NON-NLS-1$ 
        
        item = new ToolItem(toolBar, SWT.SEPARATOR);
        item.setWidth(item.getWidth()*2);
        
        final ToolItem aboutToolItem = new ToolItem(toolBar, SWT.PUSH);
        aboutToolItem.setImage(SWTHelper.loadImage("about.gif")); //$NON-NLS-1$
        aboutToolItem.setToolTipText(Messages
                .getString("MailsterSWT.about.tooltip")); //$NON-NLS-1$  
        
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                if (e.widget == serverStartToolItem)
                    smtpService.startServer(false);
                else if (e.widget == serverDebugToolItem)
                    smtpService.startServer(true);
                else if (e.widget == serverStopToolItem)
                    smtpService.shutdownServer(false);
                else if (e.widget == configToolItem)
                    ConfigurationDialog.run(sShell);
                else if (e.widget == aboutToolItem)
                	(new AboutDialog(sShell, mailView)).open();
                else if (e.widget == changelogToolItem)
					mailView.showURL(changeLogImage, "file://"+System.getProperty("user.dir")+File.separator+"changelog.htm", 
								Messages.getString("MailsterSWT.changelog.tooltip"));
				else if (e.widget == homeToolItem)
                	mailView.showURL(ConfigurationManager.MAILSTER_HOMEPAGE, false);
				else if (e.widget == versionCheckToolItem)
					versionCheck();
            }
        };
        
        serverStartToolItem.addSelectionListener(selectionAdapter);
        serverDebugToolItem.addSelectionListener(selectionAdapter);
        serverStopToolItem.addSelectionListener(selectionAdapter);
        configToolItem.addSelectionListener(selectionAdapter);
        aboutToolItem.addSelectionListener(selectionAdapter);
        homeToolItem.addSelectionListener(selectionAdapter);
        changelogToolItem.addSelectionListener(selectionAdapter);
        versionCheckToolItem.addSelectionListener(selectionAdapter);

        // Add a coolItem to the coolBar
        CoolItem coolItem = new CoolItem(coolBar, SWT.NONE);
        coolItem.setControl(toolBar);
        Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point coolSize = coolItem.computeSize(size.x, size.y);
        coolItem.setSize(coolSize);

        // Add the search cool item
        final CoolItem textItem = new CoolItem(coolBar, SWT.NONE);
        final Composite searchPanel = new Composite(coolBar, SWT.NONE);

        searchPanel.setLayout(
                LayoutUtils.createGridLayout(3, false, 0, 0, 0, 0, 0, 0, 0, 2));

        CLabel lbl = new CLabel(searchPanel, SWT.NONE);
        lbl.setText(Messages.getString("MailsterSWT.filter.label")); //$NON-NLS-1$
        lbl.setImage(SWTHelper.loadImage("filter.gif")); //$NON-NLS-1$
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalAlignment = GridData.END;
        lbl.setLayoutData(gd);

        filterTextField = new Text(searchPanel, SWT.BORDER | SWT.SINGLE
                | SWT.SEARCH | SWT.CANCEL);
        gd = new GridData();
        gd.widthHint = 120;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalAlignment = GridData.END;
        filterTextField.setLayoutData(gd);

        if ((filterTextField.getStyle() & SWT.CANCEL) == 0)
        {
            ToolBar tb = new ToolBar(searchPanel, SWT.NONE);
            final ToolItem cancel = new ToolItem(tb, SWT.FLAT | SWT.PUSH);
            cancel.setImage(SWTHelper.loadImage("delete.gif"));
            cancel.setEnabled(false);
            filterTextField.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent arg0)
                {
                    cancel.setEnabled(filterTextField.getText().length() > 0);
                }
            });
            cancel.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    filterTextField.setText("");
                    cancel.setEnabled(false);
                }
            });
            
            final ToolItem highlightToolItem = new ToolItem(tb, SWT.CHECK);
            highlightToolItem.setImage(SWTHelper.loadImage("highlight.gif")); //$NON-NLS-1$
            highlightToolItem.setToolTipText(Messages
                    .getString("MailsterSWT.highlight.tooltip")); //$NON-NLS-1$
            highlightToolItem.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e)
                {
                    if (!highlightToolItem.getSelection())
                        getMailView().executeJavaScriptOnEachMailBrowser("Hilite.clearHighlighting();"); //$NON-NLS-1$
                    else
                    {
                    	getMailView().executeJavaScriptOnEachMailBrowser("Hilite.storeHTML();"); //$NON-NLS-1$
                    	getMailView().executeJavaScriptOnEachMailBrowser("Hilite.hilite(new Array ('"+filterTextField.getText()+"'));"); //$NON-NLS-1$
                    }
                }
            });
            
            GridData tbgd = new GridData();
            tbgd.grabExcessHorizontalSpace = false;
            tbgd.horizontalAlignment = GridData.END;
            tb.setLayoutData(tbgd);
        }

        textItem.setControl(searchPanel);
        size = searchPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        textItem.setSize(textItem.computeSize(size.x, size.y));

        coolBar.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event)
            {
                Point size = searchPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                size = textItem.computeSize(size.x, size.y);
                coolBar.setItemLayout(new int[] { 0, 1 }, null, new Point[] {
                        new Point(coolBar.getParent().getClientArea().width
                                - size.x - 10, size.y), size });
                sShell.layout();
            }
        });
    }

    public Text getFilterTextField()
    {
        return filterTextField;
    }

    public static void usage()
    {
        System.out.println(ConfigurationManager.MAILSTER_VERSION + "\n");
        System.out.println(Messages.getString("MailsterSWT.usage.line1")); //$NON-NLS-1$
        System.out.println(Messages.getString("MailsterSWT.usage.line2")); //$NON-NLS-1$
        System.out.println(Messages.getString("MailsterSWT.usage.line3")); //$NON-NLS-1$
        System.out.println(Messages.getString("MailsterSWT.usage.line4")); //$NON-NLS-1$
        System.exit(0);
    }

    private static void showSplashScreen(Display display,
            final String[] args)
    {
    	LOG.debug("Loading splash screen ...");
        final Image image = new Image(display, MailsterSWT.class
                .getResourceAsStream("/org/mailster/gui/resources/splash.png")); //$NON-NLS-1$
        GC gc = new GC(image);
        gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        gc.drawText(ConfigurationManager.MAILSTER_COPYRIGHT, 5, 280, true);
        gc.dispose();
        final Shell splash = new Shell(SWT.ON_TOP);
        Label label = new Label(splash, SWT.NONE);
        label.setImage(image);
        label.setBounds(image.getBounds());
        splash.setBounds(label.getBounds());

        DialogUtils.centerShellOnScreen(splash);
        splash.open();
        
        LOG.debug("Starting ...");
        _instance = new MailsterSWT();
        
        display.asyncExec(new Runnable() {
            public void run()
            {
                try
                {
                	long start = System.currentTimeMillis();
                    startApplication(args);
                    long elapsed = System.currentTimeMillis() - start;
                    LOG.debug("Application started in {} ms", elapsed);

                    _instance.sShell.open();

                    if (elapsed < 2000)
                    	Thread.sleep(2000 - elapsed);
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
                splash.close();
                image.dispose();
                _instance.versionCheck();
            }
        });
    }

    private void applyPreferences()
    {
        LOG.debug("Applying startup preferences ...");
    	MailsterPrefStore store = ConfigurationManager.CONFIG_STORE;
        
        if (store.getBoolean(ConfigurationManager.APPLY_MAIN_WINDOW_PARAMS_KEY))
        {
            Point pt = new Point(store.getInt(ConfigurationManager.WINDOW_X_KEY),
                    store.getInt(ConfigurationManager.WINDOW_Y_KEY));
            
            sShell.setLocation(pt);
            
            pt = new Point(store.getInt(ConfigurationManager.WINDOW_WIDTH_KEY),
                    store.getInt(ConfigurationManager.WINDOW_HEIGHT_KEY));
            
            if (pt.x > 0 && pt.y > 0)
                sShell.setSize(pt);
            
            int ratio = store.getInt(ConfigurationManager.FILTER_VIEW_RATIO_KEY);
            if (ratio > 0 && ratio < 100)
                filterViewDivider.setWeights(new int[] {ratio, 100 - ratio});
            
            ratio = store.getInt(ConfigurationManager.TABLE_VIEW_RATIO_KEY);
            if (ratio > 0 && ratio < 100)
                mailView.getDivider().setWeights(new int[] {ratio, 100 - ratio});            
        }
        
        smtpService.setQueueRefreshTimeout(store.
        		getLong(ConfigurationManager.MAIL_QUEUE_REFRESH_INTERVAL_KEY) / 1000);
        
        mailView.setForcedMozillaBrowserUse(
        		store.getInt(ConfigurationManager.PREFERRED_BROWSER_KEY) != 0);
        
        mailView.setPreferredContentType(
        		store.getInt(ConfigurationManager.PREFERRED_CONTENT_TYPE_KEY) == 0 ? 
        	        			SmtpHeadersInterface.TEXT_HTML_CONTENT_TYPE : 
        	        			SmtpHeadersInterface.TEXT_PLAIN_CONTENT_TYPE);
        
        smtpService.getPop3Service().setPort(
        		store.getInt(ConfigurationManager.POP3_PORT_KEY));

        smtpService.getPop3Service().getUserManager().getMailBoxManager().
        	setPop3SpecialAccountLogin(store.
            		getString(ConfigurationManager.POP3_SPECIAL_ACCOUNT_KEY));
        
        smtpService.getPop3Service().setUsingAPOPAuthMethod(store.
    			getBoolean(ConfigurationManager.POP3_ALLOW_APOP_AUTH_METHOD_KEY));

        smtpService.getPop3Service().setSecureAuthRequired(store.
    			getBoolean(ConfigurationManager.POP3_REQUIRE_SECURE_AUTH_METHOD_KEY));
        
        smtpService.getPop3Service().setHost(store.
                getString(ConfigurationManager.POP3_SERVER_KEY));
        
        smtpService.setHostName(store.getString(ConfigurationManager.SMTP_SERVER_KEY));
        
        smtpService.setPort(store.getInt(ConfigurationManager.SMTP_PORT_KEY));
        
        smtpService.setConnectionTimeout(store.
                getInt(ConfigurationManager.SMTP_CONNECTION_TIMEOUT_KEY));
        
        UserManager.setDefaultPassword(
        		store.getString(ConfigurationManager.POP3_PASSWORD_KEY));
        
        Pop3ProtocolHandler.setTimeout(store.
			getInt(ConfigurationManager.POP3_CONNECTION_TIMEOUT_KEY));
        
        SSLProtocol protocol = store.getInt(ConfigurationManager.PREFERRED_SSL_PROTOCOL_KEY) == 0 ? 
				SSLProtocol.SSL : SSLProtocol.TLS;
        boolean clientAuthNeeded = store.getBoolean(ConfigurationManager.AUTH_SSL_CLIENT_KEY);
        
        MinaPop3Connection.setupSSLParameters(protocol, clientAuthNeeded); 
        MailsterSMTPServer.setupSSLParameters(protocol, clientAuthNeeded);
    }
    
    private static void startApplication(String[] args)
    {
    	MailsterSWT main = getInstance();
    	main.smtpService = new MailsterSmtpService();
    	MailsterPrefStore store = ConfigurationManager.CONFIG_STORE;
    	
    	try
        {
            store.load();
        }
        catch (IOException e)
        {
        	LOG.debug("Unable to read preferences file. Loading defaults ...");
        	
        	// Set default preferences
        	store.setValue(ConfigurationManager.MAIL_QUEUE_REFRESH_INTERVAL_KEY, 
        			"300000"); //$NON-NLS-1$
        	store.setValue(ConfigurationManager.ASK_ON_REMOVE_MAIL_KEY, true);
        	store.setValue(ConfigurationManager.APPLY_MAIN_WINDOW_PARAMS_KEY, true);
        	store.setValue(ConfigurationManager.PREFERRED_BROWSER_KEY, 
                    Messages.getString("MailsterSWT.default.browser")); //$NON-NLS-1$
        	store.setValue(ConfigurationManager.PREFERRED_CONTENT_TYPE_KEY, "text/html"); //$NON-NLS-1$
        	
        	store.setValue(ConfigurationManager.NOTIFY_ON_NEW_MESSAGES_RECEIVED_KEY, true);
        	store.setValue(ConfigurationManager.AUTO_HIDE_NOTIFICATIONS_KEY, true);
        	
        	store.setValue(ConfigurationManager.LANGUAGE_KEY, "en");
        	
        	store.setValue(ConfigurationManager.EXECUTE_ENCLOSURE_ON_CLICK_KEY, true);
        	store.setValue(ConfigurationManager.DEFAULT_ENCLOSURES_DIRECTORY_KEY, 
        			System.getProperty("user.home")); //$NON-NLS-1$

        	store.setValue(ConfigurationManager.START_POP3_ON_SMTP_START_KEY, true);
        	
        	store.setValue(ConfigurationManager.POP3_SERVER_KEY, "");
        	store.setValue(ConfigurationManager.POP3_PORT_KEY, 
        			MailsterPop3Service.POP3_PORT);        	
        	store.setValue(ConfigurationManager.POP3_SPECIAL_ACCOUNT_KEY, 
        			MailBoxManager.POP3_SPECIAL_ACCOUNT_LOGIN);
        	store.setValue(ConfigurationManager.POP3_ALLOW_APOP_AUTH_METHOD_KEY, true);
        	store.setValue(ConfigurationManager.POP3_REQUIRE_SECURE_AUTH_METHOD_KEY, true);
        	store.setValue(ConfigurationManager.POP3_PASSWORD_KEY, 
        			UserManager.DEFAULT_PASSWORD);
        	store.setValue(ConfigurationManager.POP3_CONNECTION_TIMEOUT_KEY, 
        			Pop3ProtocolHandler.DEFAULT_TIMEOUT_SECONDS);
        	
        	store.setValue(ConfigurationManager.AUTH_SSL_CLIENT_KEY, true);
        	store.setValue(ConfigurationManager.PREFERRED_SSL_PROTOCOL_KEY, SSLProtocol.TLS.toString());
        	store.setValue(ConfigurationManager.CRYPTO_STRENGTH_KEY, 512);
        	
        	store.setValue(ConfigurationManager.SMTP_SERVER_KEY, "");
        	store.setValue(ConfigurationManager.SMTP_PORT_KEY, 
        			MailsterSMTPServer.DEFAULT_SMTP_PORT);
        	store.setValue(ConfigurationManager.SMTP_CONNECTION_TIMEOUT_KEY, 
        			MailsterSMTPServer.DEFAULT_TIMEOUT / 1000);        	
        }
        
        String localeInfo = store.getString(ConfigurationManager.LANGUAGE_KEY);
        
        if (localeInfo != null && !"".equals(localeInfo))
        {
	        if (localeInfo.indexOf('_') != -1)
	        	Messages.setLocale(new Locale(localeInfo.substring(0, 2), localeInfo.substring(3)));
	        else	
	        	Messages.setLocale(new Locale(localeInfo));
        }
        
    	main.smtpService.setQueueRefreshTimeout(
    			store.getLong(ConfigurationManager.SMTP_CONNECTION_TIMEOUT_KEY)	/ 1000);
        
    	main.smtpService.setAutoStart(
    			store.getBoolean(ConfigurationManager.START_SMTP_ON_STARTUP_KEY));
    	
        if (args.length > 3)
            usage();
        {
            for (int i = 0, max = args.length; i < max; i++)
            {
                if ("-autostart".equals(args[i]))
                	main.smtpService.setAutoStart(true);
                else
            	if (args[i].startsWith("-lang="))
            	{
            		Messages.setLocale(new Locale(args[i].substring(6)));
            	}
                else
                {
                    try
                    {
                    	main.smtpService.setQueueRefreshTimeout(Long.parseLong(args[i]));
                    }
                    catch (NumberFormatException e)
                    {
                        usage();
                    }
                }
            }
        }    	
    	
        final MailsterSWT _main = main;
        Thread.UncaughtExceptionHandler exHandler  = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(final Thread t,
                    final Throwable ex)
            {            
            	ex.printStackTrace();
                Display.getDefault().asyncExec(new Runnable() {
                    public void run()
                    {
                        _main.log(Messages
                            .getString("MailsterSWT.exception.log1") + t.getName() //$NON-NLS-1$
                            + Messages.getString("MailsterSWT.exception.log2") //$NON-NLS-1$
                            + ex.getMessage());                        
                    }
                });
                _main.smtpService.shutdownServer(false);
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(exHandler);
        Thread.currentThread().setUncaughtExceptionHandler(exHandler);
        
        LOG.debug("Creating shell ...");
        main.createSShell();
        main.applyPreferences();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
        	public void run() {
                try
                {                    
                	_main.smtpService.shutdownServer(true);
                    if (_main.trayItem != null)
                    	_main.trayItem.dispose();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
        	};
        });
    }

    public static void main(String[] args)
    {
        Display display = SWTHelper.getDisplay();

        showSplashScreen(display, args);

        while (getInstance().sShell == null || !getInstance().sShell.isDisposed())
        {
            if (display != null && !display.readAndDispatch())
                display.sleep();
        }

        if (display != null)
        	display.dispose();
        
        System.exit(0);
    }

    /**
     * This method initializes sShell
     */
    private void createSShell()
    {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.marginHeight = 1;
        gridLayout.marginWidth = 2;
        sShell = new Shell();
        sShell.setText(ConfigurationManager.MAILSTER_VERSION);
        sShell.setLayout(gridLayout);
        sShell.setImage(trayImage);
        createSystemTray();
        createShellToolBars();

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = GridData.FILL;

        createPShelfAndMailView(sShell);
        
        Composite statusBar = new Composite(sShell, SWT.BORDER);
        statusBar.setLayout(LayoutUtils.createGridLayout(2, false, 0, 1, 0, 0, 1, 1, 0, 0));
        statusBar.setLayoutData(LayoutUtils.createGridData(GridData.FILL, 
        		GridData.CENTER, true, false, 1, 1, SWT.DEFAULT, 18));
        
        CLabel statusLabel = new CLabel(statusBar, SWT.NONE);
        statusLabel.setLayoutData(LayoutUtils.createGridData(GridData.FILL, 
        		GridData.CENTER, true, false, 1, 1, SWT.DEFAULT, SWT.DEFAULT));        
        statusLabel.setText(" "+ConfigurationManager.MAILSTER_COPYRIGHT);
        
        MemoryProgressBar bar = new MemoryProgressBar(statusBar, SWT.SMOOTH | SWT.FLAT);
        bar.setLayoutData(LayoutUtils.createGridData(GridData.END, 
        		GridData.CENTER, false, false, 1, 1, 80, SWT.DEFAULT));
        bar.setMinimum(0);
        bar.setMaximum(100);
        
        sShell.setSize(new Point(800, 600));
        DialogUtils.centerShellOnScreen(sShell);

        smtpService.addSMTPServerListener(new SMTPServerAdapter() {
            public void updateUI(boolean stopped)
            {
                if (sShell.isDisposed())
                    return;

                serverStartMenuItem.setEnabled(stopped);
                serverDebugMenuItem.setEnabled(stopped);
                serverStopMenuItem.setEnabled(!stopped);
                serverStartToolItem.setEnabled(stopped);
                serverDebugToolItem.setEnabled(stopped);
                serverStopToolItem.setEnabled(!stopped);
                
                if (stopped)
                	trayItem.setImage(trayImage);
                else
                	trayItem.setImage(trayRunningImage);
            }

            public void stopped(SMTPServerEvent event)
            {
                Display.getDefault().asyncExec(new Thread() {
                    public void run()
                    {
                        updateUI(true);
                    }
                });
            }

            public void started(SMTPServerEvent event)
            {
                Display.getDefault().asyncExec(new Thread() {
                    public void run()
                    {
                        updateUI(false);
                        if(ConfigurationManager.CONFIG_STORE.
                                getBoolean(ConfigurationManager.SEND_TO_TRAY_ON_SERVER_START_KEY))
                            sShell.setMinimized(true);
                    }
                });
            }
        });

        if (smtpService.isAutoStart())
            smtpService.startServer(
                    System.getProperty("com.dumbster.smtp.debug") != null);
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run()
            {
                try
                {                    
                    smtpService.shutdownServer(true);
                    if (trayItem != null)
                        trayItem.dispose();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }));
        
        sShell.addDisposeListener(new DisposeListener() {        
            public void widgetDisposed(DisposeEvent event)
            {
                try
                {
                    storeWindowOptions();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }        
        });
        
        sShell.addShellListener(new ShellAdapter() {
                /**
                 * Sent when a <code>Shell</code> is closed.
                 * Handles the application's tray behaviour if the enclosing
                 * <code>Shell</code> is about to be closed.
                 *
                 * @param e an event containing information about the close
                 * 
                 * @see org.mailster.gui.prefs.ConfigurationManager#SEND_TO_TRAY_ON_CLOSE_KEY
                 */
                public void shellClosed(ShellEvent e) 
                {
                    if (ConfigurationManager.CONFIG_STORE.getBoolean(
                            ConfigurationManager.SEND_TO_TRAY_ON_CLOSE_KEY)) 
                    {
                        Shell shell = (Shell) e.widget;
                        shell.setVisible(false);
                    }
                }

                /**
                 * Sent when a <code>Shell</code> is minimized.
                 * Handles the application's tray behaviour if the enclosing
                 * <code>Shell</code> is iconified.
                 * 
                 * @param e an event containing information about the minimization
                 * 
                 * @see org.mailster.gui.prefs.ConfigurationManager#SEND_TO_TRAY_ON_MINIMIZE_KEY
                 */
                public void shellIconified(ShellEvent e) 
                {
                    if (ConfigurationManager.CONFIG_STORE.getBoolean(
                            ConfigurationManager.SEND_TO_TRAY_ON_MINIMIZE_KEY)) 
                    {
                        Shell shell = (Shell) e.widget;
                        shell.setVisible(false);
                    }
                }
        });
    }

    private void storeRatio(SashForm divider, String key, IPreferenceStore store)
    {
        int[] weights = divider.getWeights();
        int ratio = (weights[0] * 100) / (weights[0]+ weights[1]);
        store.setValue(key, ratio);
    }
    
    protected void storeWindowOptions()
        throws IOException
    {
        MailsterPrefStore store = ConfigurationManager.CONFIG_STORE;
        Point pt = sShell.getLocation();
        store.setValue(ConfigurationManager.WINDOW_X_KEY, pt.x);
        store.setValue(ConfigurationManager.WINDOW_Y_KEY, pt.y);
        
        pt = sShell.getSize();
        store.setValue(ConfigurationManager.WINDOW_WIDTH_KEY, pt.x);
        store.setValue(ConfigurationManager.WINDOW_HEIGHT_KEY, pt.y);

        storeRatio(filterViewDivider, ConfigurationManager.FILTER_VIEW_RATIO_KEY, store);
        storeRatio(mailView.getDivider(), ConfigurationManager.TABLE_VIEW_RATIO_KEY, store);
        
        ConfigurationManager.CONFIG_STORE.save();
    }
    
    public void showTrayItemTooltipMessage(final String title, final String message)
    {
    	Display.getDefault().asyncExec(new Thread() {
    		public void run() {
    			final ToolTip tip = new ToolTip(sShell, SWT.BALLOON | SWT.ICON_INFORMATION);    	
	        	tip.setMessage(message);
        		tip.setText(title);
	        	
    	    	if (Display.getDefault().getSystemTray() != null) 
	        		trayItem.setToolTip(tip);
	        	else 
	        		tip.setLocation(sShell.getLocation());

    	    	tip.setVisible(true);
	            tip.setAutoHide(ConfigurationManager.CONFIG_STORE.
	            		getBoolean(ConfigurationManager.AUTO_HIDE_NOTIFICATIONS_KEY));    		
    		}
    	});
    }
    	
    private void createSystemTray()
    {
        final Tray tray = Display.getDefault().getSystemTray();
        if (tray != null)
        {
            trayItem = new TrayItem(tray, SWT.NONE);
            trayItem.setToolTipText(ConfigurationManager.MAILSTER_VERSION);
            final Menu menu = createTrayMenu();

            Listener trayListener = new Listener() {
                public void handleEvent(Event e)
                {
                    if (e.type == SWT.Selection)
                    {
                        sShell.setVisible(true);                                                
                        boolean min = sShell.getMinimized();
                        if (min)
                            sShell.setActive();
                        sShell.setMinimized(!min);
                    }
                    else if (e.type == SWT.MenuDetect)
                        menu.setVisible(true);
                }
            };

            trayItem.addListener(SWT.Selection, trayListener);
            trayItem.addListener(SWT.MenuDetect, trayListener);
            trayItem.setImage(trayImage);                        
        }
    }

    private Menu createTrayMenu()
    {
        final Menu menu = new Menu(sShell, SWT.POP_UP);
        serverStartMenuItem = new MenuItem(menu, SWT.PUSH);
        serverStartMenuItem.setText(Messages
                .getString("MailsterSWT.start.label")); //$NON-NLS-1$
        serverStartMenuItem.setImage(startImage);
        serverDebugMenuItem = new MenuItem(menu, SWT.PUSH);
        serverDebugMenuItem.setText(Messages
                .getString("MailsterSWT.debug.label")); //$NON-NLS-1$
        serverDebugMenuItem.setImage(debugImage);
        serverStopMenuItem = new MenuItem(menu, SWT.PUSH);
        serverStopMenuItem
                .setText(Messages.getString("MailsterSWT.stop.label")); //$NON-NLS-1$
        serverStopMenuItem.setImage(stopImage);
        serverStopMenuItem.setEnabled(false);
        new MenuItem(menu, SWT.SEPARATOR);

        final MenuItem quitMenuItem = new MenuItem(menu, SWT.PUSH);
        quitMenuItem.setText(Messages.getString("MailsterSWT.quit.menuitem")); //$NON-NLS-1$

        Listener menuListener = new Listener() {
            public void handleEvent(Event event)
            {
                if (event.widget == serverStartMenuItem)
                    smtpService.startServer(false);
                else if (event.widget == serverDebugMenuItem)
                    smtpService.startServer(true);
                else if (event.widget == serverStopMenuItem)
                    smtpService.shutdownServer(false);
                else if (event.widget == quitMenuItem)
                    sShell.close();
            }
        };

        serverStartMenuItem.addListener(SWT.Selection, menuListener);
        serverDebugMenuItem.addListener(SWT.Selection, menuListener);
        serverStopMenuItem.addListener(SWT.Selection, menuListener);
        quitMenuItem.addListener(SWT.Selection, menuListener);

        return menu;
    }

    public void log(String msg)
    {
        mailView.log(msg);
    }

    public MailsterSmtpService getSMTPService()
    {
        return smtpService;
    }

    public Shell getShell()
    {
        return sShell;
    }
    
    public OutLineView getOutlineView()
    {
    	return outlineView;
    }
    
    public FilterTreeView getFilterTreeView()
    {
    	return treeView;
    }
    
    public void versionCheck()
    {
        (new Thread() {
            public void run() {
		    	try
		    	{
					InputStream in = 
						(new URL(ConfigurationManager.MAILSTER_VERSION_CHECK_URL)).openStream();
					byte[] buf = new byte[64];
					int len=0;
					int offset=0;
					while ((len = in.read(buf, offset, 64-offset)) != -1) {
						offset += len;
					}
					in.close();
			        
					String line = new String(buf, 0, offset);
					int pos = line.indexOf(' ');
					String ver = line.substring(0, pos);
					String currentVer = ConfigurationManager.MAILSTER_VERSION_NB.substring(1);
					
					boolean updateNeeded = 
						currentVer.charAt(0) < ver.charAt(0) ||
						currentVer.charAt(2) < ver.charAt(2) ||
						currentVer.charAt(4) < ver.charAt(4);
					
					String msg = null;
			    	
			    	if (updateNeeded)
			    	{
			    		Date d = new Date(Long.parseLong(line.substring(pos+1)));
			    		StringBuilder sb = new StringBuilder(
			    				Messages.getString("MailView.tray.versioncheck.needUpdate")); //$NON-NLS-1$
			    		sb.append('\n');
			    		sb.append(MessageFormat.format(
			    					Messages.getString("MailView.tray.versioncheck.available"), //$NON-NLS-1$
			    					ver,
			    					DateFormatUtils.ISO_DATE_FORMAT.format(d)));
			    		msg = sb.toString();
			    	}
			    	else
			    		msg = Messages.getString("MailView.tray.versioncheck.upToDate"); //$NON-NLS-1$
			    	
			    	showTrayItemTooltipMessage(
			    			Messages.getString("MailView.tray.versioncheck.title") //$NON-NLS-1$
			    			+DateUtilities.hourDateFormat.format(new Date())+")",  //$NON-NLS-1$
			    			msg);
			    	
			    	if (updateNeeded)
						Display.getDefault().asyncExec(new Thread() {
							public void run() {
								mailView.showURL(
									ConfigurationManager.MAILSTER_DOWNLOAD_PAGE,
									false);
							}
						});
		    	}
		    	catch (Exception ex)
		    	{
		    		ex.printStackTrace();
		    		log("Failed to check if version is up to date."); //$NON-NLS-1$
		    	}
            }
        }).start();
    }
    
    public void configureDragAndDrop(Control ctrl)
    {
    	DropTarget dt = new DropTarget(ctrl, DND.DROP_DEFAULT
				| DND.DROP_MOVE);
		dt.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dt.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) 
			{
				FileTransfer ft = FileTransfer.getInstance();
				if (ft.isSupportedType(event.currentDataType)) 
				{
					String[] files = (String[]) event.data;
					for (String file : files)
					{
						if (file.toLowerCase().endsWith(".eml"))
							ImportExportUtilities.importFromEmailFile(file);
						else
						if (file.toLowerCase().endsWith(".mbx"))
							ImportExportUtilities.importFromMbox(file);								
					}
				}
			}
		});    	
    }
    
    
}