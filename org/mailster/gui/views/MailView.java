package org.mailster.gui.views;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.mailster.MailsterSWT;
import org.mailster.gui.DropDownListener;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.service.MailsterConstants;
import org.mailster.service.MailsterSmtpService;
import org.mailster.service.smtp.parser.SmtpMessage;
import org.mailster.service.smtp.parser.SmtpMessagePart;
import org.mailster.util.DateUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.AbstractEventList;

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
 * MailView.java - Handles the TabFolder view which contains mail tabs, the log view
 * and even browser tabs.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class MailView
{
    /**
     * This class provides the "drop down" functionality for our attached file
     * dropdown.
     */
    class AttachedFilesDropdownListener extends DropDownListener
    {
    	public DecimalFormat dcFormat;
    	
        public AttachedFilesDropdownListener(ToolItem dropdown)
        {
            super(dropdown);

        	dcFormat = new DecimalFormat("#,##0");
        	dcFormat.setGroupingSize(3);
        }
        
        private void getFormattedPartSize(StringBuilder sb, SmtpMessagePart part)
        {
        	int size = part.getSize();
        	String unit = Messages.getString("MailView.fileSizeUnit.bytes");
        	
        	if (size > 1E9)
        	{
        		size = (int) (size / 1E9);
        		unit = Messages.getString("MailView.fileSizeUnit.gigabytes");;
        	}
        	else
        	if (size > 1E6)
        	{
        		size = (int) (size / 1E6);
        		unit = Messages.getString("MailView.fileSizeUnit.megabytes");;
        	}
        	else        		
        	if (size > 1E4)
        	{
        		size = (int) (size / 1E3);
        		unit = Messages.getString("MailView.fileSizeUnit.kilobytes");;
        	}
        	
        	sb.append(dcFormat.format(size)).append(' ').append(unit);
        }
        
        /**
         * Adds an item to the dropdown list
         * 
         * @param item the item to add
         */
        public void add(SmtpMessagePart part)
        {
            dropdown.setEnabled(true);
            MenuItem menuItem = new MenuItem(menu, SWT.NONE);
            String fileName = part.getFileName();
            StringBuilder sb = new StringBuilder(fileName);
            sb.append(" (");
            getFormattedPartSize(sb, part);
            sb.append(')');
            menuItem.setText(sb.toString());

            if (part.getContentType().contains("pkcs")) //$NON-NLS-1$
                menuItem.setImage(SWTHelper.loadImage("smime.gif")); //$NON-NLS-1$
            else
            if (fileName.lastIndexOf('.') != -1)
            {
                Program p = Program.findProgram(fileName.substring(fileName
                        .lastIndexOf('.')));
                if (p != null)
                {
                    ImageData data = p.getImageData();
                    if (data != null)
                        menuItem.setImage(new Image(menu.getDisplay(), data));
                }
            }
            else 
            if (part.getContentType().startsWith("message")) //$NON-NLS-1$
                menuItem.setImage(SWTHelper.loadImage("mail.gif")); //$NON-NLS-1$

            menuItem.setData(part);
            menuItem.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event)
                {
                    String fileName = ((SmtpMessagePart) event.widget.getData())
                            .getFileName();
                    Program p = Program.findProgram(fileName.substring(fileName
                            .lastIndexOf('.')));
                    if (p != null &&
                    		ConfigurationManager.CONFIG_STORE.
                    		getBoolean(ConfigurationManager.EXECUTE_ENCLOSURE_ON_CLICK_KEY))
                    {
                    	String tmpDir = System.getProperty("java.io.tmpdir");
                        fileName = tmpDir + fileName;
                        saveAllAttachments(new MenuItem[] { (MenuItem) event.widget }, tmpDir);
                        main.log(Messages
                                        .getString("MailView.execute.file") + fileName + " ..."); //$NON-NLS-1$ //$NON-NLS-2$
                        p.execute(fileName);
                    }
                    else
                        saveAttachedFile((SmtpMessagePart) event.widget.getData());
                }
            });
        }

        public void buttonPushed()
        {
        	saveAllAttachments(menu.getItems(), main.getSMTPService().getOutputDirectory());
        }
    }
    
    /**
     * This class provides the "drop down" functionality for our mail view mode
     * dropdown.
     */
    class MailViewModeDropdownListener extends DropDownListener
    {
    	public final static int HTML_MODE 	= 0;
    	public final static int MIXED_MODE 	= 1;
    	public final static int RAW_MODE 	= 2;
    	
    	private int currentMode = HTML_MODE;
    	
        public MailViewModeDropdownListener(ToolItem dropdown)
        {
            super(dropdown);
            buildMenu();
        }
        
        public void buildMenu()
        {
        	dropdown.setToolTipText(Messages.getString("MailView.toggle.viewModes")); //$NON-NLS-1$
        	
        	final MenuItem normalModeItem = new MenuItem(menu, SWT.NONE);
        	normalModeItem.setText(Messages.getString("MailView.toggle.toInterpretedViewTooltip")); //$NON-NLS-1$
        	normalModeItem.setImage(browserImage);

            final MenuItem mixedModeItem = new MenuItem(menu, SWT.NONE);
            mixedModeItem.setText(Messages.getString("MailView.toggle.toMixedViewTooltip")); //$NON-NLS-1$
            mixedModeItem.setImage(mixedViewImage);
                    	
            final MenuItem rawModeItem = new MenuItem(menu, SWT.NONE);
            rawModeItem.setText(Messages.getString("MailView.toggle.toRawViewTooltip")); //$NON-NLS-1$
            rawModeItem.setImage(rawViewImage);

            SelectionAdapter adapter = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) 
				{
					if (evt.getSource() == normalModeItem)
						currentMode = HTML_MODE;
					else
					if (evt.getSource() == mixedModeItem)
						currentMode = MIXED_MODE;
					else
						currentMode = RAW_MODE;
					
					updateMailViewMode();
				}
			};
			
			normalModeItem.addSelectionListener(adapter);
			rawModeItem.addSelectionListener(adapter);
			mixedModeItem.addSelectionListener(adapter);
        }

        public void updateMailViewMode()
        {
        	Image img = null;
        	
        	if (currentMode == HTML_MODE)
				img = browserImage;
        	else
    		if (currentMode == MIXED_MODE)
    			img = mixedViewImage;
    		else
    			img = rawViewImage;
        	
        	dropdown.setImage(img);
        	dropdown.setHotImage(img);
        	
        	if (folder.getSelection() == null ||
            		folder.getSelection().getData() == null)
                return;
        	
        	Composite c = (Composite) folder.getSelection().getControl();
        	Control c0 = c.getChildren()[0]; // the browser
        	Control c1 = c.getChildren()[1]; // the raw view
        	
        	if (currentMode == HTML_MODE || currentMode == RAW_MODE)
        	{
        		StackLayout layout = null;
        		
        		if (!(c.getLayout() instanceof StackLayout))
        		{
        			layout = new StackLayout();
        			c.setLayout(layout);
        		}
        		else
        			layout = (StackLayout) c.getLayout();
        		
        		layout.topControl = currentMode == HTML_MODE ? c0 : c1;
        	}
        	else
        	{
        		c.setLayout(new FillLayout(SWT.VERTICAL));
    			c0.setVisible(true);
    			c1.setVisible(true);
        	}
        	
        	c.layout();
        }
        
        public void buttonPushed()
        {
        	if (currentMode == HTML_MODE)
        		currentMode = MIXED_MODE;
        	else
    		if (currentMode == MIXED_MODE)
    			currentMode = RAW_MODE;
    		else
    			currentMode = HTML_MODE;
        	
        	updateMailViewMode();
        }
    }    

    class MCTabFolder2Listener extends CTabFolder2Adapter
    {
        private int percents = 0;
        private CTabFolder folder;
        private SashForm sash;

        public MCTabFolder2Listener(SashForm sash, CTabFolder folder)
        {
            this.sash = sash;
            this.folder = folder;
        }

        private void computePercents()
        {
            int h = sash.getSize().y - sash.SASH_WIDTH;
            percents = Math.round((float) (folder.getSize().y * 100)
                    / (float) h);
        }

        public void minimize(CTabFolderEvent event)
        {
            if (!folder.getMinimized())
            {
                if (!folder.getMaximized())
                    computePercents();
                folder.setMaximized(false);
                folder.setMinimized(true);
            }

            int p = Math.round((float) (folder.getTabHeight() * 100)
                    / (float) (sash.getSize().y - sash.SASH_WIDTH)) + 1;
            sash.setWeights(new int[] { 100 - p, p });
        }

        public void maximize(CTabFolderEvent event)
        {
            if (!folder.getMaximized())
            {
                if (!folder.getMinimized())
                    computePercents();
                folder.setMinimized(false);
                folder.setMaximized(true);
            }
            sash.setWeights(new int[] { 0, 100 });
        }

        public void restore(CTabFolderEvent event)
        {
            folder.setMinimized(false);
            folder.setMaximized(false);
            sash.setWeights(new int[] { 100 - percents, percents });
        }

        public void close(CTabFolderEvent event)
        {
            dropdownListener.clear();

            if (event.item.getData() != null)
                openedMailsIds.remove(((StoredSmtpMessage) event.item.getData())
                        .getMessageId());
            else
            if (event.item != logTabItem)
                restore(event);
            else
            {
            	logTabItem = null;
            	setTopRight(mailToolbar);
            }
        }
    }

    /** 
     * Log object for this class. 
     */
    private static final Logger LOG = LoggerFactory.getLogger(MailView.class);
    
    public final static String DEFAULT_PREFERRED_CONTENT = "text/html"; //$NON-NLS-1$
    
    private final static Image mailImage  = SWTHelper.loadImage("mail_into.gif"); //$NON-NLS-1$
    private final static Image homeImage = SWTHelper.loadImage("home.gif"); //$NON-NLS-1$
    private final static Image rawViewImage = SWTHelper.loadImage("rawView.gif"); //$NON-NLS-1$
    private final static Image mixedViewImage = SWTHelper.loadImage("mixedView.gif"); //$NON-NLS-1$
    private final static Image browserImage = SWTHelper.loadImage("html.gif"); //$NON-NLS-1$

    private final static Color[] tabGradient = SWTHelper.getGradientColors(5,
   												new Color(SWTHelper.getDisplay(), 0, 84, 227),
									            new Color(SWTHelper.getDisplay(), 61, 149, 255));
    
    private ArrayList<String> openedMailsIds = new ArrayList<String>();
    private String preferredContentType = DEFAULT_PREFERRED_CONTENT;

    private TableView tableView;
    private CTabFolder folder;
    private CTabItem logTabItem;
    private Composite parent;
    private Text log;
    private MailsterSWT main;
    
    private SashForm divider;

    private MCTabFolder2Listener folderLayoutListener;
    private AttachedFilesDropdownListener dropdownListener;
    private MailViewModeDropdownListener viewModeListener;

    private boolean forcedMozillaBrowserUse = false;
    private boolean logViewIsScrollLocked;
    private boolean synced = true;
    
    private ToolBar logViewToolBar;
    private ToolBar mailToolbar;
    
    public MailView(Composite parent, FilterTreeView treeView)
    {
        this.parent = parent;
        this.main = MailsterSWT.getInstance();

        SWTHelper.getDisplay().addFilter(SWT.KeyDown, new Listener() {
            public void handleEvent(Event e)
            {
                if (e.keyCode == SWT.F4)
                {
                	if (e.stateMask == SWT.MOD1 + SWT.MOD2)                
                		closeTabs(true);
                	else if (e.stateMask == SWT.MOD2)                
                    	closeTabs(false);
                }
            }
        });
        
        createView(treeView, main.getFilterTextField());
        createLogConsole(false);
    }

    protected TableView getTableView()
    {
    	return tableView;
    }
    
    protected CTabFolder getCTabFolder()
    {
    	return folder;
    }
    
    public SashForm getDivider()
    {
        return divider;
    }
    
    private void createView(FilterTreeView treeView, Text filterTextField)
    {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;

        divider = new SashForm(parent, SWT.NONE);
        divider.setOrientation(SWT.VERTICAL);
        divider.setLayoutData(gridData);
        tableView = new TableView(divider, this, treeView, filterTextField);
        createTabFolder(divider);
        divider.setWeights(new int[] { 30, 70 });
    }

    private Text createRawMailView(Composite parent)
    {
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.verticalAlignment = GridData.BEGINNING;
        gridData.grabExcessHorizontalSpace = true;
        Text rawMailView = new Text(parent, SWT.MULTI | SWT.V_SCROLL
                | SWT.BORDER | SWT.WRAP);
        rawMailView.setLayoutData(gridData);
        rawMailView.setEditable(false);

        return rawMailView;
    }

    private Browser createBrowser(Composite parent)
    {
        if (isForcedMozillaBrowserUse())
            return new Browser(parent, SWT.BORDER | SWT.MOZILLA);
        else
            return new Browser(parent, SWT.BORDER);
    }
    
    private void createToolBarCommons(ToolBar tb)
    {
    	new ToolItem(tb, SWT.SEPARATOR);
    	
        final ToolItem syncedButton = SWTHelper.createToolItem(
        		tb,
                SWT.CHECK,
                "", //$NON-NLS-1$
                Messages.getString("MailView.synced.views.tooltip"), "synced.gif", false); //$NON-NLS-1$ //$NON-NLS-2$
                
        syncedButton.setSelection(isSynced());
        
        final ToolItem collapseAllButton = SWTHelper.createToolItem(
        		tb,
                SWT.PUSH,
                "", //$NON-NLS-1$ 
                Messages.getString("MailView.collapseall.tooltip")+" (Ctrl+Shift+F4)", //$NON-NLS-1$ //$NON-NLS-2$ 
                "closeall.gif", true); //$NON-NLS-1$ //$NON-NLS-2$
		
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                if (e.widget == syncedButton)
                	synced =  syncedButton.getSelection();
                else if (e.widget == collapseAllButton)
                	closeTabs(true);
            }
        };
        
        syncedButton.addSelectionListener(selectionAdapter);
        collapseAllButton.addSelectionListener(selectionAdapter);
    }

    private void createLogViewToolBar()
    {
    	logViewToolBar = new ToolBar(folder, SWT.FILL | SWT.FLAT);
        
        ToolItem clearLogToolItem = new ToolItem(logViewToolBar, SWT.PUSH);
        clearLogToolItem.setImage(SWTHelper.loadImage("clear.gif")); //$NON-NLS-1$
        clearLogToolItem.setToolTipText(Messages
                .getString("MailsterSWT.clear.tooltip")); //$NON-NLS-1$
        clearLogToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                log.setText(""); //$NON-NLS-1$
            }
        });

        final ToolItem scrollLockToolItem = new ToolItem(logViewToolBar, SWT.CHECK);
        scrollLockToolItem.setImage(SWTHelper.loadImage("lockscroll.gif")); //$NON-NLS-1$
        scrollLockToolItem.setToolTipText(Messages
                .getString("MailsterSWT.scrollLock.tooltip")); //$NON-NLS-1$
        scrollLockToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                logViewIsScrollLocked = scrollLockToolItem.getSelection();                
            }
        });
        
        createToolBarCommons(logViewToolBar);
    }
    
    public void createLogConsole(boolean createTabItem)
    {
    	if (logTabItem != null)
    		return;

    	final Composite logComposite = new Composite(folder, SWT.NONE);
        logComposite.setLayout(
                LayoutUtils.createGridLayout(1, false, 0, 0, 0, 0, 0, 0, 0, 0));
        
        if (log !=null && log.isReparentable())
        {
        	try
        	{
        		log.setParent(logComposite);
        	}
        	catch (Exception ex)
        	{
        		// OS does not support re-parenting ! Force log view creation
        		log.dispose();
        		log = null;
        	}
        }
        
    	if (!createTabItem || log == null)
    	{
    		log = new Text(logComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP );
            
            GridData gridData = new GridData(GridData.FILL_BOTH);
            gridData.grabExcessHorizontalSpace = true;
            gridData.grabExcessVerticalSpace = true;
			log.setLayoutData(gridData);
			
			log.setEditable(false);
			log.setForeground(SWTHelper.createColor(11, 161, 11));
			log.setBackground(log.getDisplay().getSystemColor(SWT.COLOR_BLACK));
			
			if (!createTabItem)
				return;
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
    
    protected void createMailTab(StoredSmtpMessage stored)
    {
        if (stored == null)
            return;
        
        String id = stored.getMessageId();
        if (!openedMailsIds.contains(id))
        {
        	SmtpMessage msg = stored.getMessage();
        	
            final CTabItem item = new CTabItem(folder, SWT.CLOSE);
            item.setText(msg.getSubject());
            item.setImage(mailImage);

            final Composite itemComposite = new Composite(folder, SWT.NONE);
            final StackLayout layout = new StackLayout();
            itemComposite.setLayout(layout);
            item.setControl(itemComposite);
            
            Browser _browser = null;

            try
            {
                Composite browserComposite = new Composite(itemComposite, 0);
                GridLayout g = new GridLayout();
                g.marginHeight = 0;
                g.marginWidth = 0;
                g.horizontalSpacing = 0;
                g.verticalSpacing = 0;
                browserComposite.setLayout(g);

                HeadersView h = new HeadersView(browserComposite, stored);
                GridData data = new GridData();
                data.grabExcessHorizontalSpace = true;
                data.horizontalAlignment = GridData.FILL;
                data.verticalAlignment = GridData.BEGINNING;
                h.setLayoutData(data);

                _browser = createBrowser(browserComposite);
                data = new GridData();
                data.verticalAlignment = GridData.FILL;
                data.horizontalAlignment = GridData.FILL;
                data.grabExcessVerticalSpace = true;
                data.grabExcessHorizontalSpace = true;
                _browser.setLayoutData(data);

                layout.topControl = browserComposite;
                browserComposite.layout(true);
            }
            catch (SWTError swt)
            {
                main.log(swt.getMessage());
                item.dispose();
                return;
            }

            Text rawView = createRawMailView(itemComposite);
            
            _browser.setText(msg.getPreferredContent(preferredContentType));

            final Browser b = _browser;
            b.getDisplay().asyncExec(new Runnable() {
				public void run() 
				{
					b.addProgressListener(new ProgressAdapter() {
		                public void completed(ProgressEvent event)
		                {
		                	// Dynamically add the javascript highlighting code
		                    executeJavaScript(b, "var script = document.createElement('script');\r\n" +
				                                "script.type = 'text/javascript';\r\n" +
				                                "script.src = 'file:///"+MailsterConstants.USER_DIR+"/js/highlight_mailster.js';\r\n" +
				                                "document.getElementsByTagName('head')[0].appendChild(script);");
		                }
		            });
				}
			});
        	
            rawView.setText(msg.toString());
            
            item.setData(stored);
            openedMailsIds.add(id);
            folder.setSelection(item);
            setTopRight(mailToolbar);
            updateAttachedFilesButton(msg.getInternalParts());
            viewModeListener.updateMailViewMode();
            MailsterSWT.getInstance().getOutlineView().setMessage(msg);

            final Browser browser = _browser;
            item.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    browser.dispose();
                    main.getOutlineView().setMessage(null);
                }
            });
        }
    }

    protected void selectMailTab(StoredSmtpMessage stored)
    {
        String id = stored.getMessageId();
        if (openedMailsIds.contains(id))
        {
            for (int i = 0, max = folder.getItemCount(); i < max; i++)
            {
                if (folder.getItems()[i].getData() != null
                        && id.equals(((StoredSmtpMessage) folder.getItems()[i]
                                .getData()).getMessageId()))
                {
                    folder.setSelection(folder.getItems()[i]);
                    SmtpMessage msg = stored.getMessage();
                    main.getOutlineView().setMessage(msg);
                    updateAttachedFilesButton(msg.getInternalParts());
                    viewModeListener.updateMailViewMode();
                    return;
                }
            }
        }
    }

    public void executeJavaScript(Browser browser, String script)
    {
    	boolean result = browser.execute(script);
    	
    	// Script may fail or may not be supported on certain platforms.
    	if (!result)
            main.log("Script failed to execute on Browser object ["+browser+"] !");
    }

    public void executeJavaScriptOnEachMailBrowser(String script)
    {
    	for (int i = 0, max = folder.getItemCount(); i < max; i++)
        {
    		if (folder.getItem(i).getData() != null)
    		{
    			Composite c = (Composite) folder.getItem(i).getControl();
    			// browser is the first added component
    			Browser browser = (Browser) ((Composite)c.getChildren()[0]).getChildren()[1];
    			
    			executeJavaScript(browser, script);
    		}   
        }
    }
    
    private void recurseMessageParts(final SmtpMessagePart current)
    {
        if (current != null)
        {
            SmtpMessagePart[] files = current.getAttachedFiles();

            for (int i = 0, max = files.length; i < max; i++)
                dropdownListener.add(files[i]);
            
	        if (current.getParts() != null)
	        {
	        	for (SmtpMessagePart part : current.getParts())
	        		recurseMessageParts(part);
	        }
        }
    }
    
    private void updateAttachedFilesButton(final SmtpMessagePart current)
    {
        dropdownListener.clear();
        recurseMessageParts(current);
    }

    private void saveAllAttachments(MenuItem[] items, String dir)
    {
        for (int i = 0, max = items.length; i < max; i++)
        {
            SmtpMessagePart p = (SmtpMessagePart) items[i].getData();
            String fileName = dir + File.separator + p.getFileName();

            if (fileName != null)
            {
                try
                {
                    FileOutputStream f = new FileOutputStream(
                            new File(fileName));
                    p.write(f);
                    f.flush();
                    f.close();
                    main.log(Messages.getString("MailView.saving.attached.file.log1") //$NON-NLS-1$
                                + p.getFileName() 
                                + Messages.getString("MailView.saving.attached.file.log2") //$NON-NLS-1$
                                + dir);
                }
                catch (Exception e)
                {
                    main.log(e.toString());
                }
            }
        }
    }

    private void saveAttachedFile(SmtpMessagePart part)
    {
        FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
        dialog.setFilterNames(new String[] {
                Messages.getString("MailView.attached.files.ext"), //$NON-NLS-1$
                Messages.getString("MailView.all.files.ext") }); //$NON-NLS-1$
        dialog.setFilterExtensions(new String[] { "*.eml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setFilterPath(main.getSMTPService().getOutputDirectory());
        dialog.setFileName(part.getFileName());
        dialog.setText(Messages.getString("MailView.dialog.title")); //$NON-NLS-1$
        String fileName = dialog.open();
        if (fileName != null)
        {
            try
            {
                FileOutputStream f = new FileOutputStream(new File(fileName));
                part.write(f);
                f.flush();
                f.close();
                main.log(Messages
                        .getString("MailView.saving.attached.file.log") + fileName); //$NON-NLS-1$
            }
            catch (Exception e)
            {
                main.log(e.toString());
            }
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

    private void createTabFolder(final SashForm sash)
    {
        folder = new CTabFolder(sash, SWT.BORDER);

        folder.setSimple(false);
        folder.setMinimizeVisible(true);
        folder.setMaximizeVisible(true);
        folder.setUnselectedImageVisible(false);
        folder.setUnselectedCloseVisible(false);

        folder.setTabHeight(22);

        mailToolbar = new ToolBar(folder, SWT.FILL | SWT.FLAT);
        
        folder.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent event)
            {
                update(event);
                if (folder.getMaximized())
                    folderLayoutListener.restore(null);
                else
                    folderLayoutListener.maximize(null);
            }

            public void widgetSelected(SelectionEvent event)
            {
                update(event);
                if (isSynced() && event.item.getData() != null)
                	tableView.setSelection((StoredSmtpMessage) event.item.getData());
            }

            public void update(SelectionEvent event)
            {
                if (event.item != null)
                {
                	if (event.item.getData() != null)
	                {
                		updateAttachedFilesButton(((StoredSmtpMessage) event.item.getData())
	                            .getMessage().getInternalParts());
                		viewModeListener.updateMailViewMode();
	                    
	                    setTopRight(mailToolbar);
	                }
                	else
            		if (event.item == logTabItem)
            			setTopRight(logViewToolBar);
                }
            }
        });

        final ToolItem showLogViewToolItem = new ToolItem(mailToolbar, SWT.PUSH);
        showLogViewToolItem.setImage(SWTHelper.loadImage("console_view.gif")); //$NON-NLS-1$
        showLogViewToolItem.setToolTipText(Messages
                .getString("MailsterSWT.showLogView.tooltip")); //$NON-NLS-1$
        showLogViewToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) 
			{
				createLogConsole(true);
			}		
		});
        
        new ToolItem(mailToolbar, SWT.SEPARATOR);

        ToolItem viewModeItem = SWTHelper.createToolItem(
						mailToolbar,
		                SWT.FLAT | SWT.DROP_DOWN,
		                "", Messages.getString("MailView.toggle.toRawViewTooltip"), "html.gif", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        viewModeListener = new MailViewModeDropdownListener(viewModeItem);
        viewModeItem.addSelectionListener(viewModeListener);
        viewModeItem.setEnabled(true);

        ToolItem attachedFilesItem = SWTHelper.createToolItem(
        				mailToolbar,
                        SWT.FLAT | SWT.DROP_DOWN,
                        "", Messages.getString("MailView.attach.tooltip"), "attach.gif", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        dropdownListener = new AttachedFilesDropdownListener(attachedFilesItem);
        attachedFilesItem.addSelectionListener(dropdownListener);
        
        createToolBarCommons(mailToolbar);
        
        folderLayoutListener = new MCTabFolder2Listener(sash, folder);
        folder.addCTabFolder2Listener(folderLayoutListener);
        
        new ToolItem(mailToolbar, SWT.SEPARATOR);
        
        folder.setTopRight(mailToolbar);
        folder.setSelectionBackground(tabGradient,
                new int[] { 10, 20, 30, 40 }, true);
        folder.setSelectionForeground(SWTHelper.getDisplay().getSystemColor(
                SWT.COLOR_WHITE));
    }

    public void showURL(Image img, String url,  String title)
    {
    	showURL(img, url, title, false);
    }
    
    public void showURL(String url, boolean setURLAsTitle)
    {
    	showURL(null, url, null, setURLAsTitle);
    }
    
    public void showURL(Image img, String url, String title, boolean setURLAsTitle)
    {
        CTabItem item = null;
        try
        {
            item = new CTabItem(folder, SWT.CLOSE);
            if (setURLAsTitle)
                item.setText(url);
            else
            if (title != null)
            	item.setText(title);
            else
                item.setText(Messages.getString("MailView.tabitem.title")); //$NON-NLS-1$
            
            if (img != null)
            	item.setImage(img);
            else
            	item.setImage(homeImage);

            GridData gridData = new GridData();
            gridData.horizontalAlignment = GridData.FILL;
            gridData.grabExcessHorizontalSpace = true;
            gridData.grabExcessVerticalSpace = true;
            gridData.verticalAlignment = GridData.FILL;

            final Browser b = createBrowser(folder);
            b.setUrl(url); //$NON-NLS-1$
            b.setLayoutData(gridData);
            item.setControl(b);
            folder.setSelection(item);
            folderLayoutListener.maximize(null);

            item.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    b.dispose();
                }
            });
        }
        catch (SWTError swt)
        {
            main.log(swt.getMessage());
            item.dispose();
            return;
        }
    }

    public boolean isTableDisposed()
    {
        return tableView.isTableDisposed();
    }
    
    public AbstractEventList<StoredSmtpMessage> getDataList()
    {
        return tableView.getEventList();
    }
    
    public void clearDataList()
    {
    	tableView.clearQueue(main.getFilterTreeView());    	
        closeTabs(true);
    }
    
    public void closeTabs(boolean onlyMailTabs)
    {
        for (int i = folder.getItemCount() - 1; i >= 0; i--)
        {
            CTabItem item = folder.getItems()[i];

            if (item.getData() != null)
                openedMailsIds.remove(((StoredSmtpMessage) item.getData())
                        .getMessageId());

            if (item.getData() != null || !onlyMailTabs)
            	item.dispose();
        }

        if (dropdownListener != null)
        	dropdownListener.clear();
    }    
    
    public void closeTab(String id)
    {
        if (openedMailsIds.contains(id))
        {
            for (int i = 0, max = folder.getItemCount(); i < max; i++)
            {
                if (folder.getItems()[i].getData() != null
                        && id.equals(((StoredSmtpMessage) folder.getItems()[i]
                                .getData()).getMessageId()))
                {
                    folder.getItems()[i].dispose();
                    if (folder.getSelectionIndex() == i)
                    	dropdownListener.clear();
                    break;
                }
            }
        }
    }    

    public String getPreferredContentType()
    {
        return preferredContentType;
    }

    public void setPreferredContentType(String preferredContentType)
    {
        this.preferredContentType = preferredContentType;
    }

    public boolean isForcedMozillaBrowserUse()
    {
        return forcedMozillaBrowserUse;
    }

    public void setForcedMozillaBrowserUse(boolean forcedMozillaBrowserUse)
    {
        this.forcedMozillaBrowserUse = forcedMozillaBrowserUse;
    }
    
    protected MailsterSmtpService getSMTPService()
    {
        return main.getSMTPService();
    }

	public boolean isSynced() 
	{
		return synced;
	}
}