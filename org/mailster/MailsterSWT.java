package org.mailster;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
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
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.mailster.gui.MailView;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.server.SMTPServerControl;
import org.mailster.smtp.events.SMTPServerAdapter;
import org.mailster.smtp.events.SMTPServerEvent;

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
 * MailsterSWT.java - Enter your Comment HERE.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class MailsterSWT
{
    public final static String MAILSTER_VERSION = "Mailster v0.6.1"; //$NON-NLS-1$
    public final static SimpleDateFormat df = new SimpleDateFormat(
            "dd/MM/yyyy kk:mm:ss"); //$NON-NLS-1$

    private SWTHelper sWTHelper = new SWTHelper();
    private final Image trayImage = sWTHelper.loadImage("mail_earth.gif"); //$NON-NLS-1$
    private final Image stopImage = sWTHelper.loadImage("stop.gif"); //$NON-NLS-1$
    private final Image startImage = sWTHelper.loadImage("start.gif"); //$NON-NLS-1$
    private final Image debugImage = sWTHelper.loadImage("startDebug.gif"); //$NON-NLS-1$

    // Visual components
    private Shell sShell;
    private Text log;
    private MailView mailView;

    private MenuItem serverStartMenuItem;
    private MenuItem serverDebugMenuItem;
    private MenuItem serverStopMenuItem;
    private ToolItem serverStartToolItem;
    private ToolItem serverDebugToolItem;
    private ToolItem serverStopToolItem;

    private SMTPServerControl ctrl = new SMTPServerControl(this);

    public MailView getMailView()
    {
        return mailView;
    }

    private void createExpandItem(ExpandBar bar, Composite composite,
            String text, boolean expanded)
    {
        ExpandItem item = new ExpandItem(bar, SWT.NONE);
        item.setText(text);
        item.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        item.setControl(composite);
        item.setImage(trayImage);
        item.setExpanded(expanded);
    }

    private GridLayout createLayout()
    {
        GridLayout layout = new GridLayout(3, false);
        layout.marginLeft = 4;
        layout.marginTop = 4;
        layout.marginRight = 4;
        layout.marginBottom = 4;
        layout.verticalSpacing = 4;

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

        // Options item
        Composite composite = new Composite(bar, SWT.NONE);
        composite.setLayout(createLayout());

        GridData gdata = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gdata.horizontalSpan = 1;
        Label label = new Label(composite, SWT.WRAP);
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

        if (System.getProperty("os.name").toLowerCase().startsWith("win"))
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
        label
                .setText(Messages.getString("MailsterSWT.refresh.timeout") + ctrl.getTimeout() + Messages.getString("MailsterSWT.refresh.seconds")); //$NON-NLS-1$ //$NON-NLS-2$
        label.setLayoutData(gdata);

        gdata = new GridData();
        gdata.grabExcessHorizontalSpace = true;
        gdata.horizontalSpan = 1;
        gdata.horizontalAlignment = GridData.FILL;
        Spinner spinner = new Spinner(composite, SWT.BORDER);
        spinner.setMinimum(0);
        spinner.setMaximum(990);
        spinner.setSelection((int) ctrl.getTimeout());
        spinner.setIncrement(10);
        spinner.setPageIncrement(100);
        spinner.setLayoutData(gdata);

        final Label lbl = label;
        spinner.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event)
            {
                ctrl.setTimeout(((Spinner) event.widget).getSelection());
                lbl
                        .setText(Messages
                                .getString("MailsterSWT.refresh.timeout") + ctrl.getTimeout() //$NON-NLS-1$
                                + Messages
                                        .getString("MailsterSWT.refresh.seconds")); //$NON-NLS-1$
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
        path.setText(ctrl.getDefaultOutputDirectory());
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
                    ctrl.setDefaultOutputDirectory(p);
                }
            }
        });

        createExpandItem(bar, composite, Messages
                .getString("MailsterSWT.expandbar.options"), false); //$NON-NLS-1$

        // About item
        composite = new Composite(bar, SWT.NONE);
        composite.setLayout(createLayout());

        Link l = new Link(composite, SWT.NONE);
        l.setText(MAILSTER_VERSION
                + Messages.getString("MailsterSWT.description.line1") //$NON-NLS-1$
                + Messages.getString("MailsterSWT.description.line2") //$NON-NLS-1$
                + Messages.getString("MailsterSWT.description.line3") //$NON-NLS-1$
                + Messages.getString("MailsterSWT.description.line4") //$NON-NLS-1$
                + Messages.getString("MailsterSWT.description.line5") //$NON-NLS-1$
                + Messages.getString("MailsterSWT.description.line6") //$NON-NLS-1$
                + Messages.getString("MailsterSWT.description.line7") //$NON-NLS-1$
                + Messages.getString("MailsterSWT.description.line8") //$NON-NLS-1$
                + "Copyright (c) De Oliveira Edouard 2007"); //$NON-NLS-1$
        l.setToolTipText("http://mailster.sourceforge.net"); //$NON-NLS-1$
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
        gridData.verticalSpan = 2;
        l.setLayoutData(gridData);

        createExpandItem(bar, composite, Messages
                .getString("MailsterSWT.expandbar.about"), true); //$NON-NLS-1$

        mailView = new MailView(sash, this);
        sash.setWeights(new int[] { 32, 68 });
    }

    /**
     * This method initializes toolBar
     */
    private void createToolBar()
    {
        CoolBar coolBar = new CoolBar(sShell, SWT.FLAT);

        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.BEGINNING;
        coolBar.setLayoutData(gridData);
        ToolBar toolBar = new ToolBar(coolBar, SWT.NONE);
        toolBar.setLayoutData(new FillLayout());

        SelectionAdapter adapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                if (e.widget == serverStartToolItem)
                    ctrl.startServer(false);
                else if (e.widget == serverDebugToolItem)
                    ctrl.startServer(true);
                else if (e.widget == serverStopToolItem)
                    ctrl.shutdownServer(false);
            }
        };

        serverStartToolItem = new ToolItem(toolBar, SWT.PUSH);
        serverStartToolItem.setImage(startImage);
        serverStartToolItem.setToolTipText(Messages
                .getString("MailsterSWT.start.label")); //$NON-NLS-1$
        serverStartToolItem.addSelectionListener(adapter);

        serverDebugToolItem = new ToolItem(toolBar, SWT.PUSH);
        serverDebugToolItem.setImage(debugImage);
        serverDebugToolItem.setToolTipText(Messages
                .getString("MailsterSWT.debug.label")); //$NON-NLS-1$
        serverDebugToolItem.addSelectionListener(adapter);

        serverStopToolItem = new ToolItem(toolBar, SWT.PUSH);
        serverStopToolItem.setImage(stopImage);
        serverStopToolItem.setEnabled(false);
        serverStopToolItem.setToolTipText(Messages
                .getString("MailsterSWT.stop.label")); //$NON-NLS-1$
        serverStopToolItem.addSelectionListener(adapter);

        ToolItem refreshButton = new ToolItem(toolBar, SWT.PUSH);
        refreshButton.setImage(sWTHelper.loadImage("refresh.gif")); //$NON-NLS-1$
        refreshButton.setToolTipText(Messages
                .getString("MailsterSWT.refreshQueue.tooltip")); //$NON-NLS-1$
        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                ctrl.refreshEmailQueue();
            }
        });

        ToolItem clearLogButton = new ToolItem(toolBar, SWT.PUSH);
        clearLogButton.setImage(sWTHelper.loadImage("clear.gif")); //$NON-NLS-1$
        clearLogButton.setToolTipText(Messages
                .getString("MailsterSWT.clear.tooltip")); //$NON-NLS-1$
        clearLogButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                log.setText(""); //$NON-NLS-1$
            }
        });

        // Add a coolItem to the coolBar
        CoolItem coolItem = new CoolItem(coolBar, SWT.NULL);
        coolItem.setControl(toolBar);
        Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point coolSize = coolItem.computeSize(size.x, size.y);
        coolItem.setSize(coolSize);
    }

    private void createLogView(Composite parent)
    {
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.verticalAlignment = GridData.BEGINNING;
        gridData.heightHint = 60;
        gridData.grabExcessHorizontalSpace = true;
        log = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.BORDER
                | SWT.V_SCROLL);
        log.setLayoutData(gridData);
    }

    public static void usage()
    {
        System.out.println(MAILSTER_VERSION + "\n");
        System.out.println(Messages.getString("MailsterSWT.usage.line1")); //$NON-NLS-1$
        System.out.println(Messages.getString("MailsterSWT.usage.line2")); //$NON-NLS-1$
        System.out.println(Messages.getString("MailsterSWT.usage.line3")); //$NON-NLS-1$
        System.exit(0);
    }

    private static MailsterSWT showSplashScreen(Display display,
            final String[] args)
    {
        final Image image = new Image(display, MailsterSWT.class
                .getResourceAsStream("/org/mailster/gui/resources/splash.png")); //$NON-NLS-1$
        GC gc = new GC(image);
        gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        gc.drawText("Copyright (C) De Oliveira Edouard 2007", 5, //$NON-NLS-1$
                280, true);
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
        Thread.currentThread().setUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    public void uncaughtException(final Thread t,
                            final Throwable ex)
                    {
                        Display.getDefault().asyncExec(new Runnable() {
                            public void run()
                            {
                                _main
                                        .log(Messages
                                                .getString("MailsterSWT.exception.log1") + t.getName() //$NON-NLS-1$
                                                + Messages
                                                        .getString("MailsterSWT.exception.log2") //$NON-NLS-1$
                                                + ex.getCause().toString());
                            }
                        });
                        _main.ctrl.shutdownServer(false);
                    }
                });

        if (args.length > 2)
            usage();
        {
            for (int i = 0, max = args.length; i < max; i++)
            {
                if ("-autostart".equals(args[i]))
                    _main.ctrl.setAutoStart(true);
                else
                {
                    try
                    {
                        _main.ctrl.setTimeout(Long.parseLong(args[i]));
                    }
                    catch (NumberFormatException e)
                    {
                        usage();
                    }
                }
            }
        }

        _main.createSShell();
    }

    public static void main(String[] args)
    {
        Display display = Display.getDefault();

        MailsterSWT main = showSplashScreen(display, args);

        while (main.sShell == null || !main.sShell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }

        main.getSWTHelper().disposeAll();
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
        createToolBar();

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

        ctrl.addSMTPServerListener(new SMTPServerAdapter() {
            public void updateUI(boolean stopped)
            {
                serverStartMenuItem.setEnabled(stopped);
                serverDebugMenuItem.setEnabled(stopped);
                serverStopMenuItem.setEnabled(!stopped);
                serverStartToolItem.setEnabled(stopped);
                serverDebugToolItem.setEnabled(stopped);
                serverStopToolItem.setEnabled(!stopped);
            }

            public void stopped(SMTPServerEvent event)
            {
                Display.getDefault().syncExec(new Thread() {
                    public void run()
                    {
                        updateUI(true);
                    }
                });
            }

            public void started(SMTPServerEvent event)
            {
                Display.getDefault().syncExec(new Thread() {
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

        if (ctrl.isAutoStart())
            ctrl
                    .startServer(System.getProperty("com.dumbster.smtp.debug") != null);
    }

    private void createSystemTray()
    {
        final Tray tray = Display.getCurrent().getSystemTray();
        if (tray != null)
        {
            final TrayItem trayItem = new TrayItem(tray, SWT.NONE);
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
                    ctrl.shutdownServer(true);
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
                    ctrl.startServer(false);
                else if (event.widget == serverDebugMenuItem)
                    ctrl.startServer(true);
                else if (event.widget == serverStopMenuItem)
                    ctrl.shutdownServer(false);
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
            log.append(logMessage);
        else
            System.out.println(logMessage);
    }

    public String getDefaultOutputDirectory()
    {
        return ctrl.getDefaultOutputDirectory();
    }

    public SWTHelper getSWTHelper()
    {
        return sWTHelper;
    }
}
