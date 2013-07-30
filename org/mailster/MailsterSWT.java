package org.mailster;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateFormatUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TrayItem;
import org.mailster.core.crypto.X509SecureSocketFactory.SSLProtocol;
import org.mailster.core.mail.SmtpHeadersInterface;
import org.mailster.core.pop3.MailsterPop3Service;
import org.mailster.core.pop3.Pop3ProtocolHandler;
import org.mailster.core.pop3.connection.MinaPop3Connection;
import org.mailster.core.pop3.mailbox.MailBoxManager;
import org.mailster.core.pop3.mailbox.UserManager;
import org.mailster.core.smtp.MailsterSMTPServer;
import org.mailster.core.smtp.MailsterSmtpService;
import org.mailster.gui.MailsterSWTTrayItem;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.dialogs.AboutDialog;
import org.mailster.gui.prefs.ConfigurationDialog;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.prefs.store.MailsterPrefStore;
import org.mailster.gui.utils.DialogUtils;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.gui.views.FilterTreeView;
import org.mailster.gui.views.ImportExportUtilities;
import org.mailster.gui.views.MultiView;
import org.mailster.gui.views.OutLineView;
import org.mailster.gui.views.mailbox.MailBoxView;
import org.mailster.gui.widgets.MemoryProgressBar;
import org.mailster.gui.widgets.PShelfPanelListener;
import org.mailster.gui.widgets.PshelfPanel;
import org.mailster.util.DateUtilities;
import org.mailster.util.StringUtilities;
import org.mailster.util.DateUtilities.DateFormatterEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ---<br>
 * Mailster (C) 2007-2009 De Oliveira Edouard
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * <p>
 * See&nbsp; <a href="http://tedorg.free.fr/en/projects.php" target="_parent">Mailster Web Site</a>
 * <br>
 * ---
 * <p>
 * MailsterSWT.java - The main Mailster class.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.23 $, $Date: 2009/04/05 12:13:27 $
 */
