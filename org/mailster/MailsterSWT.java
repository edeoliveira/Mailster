package org.mailster;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
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
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
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
import org.mailster.gui.AboutDialog;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.ConfigurationDialog;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.prefs.store.MailsterPrefStore;
import org.mailster.gui.utils.DialogUtils;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.gui.views.FilterTreeView;
import org.mailster.gui.views.MailView;
import org.mailster.pop3.Pop3ProtocolHandler;
import org.mailster.pop3.mailbox.MailBoxManager;
import org.mailster.pop3.mailbox.UserManager;
import org.mailster.server.MailsterPop3Service;
import org.mailster.server.MailsterSmtpService;
import org.mailster.smtp.SimpleSmtpServer;
import org.mailster.smtp.events.SMTPServerAdapter;
import org.mailster.smtp.events.SMTPServerEvent;
import org.mailster.util.DateUtilities;
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
    
    private final Image trayImage = SWTHelper.loadImage("mail_earth.gif"); //$NON-NLS-1$
    private final Image stopImage = SWTHelper.loadImage("stop.gif"); //$NON-NLS-1$
    private final Image startImage = SWTHelper.loadImage("start.gif"); //$NON-NLS-1$
    private final Image debugImage = SWTHelper.loadImage("startdebug.gif"); //$NON-NLS-1$

    // Visual components
    private Shell sShell;
    private TrayItem trayItem;
    private Text log;
    private MailView mailView;
    
    private SashForm logDivider;
    private SashForm filterViewDivider;

    private MenuItem serverStartMenuItem;
    private MenuItem serverDebugMenuItem;
    private MenuItem serverStopMenuItem;
    private ToolItem serverStartToolItem;
    private ToolItem serverDebugToolItem;
    private ToolItem serverStopToolItem;

    private Text filterTextField;
    
    private boolean logViewIsScrollLocked;

    private MailsterSmtpService smtpService = new MailsterSmtpService(this);

    public MailView getMailView()
    {
        return mailView;
    }

    private void createExpandItem(ExpandBar bar, final Composite composite,
            String text, String imageName, boolean expanded)
    {
        final ExpandItem item = new ExpandItem(bar, SWT.NONE);
        item.setText(text);
        item.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        item.setControl(composite);
        item.setImage(imageName == null ? trayImage : SWTHelper.loadImage(imageName));
        item.setExpanded(expanded);
        
        composite.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event e) 
            {
                Point size = composite.getSize();
                Point size2 = composite.computeSize(size.x, SWT.DEFAULT);
                item.setHeight(size2.y);
            }
        });
    }

    private GridLayout createLayout(boolean multipleControls)
    {
    	int spacing = multipleControls ? 2 : 0;
    	return LayoutUtils.createGridLayout(
    			(multipleControls ? 3 : 1), false, 0, 0, 4, 4, 4, 4, spacing, spacing);
    }

    private void createExpandBarAndMailView(Composite parent)
    {
        filterViewDivider = new SashForm(parent, SWT.NONE);
        filterViewDivider.setOrientation(SWT.HORIZONTAL);
        filterViewDivider.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        ExpandBar bar = new ExpandBar(filterViewDivider, SWT.V_SCROLL);
        bar.setSpacing(6);

        // Tree item
        Composite composite = new Composite(bar, SWT.NONE);
        composite.setLayout(createLayout(false));
        
        FilterTreeView treeView = new FilterTreeView(composite);
        treeView.setLayoutData(LayoutUtils.createGridData(
                GridData.FILL, GridData.FILL, true, true, 1, 1, 264, SWT.DEFAULT));
        
        createExpandItem(bar, composite, Messages
                .getString("MailsterSWT.expandbar.treeView"), "filter.gif", true); //$NON-NLS-1$ //$NON-NLS-2$
        
        // About item
        composite = new Composite(bar, SWT.NONE);
        composite.setLayout(createLayout(false));

        Link l = new Link(composite, SWT.NONE);
        l.setText(ConfigurationManager.MAILSTER_VERSION + "\n\n"
                    + ConfigurationManager.MAILSTER_COPYRIGHT);
        l.setToolTipText(ConfigurationManager.MAILSTER_HOMEPAGE);
        l.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event)
            {
                getMailView().showURL(event.text, true);
            }
        });

        l.setLayoutData(LayoutUtils.createGridData(
                GridData.FILL, GridData.FILL, true, true, 3, 1, SWT.DEFAULT, SWT.DEFAULT));

        createExpandItem(bar, composite, Messages
                .getString("MailsterSWT.expandbar.about"), null, true); //$NON-NLS-1$

        mailView = new MailView(filterViewDivider, treeView, this);
        filterViewDivider.setWeights(new int[] { 29, 71 });
        bar.layout(true);
    }

    private void createLogViewToolBar(Composite parent)
    {
        final CoolBar coolBar = new CoolBar(parent, SWT.VERTICAL | SWT.FLAT);
        ToolBar toolBar = new ToolBar(coolBar, SWT.VERTICAL);
        
        ToolItem clearLogToolItem = new ToolItem(toolBar, SWT.PUSH);
        clearLogToolItem.setImage(SWTHelper.loadImage("clear.gif")); //$NON-NLS-1$
        clearLogToolItem.setToolTipText(Messages
                .getString("MailsterSWT.clear.tooltip")); //$NON-NLS-1$
        clearLogToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                log.setText(""); //$NON-NLS-1$
            }
        });

        final ToolItem scrollLockToolItem = new ToolItem(toolBar, SWT.CHECK);
        scrollLockToolItem.setImage(SWTHelper.loadImage("lockscroll.gif")); //$NON-NLS-1$
        scrollLockToolItem.setToolTipText(Messages
                .getString("MailsterSWT.scrollLock.tooltip")); //$NON-NLS-1$
        scrollLockToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                logViewIsScrollLocked = scrollLockToolItem.getSelection();                
            }
        });
        
        GridData gridData = new GridData(GridData.FILL_VERTICAL);        
        gridData.grabExcessVerticalSpace = true;        
        coolBar.setLayoutData(gridData);
        
        CoolItem coolItem = new CoolItem(coolBar, SWT.NONE);
        coolItem.setControl(toolBar);
        Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point coolSize = coolItem.computeSize(size.x, size.y);
        coolItem.setSize(coolSize);
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

        new ToolItem(toolBar, SWT.SEPARATOR);
        
        final ToolItem refreshToolItem = new ToolItem(toolBar, SWT.PUSH);
        refreshToolItem.setImage(SWTHelper.loadImage("refresh.gif")); //$NON-NLS-1$
        refreshToolItem.setToolTipText(Messages
                .getString("MailsterSWT.refreshQueue.tooltip")); //$NON-NLS-1$        

        final ToolItem configToolItem = new ToolItem(toolBar, SWT.PUSH);
        configToolItem.setImage(SWTHelper.loadImage("config.gif")); //$NON-NLS-1$
        configToolItem.setToolTipText(Messages
                .getString("MailsterSWT.config.tooltip")); //$NON-NLS-1$  
        
        new ToolItem(toolBar, SWT.SEPARATOR);
        
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
                else if (e.widget == refreshToolItem)
                    smtpService.refreshEmailQueue(false);
                else if (e.widget == configToolItem)
                    ConfigurationDialog.run(sShell);
                else if (e.widget == aboutToolItem)
                	(new AboutDialog(sShell, mailView)).open();                	
            }
        };
        
        serverStartToolItem.addSelectionListener(selectionAdapter);
        serverDebugToolItem.addSelectionListener(selectionAdapter);
        serverStopToolItem.addSelectionListener(selectionAdapter);
        refreshToolItem.addSelectionListener(selectionAdapter);
        configToolItem.addSelectionListener(selectionAdapter);
        aboutToolItem.addSelectionListener(selectionAdapter);

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
                        getMailView().executeJavaScript("Hilite.clearHighlighting();"); //$NON-NLS-1$
                    else
                    {
                    	getMailView().executeJavaScript("Hilite.storeHTML();"); //$NON-NLS-1$
                    	getMailView().executeJavaScript("Hilite.hilite(new Array ('"+filterTextField.getText()+"'));"); //$NON-NLS-1$
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

    private void createLogView(Composite parent)
    {
        final Composite logComposite = new Composite(parent, SWT.NONE);
        logComposite.setLayout(
                LayoutUtils.createGridLayout(2, false, 0, 0, 0, 0, 0, 0, 0, 0));
        
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        
        log = new Text(logComposite, SWT.MULTI | SWT.BORDER
                | SWT.V_SCROLL | SWT.WRAP );
        
        log.setLayoutData(gridData);
        
        createLogViewToolBar(logComposite);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;        
        logComposite.setLayoutData(gd);
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

    private static MailsterSWT showSplashScreen(Display display,
            final String[] args)
    {
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
        final MailsterSWT main = new MailsterSWT();
        display.asyncExec(new Runnable() {
            public void run()
            {
                try
                {
                    startApplication(main, args);
                    main.sShell.open();
                    Thread.sleep(1000);
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
                splash.close();
                image.dispose();
            }
        });

        return main;
    }

    private void applyPreferences()
    {
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
            
            int ratio = store.getInt(ConfigurationManager.LOG_DIVIDER_RATIO_KEY);
            if (ratio > 0 && ratio < 100)
                logDivider.setWeights(new int[] {ratio, 100 - ratio});
            
            ratio = store.getInt(ConfigurationManager.FILTER_VIEW_RATIO_KEY);
            if (ratio > 0 && ratio < 100)
                filterViewDivider.setWeights(new int[] {ratio, 100 - ratio});
            
            ratio = store.getInt(ConfigurationManager.TABLE_VIEW_RATIO_KEY);
            if (ratio > 0 && ratio < 100)
                mailView.getDivider().setWeights(new int[] {ratio, 100 - ratio});            
        }
        
        smtpService.setQueueRefreshTimeout(store.
        		getLong(ConfigurationManager.MAIL_QUEUE_REFRESH_INTERVAL_KEY) / 1000);
        
        mailView.setForcedMozillaBrowserUse(
        		store.getString(ConfigurationManager.PREFERRED_BROWSER_KEY).
        		toLowerCase().startsWith("mozilla"));
        
        mailView.setPreferredContentType(store.
        		getString(ConfigurationManager.PREFERRED_CONTENT_TYPE_KEY));
        
        smtpService.getPop3Service().setPort(
        		store.getInt(ConfigurationManager.POP3_PORT_KEY));

        smtpService.getPop3Service().getUserManager().getMailBoxManager().
        	setPop3SpecialAccountLogin(store.
            		getString(ConfigurationManager.POP3_SPECIAL_ACCOUNT_KEY));
        
        smtpService.setUsingAPOPAuthMethod(store.
    			getString(ConfigurationManager.POP3_AUTH_METHOD_KEY).equals("APOP"));
        
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
    }
    
    private static void startApplication(MailsterSWT main, String[] args)
    {
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
        	store.setValue(ConfigurationManager.POP3_AUTH_METHOD_KEY, "APOP"); //$NON-NLS-1$
        	store.setValue(ConfigurationManager.POP3_PASSWORD_KEY, 
        			UserManager.DEFAULT_PASSWORD);
        	store.setValue(ConfigurationManager.POP3_CONNECTION_TIMEOUT_KEY, 
        			Pop3ProtocolHandler.DEFAULT_TIMEOUT_SECONDS);
        	
        	store.setValue(ConfigurationManager.SMTP_SERVER_KEY, "");
        	store.setValue(ConfigurationManager.SMTP_PORT_KEY, 
        			SimpleSmtpServer.DEFAULT_SMTP_PORT);
        	store.setValue(ConfigurationManager.SMTP_CONNECTION_TIMEOUT_KEY, 
        			SimpleSmtpServer.DEFAULT_TIMEOUT / 1000);        	
        }
        
        store.setMailsterMainWindow(main);
        
        String localeInfo = store.getString(ConfigurationManager.LANGUAGE_KEY);
        
        if (localeInfo.indexOf('_') != -1)
        	Messages.setLocale(new Locale(localeInfo.substring(0, 2), localeInfo.substring(3)));
        else
        	Messages.setLocale(new Locale(localeInfo));
    	
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
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(final Thread t,
                    final Throwable ex)
            {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run()
                    {
                        _main.log(Messages
                            .getString("MailsterSWT.exception.log1") + t.getName() //$NON-NLS-1$
                            + Messages
                                    .getString("MailsterSWT.exception.log2") //$NON-NLS-1$
                            + ex.getCause().toString());
                        System.out.println(ex.getCause());
                    }
                });
                _main.smtpService.shutdownServer(false);
            }
        });
        main.createSShell();
        main.applyPreferences();      
    }

    public static void main(String[] args)
    {
        Display display = SWTHelper.getDisplay();

        MailsterSWT main = showSplashScreen(display, args);

        while (main.sShell == null || !main.sShell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }

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

        logDivider = new SashForm(sShell, SWT.NONE);
        logDivider.setOrientation(SWT.VERTICAL);
        logDivider.setLayoutData(gridData);

        createExpandBarAndMailView(logDivider);
        createLogView(logDivider);
        logDivider.setWeights(new int[] { 80, 20 });

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

        storeRatio(logDivider, ConfigurationManager.LOG_DIVIDER_RATIO_KEY, store);
        storeRatio(filterViewDivider, ConfigurationManager.FILTER_VIEW_RATIO_KEY, store);
        storeRatio(mailView.getDivider(), ConfigurationManager.TABLE_VIEW_RATIO_KEY, store);
        
        ConfigurationManager.CONFIG_STORE.save();
    }
    
    public void showTrayItemTooltipMessage(String title, String message)
    {
    	final ToolTip tip = new ToolTip(sShell, SWT.BALLOON | SWT.ICON_INFORMATION);
    	final Tray tray = Display.getDefault().getSystemTray();
    	
    	tip.setMessage(message);    	
    	if (tray != null) 
    	{
    		tip.setText(title);
    		trayItem.setToolTip(tip);
    	} 
    	else 
    	{
    		tip.setText(title);
    		tip.setLocation(sShell.getLocation());
    	}
    	tip.setVisible(true);
        tip.setAutoHide(ConfigurationManager.CONFIG_STORE.
        		getBoolean(ConfigurationManager.AUTO_HIDE_NOTIFICATIONS_KEY));
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
        if (log != null && !log.isDisposed())
        {
            String date = DateUtilities.df.format(new Date());
            StringBuilder sb = new StringBuilder(3+date.length()+msg.length());
            sb.append('['); //$NON-NLS-1$
            sb.append(date);
            sb.append(']'); //$NON-NLS-1$
            sb.append(msg);
            sb.append('\n'); //$NON-NLS-1$
            
            int idx = log.getTopIndex();
            log.append(sb.toString());
            if (logViewIsScrollLocked)
                log.setTopIndex(idx);
        }
        else
            LOG.info(msg);
    }

    public MailsterSmtpService getSMTPService()
    {
        return smtpService;
    }
}
