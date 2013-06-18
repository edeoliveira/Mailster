package org.mailster.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.mailster.MailsterSWT;
import org.mailster.core.smtp.MailsterSmtpService;
import org.mailster.core.smtp.events.SMTPServerAdapter;
import org.mailster.core.smtp.events.SMTPServerEvent;
import org.mailster.gui.prefs.ConfigurationManager;

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
 * MailsterSWTTrayItem.java - The Mailster TrayItem class.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.23 $, $Date: 2009/04/05 12:13:27 $
 */
public class MailsterSWTTrayItem
{
	private static final Tray tray = Display.getDefault().getSystemTray();

	private static MenuItem serverStartMenuItem;
	private static MenuItem serverDebugMenuItem;
	private static MenuItem serverStopMenuItem;

	private TrayItem trayItem;

	public MailsterSWTTrayItem()
	{
		trayItem = new TrayItem(tray, SWT.NONE);
		createSystemTray();
	}

	private void createSystemTray()
	{
		if (tray != null)
		{
			final Shell sShell = MailsterSWT.getInstance().getShell();
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
			trayItem.setImage(MailsterSWT.trayImage);
		}
	}

	private Menu createTrayMenu()
	{
		final Shell sShell = MailsterSWT.getInstance().getShell();
		final Menu menu = new Menu(sShell, SWT.POP_UP);
		serverStartMenuItem = new MenuItem(menu, SWT.PUSH);
		serverStartMenuItem.setText(Messages.getString("MailsterSWT.start.label")); //$NON-NLS-1$
		serverStartMenuItem.setImage(MailsterSWT.startImage);
		serverDebugMenuItem = new MenuItem(menu, SWT.PUSH);
		serverDebugMenuItem.setText(Messages.getString("MailsterSWT.debug.label")); //$NON-NLS-1$
		serverDebugMenuItem.setImage(MailsterSWT.debugImage);
		serverStopMenuItem = new MenuItem(menu, SWT.PUSH);
		serverStopMenuItem.setText(Messages.getString("MailsterSWT.stop.label")); //$NON-NLS-1$
		serverStopMenuItem.setImage(MailsterSWT.stopImage);
		serverStopMenuItem.setEnabled(false);
		new MenuItem(menu, SWT.SEPARATOR);

		final MenuItem quitMenuItem = new MenuItem(menu, SWT.PUSH);
		quitMenuItem.setText(Messages.getString("MailsterSWT.quit.menuitem")); //$NON-NLS-1$

		final MailsterSmtpService smtpService = MailsterSWT.getInstance().getSMTPService();
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

		smtpService.addSMTPServerListener(new SMTPServerAdapter() {
			public void updateUI(boolean stopped)
			{
				if (sShell.isDisposed())
					return;

				serverStartMenuItem.setEnabled(stopped);
				serverDebugMenuItem.setEnabled(stopped);
				serverStopMenuItem.setEnabled(!stopped);
				MailsterSWT.serverStartToolItem.setEnabled(stopped);
				MailsterSWT.serverDebugToolItem.setEnabled(stopped);
				MailsterSWT.serverStopToolItem.setEnabled(!stopped);

				if (stopped)
					trayItem.setImage(MailsterSWT.trayImage);
				else
					trayItem.setImage(MailsterSWT.trayRunningImage);
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
						if (ConfigurationManager.CONFIG_STORE.getBoolean(ConfigurationManager.SEND_TO_TRAY_ON_SERVER_START_KEY))
							sShell.setMinimized(true);
					}
				});
			}
		});

		serverStartMenuItem.addListener(SWT.Selection, menuListener);
		serverDebugMenuItem.addListener(SWT.Selection, menuListener);
		serverStopMenuItem.addListener(SWT.Selection, menuListener);
		quitMenuItem.addListener(SWT.Selection, menuListener);

		return menu;
	}

	public static void showTrayItemTooltipMessage(final TrayItem trayItem, final String title, final String message)
	{
		final Shell sShell = MailsterSWT.getInstance().getShell();
		Display.getDefault().asyncExec(new Thread() {
			public void run()
			{
				final ToolTip tip = new ToolTip(sShell, SWT.BALLOON | SWT.ICON_INFORMATION);
				tip.setMessage(message);
				tip.setText(title);

				if (Display.getDefault().getSystemTray() != null)
					trayItem.setToolTip(tip);
				else
					tip.setLocation(sShell.getLocation());

				tip.setVisible(true);
				tip.setAutoHide(ConfigurationManager.CONFIG_STORE.getBoolean(ConfigurationManager.AUTO_HIDE_NOTIFICATIONS_KEY));
			}
		});
	}

	public TrayItem getTrayItem()
	{
		return trayItem;
	}
}
