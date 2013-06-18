package org.mailster.gui.views;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.mailster.MailsterSWT;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.gui.views.mailview.OutlookMailView;
import org.mailster.gui.widgets.GIFAnimator;
import org.mailster.util.DateUtilities;
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
 * MultiView.java - The right view which contains a web view, a log view and the mail view.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.27 $, $Date: 2009/05/18 22:14:26 $
 */
public class MultiView
{
	private static final Logger LOG = LoggerFactory.getLogger(MultiView.class);

	private static final Color[] TAB_GRADIENT = SWTHelper.getGradientColors(5, SWTHelper.createColor(0, 84, 227), SWTHelper
			.createColor(61, 149, 255));
	private static final Image HOME_IMAGE = SWTHelper.loadImage("home.gif"); //$NON-NLS-1$

	private MailsterSWT main;
	private OutlookMailView mailView;

	private Composite back;
	private StackLayout stackLayout = new StackLayout();
	private CTabFolder folder;

	private Text log;
	private CTabItem logTabItem;
	private ToolBar logViewToolBar;
	private boolean logViewIsScrollLocked;

	private ToolBar browserViewToolBar;
	private ToolItem stopToolItem;
	private ToolItem backToolItem;
	private ToolItem forwardToolItem;
	private GIFAnimator gThread;
	
	class BrowserUrlSelectionAdapter extends SelectionAdapter {
		
		private Map<Browser, String> map = new HashMap<Browser, String>();
		
		public void mapHomeUrl(Browser browser, String url)
		{
			map.put(browser, url);
		}
		
		public void widgetSelected(SelectionEvent e)
		{
			Control c = folder.getSelection().getControl();
			if (c instanceof Browser)
			{
				Browser browser = (Browser) c;
				browser.setUrl(map.get(browser));
			}
		}
	};
	
	private BrowserUrlSelectionAdapter adapter;
	
	private boolean forcedMozillaBrowserUse = false;
	
	public MultiView(Composite parent)
	{
		this.main = MailsterSWT.getInstance();

		createViews(parent);
		createLogConsole(false);
	}

	public OutlookMailView getMailView()
	{
		return mailView;
	}

