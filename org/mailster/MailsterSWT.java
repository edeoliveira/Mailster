package org.mailster;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.DirectoryDialog;
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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.mailster.gui.AboutDialog;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.views.FilterTreeView;
import org.mailster.gui.views.MailView;
import org.mailster.server.MailsterPop3Service;
import org.mailster.server.MailsterSmtpService;
import org.mailster.smtp.events.SMTPServerAdapter;
import org.mailster.smtp.events.SMTPServerEvent;
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
 * @version %I%, %G%
 */
public class MailsterSWT
{
    /** 
     * Log object for this class. 
     */
    private static final Logger LOG = LoggerFactory.getLogger(MailsterSWT.class);
    
    public final static String MAILSTER_VERSION_NB	= "v0.8.0"; //$NON-NLS-1$
    public final static String MAILSTER_VERSION     = "Mailster "+MAILSTER_VERSION_NB; //$NON-NLS-1$
    public final static String MAILSTER_HOMEPAGE    = "http://mailster.sourceforge.net/"; //$NON-NLS-1$
    public final static String MAILSTER_COPYRIGHT   = "Copyright (C) De Oliveira Edouard 2007"; //$NON-NLS-1$
    
    public final static SimpleDateFormat df = new SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$

    private final Image trayImage = SWTHelper.loadImage("mail_earth.gif"); //$NON-NLS-1$
    private final Image stopImage = SWTHelper.loadImage("stop.gif"); //$NON-NLS-1$
    private final Image startImage = SWTHelper.loadImage("start.gif"); //$NON-NLS-1$
    private final Image debugImage = SWTHelper.loadImage("startDebug.gif"); //$NON-NLS-1$