public class MailsterSWT
{
	/**
	 * Log object for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(MailsterSWT.class);

	public static final Color BGCOLOR = SWTHelper.createColor(230, 234, 238);

	private static final Color[] BORDER_COLORS = new Color[] {SWTHelper.createColor(227, 230, 234), SWTHelper.createColor(222, 226, 230),
			SWTHelper.createColor(217, 220, 224), SWTHelper.createColor(211, 215, 218), SWTHelper.createColor(165, 172, 181)};

	public static final Image trayImage = SWTHelper.loadImage("mail_earth.gif"); //$NON-NLS-1$
	public static final Image trayRunningImage = SWTHelper.loadImage("mail_earth_running.gif"); //$NON-NLS-1$
	public static final Image stopImage = SWTHelper.loadImage("stop.gif"); //$NON-NLS-1$
	public static final Image startImage = SWTHelper.loadImage("start.gif"); //$NON-NLS-1$
	public static final Image debugImage = SWTHelper.loadImage("startdebug.gif"); //$NON-NLS-1$

	public static ToolItem serverStartToolItem;
	public static ToolItem serverDebugToolItem;
	public static ToolItem serverStopToolItem;

	private final ScheduledExecutorService svc = Executors.newSingleThreadScheduledExecutor();
	
	// Visual components
	private Shell sShell;
	private CoolBar coolBar;
	private TrayItem trayItem;
	private MultiView multiView;
	private OutLineView outlineView;
	private FilterTreeView treeView;
	private MailBoxView mailBoxView;
	private PshelfPanel pshelfPanel;

	private SashForm filterViewDivider;
	private SashForm mailSash;

	private MailsterSmtpService smtpService;

	private static MailsterSWT _instance;

	public static MailsterSWT getInstance()
	{
		return _instance;
	}

	public MultiView getMultiView()
	{
		return multiView;
	}

	public MailBoxView getMailBoxView()
	{
		return mailBoxView;
	}

	private void createPShelfAndMailView(final Composite parent)
	{
		parent.setBackground(BGCOLOR);
		parent.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e)
			{
				Rectangle r = coolBar.getBounds();
				int h = r.y + r.height;
				for (int i = 0, max = BORDER_COLORS.length; i < max; i++)
				{
					e.gc.setForeground(BORDER_COLORS[max - i - 1]);
					e.gc.drawLine(0, h + i, r.width + 8, h + i);
				}
			}
		});

		filterViewDivider = new SashForm(parent, SWT.NONE);
		filterViewDivider.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_SIZEWE));
		filterViewDivider.setOrientation(SWT.HORIZONTAL);
		filterViewDivider.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		filterViewDivider.setSashWidth(3);
		filterViewDivider.setBackground(BGCOLOR);

		Cursor normal = parent.getDisplay().getSystemCursor(SWT.CURSOR_ARROW);

		pshelfPanel = new PshelfPanel(filterViewDivider, SWT.NONE);
		pshelfPanel.setBackground(BGCOLOR);
		pshelfPanel.setCursor(normal);
		pshelfPanel.setTitle(Messages.getString("MailsterSWT.mailpanel.title")); //$NON-NLS-1$
		pshelfPanel.setLayoutData(LayoutUtils.createGridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
		pshelfPanel.addPShelfPanelListener(new PShelfPanelListener() {
			private int[] savedWeights;
			
			public void handleEvent(Event e)
			{
				if (pshelfPanel.isToolbarVisible())
				{
					Point max = parent.getSize();
				
					if (max.x > 0)
					{
						Point pt = pshelfPanel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
						savedWeights = filterViewDivider.getWeights();						
						int p = (pt.x * 100) / max.x;
						if (filterViewDivider.getWeights()[0] != p)
							filterViewDivider.setWeights(new int[] {p, 100 - p});
					}
				}
				else if (savedWeights != null)
					filterViewDivider.setWeights(savedWeights);
			}
		});
		
		Composite treeItem = pshelfPanel.addPShelfItem(Messages.getString("MailsterSWT.expandbar.treeView"), //$NON-NLS-1$
				SWTHelper.loadImage("filter.gif"), SWT.BORDER); //$NON-NLS-1$
		treeItem.setLayout(LayoutUtils.createGridLayout(1, false, 1, 1, 1, 1, 0, 0, 0, 0));
				
		treeView = new FilterTreeView(treeItem, true);
		treeView.setLayoutData(LayoutUtils.createGridData(GridData.FILL, GridData.FILL, true, true, 1, 1));

		final Composite mailComposite = new Composite(filterViewDivider, SWT.NONE);
		final FillLayout fl = new FillLayout();
		fl.marginWidth = 5;
		fl.marginHeight = 10;

		mailComposite.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e)
			{
				Rectangle r = mailComposite.getBounds();
				for (int i = 0, max = BORDER_COLORS.length; i < max; i++)
				{
					e.gc.setForeground(BORDER_COLORS[i]);
					int d = 1 + (i * 2);
					int mw = fl.marginWidth - max;
					int mh = fl.marginHeight - max;
					e.gc.drawRectangle(i + mw, i + mh, r.width - d - mw, r.height - d - mh * 2);
				}
			}
		});

		mailComposite.setBackground(BGCOLOR);
		mailComposite.setLayout(fl);
		mailComposite.setCursor(normal);

		mailSash = new SashForm(mailComposite, SWT.NONE);
		mailSash.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_SIZEWE));
		mailSash.setOrientation(SWT.HORIZONTAL);
		mailSash.setSashWidth(2);

		Composite c0 = new Composite(mailSash, SWT.NONE);
		c0.setCursor(normal);
		mailBoxView = new MailBoxView(c0);
		multiView = new MultiView(mailSash);

		mailSash.setWeights(new int[] {35, 65});

		Composite outlineItem = pshelfPanel.addPShelfItem(Messages.getString("MailsterSWT.expandbar.outlineView"), //$NON-NLS-1$
				SWTHelper.loadImage("outline.gif"), SWT.BORDER); //$NON-NLS-1$
		outlineItem.setLayout(LayoutUtils.createGridLayout(1, false, 1, 1, 1, 1, 0, 0, 0, 0));

		outlineView = new OutLineView(outlineItem, true);
		outlineView.setLayoutData(LayoutUtils.createGridData(GridData.FILL, GridData.FILL, true, true, 1, 1));

		filterViewDivider.setWeights(new int[] {20, 80});
	}

	private void createShellToolBars()
	{
		coolBar = new CoolBar(sShell, SWT.FLAT);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.heightHint = 26;
		gridData.verticalIndent = 2;
		coolBar.setLayoutData(gridData);
		coolBar.setLocked(true);
		coolBar.setBackground(BGCOLOR);
		ToolBar toolBar = new ToolBar(coolBar, SWT.NONE);

		serverStartToolItem = new ToolItem(toolBar, SWT.PUSH);
		serverStartToolItem.setImage(startImage);
		serverStartToolItem.setToolTipText(Messages.getString("MailsterSWT.start.label")); //$NON-NLS-1$

		serverDebugToolItem = new ToolItem(toolBar, SWT.PUSH);
		serverDebugToolItem.setImage(debugImage);
		serverDebugToolItem.setToolTipText(Messages.getString("MailsterSWT.debug.label")); //$NON-NLS-1$

		serverStopToolItem = new ToolItem(toolBar, SWT.PUSH);
		serverStopToolItem.setImage(stopImage);
		serverStopToolItem.setEnabled(false);
		serverStopToolItem.setToolTipText(Messages.getString("MailsterSWT.stop.label")); //$NON-NLS-1$        

		ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);
		item.setWidth(item.getWidth() * 2);

		final ToolItem configToolItem = new ToolItem(toolBar, SWT.PUSH);
		configToolItem.setImage(SWTHelper.loadImage("config.gif")); //$NON-NLS-1$
		configToolItem.setToolTipText(Messages.getString("MailsterSWT.config.tooltip")); //$NON-NLS-1$  

		item = new ToolItem(toolBar, SWT.SEPARATOR);
		item.setWidth(item.getWidth() * 2);

		final ToolItem homeToolItem = new ToolItem(toolBar, SWT.PUSH);
		homeToolItem.setImage(SWTHelper.loadImage("home.gif")); //$NON-NLS-1$
		homeToolItem.setToolTipText(Messages.getString("MailView.home.page.tooltip")); //$NON-NLS-1$  

		final ToolItem changelogToolItem = new ToolItem(toolBar, SWT.PUSH);
		final Image changeLogImage = SWTHelper.loadImage("changelog.gif"); //$NON-NLS-1$
		changelogToolItem.setImage(changeLogImage);
		changelogToolItem.setToolTipText(Messages.getString("MailsterSWT.changelog.tooltip")); //$NON-NLS-1$ 

		final ToolItem versionCheckToolItem = new ToolItem(toolBar, SWT.PUSH);
		versionCheckToolItem.setImage(SWTHelper.loadImage("versioncheck.gif")); //$NON-NLS-1$
		versionCheckToolItem.setToolTipText(Messages.getString("MailsterSWT.versioncheck.tooltip")); //$NON-NLS-1$ 

		final ToolItem showLogViewToolItem = new ToolItem(toolBar, SWT.PUSH);
		showLogViewToolItem.setImage(SWTHelper.loadImage("console_view.gif")); //$NON-NLS-1$
		showLogViewToolItem.setToolTipText(Messages.getString("MailsterSWT.showLogView.tooltip")); //$NON-NLS-1$
		
		item = new ToolItem(toolBar, SWT.SEPARATOR);
		item.setWidth(item.getWidth() * 2);

		final ToolItem aboutToolItem = new ToolItem(toolBar, SWT.PUSH);
		aboutToolItem.setImage(SWTHelper.loadImage("about.gif")); //$NON-NLS-1$
		aboutToolItem.setToolTipText(Messages.getString("MailsterSWT.about.tooltip")); //$NON-NLS-1$  

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
					(new AboutDialog(sShell, multiView)).open();
				else 
				{
					if (e.widget == changelogToolItem)
						multiView.showURL(changeLogImage, "file://" + System.getProperty("user.dir") + File.separator
								+ "changelog.htm", Messages.getString("MailsterSWT.changelog.tooltip"), false);
					else if (e.widget == homeToolItem)
						multiView.showURL(ConfigurationManager.MAILSTER_HOMEPAGE, false, true);
					else if (e.widget == versionCheckToolItem)
						versionCheck();
					else if (e.widget == showLogViewToolItem)
						multiView.createLogConsole(true);
				}
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
		showLogViewToolItem.addSelectionListener(selectionAdapter);

		// Add a coolItem to the coolBar
		CoolItem coolItem = new CoolItem(coolBar, SWT.NONE);
		coolItem.setControl(toolBar);
		Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point coolSize = coolItem.computeSize(size.x, size.y);
		coolItem.setSize(coolSize);
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

	private static void showSplashScreen(Display display, final String[] args)
	{
		LOG.debug("Loading splash screen ...");
		final Image image = new Image(display, MailsterSWT.class.getResourceAsStream("/org/mailster/gui/resources/splash.png")); //$NON-NLS-1$
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
				} catch (Throwable e)
				{
					e.printStackTrace();
				}
				splash.close();
				image.dispose();
				_instance.versionCheck();
				_instance.loadAndSetupAutosave();
			}
		});
	}

	private void loadAndSetupAutosave()
	{
		LOG.debug("Loading autosaved mailbox ...");
		final File f = new File("autosave.mbx");
		if (f.exists())
			ImportExportUtilities.importFromMbox(f.getName());

		svc.schedule(new Runnable() {
			public void run()
			{
				SWTHelper.getDisplay().asyncExec(new Runnable() {
					public void run()
					{
						File tmpFile = new File("autosave" + UUID.randomUUID() + ".tmp");
						if (ImportExportUtilities.exportAsMbox(tmpFile.getName(), true))
						{
							f.delete();
							tmpFile.renameTo(f);
						}
					}
				});
			}
		}, 10, TimeUnit.MINUTES);
	}

	private void applyPreferences()
	{
		LOG.debug("Applying startup preferences ...");
		MailsterPrefStore store = ConfigurationManager.CONFIG_STORE;
		
		if (!(new File(ConfigurationManager.CONFIGURATION_FILENAME)).exists())
			return;
			
		if (store.getBoolean(ConfigurationManager.APPLY_MAIN_WINDOW_PARAMS_KEY))
		{
			pshelfPanel.setToolbarVisible(store.getBoolean(ConfigurationManager.MAIL_PANEL_MINIMIZED_KEY));
			Point pt = new Point(store.getInt(ConfigurationManager.WINDOW_X_KEY), store.getInt(ConfigurationManager.WINDOW_Y_KEY));

			sShell.setLocation(pt);

			pt = new Point(store.getInt(ConfigurationManager.WINDOW_WIDTH_KEY), store
					.getInt(ConfigurationManager.WINDOW_HEIGHT_KEY));

			if (pt.x > 0 && pt.y > 0)
				sShell.setSize(pt);

			extractRatio(filterViewDivider, ConfigurationManager.FILTER_VIEW_RATIO_KEY, store);

			extractRatio(mailSash, ConfigurationManager.TABLE_VIEW_RATIO_KEY, store);
		}

		smtpService.setQueueRefreshTimeout(store.getLong(ConfigurationManager.MAIL_QUEUE_REFRESH_INTERVAL_KEY) / 1000);

		multiView.setForcedMozillaBrowserUse(store.getInt(ConfigurationManager.PREFERRED_BROWSER_KEY) != 0);

		getMultiView().getMailView().setPreferredContentType(
				store.getInt(ConfigurationManager.PREFERRED_CONTENT_TYPE_KEY) == 0 ? SmtpHeadersInterface.TEXT_HTML_CONTENT_TYPE
						: SmtpHeadersInterface.TEXT_PLAIN_CONTENT_TYPE);

		smtpService.getPop3Service().setPort(store.getInt(ConfigurationManager.POP3_PORT_KEY));

		smtpService.getPop3Service().getUserManager().getMailBoxManager().setPop3SpecialAccountLogin(
				store.getString(ConfigurationManager.POP3_SPECIAL_ACCOUNT_KEY));

		smtpService.getPop3Service().setUsingAPOPAuthMethod(
				store.getBoolean(ConfigurationManager.POP3_ALLOW_APOP_AUTH_METHOD_KEY));

		smtpService.getPop3Service().setSecureAuthRequired(
				store.getBoolean(ConfigurationManager.POP3_REQUIRE_SECURE_AUTH_METHOD_KEY));

		smtpService.getPop3Service().setHost(store.getString(ConfigurationManager.POP3_SERVER_KEY));

		smtpService.setHostName(store.getString(ConfigurationManager.SMTP_SERVER_KEY));

		smtpService.setPort(store.getInt(ConfigurationManager.SMTP_PORT_KEY));

		smtpService.setConnectionTimeout(store.getInt(ConfigurationManager.SMTP_CONNECTION_TIMEOUT_KEY));

		UserManager.setDefaultPassword(store.getString(ConfigurationManager.POP3_PASSWORD_KEY));

		Pop3ProtocolHandler.setTimeout(store.getInt(ConfigurationManager.POP3_CONNECTION_TIMEOUT_KEY));

		SSLProtocol protocol = store.getInt(ConfigurationManager.PREFERRED_SSL_PROTOCOL_KEY) == 0 ? SSLProtocol.SSL
				: SSLProtocol.TLS;
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
		} catch (IOException e)
		{
			LOG.debug("Unable to read preferences file. Loading defaults ...");

			// Set default preferences
			store.setValue(ConfigurationManager.MAIL_QUEUE_REFRESH_INTERVAL_KEY, "300000"); //$NON-NLS-1$
			store.setValue(ConfigurationManager.ASK_ON_REMOVE_MAIL_KEY, true);
			store.setValue(ConfigurationManager.APPLY_MAIN_WINDOW_PARAMS_KEY, true);
			store.setValue(ConfigurationManager.PREFERRED_BROWSER_KEY, Messages.getString("MailsterSWT.default.browser")); //$NON-NLS-1$
			store.setValue(ConfigurationManager.PREFERRED_CONTENT_TYPE_KEY, "text/html"); //$NON-NLS-1$

			store.setValue(ConfigurationManager.NOTIFY_ON_NEW_MESSAGES_RECEIVED_KEY, true);
			store.setValue(ConfigurationManager.AUTO_HIDE_NOTIFICATIONS_KEY, true);

			store.setValue(ConfigurationManager.LANGUAGE_KEY, "en");

			store.setValue(ConfigurationManager.EXECUTE_ENCLOSURE_ON_CLICK_KEY, true);
			store.setValue(ConfigurationManager.DEFAULT_ENCLOSURES_DIRECTORY_KEY, System.getProperty("user.home")); //$NON-NLS-1$

			store.setValue(ConfigurationManager.START_POP3_ON_SMTP_START_KEY, true);

			store.setValue(ConfigurationManager.POP3_SERVER_KEY, "");
			store.setValue(ConfigurationManager.POP3_PORT_KEY, MailsterPop3Service.POP3_PORT);
			store.setValue(ConfigurationManager.POP3_SPECIAL_ACCOUNT_KEY, MailBoxManager.POP3_SPECIAL_ACCOUNT_LOGIN);
			store.setValue(ConfigurationManager.POP3_ALLOW_APOP_AUTH_METHOD_KEY, true);
			store.setValue(ConfigurationManager.POP3_REQUIRE_SECURE_AUTH_METHOD_KEY, true);
			store.setValue(ConfigurationManager.POP3_PASSWORD_KEY, UserManager.DEFAULT_PASSWORD);
			store.setValue(ConfigurationManager.POP3_CONNECTION_TIMEOUT_KEY, Pop3ProtocolHandler.DEFAULT_TIMEOUT_SECONDS);

			store.setValue(ConfigurationManager.AUTH_SSL_CLIENT_KEY, true);
			store.setValue(ConfigurationManager.PREFERRED_SSL_PROTOCOL_KEY, SSLProtocol.TLS.toString());
			store.setValue(ConfigurationManager.CRYPTO_STRENGTH_KEY, 512);

			store.setValue(ConfigurationManager.SMTP_SERVER_KEY, "");
			store.setValue(ConfigurationManager.SMTP_PORT_KEY, MailsterSMTPServer.DEFAULT_SMTP_PORT);
			store.setValue(ConfigurationManager.SMTP_CONNECTION_TIMEOUT_KEY, MailsterSMTPServer.DEFAULT_TIMEOUT / 1000);
		}

		String localeInfo = store.getString(ConfigurationManager.LANGUAGE_KEY);

		if (localeInfo != null && !"".equals(localeInfo))
		{
			if (localeInfo.indexOf('_') != -1)
				Messages.setLocale(new Locale(localeInfo.substring(0, 2), localeInfo.substring(3)));
			else
				Messages.setLocale(new Locale(localeInfo));
		}

		main.smtpService.setQueueRefreshTimeout(store.getLong(ConfigurationManager.SMTP_CONNECTION_TIMEOUT_KEY) / 1000);

		main.smtpService.setAutoStart(store.getBoolean(ConfigurationManager.START_SMTP_ON_STARTUP_KEY));

		if (args.length > 3)
			usage();
		{
			for (int i = 0, max = args.length; i < max; i++)
			{
				if ("-autostart".equals(args[i]))
					main.smtpService.setAutoStart(true);
				else if (args[i].startsWith("-lang="))
				{
					Messages.setLocale(new Locale(args[i].substring(6)));
				}
				else
				{
					try
					{
						main.smtpService.setQueueRefreshTimeout(Long.parseLong(args[i]));
					} catch (NumberFormatException e)
					{
						usage();
					}
				}
			}
		}

		final MailsterSWT _main = main;
		Thread.UncaughtExceptionHandler exHandler = new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(final Thread t, final Throwable ex)
			{
				ex.printStackTrace();
				Display.getDefault().asyncExec(new Runnable() {
					public void run()
					{
						_main.log(Messages.getString("MailsterSWT.exception.log1") + t.getName() //$NON-NLS-1$
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
			public void run()
			{
				try
				{
					_main.smtpService.shutdownServer(true);
					if (_main.trayItem != null)
						_main.trayItem.dispose();
				} catch (Exception ex)
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
		trayItem = (new MailsterSWTTrayItem()).getTrayItem();
		createShellToolBars();

		createPShelfAndMailView(sShell);

		Composite statusBar = new Composite(sShell, SWT.BORDER);
		statusBar.setLayout(LayoutUtils.createGridLayout(3, false, 0, 1, 0, 0, 1, 1, 0, 0));
		statusBar.setLayoutData(LayoutUtils.createGridData(GridData.FILL, GridData.CENTER, true, false, 1, 1, SWT.DEFAULT, 18));

		Link l = new Link(statusBar, SWT.NONE);
		l.setLayoutData(LayoutUtils.createGridData(GridData.FILL, GridData.CENTER, true, true, 1, 1, SWT.DEFAULT, SWT.DEFAULT));
		l.setText(ConfigurationManager.MAILSTER_VERSION + " - " + ConfigurationManager.MAILSTER_COPYRIGHT);
		l.setToolTipText(ConfigurationManager.MAILSTER_HOMEPAGE);
		l.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event)
			{
				multiView.switchTopControl(false);
				multiView.showURL(ConfigurationManager.MAILSTER_HOMEPAGE, true, true);
			}
		});

		MemoryProgressBar bar = new MemoryProgressBar(statusBar, SWT.SMOOTH | SWT.FLAT);
		bar.setLayoutData(LayoutUtils.createGridData(GridData.END, GridData.CENTER, false, true, 1, 1, 80, SWT.DEFAULT));
		bar.setMinimum(0);
		bar.setMaximum(100);

		sShell.setSize(new Point(800, 600));
		DialogUtils.centerShellOnScreen(sShell);

		if (smtpService.isAutoStart())
			smtpService.startServer(System.getProperty("org.mailster.smtp.debug") != null);

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run()
			{
				try
				{
					smtpService.shutdownServer(true);
					if (trayItem != null)
						trayItem.dispose();
				} catch (Exception ex)
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
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});

		sShell.getDisplay().addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e)
			{
				if (e.keyCode == SWT.F5)
					getSMTPService().refreshEmailQueue(false);
			}
		});

		sShell.addShellListener(new ShellAdapter() {
			/**
			 * Sent when a <code>Shell</code> is closed. Handles the application's tray behaviour if
			 * the enclosing <code>Shell</code> is about to be closed.
			 * 
			 * @param e
			 *            an event containing information about the close
			 * 
			 * @see org.mailster.gui.prefs.ConfigurationManager#SEND_TO_TRAY_ON_CLOSE_KEY
			 */
			public void shellClosed(ShellEvent e)
			{
				if (ConfigurationManager.CONFIG_STORE.getBoolean(ConfigurationManager.SEND_TO_TRAY_ON_CLOSE_KEY))
				{
					Shell shell = (Shell) e.widget;
					shell.setVisible(false);
				}
			}

