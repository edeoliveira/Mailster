package org.mailster.gui.views.mailview;

import javax.mail.internet.MimeMultipart;

import org.bouncycastle.mail.smime.SMIMESigned;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.mailster.MailsterSWT;
import org.mailster.core.crypto.MailsterKeyStoreFactory;
import org.mailster.core.crypto.smime.SmimeUtilities;
import org.mailster.core.mail.SmtpHeadersInterface;
import org.mailster.core.mail.SmtpMessage;
import org.mailster.core.mail.SmtpMessagePart;
import org.mailster.core.pop3.mailbox.StoredSmtpMessage;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.crypto.CertificateDialog;
import org.mailster.util.DateUtilities;
import org.mailster.util.MailUtilities;

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
 * OutlookMailView.java - The new outlook like mail view.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.27 $, $Date: 2009/05/18 22:14:26 $
 */
public class OutlookMailView
{
	public static final String DEFAULT_PREFERRED_CONTENT = "text/html"; //$NON-NLS-1$

	private static final Image SMIME_IMAGE = SWTHelper.loadImage("smime.gif"); //$NON-NLS-1$
	private static final Image SMIME_OK_IMAGE = SWTHelper.loadImage("smime_ok.gif"); //$NON-NLS-1$
	private static final Image SMIME_NOK_IMAGE = SWTHelper.loadImage("smime_nok.gif"); //$NON-NLS-1$

	private MailsterSWT main;

	private boolean forcedMozillaBrowserUse = false;
	private String preferredContentType = DEFAULT_PREFERRED_CONTENT;

	private StoredSmtpMessage message;

	private Composite view;
	private SashForm sash;
	private Composite header;
	private Composite body;
	private StackLayout bodyCompositeStackLayout;
	private Browser browser;
	private Text rawMailView;

	private FormData data_cc;
	private FormData dataCc;
	private FormData data_bcc;
	private FormData dataBcc;
	private FormData dataToolbar;
	private FormData dataAtts;

	private boolean drawToolbarImage;
	private ToolBar toolbar;
	private ToolItem signedItem;
	private Image toolbarImage;
	private AttachmentsView attachmentsView;

	private Label title;
	private Label from;
	private Label date;
	private Label to;
	private Label cc;
	private Label bcc;
	private Label _cc;
	private Label _bcc;

	public OutlookMailView(Composite parent)
	{
		this.main = MailsterSWT.getInstance();
		createView(parent);
	}

	private void updateAttachments(final SmtpMessagePart current)
	{
		attachmentsView.clear();
		attachmentsView.recurseMessageParts(current);
		attachmentsView.updateVisibility();
	}

	public void setMail(StoredSmtpMessage stored)
	{
		message = stored;
		if (stored == null)
		{
			main.getOutlineView().setMessage(null);
			return;
		}

		final SmtpMessage msg = stored.getMessage();
		updateAttachments(msg.getInternalParts());

		browser.setText(msg.getPreferredContent(preferredContentType));
		rawMailView.setText(msg.toString());
		title.setText(msg.getSubject());
		from.setText(stored.getMessageFrom());
		synchronized(DateUtilities.ADF_FORMATTER)
		{
			date.setText(DateUtilities.ADF_FORMATTER.format(stored.getMessageDate()));
		}

		SmtpHeadersInterface headers = msg.getHeaders();
		String list = MailUtilities.formatEmailList(headers, SmtpHeadersInterface.TO);
		to.setText(list);

		list = MailUtilities.formatEmailList(headers, SmtpHeadersInterface.CC);

		if ("".equals(list))
		{
			cc.setText("");
			cc.setVisible(false);
			_cc.setVisible(false);
			dataCc.height = 0;
			data_cc.height = 0;
			dataToolbar.top = new FormAttachment(date, 4);
		}
		else
		{
			cc.setText(list);
			cc.setVisible(true);
			_cc.setVisible(true);
			dataCc.height = SWT.DEFAULT;
			data_cc.height = SWT.DEFAULT;
			dataToolbar.top = new FormAttachment(to, 4);
		}

		list = MailUtilities.formatBccList(msg);

		if ("".equals(list))
		{
			bcc.setText("");
			bcc.setVisible(false);
			_bcc.setVisible(false);
			dataBcc.height = 0;
			data_bcc.height = 0;
		}
		else
		{
			bcc.setText(list);
			bcc.setVisible(true);
			_bcc.setVisible(true);
			dataBcc.height = SWT.DEFAULT;
			data_bcc.height = SWT.DEFAULT;
			dataToolbar.top = new FormAttachment(cc.isVisible()? cc : to, 4);
		}

		dataAtts.height = attachmentsView.hasItems() ? attachmentsView.computeSize(SWT.DEFAULT, SWT.DEFAULT).y : 8 ;
		boolean isSigned = SmimeUtilities.isSignedMessage(msg);

		signedItem.setImage(SMIME_IMAGE);

		if (isSigned)
		{
			view.getDisplay().asyncExec(new Runnable() {
				public void run()
				{
					boolean isSignatureValid = false;
					signedItem.setEnabled(true);
					try
					{
						isSignatureValid = SmimeUtilities.isValid(new SMIMESigned((MimeMultipart) msg.asMimeMessage()
								.getContent()), MailsterKeyStoreFactory.getInstance().getRootCertificate());
					} catch (Exception e)
					{
						MailsterSWT.getInstance().log(e.getMessage());
					}

					if (isSignatureValid)
					{
						signedItem.setImage(SMIME_OK_IMAGE);
						signedItem.setHotImage(SMIME_OK_IMAGE);
						signedItem.setToolTipText(Messages.getString("HeadersView.signatureOk.tooltip")); //$NON-NLS-1$
					}
					else
					{
						signedItem.setImage(SMIME_NOK_IMAGE);
						signedItem.setHotImage(SMIME_NOK_IMAGE);
						signedItem.setToolTipText(Messages.getString("HeadersView.signatureNotOk.tooltip")); //$NON-NLS-1$
					}
					hideToolbar();
				}
			});
		}
		else
		{
			signedItem.setEnabled(false);
			signedItem.setToolTipText("");
		}

		resizeHeader();
		hideToolbar();

		main.getOutlineView().setMessage(msg);
	}