    // Visual components
    private Shell sShell;
    private TrayItem trayItem;
    private Text log;
    private MailView mailView;

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
        GridLayout layout = new GridLayout((multipleControls ? 3 : 1), false);
        layout.marginLeft = 4;
        layout.marginTop = 4;
        layout.marginRight = 4;
        layout.marginBottom = 4;
        layout.verticalSpacing = multipleControls ? 2 : 0;
        layout.horizontalSpacing = multipleControls ? 2 : 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        
        return layout;
    }

    private void createExpandBarAndMailView(Composite parent)
    {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = GridData.FILL;

        SashForm sash = new SashForm(parent, SWT.NONE);
        sash.setOrientation(SWT.HORIZONTAL);
        sash.setLayoutData(gridData);

        ExpandBar bar = new ExpandBar(sash, SWT.V_SCROLL);
        bar.setSpacing(8);

        // Tree item
        Composite composite = new Composite(bar, SWT.NONE);
        composite.setLayout(createLayout(false));
        
        FilterTreeView treeView = new FilterTreeView(composite);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.heightHint=194;
        gd.horizontalIndent=0;
        treeView.setLayoutData(gd);
        
        createExpandItem(bar, composite, Messages
                .getString("MailsterSWT.expandbar.treeView"), "filter.gif", true); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Options item
        composite = new Composite(bar, SWT.NONE);
        composite.setLayout(createLayout(true));

        GridData gdata = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gdata.horizontalSpan = 1;
        Label label = new Label(composite, SWT.WRAP);
        label.setText(Messages.getString("MailsterSWT.preferred.pop3auth.label")); //$NON-NLS-1$
        label.setLayoutData(gdata);

        gdata = new GridData();
        gdata.grabExcessHorizontalSpace = true;
        gdata.horizontalSpan = 2;
        gdata.horizontalAlignment = GridData.FILL;
        final Combo authCombo = new Combo(composite, SWT.READ_ONLY);
        authCombo.setItems(new String[] { "APOP", "USER/PASS" }); //$NON-NLS-1$ //$NON-NLS-2$
        authCombo.setText("APOP"); //$NON-NLS-1$
        authCombo.setLayoutData(gdata);
        authCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event)
            {
                smtpService.setUsingAPOPAuthMethod(authCombo.getText().equals("APOP"));
            }
        });
        
        gdata = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gdata.horizontalSpan = 1;
        label = new Label(composite, SWT.WRAP);
        label.setText(Messages.getString("MailsterSWT.preferred.pop3port.label")); //$NON-NLS-1$
        label.setLayoutData(gdata);

        gdata = new GridData();
        gdata.grabExcessHorizontalSpace = true;
        gdata.horizontalSpan = 2;
        gdata.horizontalAlignment = GridData.FILL;
        final Text pop3PortText = new Text(composite, SWT.BORDER);
        pop3PortText.setText(MailsterPop3Service.POP3_PORT+""); //$NON-NLS-1$
        pop3PortText.setLayoutData(gdata);
        pop3PortText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event)
            {
                try
                {
                    smtpService.getPop3Service().setPort(
                            new Integer(pop3PortText.getText()).intValue());
                }
                catch (Exception ex) {}
            }
        });
        
        gdata = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gdata.horizontalSpan = 1;
        label = new Label(composite, SWT.WRAP);
        label.setText(Messages.getString("MailsterSWT.preferred.ctype.label")); //$NON-NLS-1$
        label.setLayoutData(gdata);

        gdata = new GridData();
        gdata.grabExcessHorizontalSpace = true;
        gdata.horizontalSpan = 2;
        gdata.horizontalAlignment = GridData.FILL;
        final Combo combo = new Combo(composite, SWT.READ_ONLY);
        combo.setItems(new String[] { "text/html", "text/plain" }); //$NON-NLS-1$ //$NON-NLS-2$
        combo.setText(MailView.DEFAULT_PREFERRED_CONTENT);
        combo.setLayoutData(gdata);
        combo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event)
            {
                getMailView().setPreferredContentType(combo.getText());
            }
        });
        
        // If OS is a Windows system and Xul is available, then give a chance to
        // use it.
        boolean xulAvailable = false;
        Shell testShell = new Shell();
        try
        {
            new Browser(testShell, SWT.BORDER | SWT.MOZILLA);            
            xulAvailable = true;
        }
        catch(SWTError e)
        {
        }
        finally
        {
            testShell.dispose();
        }

        if (xulAvailable
                && System.getProperty("os.name").toLowerCase()
                        .startsWith("win"))
        {
            gdata = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            gdata.horizontalSpan = 1;
            label = new Label(composite, SWT.WRAP);
            label.setText(Messages
                    .getString("MailsterSWT.preferred.browser.label")); //$NON-NLS-1$
            label.setLayoutData(gdata);

            gdata = new GridData();
            gdata.grabExcessHorizontalSpace = true;
            gdata.horizontalSpan = 2;
            gdata.horizontalAlignment = GridData.FILL;
            final Combo comboBrowser = new Combo(composite, SWT.READ_ONLY);
            comboBrowser
                    .setItems(new String[] {
                            Messages.getString("MailsterSWT.default.browser"), "Mozilla/XulRunner" }); //$NON-NLS-1$ //$NON-NLS-2$
            comboBrowser.setText(Messages
                    .getString("MailsterSWT.default.browser")); //$NON-NLS-1$
            comboBrowser.setLayoutData(gdata);
            comboBrowser.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event)
                {
                    getMailView().setForcedMozillaBrowserUse(
                            comboBrowser.getSelectionIndex() != 0);
                }
            });
        }

        gdata = new GridData();
        gdata.horizontalSpan = 2;
        gdata.horizontalAlignment = GridData.FILL;
        label = new Label(composite, SWT.NONE);
        label.setText(Messages.getString("MailsterSWT.refresh.timeout") //$NON-NLS-1$
                        + smtpService.getTimeout() 
                        + Messages.getString("MailsterSWT.refresh.seconds")); //$NON-NLS-1$
        label.setLayoutData(gdata);

        gdata = new GridData();
        gdata.grabExcessHorizontalSpace = true;
        gdata.horizontalSpan = 1;
        gdata.horizontalAlignment = GridData.FILL;
        gdata.widthHint = 60;
        Spinner spinner = new Spinner(composite, SWT.BORDER);
        spinner.setMinimum(0);
        spinner.setMaximum(990);
        spinner.setSelection((int) smtpService.getTimeout());
        spinner.setIncrement(10);
        spinner.setPageIncrement(100);
        spinner.setLayoutData(gdata);

        final Label lbl = label;
        spinner.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event)
            {
                smtpService.setTimeout(((Spinner) event.widget).getSelection());
                lbl.setText(Messages
                    .getString("MailsterSWT.refresh.timeout") + smtpService.getTimeout() //$NON-NLS-1$
                    + Messages.getString("MailsterSWT.refresh.seconds")); //$NON-NLS-1$
            }
        });

        gdata = new GridData();
        gdata.grabExcessHorizontalSpace = true;
        gdata.horizontalSpan = 3;
        gdata.horizontalAlignment = GridData.FILL;
        label = new Label(composite, SWT.WRAP);
        label.setText(Messages
                .getString("MailsterSWT.default.output.directory.label")); //$NON-NLS-1$
        label.setLayoutData(gdata);

        gdata = new GridData();
        gdata.grabExcessHorizontalSpace = true;
        gdata.horizontalSpan = 2;
        gdata.horizontalAlignment = GridData.FILL;
        final Text path = new Text(composite, SWT.BORDER | SWT.SINGLE);
        path.setText(smtpService.getDefaultOutputDirectory());
        path.setEditable(false);
        path.setLayoutData(gdata);

        gdata = new GridData();
        gdata.grabExcessHorizontalSpace = false;
        gdata.horizontalSpan = 1;
        Button dir = new Button(composite, SWT.FLAT | SWT.PUSH);
        dir.setText("..."); //$NON-NLS-1$
        dir.setLayoutData(new GridData());

        dir.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0)
            {
                DirectoryDialog dialog = new DirectoryDialog(sShell);
                dialog.setFilterPath(path.getText());
                dialog.setMessage(Messages
                        .getString("MailsterSWT.default.ouput.dialog.title")); //$NON-NLS-1$
                String p = dialog.open();
                if (p != null)
                {
                    path.setText(p);
                    smtpService.setDefaultOutputDirectory(p);
                }
            }
        });

        createExpandItem(bar, composite, Messages
                .getString("MailsterSWT.expandbar.options"), null, false); //$NON-NLS-1$

        // About item
        composite = new Composite(bar, SWT.NONE);
        composite.setLayout(createLayout(false));

        Link l = new Link(composite, SWT.NONE);
        l.setText(MAILSTER_VERSION 
                    + Messages.getString("MailsterSWT.description.lineAll") //$NON-NLS-1$
                    + MAILSTER_COPYRIGHT);
        l.setToolTipText(MAILSTER_HOMEPAGE);
        l.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event)
            {
                getMailView().showURL(event.text, true);
            }
        });
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalSpan = 3;
        l.setLayoutData(gridData);

        createExpandItem(bar, composite, Messages
                .getString("MailsterSWT.expandbar.about"), null, false); //$NON-NLS-1$

        mailView = new MailView(sash, treeView, this);
        sash.setWeights(new int[] { 29, 71 });
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

        final ToolItem refreshToolItem = new ToolItem(toolBar, SWT.PUSH);
        refreshToolItem.setImage(SWTHelper.loadImage("refresh.gif")); //$NON-NLS-1$
        refreshToolItem.setToolTipText(Messages
                .getString("MailsterSWT.refreshQueue.tooltip")); //$NON-NLS-1$        
        
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
                else if (e.widget == aboutToolItem)
                	(new AboutDialog(sShell, mailView)).open();                	
            }
        };
        
        serverStartToolItem.addSelectionListener(selectionAdapter);
        serverDebugToolItem.addSelectionListener(selectionAdapter);
        serverStopToolItem.addSelectionListener(selectionAdapter);
        refreshToolItem.addSelectionListener(selectionAdapter);
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

        GridLayout layout = new GridLayout(3, false);
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginRight = 0;
        layout.marginLeft = 0;
        layout.horizontalSpacing = 2;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        searchPanel.setLayout(layout);

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
        GridLayout layout = new GridLayout(2, false);
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginRight = 0;
        layout.marginLeft = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;        
        logComposite.setLayout(layout);
        
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        
        log = new Text(logComposite, SWT.MULTI | SWT.H_SCROLL | SWT.BORDER
                | SWT.V_SCROLL);
        
        log.setLayoutData(gridData);
        
        createLogViewToolBar(logComposite);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;        
        logComposite.setLayoutData(gd);
    }

    public static void usage()
    {
        System.out.println(MAILSTER_VERSION + "\n");
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
        gc.drawText(MAILSTER_COPYRIGHT, 5, 280, true);
        gc.dispose();
        final Shell splash = new Shell(SWT.ON_TOP);
        Label label = new Label(splash, SWT.NONE);
        label.setImage(image);
        label.setBounds(image.getBounds());
        splash.setBounds(label.getBounds());

        Rectangle splashRect = splash.getBounds();
        Rectangle displayRect = display.getBounds();
        int x = (displayRect.width - splashRect.width) / 2;
        int y = (displayRect.height - splashRect.height) / 2;
        splash.setLocation(x, y);
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

    private static void startApplication(MailsterSWT main, String[] args)
    {
        final MailsterSWT _main = main;
        if (args.length > 3)
            usage();
        {
            for (int i = 0, max = args.length; i < max; i++)
            {
                if ("-autostart".equals(args[i]))
                    _main.smtpService.setAutoStart(true);
                else
            	if (args[i].startsWith("-lang="))
            	{
            		Messages.setLocale(new Locale(args[i].substring(6)));
            	}
                else
                {
                    try
                    {
                        _main.smtpService.setTimeout(Long.parseLong(args[i]));
                    }
                    catch (NumberFormatException e)
                    {
                        usage();
                    }
                }
            }
        }
        
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

        _main.createSShell();
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

        SWTHelper.disposeAll();
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
        sShell.setText(MAILSTER_VERSION);
        sShell.setLayout(gridLayout);
        sShell.setImage(trayImage);
        createSystemTray();
        createShellToolBars();

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = GridData.FILL;

        SashForm sash = new SashForm(sShell, SWT.NONE);
        sash.setOrientation(SWT.VERTICAL);
        sash.setLayoutData(gridData);

        createExpandBarAndMailView(sash);
        createLogView(sash);
        sash.setWeights(new int[] { 80, 20 });

        sShell.setSize(new Point(800, 600));
        Rectangle splashRect = sShell.getBounds();
        Rectangle displayRect = sShell.getDisplay().getBounds();
        int x = (displayRect.width - splashRect.width) / 2;
        int y = (displayRect.height - splashRect.height) / 2;
        sShell.setLocation(x, y);

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
                        sShell.setMinimized(true);
                    }
                });
            }
        });

        sShell.getDisplay().addFilter(SWT.KeyDown, new Listener() {
            public void handleEvent(Event e)
            {
                if (e.keyCode == SWT.F4 && e.stateMask == SWT.MOD1 + SWT.MOD2)
                    mailView.closeAllTabs();
            }
        });

        if (smtpService.isAutoStart())
            smtpService
                    .startServer(System.getProperty("com.dumbster.smtp.debug") != null);
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
    }
    	
    private void createSystemTray()
    {
        final Tray tray = Display.getDefault().getSystemTray();
        if (tray != null)
        {
            trayItem = new TrayItem(tray, SWT.NONE);
            trayItem.setToolTipText(MAILSTER_VERSION);
            final Menu menu = createTrayMenu();

            Listener trayListener = new Listener() {
                public void handleEvent(Event e)
                {
                    if (e.type == SWT.Selection)
                    {
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
            sShell.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    smtpService.shutdownServer(true);
                    trayItem.dispose();
                }
            });
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
        String logMessage = "[" + df.format(new Date()) + "] " + msg + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (log != null)
        {
            int idx = log.getTopIndex();
            log.append(logMessage);
            if (logViewIsScrollLocked)
                log.setTopIndex(idx);
        }
        else
            LOG.info(logMessage);
    }

    public MailsterSmtpService getSMTPService()
    {
        return smtpService;
    }
}
