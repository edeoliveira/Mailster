package org.mailster;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.mailster.util.MailUtilities;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpHeaders;
import com.dumbster.smtp.SmtpMessage;

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
 * See&nbsp; <a href="http://mailster.sourceforge.org" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * MailsterSWT.java - Enter your comment HERE.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class MailsterSWT
{
    private final static String MAILSTER_VERSION = "Mailster/SWT v0.1";
    private final static long DEFAULT_REFRESH_TIMEOUT = 5;
    private final static SimpleDateFormat df = new SimpleDateFormat(
	    "dd-MM-yyyy kk:mm:ss");

    private final static Image trayImage = new Image(Display.getCurrent(),
	    MailsterSWT.class
		    .getResourceAsStream("/org/mailster/gui/icons/mail_earth.png"));

    private final static Image homeImage = new Image(Display.getCurrent(),
	    MailsterSWT.class
		    .getResourceAsStream("/org/mailster/gui/icons/home.gif"));  //  @jve:decl-index=0:
    
    private final static Image stopImage = new Image(Display.getCurrent(),
	    MailsterSWT.class
		    .getResourceAsStream("/org/mailster/gui/icons/Stop16.gif"));  //  @jve:decl-index=0:

    private final static Image startImage = new Image(Display.getCurrent(),
	    MailsterSWT.class
		    .getResourceAsStream("/org/mailster/gui/icons/Play16.gif"));

    private Hashtable<String, SmtpMessage> retrievedMessages = new Hashtable<String, SmtpMessage>();
    private long timeout = DEFAULT_REFRESH_TIMEOUT;
    private MailQueueControl updater = new MailQueueControl();
    private ArrayList<SmtpMessage> data = new ArrayList<SmtpMessage>();
    private SimpleSmtpServer server;  //  @jve:decl-index=0:

    // Visual components
    private TableColumn to, subject, date;
    private Shell sShell;
    private Browser browser;
    private Text log;
    private Text headersView;
    private Table table;
    private TrayItem trayItem;
    private Shell about = null;  //  @jve:decl-index=0:visual-constraint="126,0"
    private Link link = null;
    private Button close = null;

    class MailQueueControl
    {
	private final ScheduledExecutorService scheduler = Executors
		.newScheduledThreadPool(1);

	private final Runnable mq = new Runnable()
	{
	    public void run()
	    {
		Display.getDefault().asyncExec(new MailQueueObserver());
	    }
	};

	private ScheduledFuture handle = null;

	public void start()
	{
	    handle = scheduler.scheduleWithFixedDelay(mq, timeout, timeout,
		    SECONDS);
	}

	public void stop()
	{
	    handle.cancel(false);
	    data.clear();
	}
    }

    class MailQueueObserver implements Runnable
    {
	public void run()
	{
	    if (table.isDisposed())
		return;

	    int queueSize = table.getItemCount();

	    if (server == null || server.isStopped())
		log("ERROR - Server not started");
	    else
	    {
		int nb = server.getReceivedEmailSize() - queueSize;
		log("Updating email queue (" + nb + "/"
			+ server.getReceivedEmailSize() + " new msgs) ... ");
		Iterator it = server.getReceivedEmail();

		while (it.hasNext())
		{
		    SmtpMessage msg = (SmtpMessage) it.next();
		    String id = msg
			    .getHeaderValue(SmtpHeaders.HEADER_MESSAGE_ID);
		    if (retrievedMessages.get(id) == null)
		    {
			retrievedMessages.put(id, msg);
			data.add(msg);
		    }
		}
		table.setItemCount(data.size());
		table.clearAll();
	    }
	}
    }

    /**
         * This method initializes toolBar
         */
    private void createToolBar()
    {
	CoolBar coolBar = new CoolBar(sShell, SWT.FLAT);

	GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	gridData.grabExcessHorizontalSpace = true;
	gridData.verticalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
	coolBar.setLayoutData(gridData);
	ToolBar toolBar = new ToolBar(coolBar, SWT.NONE);
	toolBar.setLayoutData(new FillLayout());
	final ToolItem runButton = new ToolItem(toolBar, SWT.CHECK);
	runButton.setImage(startImage);
	runButton.setToolTipText("start/stop server");
	runButton.addSelectionListener(new SelectionListener()
	{
	    public void widgetSelected(SelectionEvent e)
	    {
		if (runButton.getSelection())
		{
		    startServer();
		    runButton.setImage(stopImage);
		}
		else
		{
		    shutdownServer(false);
		    runButton.setImage(startImage);
		}
	    }

	    public void widgetDefaultSelected(SelectionEvent e)
	    {}
	});
	ToolItem refreshButton = new ToolItem(toolBar, SWT.PUSH);
	refreshButton.setImage(new Image(Display.getCurrent(), getClass()
		.getResourceAsStream("/org/mailster/gui/icons/Refresh16.gif")));
	refreshButton.setToolTipText("get new messages");
	refreshButton.addSelectionListener(new SelectionListener()
	{
	    public void widgetSelected(SelectionEvent e)
	    {
		refreshEmailQueue();
	    }

	    public void widgetDefaultSelected(SelectionEvent e)
	    {}
	});

	ToolItem clearLogButton = new ToolItem(toolBar, SWT.PUSH);
	clearLogButton.setImage(new Image(Display.getCurrent(), getClass()
		.getResourceAsStream("/org/mailster/gui/icons/Remove16.gif")));
	clearLogButton.setToolTipText("clear log");
	clearLogButton.addSelectionListener(new SelectionListener()
	{
	    public void widgetSelected(SelectionEvent e)
	    {
		log.setText("");
	    }

	    public void widgetDefaultSelected(SelectionEvent e)
	    {}
	});
	
	ToolItem homeButton = new ToolItem(toolBar, SWT.PUSH);
	homeButton.setImage(homeImage);
	homeButton.setToolTipText("About ...");
	homeButton.addSelectionListener(new SelectionListener()
	{
	    public void widgetSelected(SelectionEvent e)
	    {
		createAbout();
	    }

	    public void widgetDefaultSelected(SelectionEvent e)
	    {}
	});

	// Add a coolItem to a coolBar
	CoolItem coolItem = new CoolItem(coolBar, SWT.NULL);
	// set the control of the coolItem
	coolItem.setControl(toolBar);
	// You have to specify the size
	Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	Point coolSize = coolItem.computeSize(size.x, size.y);
	coolItem.setSize(coolSize);
    }

    private void createLog(Composite parent)
    {
	GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	gridData.verticalAlignment = GridData.BEGINNING;
	gridData.heightHint = 60;
	gridData.grabExcessHorizontalSpace = true;
	log = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.BORDER
		| SWT.V_SCROLL);
	log.setLayoutData(gridData);
	log(MAILSTER_VERSION+" Open Source Project is licensed under GPL (http://mailster.sourceforge.net)");
    }

    private void createBrowser(Composite parent)
    {
	browser = new Browser(parent, SWT.BORDER);
    }

    private void createHeadersView(Composite parent)
    {
	GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	gridData.verticalAlignment = GridData.BEGINNING;
	gridData.heightHint = 60;
	gridData.grabExcessHorizontalSpace = true;
	headersView = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.BORDER
		| SWT.V_SCROLL);
	headersView.setLayoutData(gridData);
	headersView.setEditable(false);
    }

    /**
         * This method initializes sashForm
         */
    private void createSashForm(Composite parent)
    {
	GridData gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace = true;
	gridData.verticalAlignment = GridData.FILL;
	SashForm sashForm = new SashForm(parent, SWT.NONE);
	sashForm.setOrientation(SWT.VERTICAL);
	sashForm.setLayoutData(gridData);
	createTable(sashForm);
	createTabFolder(sashForm);
	createLog(sashForm);
	sashForm.setWeights(new int[] { 20, 60, 20 });
    }

    private void createTable(Composite parent)
    {
	table = new Table(parent, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION);
	table.setHeaderVisible(true);
	table.setLinesVisible(true);
	table.setItemCount(0);

	to = new TableColumn(table, SWT.NONE);
	to.setText("To");
	to.setResizable(true);
	to.setMoveable(true);
	to.setWidth(100);
	subject = new TableColumn(table, SWT.NONE);
	subject.setText("Subject");
	subject.setResizable(true);
	subject.setMoveable(true);
	subject.setWidth(100);
	date = new TableColumn(table, SWT.NONE);
	date.setText("Date");
	date.setResizable(true);
	date.setMoveable(true);
	date.setWidth(100);

	table.addListener(SWT.Selection, new Listener()
	{
	    public void handleEvent(Event event)
	    {
		SmtpMessage msg = (SmtpMessage) table.getSelection()[0].getData();
     		try
		{
		    browser.setText(MailUtilities.outputBody(msg));
		}
		catch (IOException e) {log(e.toString());}
     		headersView.setText(msg.outputHeadersToString());
	    }
	});
	table.pack();

	table.addListener(SWT.SetData, new Listener()
	{
	    public void handleEvent(Event e)
	    {
		TableItem item = (TableItem) e.item;
		SmtpMessage msg = (SmtpMessage) data.get(table.indexOf(item));
		item.setText(new String[] {
			msg.getHeaderValue(SmtpHeaders.HEADER_TO),
			msg.getHeaderValue(SmtpHeaders.HEADER_SUBJECT),
			msg.getHeaderValue(SmtpHeaders.HEADER_DATE) });
		item.setData(msg);
	    }
	});
    }

    public void setTimeout(long timeout)
    {
	this.timeout = timeout;
    }

    /**
     * This method initializes about	
     *
     */
    private void createAbout()
    {
        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
        GridData gridData1 = new GridData();
        gridData1.grabExcessVerticalSpace = true;
        gridData1.verticalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
        gridData1.grabExcessHorizontalSpace = true;
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 1;
        about = new Shell(sShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        //about = new Shell();
        about.setText("About ...");
        about.setLayout(gridLayout1);
        about.setImage(trayImage);
        about.setSize(new Point(305, 180));
        link = new Link(about, SWT.NONE);
        link.setText(MAILSTER_VERSION
        	+ " Open Source Project is licensed under GPL (<a href=\"http://mailster.sourceforge.org\">http://mailster.sourceforge.net</a>). "
		+ "It's based on the simple Open Source SMTP project " 
		+ "<a href=\"http://dumbster.sourceforge.org\">Dumbster</a>.\n\n"
		+ "Main goal is to provide a NO source code "
		+ "modification test environnement for "
		+ "mailing applications.\n\n"
		+ "Copyright (c) De Oliveira Edouard 2007");
        link.setLayoutData(gridData1);
        close = new Button(about, SWT.NONE);
        close.setText("Ok");
        close.setLayoutData(gridData2);
        close.addSelectionListener(new SelectionAdapter()
	{
	    public void widgetSelected(SelectionEvent e)
	    {
		about.close();
	    }
	});
        about.open();
    }

    public static void main(String[] args)
    {
	Display display = Display.getDefault();
	final MailsterSWT thisClass = new MailsterSWT();
	Thread.currentThread().setUncaughtExceptionHandler(
		new Thread.UncaughtExceptionHandler()
		{
		    public void uncaughtException(final Thread t,
			    final Throwable ex)
		    {
			Display.getDefault().asyncExec(new Runnable()
			{
			    public void run()
			    {
				thisClass.log("Thread " + t.getName()
					+ " exception - "
					+ ex.getCause().toString());
			    }
			});
			thisClass.shutdownServer(false);
		    }
		});

	if (args.length == 1)
	    thisClass.setTimeout(Long.parseLong(args[0]));

	thisClass.createSShell();
	thisClass.sShell.open();
	thisClass.sShell.setText(MAILSTER_VERSION);
	thisClass.updateTableColumnsWidth();

	while (!thisClass.sShell.isDisposed())
	{
	    if (!display.readAndDispatch())
		display.sleep();
	}
	display.dispose();
	System.exit(0);
    }

    private void updateTableColumnsWidth()
    {
	int w = (table.getSize().x - (table.getBorderWidth() * (table
		.getColumnCount() - 1)))
		/ table.getColumnCount();

	for (int i = 0, max = table.getColumnCount(); i < max; i++)
	    table.getColumn(i).setWidth(w);
    }

    private void createTabFolder(Composite parent)
    {
	TabFolder folder = new TabFolder(parent, SWT.NONE);
	TabItem mail = new TabItem(folder, SWT.BORDER);
	mail.setText("Body");
	createBrowser(folder);
	mail.setControl(browser);
	TabItem headers = new TabItem(folder, SWT.NONE);
	headers.setText("Headers");
	createHeadersView(folder);
	headers.setControl(headersView);
    }

    /**
         * This method initializes sShell
         */
    private void createSShell()
    {
	GridLayout gridLayout = new GridLayout();
	gridLayout.numColumns = 1;
	sShell = new Shell();
	sShell.setText("Shell");
	sShell.setLayout(gridLayout);
	sShell.setImage(trayImage);
	createSystemTray();
	createToolBar();
	createSashForm(sShell);	
	sShell.setSize(new Point(800, 600));
	sShell.addDisposeListener(new DisposeListener()
	{
	    public void widgetDisposed(DisposeEvent e)
	    {
		shutdownServer(true);
		trayItem.dispose();
	    }
	});
	sShell.addControlListener(new ControlListener()
	{
	    public void controlResized(ControlEvent e)
	    {
		updateTableColumnsWidth();
	    }

	    public void controlMoved(ControlEvent e)
	    {}
	});
    }

    private void createSystemTray()
    {
	Display display = Display.getDefault();
	final Tray tray = display.getSystemTray();
	if (tray == null)
	    log("Error - The system tray is not available");
	else
	{
	    trayItem = new TrayItem(tray, SWT.NONE);
	    trayItem.setToolTipText(MAILSTER_VERSION);
	    trayItem.addListener(SWT.Selection, new Listener()
	    {
		public void handleEvent(Event event)
		{
		    boolean min = sShell.getMinimized();
		    if (min)
			sShell.setActive();
		    sShell.setMinimized(!min);
		}
	    });

	    final Menu menu = new Menu(sShell, SWT.POP_UP);
	    final MenuItem serverCtrl = new MenuItem(menu, SWT.PUSH);
	    serverCtrl.setText("Server");
	    serverCtrl.setImage(startImage);
	    serverCtrl.addListener(SWT.Selection, new Listener()
	    {
		public void handleEvent(Event event)
		{

		    if (!serverCtrl.getSelection())
		    {
			startServer();
			serverCtrl.setImage(stopImage);
		    }
		    else
		    {
			shutdownServer(false);
			serverCtrl.setImage(startImage);
		    }
		}
	    });

	    new MenuItem(menu, SWT.SEPARATOR);
	    final MenuItem quitMenu = new MenuItem(menu, SWT.PUSH);
	    quitMenu.setText("Quit");
	    quitMenu.addListener(SWT.Selection, new Listener()
	    {
		public void handleEvent(Event event)
		{
		    sShell.dispose();
		}
	    });

	    trayItem.addListener(SWT.MenuDetect, new Listener()
	    {
		public void handleEvent(Event event)
		{
		    menu.setVisible(true);
		}
	    });
	    trayItem.setImage(trayImage);
	}
    }

    private void refreshEmailQueue()
    {
	(new MailQueueObserver()).run();
    }

    private void startServer()
    {
	retrievedMessages.clear();
	table.removeAll();
	server = SimpleSmtpServer.start();
	updater.start();
	log(MAILSTER_VERSION + " started (refresh timeout set to " + timeout
		+ " seconds) ...");
    }

    private void shutdownServer(boolean force)
    {
	try
	{
	    updater.stop();
	    if (!server.isStopped())
	    {
		server.stop();
		log("Server stopped successfully");
	    }
	}
	catch (Exception ex)
	{
	    log("ERROR - server failed to stop ...");
	}
    }

    private void log(String msg)
    {
	log.append("[" + df.format(new Date()) + "] " + msg + "\n");
    }
}