	public Composite getView()
	{
		return view;
	}

	private void resizeHeader()
	{
		Point max = view.getSize();
		if (max.y > 0)
		{
			int y = max.y - header.computeSize(max.x, SWT.DEFAULT).y - sash.getSashWidth();
			int p = (y * 100) / max.y;
			if (sash.getWeights()[1] != p)
				sash.setWeights(new int[] {100 - p, p});
		}
	}

	private void hideToolbar()
	{
		drawToolbarImage = true;
		generateToolbarImage();
		toolbar.setVisible(false);
		header.layout();
	}

	private void generateToolbarImage()
	{
		Display display = header.getDisplay();
		Point pt = toolbar.getSize();
		toolbarImage = new Image(display, pt.x, pt.y);
		GC gc = new GC(toolbarImage);
		toolbar.print(gc);
		gc.dispose();
		ImageData imageData = toolbarImage.getImageData();
		imageData.alpha = 64;
		toolbarImage = new Image(display, imageData);
	}

	private void createView(Composite parent)
	{
		view = new Composite(parent, SWT.NONE);
		view.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_SIZENS));
		view.setLayout(new FillLayout());
		
		sash = new SashForm(view, SWT.VERTICAL | SWT.FLAT);
		sash.setLayout(new FillLayout());
		sash.setSashWidth(2);

		view.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event)
			{
				resizeHeader();
			}
		});

		header = new Composite(sash, SWT.NONE);
		body = new Composite(sash, SWT.NONE);
		sash.setWeights(new int[] {20, 80});

		Cursor arrow = parent.getDisplay().getSystemCursor(SWT.CURSOR_ARROW);
		header.setCursor(arrow);
		body.setCursor(arrow);
		Color w = SWTHelper.getColor(SWT.COLOR_WHITE);
		header.setBackground(w);
		body.setBackground(w);
		FillLayout fl = new FillLayout();
		fl.marginWidth = 4;
		body.setLayout(fl);

		FormLayout layout = new FormLayout();
		layout.marginLeft = 4;
		layout.marginRight = 4;
		header.setLayout(layout);

		title = new Label(header, SWT.WRAP);
		FontData fdata = title.getFont().getFontData()[0];
		title.setFont(SWTHelper.createFont(new FontData(fdata.getName(), 12, SWT.BOLD)));
		from = new Label(header, SWT.WRAP);
		from.setFont(SWTHelper.createFont(new FontData(fdata.getName(), 9, SWT.NONE)));
		date = new Label(header, SWT.WRAP);
		to = new Label(header, SWT.WRAP | SWT.TRANSPARENT);
		cc = new Label(header, SWT.WRAP);
		cc.setVisible(false);
		bcc = new Label(header, SWT.WRAP);
		bcc.setVisible(false);

		toolbar = new ToolBar(header, SWT.FLAT | SWT.RIGHT);
		drawToolbarImage = true;

		toolbar.addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseExit(MouseEvent e)
			{
				hideToolbar();
			}
		});

		header.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e)
			{
				boolean post = toolbar.getBounds().contains(e.x, e.y);
				if (post)
				{
					drawToolbarImage = false;
					toolbar.setVisible(true);
					header.layout();
				}
			}
		});

		header.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e)
			{
				if (!drawToolbarImage)
					return;

				Rectangle tb = toolbar.getBounds();
				e.gc.drawImage(toolbarImage, tb.x, tb.y);
			}
		});

		final ToolItem viewModeItem = SWTHelper.createToolItem(toolbar, SWT.CHECK,
				"", Messages.getString("MailView.toggle.toMixedViewTooltip"), "mixedView.gif", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		signedItem = SWTHelper.createToolItem(toolbar, SWT.FLAT, "", "", "smime.gif", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		signedItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event)
			{
				showCertificateDialogBox();
			}
		});

		attachmentsView = new AttachmentsView(header, SWT.NONE);

		Label _date = new Label(header, SWT.NONE);
		_date.setText("Envoyé : ");
		Label _to = new Label(header, SWT.NONE);
		_to.setText("A : ");

		_cc = new Label(header, SWT.NONE);
		_cc.setText("Cc : ");
		_cc.setVisible(false);
		_bcc = new Label(header, SWT.NONE);
		_bcc.setText("Bcc : ");
		_bcc.setVisible(false);

		final FormData dataTitle = new FormData();
		dataTitle.top = new FormAttachment(0, 6);
		dataTitle.left = new FormAttachment(0);
		title.setLayoutData(dataTitle);

		final FormData dataFrom = new FormData();
		dataFrom.top = new FormAttachment(title, 4);
		from.setLayoutData(dataFrom);

		FormData data_date = new FormData();
		data_date.top = new FormAttachment(from, 4);
		_date.setLayoutData(data_date);

		FormData dataDate = new FormData();
		dataDate.top = new FormAttachment(from, 4);
		dataDate.left = new FormAttachment(_date, 4);
		date.setLayoutData(dataDate);

		FormData data_to = new FormData();
		data_to.top = new FormAttachment(_date, 4);
		_to.setLayoutData(data_to);

		final FormData dataTo = new FormData();
		dataTo.top = new FormAttachment(_date, 4);
		dataTo.left = new FormAttachment(_date, 4);
		to.setLayoutData(dataTo);

		data_cc = new FormData();
		data_cc.top = new FormAttachment(to, 4);
		_cc.setLayoutData(data_cc);

		dataCc = new FormData();
		dataCc.top = new FormAttachment(to, 4);
		dataCc.left = new FormAttachment(_date, 4);
		cc.setLayoutData(dataCc);

		data_bcc = new FormData();
		data_bcc.top = new FormAttachment(cc, 4);
		_bcc.setLayoutData(data_bcc);

		dataBcc = new FormData();
		dataBcc.top = new FormAttachment(cc, 4);
		dataBcc.left = new FormAttachment(_date, 4);
		bcc.setLayoutData(dataBcc);

		dataToolbar = new FormData();
		dataToolbar.top = new FormAttachment(bcc, 4);
		dataToolbar.right = new FormAttachment(100);
		toolbar.setLayoutData(dataToolbar);

		dataAtts = new FormData();
		dataAtts.left = new FormAttachment(0);
		dataAtts.top = new FormAttachment(toolbar, 4);
		dataAtts.right = new FormAttachment(100);
		attachmentsView.setLayoutData(dataAtts);

		header.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event)
			{
				int w = header.getBounds().width - 8;
				dataTitle.width = w;
				dataFrom.width = w;
				w -= date.getLocation().x + toolbar.getSize().x;
				dataTo.width = w;
				dataCc.width = w;
				dataBcc.width = w;
				resizeHeader();
			}
		});

		header.pack();

		title.setBackground(w);
		from.setBackground(w);
		from.setForeground(SWTHelper.getColor(SWT.COLOR_DARK_GRAY));
		date.setBackground(w);
		to.setBackground(w);
		_date.setBackground(w);
		_to.setBackground(w);
		_cc.setBackground(w);
		_bcc.setBackground(w);
		cc.setBackground(w);
		bcc.setBackground(w);
		sash.setBackground(w);
		toolbar.setBackground(w);

		Color b = SWTHelper.createColor(155, 189, 231);
		_date.setForeground(b);
		_to.setForeground(b);
		_cc.setForeground(b);
		_bcc.setForeground(b);

		final Composite bodyComposite = new Composite(body, SWT.NONE);
		bodyCompositeStackLayout = new StackLayout();
		bodyComposite.setLayout(bodyCompositeStackLayout);

		browser = createBrowser(bodyComposite);
		rawMailView = new Text(bodyComposite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
		rawMailView.setEditable(false);
		bodyCompositeStackLayout.topControl = browser;

		viewModeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				if (viewModeItem.getSelection())
					bodyCompositeStackLayout.topControl = rawMailView;
				else
					bodyCompositeStackLayout.topControl = browser;
				bodyComposite.layout();
			}
		});
	}

	private Browser createBrowser(Composite parent)
	{
		if (forcedMozillaBrowserUse)
			return new Browser(parent, SWT.NONE | SWT.MOZILLA);
		else
			return new Browser(parent, SWT.NONE);
	}

	public void setRawText(String text)
	{
		rawMailView.setText(text);
	}

	public void showCertificateDialogBox()
	{
		try
		{
			if (message != null)
				(new CertificateDialog(view.getShell(), message.getMessage().asMimeMessage())).open();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setPreferredContentType(String preferredContentType)
	{
		this.preferredContentType = preferredContentType;
	}

	public void setForcedMozillaBrowserUse(boolean forcedMozillaBrowserUse)
	{
		this.forcedMozillaBrowserUse = forcedMozillaBrowserUse;
	}
}
