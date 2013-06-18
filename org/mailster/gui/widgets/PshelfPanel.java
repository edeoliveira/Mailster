package org.mailster.gui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.gui.widgets.pshelf.PShelf;
import org.mailster.gui.widgets.pshelf.PShelfItem;

public class PshelfPanel
	extends Composite
{
	private CLabel titleLabel;
	
	private StackLayout sl = new StackLayout();
	private Shell popup;
	private Object savedLayoutData;

	private Composite shelfComposite;
	private Composite toolbarComposite;

	private PShelf shelf;

	private MouseAdapter mouseAdapter = new MouseAdapter() {
		public void mouseUp(MouseEvent e)
		{
			reparent();
		}
	};
	
	public PshelfPanel(Composite parent, int style)
	{
		super(parent, style);
		setLayout(sl);
		popup = new Shell(parent.getDisplay(), SWT.NONE);
		popup.setLayout(new FillLayout());

		shelfComposite = new Composite(this, style);

		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 2;
		layout.marginHeight = 10;
		layout.verticalSpacing = 4;
		shelfComposite.setLayout(layout);

		titleLabel = new CLabel(shelfComposite, SWT.NONE);
		titleLabel.setLayoutData(LayoutUtils.createGridData(GridData.BEGINNING, GridData.BEGINNING, true, false, 1, 1));
		titleLabel.setFont(SWTHelper.createFont(new FontData("Segoe UI", 10, SWT.BOLD))); //$NON-NLS-1$
		titleLabel.setImage(SWTHelper.loadImage("mail.gif")); //$NON-NLS-1$
		
		CVerticalLabel cl = new CVerticalLabel(shelfComposite, SWT.NONE);
		cl.setImage(SWTHelper.loadImage("icon_shrink_panel.png")); //$NON-NLS-1$
		cl.setToolTipText("Shrink panel");
		cl.setLayoutData(LayoutUtils.createGridData(GridData.END, GridData.BEGINNING, false, false, 1, 1));
		cl.setMargin(1);
		cl.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				switchViews(toolbarComposite);
			}
		});

		shelf = new PShelf(shelfComposite, SWT.NONE);
		shelf.setBackground(SWTHelper.createColor(128, 173, 249));
		shelf.setLayoutData(LayoutUtils.createGridData(GridData.FILL, GridData.FILL, true, true, 2, 1));

		sl.topControl = shelfComposite;

		toolbarComposite = new Composite(this, style);
		toolbarComposite.setLayout(new GridLayout(1, false));

		cl = new CVerticalLabel(toolbarComposite, SWT.NONE);
		cl.setImage(SWTHelper.loadImage("icon_expand_panel.png")); //$NON-NLS-1$
		cl.setToolTipText("Expand panel");
		cl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		cl.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				switchViews(shelfComposite);
			}
		});
	}

	public String getTitle()
	{
		return titleLabel.getText(); 
	}
	
	public void setTitle(String text)
	{
		titleLabel.setText(text); 
	}
	
	private void switchViews(Composite c)
	{
		if (popup.isVisible())
			reparent();
		sl.topControl = c;
		
		PshelfPanel.this.layout();
		Event ev = new Event();
		ev.item = PshelfPanel.this;
		PshelfPanel.this.notifyListeners(PShelfPanelListener.SWITCHED_EVENT_TYPE, ev);
	}
	
	private void reparent()
	{
		popup.setVisible(false);

		if (popup.getChildren().length > 0)
		{
			Composite pane = (Composite) popup.getChildren()[0];
			PShelfItem item = (PShelfItem) pane.getData();
			pane.setParent(item.getBodyParent());
			pane.setLayoutData(savedLayoutData);
			shelf.layout();
		}		
		
		removePopupListener(popup, mouseAdapter);
	}

	public void addPShelfPanelListener(PShelfPanelListener listener)
	{
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		addListener(PShelfPanelListener.SWITCHED_EVENT_TYPE, listener);
	}

	public void removePShelfPanelListener(PShelfPanelListener listener)
	{
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);

		removeListener(PShelfPanelListener.SWITCHED_EVENT_TYPE, listener);
	}
	
	private static void addPopupListener(Composite c, MouseListener ml)
	{
		for (Control ctrl : c.getChildren())
		{
			if (ctrl instanceof Composite)
				addPopupListener((Composite) ctrl, ml);
			else
				ctrl.addMouseListener(ml);
		}
		c.addMouseListener(ml);
	}

	private static void removePopupListener(Composite c, MouseListener ml)
	{
		for (Control ctrl : c.getChildren())
		{
			if (ctrl instanceof Composite)
				removePopupListener((Composite) ctrl, ml);
			else
				ctrl.removeMouseListener(ml);
		}
		c.removeMouseListener(ml);
	}

	public void setBackground(Color color)
	{
		super.setBackground(color);
		shelfComposite.setBackground(color);
		toolbarComposite.setBackground(color);
		titleLabel.setBackground(color);
	}

	public Composite addPShelfItem(String text, Image img, int style)
	{
		PShelfItem item = new PShelfItem(shelf, SWT.BORDER);
		item.setText(text);
		item.setImage(img);

		CVerticalLabel cl = new CVerticalLabel(toolbarComposite, SWT.NONE);
		cl.setText(text);
		cl.setImage(img);
		cl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

		final Composite pane = item.getBody();
		pane.setData(item);

		cl.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				if (popup.isVisible())
					reparent();
				pane.setParent(popup);
				savedLayoutData = pane.getLayoutData();
				pane.setLayoutData(null);
				popup.pack();
				popup.layout();

				CVerticalLabel lbl = (CVerticalLabel) e.widget;
				Rectangle r = lbl.getBounds();

				addPopupListener(popup, mouseAdapter);
				popup.setLocation(lbl.getParent().toDisplay(r.x + r.width, Math.max(r.y-1,0)));
				popup.setVisible(true);
				popup.forceActive();
			}
		});

		return item.getBody();
	}

	public boolean isToolbarVisible()
	{
		return sl.topControl == toolbarComposite;
	}
	
	public void setToolbarVisible(boolean visible)
	{
		if (visible)
			switchViews(toolbarComposite);
		else
			switchViews(shelfComposite);
	}	

	public Point computeSize(int wHint, int hHint, boolean changed)
	{
		return sl.topControl.computeSize(wHint, hHint, changed);
	}

	public Composite getSelectedPShelfItemBody()
	{
		return shelf.getSelection().getBody();
	}
	
	public Composite getPShelfItemBody(int idx)
	{
		return shelf.getItems()[idx].getBody();
	}
}