	private void createViews(Composite parent)
	{
		back = new Composite(parent, SWT.NONE);
		back.setLayout(stackLayout);
		back.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));

		mailView = new OutlookMailView(back);

		folder = new CTabFolder(back, SWT.NONE);
		folder.setUnselectedImageVisible(false);
		folder.setUnselectedCloseVisible(false);
		folder.setTabHeight(22);
		folder.setBackground(MailsterSWT.BGCOLOR);

		folder.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event)
			{
				update(event);
			}

			public void widgetSelected(SelectionEvent event)
			{
				update(event);
			}

			public void update(SelectionEvent event)
			{
				if (event.item != null)
				{
					if (event.item == logTabItem)
						setTopRight(logViewToolBar);
					else
					{
						setTopRight(browserViewToolBar);
						Browser browser = (Browser) folder.getSelection().getControl();
						backToolItem.setEnabled(browser.isBackEnabled());
						forwardToolItem.setEnabled(browser.isForwardEnabled());						
					}
				}
			}
		});

		final Image changeLogImage = SWTHelper.loadImage("changelog.gif"); //$NON-NLS-1$
		showURL(changeLogImage, "file://" + System.getProperty("user.dir") + File.separator + "changelog.htm", Messages
				.getString("MailsterSWT.changelog.tooltip"), false);

		folder.setSelectionBackground(TAB_GRADIENT, new int[] {10, 30, 50, 70}, true);
		folder.setSelectionForeground(SWTHelper.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event)
			{
				if (event.item == logTabItem)
					logTabItem = null;
			}
		});
		stackLayout.topControl = folder;
	}

	public void switchTopControl(boolean mailSelected)
	{
		if ((mailSelected && stackLayout.topControl == mailView.getView()) 
				|| (!mailSelected && stackLayout.topControl == folder))
			return;

		if (mailSelected)
			stackLayout.topControl = mailView.getView();
		else
			stackLayout.topControl = folder;
		back.layout();
	}

	public void createLogConsole(boolean createTabItem)
	{
		switchTopControl(false);
		
		if (logTabItem != null)
		{
			folder.setSelection(logTabItem);
			return;
		}

		final Composite logComposite = new Composite(folder, SWT.NONE);
		logComposite.setLayout(LayoutUtils.createGridLayout(1, false, 0, 0, 0, 0, 0, 0, 0, 0));

		if (log != null && log.isReparentable())
		{
			try
			{
				log.setParent(logComposite);
			} catch (Exception ex)
			{
				// OS does not support re-parenting ! Force log view creation
				log.dispose();
				log = null;
			}
		}

		if (log == null)
		{
			log = new Text(logComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);

			GridData gridData = new GridData(GridData.FILL_BOTH);
			gridData.grabExcessHorizontalSpace = true;
			gridData.grabExcessVerticalSpace = true;
			log.setLayoutData(gridData);

			log.setEditable(false);
			log.setForeground(SWTHelper.getColor(SWT.COLOR_DARK_RED));
			log.setBackground(SWTHelper.getColor(SWT.COLOR_WHITE));
		}

		logTabItem = new CTabItem(folder, SWT.CLOSE);
		logTabItem.setText(Messages.getString("Mailview.log.console.tabname"));
		logTabItem.setImage(SWTHelper.loadImage("console_view.gif"));

		if (logViewToolBar == null)
			createLogViewToolBar();

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		logComposite.setLayoutData(gd);
		logTabItem.setControl(logComposite);
		folder.setSelection(logTabItem);
		setTopRight(logViewToolBar);
	}

	private void createLogViewToolBar()
	{
		logViewToolBar = new ToolBar(folder, SWT.FILL | SWT.FLAT);
		logViewToolBar.setBackground(MailsterSWT.BGCOLOR);

		ToolItem clearLogToolItem = new ToolItem(logViewToolBar, SWT.PUSH);
		clearLogToolItem.setImage(SWTHelper.loadImage("clear.gif")); //$NON-NLS-1$
		clearLogToolItem.setToolTipText(Messages.getString("MailsterSWT.clear.tooltip")); //$NON-NLS-1$
		clearLogToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				log.setText(""); //$NON-NLS-1$
			}
		});

		final ToolItem scrollLockToolItem = new ToolItem(logViewToolBar, SWT.CHECK);
		scrollLockToolItem.setImage(SWTHelper.loadImage("lockscroll.gif")); //$NON-NLS-1$
		scrollLockToolItem.setToolTipText(Messages.getString("MailsterSWT.scrollLock.tooltip")); //$NON-NLS-1$
		scrollLockToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				logViewIsScrollLocked = scrollLockToolItem.getSelection();
			}
		});
	}

	private Browser createBrowser(Composite parent)
	{
		if (isForcedMozillaBrowserUse())
			return new Browser(parent, SWT.BORDER | SWT.MOZILLA);
		else
			return new Browser(parent, SWT.BORDER);
	}

	private synchronized void createBrowserViewToolBar(final Browser browser, final String url)
	{
		if (browserViewToolBar == null)
		{
			browserViewToolBar = new ToolBar(folder, SWT.FILL | SWT.FLAT);
			browserViewToolBar.setBackground(MailsterSWT.BGCOLOR);
	
			ToolItem placeHolder = new ToolItem(browserViewToolBar, SWT.NONE);
			placeHolder.setEnabled(false);
			placeHolder.setWidth(30);
	
			backToolItem = new ToolItem(browserViewToolBar, SWT.PUSH);
			forwardToolItem = new ToolItem(browserViewToolBar, SWT.PUSH);
	
			backToolItem.setImage(SWTHelper.loadImage("backward_nav.gif")); //$NON-NLS-1$
			backToolItem.setToolTipText(Messages.getString("MailsterSWT.browser.back.tooltip")); //$NON-NLS-1$
			backToolItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e)
				{
					Control c = folder.getSelection().getControl();
					if (c instanceof Browser)
						((Browser)c).back();
				}
			});
			backToolItem.setEnabled(false);
	
			forwardToolItem.setImage(SWTHelper.loadImage("forward_nav.gif")); //$NON-NLS-1$
			forwardToolItem.setToolTipText(Messages.getString("MailsterSWT.browser.forward.tooltip")); //$NON-NLS-1$
			forwardToolItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e)
				{
					Control c = folder.getSelection().getControl();
					if (c instanceof Browser)
						((Browser)c).forward();
				}
			});
			forwardToolItem.setEnabled(false);
	
			final ToolItem homeToolItem = new ToolItem(browserViewToolBar, SWT.PUSH);
			homeToolItem.setImage(SWTHelper.loadImage("nav_home.gif")); //$NON-NLS-1$
			homeToolItem.setToolTipText(Messages.getString("MailsterSWT.browser.home.tooltip")); //$NON-NLS-1$
			adapter = new BrowserUrlSelectionAdapter();
			homeToolItem.addSelectionListener(adapter);
	
			final ToolItem refreshToolItem = new ToolItem(browserViewToolBar, SWT.PUSH);
			refreshToolItem.setImage(SWTHelper.loadImage("refresh_nav.gif")); //$NON-NLS-1$
			refreshToolItem.setToolTipText(Messages.getString("MailsterSWT.browser.refresh.tooltip")); //$NON-NLS-1$
			refreshToolItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e)
				{
					Control c = folder.getSelection().getControl();
					if (c instanceof Browser)
						((Browser)c).refresh();
				}
			});
	
			gThread = new GIFAnimator("Multiview animated gif thread", //$NON-NLS-1$
					"load.gif", browserViewToolBar, false); //$NON-NLS-1$
			gThread.setOffsetY(3);
			gThread.start();
			
			browserViewToolBar.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent evt)
				{
					gThread.dispose();
				}
			});			
	
			stopToolItem = new ToolItem(browserViewToolBar, SWT.PUSH);
			stopToolItem.setImage(SWTHelper.loadImage("stop.gif")); //$NON-NLS-1$
			stopToolItem.setToolTipText(Messages.getString("MailsterSWT.browser.stop.tooltip")); //$NON-NLS-1$
			stopToolItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e)
				{
					Control c = folder.getSelection().getControl();
					if (c instanceof Browser)
						((Browser)c).stop();
				}
			});
	
			new ToolItem(browserViewToolBar, SWT.SEPARATOR);
	
			final ToolItem showLogViewToolItem = new ToolItem(browserViewToolBar, SWT.PUSH);
			showLogViewToolItem.setImage(SWTHelper.loadImage("console_view.gif")); //$NON-NLS-1$
			showLogViewToolItem.setToolTipText(Messages.getString("MailsterSWT.showLogView.tooltip")); //$NON-NLS-1$
			showLogViewToolItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e)
				{
					createLogConsole(true);
				}
			});
		}
		
		adapter.mapHomeUrl(browser, url);
		
		browser.addLocationListener(new LocationListener() {
			public void changed(LocationEvent evt)
			{
				stopToolItem.setEnabled(false);
				backToolItem.setEnabled(browser.isBackEnabled());
				forwardToolItem.setEnabled(browser.isForwardEnabled());
				gThread.stopAnimation();
			}

			public void changing(LocationEvent evt)
			{
				stopToolItem.setEnabled(true);
				gThread.startAnimation();
			}
		});
	}

	public void log(String msg)
	{
		if (log != null && !log.isDisposed() && msg != null)
		{
			String date = DateUtilities.format(DateFormatterEnum.DF, new Date());
			
			StringBuilder sb = new StringBuilder(3 + date.length() + msg.length());
			sb.append('['); //$NON-NLS-1$
			sb.append(date);
			sb.append(']'); //$NON-NLS-1$
			sb.append(msg);
			sb.append('\n'); //$NON-NLS-1$
			final String s = sb.toString();

			log.getDisplay().asyncExec(new Thread() {
				public void run()
				{
					int idx = log.getTopIndex();
					log.append(s.toString());
					if (logViewIsScrollLocked)
						log.setTopIndex(idx);
				}
			});
		}
		else
			LOG.info(msg);
	}

	public void showURL(String url, boolean setURLAsTitle, boolean showCloseHandle)
	{
		showURL(HOME_IMAGE, url, null, setURLAsTitle, showCloseHandle);
	}
	
	public void showURL(Image img, String url, String title, boolean showCloseHandle)
	{
		showURL(img, url, title, false, showCloseHandle);
	}

	private void showURL(Image img, String url, String title, boolean setURLAsTitle, boolean showCloseHandle)
	{
		switchTopControl(false);
		CTabItem item = null;
		try
		{
			String text;
			if (setURLAsTitle)
				text = url;
			else if (title != null)
				text = title;
			else
				text = Messages.getString("MailView.tabitem.title"); //$NON-NLS-1$
			
			for (CTabItem it : folder.getItems())
			{
				if (it.getText().equals(text))
				{
					folder.setSelection(it);
					return;
				}
			}
			
			item = new CTabItem(folder, showCloseHandle ? SWT.CLOSE : SWT.NONE);
			item.setText(text);
			
			if (img != null)
				item.setImage(img);
			else
				item.setImage(HOME_IMAGE);

			GridData gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.grabExcessVerticalSpace = true;
			gridData.verticalAlignment = GridData.FILL;

			final Browser b = createBrowser(folder);
			b.setLayoutData(gridData);
			item.setControl(b);
			folder.setSelection(item);

			createBrowserViewToolBar(b, url);
			item.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e)
				{
					b.dispose();
				}
			});

			setTopRight(browserViewToolBar);
			b.setUrl(url);
		} catch (SWTError swt)
		{
			main.log(swt.getMessage());
			if (item != null)
				item.dispose();
			return;
		}
	}

	private void setTopRight(ToolBar tb)
	{
		Control c = folder.getTopRight();
		if (c != null)
			c.setVisible(false);
		tb.setVisible(true);
		folder.setTopRight(tb);
	}

	public boolean isForcedMozillaBrowserUse()
	{
		return forcedMozillaBrowserUse;
	}

	public void setForcedMozillaBrowserUse(boolean forcedMozillaBrowserUse)
	{
		this.forcedMozillaBrowserUse = forcedMozillaBrowserUse;
		getMailView().setForcedMozillaBrowserUse(forcedMozillaBrowserUse);
	}
}
