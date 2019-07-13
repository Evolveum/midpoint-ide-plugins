package com.evolveum.midpoint.eclipse.ui.controls.text;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

// TODO make this Composite itself
public class TextValueViewer  {	

	private Combo format;
	private Composite textSwitched;
	private Text textWrapped;
	private Text textUnwrapped;
	private Button wrap;
	private Object object;
	
	public void create(Composite parent) {

		Composite main = new Composite(parent, SWT.SHADOW_NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		main.setLayout(gridLayout);
		main.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Group options = new Group(main, SWT.SHADOW_IN);
		options.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginHeight = 5;
		fillLayout.marginWidth = 5;
		fillLayout.spacing = 15;
		options.setLayout(fillLayout);
		
		format = new Combo(options, SWT.DROP_DOWN | SWT.READ_ONLY);
		format.setItems(Format.getDisplayNames());
		format.select(0);
		format.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setText();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		textSwitched = new Composite(main, SWT.SHADOW_NONE);
		textSwitched.setLayoutData(new GridData(GridData.FILL_BOTH));
		StackLayout stackLayout = new StackLayout();
		textSwitched.setLayout(stackLayout);
		
		textWrapped = createText(textSwitched, true);
		textUnwrapped = createText(textSwitched, false);

		wrap = new Button(options, SWT.CHECK);
		wrap.setText("Wrap text");
		wrap.setSelection(true);
		wrap.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stackLayout.topControl = wrap.getSelection() ? textWrapped : textUnwrapped;
				textSwitched.layout();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		stackLayout.topControl = wrap.getSelection() ? textWrapped : textUnwrapped;
		textSwitched.layout();
	}
	
	public Text createText(Composite parent, boolean isWrap) {
		Text text = new Text(parent, SWT.MULTI | SWT.BORDER | (isWrap ? SWT.WRAP : SWT.H_SCROLL) | SWT.V_SCROLL);
		//text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.setFont(JFaceResources.getTextFont());
		return text;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
		setText();
	}

	private void setText() {
		int index = format.getSelectionIndex();
		if (index < 0) {
			index = 0;
		}
		Format format = Format.values()[index];
		String formattedText = format.format(object);
		textWrapped.setText(formattedText);
		textUnwrapped.setText(formattedText);
	}
	
}