			/**
			 * Sent when a <code>Shell</code> is minimized. Handles the application's tray behaviour
			 * if the enclosing <code>Shell</code> is iconified.
			 * 
			 * @param e
			 *            an event containing information about the minimization
			 * 
			 * @see org.mailster.gui.prefs.ConfigurationManager#SEND_TO_TRAY_ON_MINIMIZE_KEY
			 */
			public void shellIconified(ShellEvent e)
			{
				if (ConfigurationManager.CONFIG_STORE.getBoolean(ConfigurationManager.SEND_TO_TRAY_ON_MINIMIZE_KEY))
				{
					Shell shell = (Shell) e.widget;
					shell.setVisible(false);
				}
			}
		});
	}

	private void extractRatio(SashForm divider, String key, IPreferenceStore store)
	{
		if (store.getString(key) == null)
			return;

		String[] s = StringUtilities.split(store.getString(key), ",");
		int w[] = new int[s.length];
		for (int i = 0; i < s.length; i++)
			w[i] = Integer.parseInt(s[i]);
		divider.setWeights(w);
	}

	private void storeRatio(SashForm divider, String key, IPreferenceStore store)
	{
		int[] weights = divider.getWeights();
		int total = 0;
		for (int i = 0; i < weights.length; i++)
			total += weights[i];
		StringBuilder ratio = new StringBuilder();
		int p = 100;
		for (int i = 0, max = weights.length - 1; i < max; i++)
		{
			int r = (weights[0] * 100) / total;
			if (i != 0)
				ratio.append(',');
			ratio.append(r);
			p -= r;
		}
		ratio.append(',').append(p);
		store.setValue(key, ratio.toString());
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
		storeRatio(mailSash, ConfigurationManager.TABLE_VIEW_RATIO_KEY, store);
		store.setValue(ConfigurationManager.MAIL_PANEL_MINIMIZED_KEY, pshelfPanel.isToolbarVisible());

		ConfigurationManager.CONFIG_STORE.save();
	}

	public void log(String msg)
	{
		multiView.log(msg);
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
			public void run()
			{
				try
				{
					InputStream in = (new URL(ConfigurationManager.MAILSTER_VERSION_CHECK_URL)).openStream();
					byte[] buf = new byte[64];
					int len = 0;
					int offset = 0;
					while ((len = in.read(buf, offset, 64 - offset)) != -1)
					{
						offset += len;
					}
					in.close();

					String line = new String(buf, 0, offset);
					int pos = line.indexOf(' ');
					String ver = line.substring(0, pos);
					String currentVer = ConfigurationManager.MAILSTER_VERSION_NB.substring(1);

					boolean updateNeeded = true;
					if (currentVer.charAt(0) > ver.charAt(0))
						updateNeeded = false;
					else
					if (currentVer.charAt(2) > ver.charAt(2))
						updateNeeded = false;
					else
					if (currentVer.charAt(4) > ver.charAt(4))
						updateNeeded = false;

					String msg = null;

					if (updateNeeded)
					{
						Date d = new Date(Long.parseLong(line.substring(pos + 1)));
						StringBuilder sb = new StringBuilder(Messages.getString("MailView.tray.versioncheck.needUpdate")); //$NON-NLS-1$
						sb.append('\n');
						sb.append(MessageFormat.format(Messages.getString("MailView.tray.versioncheck.available"), //$NON-NLS-1$
								ver, DateFormatUtils.ISO_DATE_FORMAT.format(d)));
						msg = sb.toString();
					}
					else
						msg = Messages.getString("MailView.tray.versioncheck.upToDate"); //$NON-NLS-1$

					showTrayItemTooltipMessage(Messages.getString("MailView.tray.versioncheck.title") //$NON-NLS-1$
						+ DateUtilities.format(DateFormatterEnum.HOUR, new Date()) + ")", //$NON-NLS-1$
						msg);

					if (updateNeeded)
						Display.getDefault().asyncExec(new Thread() {
							public void run()
							{
								multiView.showURL(ConfigurationManager.MAILSTER_DOWNLOAD_PAGE, false, true);
							}
						});
				} catch (Exception ex)
				{
					LOG.debug("Failed to check if version is up to date", ex); //$NON-NLS-1$
					log("Failed to check if version is up to date"); //$NON-NLS-1$
				}
			}
		}).start();
	}

	public void setWaitCursor()
	{
		getShell().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
	}

	public void setDefaultCursor()
	{
		getShell().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
	}

	public void showTrayItemTooltipMessage(final String title, final String message)
	{
		MailsterSWTTrayItem.showTrayItemTooltipMessage(trayItem, title, message);
	}
}
