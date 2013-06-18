package org.mailster.gui.views.mailview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.mailster.MailsterSWT;
import org.mailster.core.mail.SmtpMessagePart;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.views.mailview.cfilelist.CFileItem;
import org.mailster.gui.views.mailview.cfilelist.CFileList;
import org.mailster.gui.views.mailview.cfilelist.DefaultCFileListRenderer;
import org.mailster.util.OsUtilities;

public class AttachmentsView
	extends Composite
{
	private static final Color WHITE = SWTHelper.getColor(SWT.COLOR_WHITE);
	private static final Color GRADIENT_COLOR = SWTHelper.createColor(238, 239, 240);
	private static final Color GRAY = GRADIENT_COLOR;

	private DecimalFormat dcFormat;

	private CLabel label;
	private Composite outer;
	private ScrolledComposite sc;
	private CFileList list;
	private Menu menu;

	public AttachmentsView(Composite parent, int style)
	{
		super(parent, style);

		dcFormat = new DecimalFormat("#,##0"); //$NON-NLS-1$
		dcFormat.setGroupingSize(3);

		buildView();
		buildMenu();
	}

	private void buildMenu()
	{
		menu = new Menu(getShell());
		MenuItem open = new MenuItem(menu, SWT.NONE);
		open.setImage(SWTHelper.loadImage("open_doc.gif"));
		open.setText("Open");
		open.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event)
			{
				CFileItem item = list.getSelection();
				String fileName = ((SmtpMessagePart) item.getData()).getFileName();
				Program p = Program.findProgram(fileName.substring(fileName.lastIndexOf('.')));
				if (p != null
						&& ConfigurationManager.CONFIG_STORE.getBoolean(ConfigurationManager.EXECUTE_ENCLOSURE_ON_CLICK_KEY))
				{
					String tmpDir = System.getProperty("java.io.tmpdir");
					fileName = tmpDir + fileName;
					saveAllAttachments(new CFileItem[] {(CFileItem) item}, tmpDir);
					MailsterSWT.getInstance().log(Messages.getString("MailView.execute.file") + fileName + " ..."); //$NON-NLS-1$ //$NON-NLS-2$
					p.execute(fileName);
				}
			}
		});
		
		new MenuItem(menu, SWT.SEPARATOR);
		MenuItem save = new MenuItem(menu, SWT.NONE);
		save.setImage(SWTHelper.loadImage("save.gif"));
		save.setText("Save as ...");
		save.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event)
			{
				CFileItem item = list.getSelection();
				saveAttachedFile((SmtpMessagePart) item.getData());
			}
		});
		
		MenuItem saveAll = new MenuItem(menu, SWT.NONE);
		saveAll.setImage(SWTHelper.loadImage("saveall.gif"));
		saveAll.setText("Save all attachments ...");
		saveAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event)
			{
				try
				{
					String tmpDir = "."+File.separatorChar+"tmp";
					File dir = new File(tmpDir);
					dir.mkdir();
					tmpDir = dir.getCanonicalPath();
					DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
					directoryDialog.setMessage("Please select the directory where attachments will be saved and click OK");
			        directoryDialog.setFilterPath(tmpDir);
			        
			        tmpDir = directoryDialog.open();
			        if (tmpDir != null)
			        {
						saveAllAttachments(list.getItems(), tmpDir);
						OsUtilities.showDirectory(tmpDir);
			        }
				} catch (IOException e)
				{
					MailsterSWT.getInstance().log(e.getMessage());
				}
			}
		});
	}

	private void buildView()
	{
		GridLayout gl = new GridLayout(2, false);
		gl.marginRight = gl.marginWidth = gl.marginLeft = 0;
		setLayout(gl);
		setBackground(WHITE);

		label = new CLabel(this, SWT.NONE);
		label.setText("Message");
		label.setImage(SWTHelper.loadImage("mail.gif"));
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		label.setBackground(GRAY);

		outer = new Composite(this, SWT.NONE);
		outer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 1, 1));
		outer.setLayout(new FillLayout());

		sc = new ScrolledComposite(outer, SWT.V_SCROLL);
		sc.setBackgroundMode(SWT.INHERIT_DEFAULT);
		sc.setLayout(new FillLayout());

		sc.addListener(SWT.Resize, new Listener() {
			private Image oldImage;

			public void handleEvent(Event e)
			{
				Point pt = sc.getSize();
				Image newImage = new Image(sc.getDisplay(), 1, Math.max(1, pt.y));
				GC gc = new GC(newImage);
				gc.setBackground(WHITE);
				gc.setForeground(GRADIENT_COLOR);
				gc.fillGradientRectangle(0, 0, 1, pt.y * 4 / 5, true);
				gc.dispose();
				sc.setBackgroundImage(newImage);

				if (oldImage != null)
					oldImage.dispose();
				oldImage = newImage;
			}
		});

		list = new CFileList(sc, SWT.NONE);

		sc.setContent(list);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		list.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e)
			{
				int w = sc.getSize().x - (sc.getVerticalBar().isVisible() ? sc.getVerticalBar().getSize().x : 0);
				sc.setMinSize(list.computeSize(w, SWT.DEFAULT));
			}
		});

		addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e)
			{
				int h = e.height - 1;
				if (label.isVisible())
				{
					Rectangle rl = label.getBounds();
					Rectangle r = outer.getBounds();

					e.gc.setBackground(GRAY);
					e.gc.fillRectangle(0, rl.y, r.x - 1, rl.height);
					e.gc.drawLine(r.x - 1, r.y, r.x - 1, Math.max(r.y + r.height, rl.y + rl.height) - 1);

					e.gc.setLineDash(DefaultCFileListRenderer.DASH_LINE);
					int w = e.width - 1;
					e.gc.drawLine(0, r.y - 1, w, r.y - 1);
					e.gc.drawLine(0, h, w, h);
				}
				else
				{
					e.gc.setLineDash(DefaultCFileListRenderer.DASH_LINE);
					e.gc.drawLine(0, h, e.width - 1, h);
				}
			}
		});

		updateVisibility();
	}

	void recurseMessageParts(final SmtpMessagePart current)
	{
		if (current != null)
		{
			SmtpMessagePart[] files = current.getAttachedFiles();

			for (int i = 0, max = files.length; i < max; i++)
				add(files[i]);

			if (current.getParts() != null)
			{
				for (SmtpMessagePart part : current.getParts())
					recurseMessageParts(part);
			}
		}
	}

	public void clear()
	{
		list.removeAll();
	}

	private void add(SmtpMessagePart part)
	{
		CFileItem item = new CFileItem(list, SWT.NONE);
		String fileName = part.getFileName();
		StringBuilder sb = new StringBuilder(fileName);
		sb.append(" (");
		getFormattedPartSize(sb, part);
		sb.append(')');
		item.setText(sb.toString());

		if (part.getContentType().contains("pkcs")) //$NON-NLS-1$
			item.setImage(SWTHelper.loadImage("smime.gif")); //$NON-NLS-1$
		else if (fileName.lastIndexOf('.') != -1)
		{
			Program p = Program.findProgram(fileName.substring(fileName.lastIndexOf('.')));
			if (p != null)
			{
				ImageData data = p.getImageData();
				if (data != null)
					item.setImage(new Image(getDisplay(), data));
			}
		}
		else if (part.getContentType().startsWith("message")) //$NON-NLS-1$
			item.setImage(SWTHelper.loadImage("mail.gif")); //$NON-NLS-1$

		item.setData(part);
		list.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event)
			{
	            // Find where to show the dropdown list
	            CFileItem item = (CFileItem) event.item;
	            Rectangle rect = item.getBounds();
	            Point pt = list.toDisplay(rect.x, rect.y);
	            menu.setLocation(pt.x, pt.y + rect.height);
	            menu.setVisible(true);
			}
		});
	}

	public void updateVisibility()
	{
		boolean visible = list.getItems().length > 0;
		label.setVisible(visible);
		outer.setVisible(visible);
		redraw();
	}

	public boolean hasItems()
	{
		return list.getItems().length > 0;
	}

	private void getFormattedPartSize(StringBuilder sb, SmtpMessagePart part)
	{
		int size = part.getSize();
		String unit = Messages.getString("MailView.fileSizeUnit.bytes"); //$NON-NLS-1$

		if (size > 1E9)
		{
			size = (int) (size / 1E9);
			unit = Messages.getString("MailView.fileSizeUnit.gigabytes"); //$NON-NLS-1$
		}
		else if (size > 1E6)
		{
			size = (int) (size / 1E6);
			unit = Messages.getString("MailView.fileSizeUnit.megabytes"); //$NON-NLS-1$
		}
		else if (size > 1E4)
		{
			size = (int) (size / 1E3);
			unit = Messages.getString("MailView.fileSizeUnit.kilobytes"); //$NON-NLS-1$
		}

		sb.append(dcFormat.format(size)).append(' ').append(unit); //$NON-NLS-1$
	}

	private void save(SmtpMessagePart p, String fileName, String dir)
	{
		MailsterSWT main = MailsterSWT.getInstance();

		if (fileName != null)
		{
			try
			{
				FileOutputStream f = new FileOutputStream(new File(fileName));
				p.write(f);
				f.flush();
				f.close();
				main.log(Messages.getString("MailView.saving.attached.file.log1") //$NON-NLS-1$
						+ p.getFileName() + Messages.getString("MailView.saving.attached.file.log2") //$NON-NLS-1$
						+ dir);
			} catch (Exception e)
			{
				main.log(e.toString());
			}
		}
	}

	private void saveAllAttachments(CFileItem[] items, String dir)
	{
		for (int i = 0, max = items.length; i < max; i++)
		{
			SmtpMessagePart p = (SmtpMessagePart) items[i].getData();
			String fileName = dir + File.separator + p.getFileName();

			save(p, fileName, dir);
		}
	}

	private void saveAttachedFile(SmtpMessagePart part)
	{
		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setFilterNames(new String[] {Messages.getString("MailView.all.files.ext")}); //$NON-NLS-1$
		dialog.setFilterExtensions(new String[] {"*.*"}); //$NON-NLS-1$
		dialog.setFilterPath(MailsterSWT.getInstance().getSMTPService().getOutputDirectory());
		dialog.setFileName(part.getFileName());
		dialog.setText(Messages.getString("MailView.dialog.title")); //$NON-NLS-1$
		String fileName = dialog.open();

		save(part, fileName, ".");
	}
}
