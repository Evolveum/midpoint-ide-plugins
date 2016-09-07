package com.evolveum.midpoint.eclipse.ui.prefs;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import com.evolveum.midpoint.eclipse.ui.util.Console;

/**
 * Hacked from the nice original of https://raw.githubusercontent.com/orctom/pathtools/master/PathTools/src/pathtools/TableFieldEditor.java
 * Original made by Sandip V. Chitale. Thank you for that.
 * 
 * This is semi-abstract, semi-hacked version: contains serverlist-specific functionality, but is not entirely specialized.
 * This is to allow finishing this class in the future.
 */
public abstract class AbstractServersFieldEditor<T extends DataItem> extends FieldEditor {

	/**
	 * The table widget; <code>null</code> if none (before creation or after
	 * disposal).
	 */
	protected Table table;

	/**
	 * The button box containing the Edit, Add, Remove, Up, and Down and buttons;
	 * <code>null</code> if none (before creation or after disposal).
	 */
	protected Composite buttonBox;

	protected Button addButton;
	protected Button editButton;
	protected Button selectButton;
	protected Button testButton;
	protected Button duplicateButton;
	protected Button removeButton;
	protected Button upButton;
	protected Button downButton;

	protected SelectionListener selectionListener;
	
	protected final String[] columnNames;
	protected final int[] columnWidths;
	
	protected List<T> currentItems;

	/**
	 * Creates a table field editor.
	 *
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param columnNames
	 *            the names of columns
	 * @param columnWidths
	 *            the widths of columns
	 * @param parent
	 *            the parent of the field editor's control
	 *
	 */
	protected AbstractServersFieldEditor(String name, String labelText,
			String[] columnNames, int[] columnWidths, Composite parent) {
		init(name, labelText);
		this.columnNames = columnNames;
		this.columnWidths = columnWidths;
		createControl(parent);
	}

	/**
	 * Combines the given list of items into a single string. This method is the
	 * converse of <code>parseStringRepresentation</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 *
	 * @param items
	 *            the list of items
	 * @return the combined string
	 * @see #parseStringRepresentation
	 */
	protected abstract String createStringRepresentation(List<T> items);

	/**
	 * Splits the given string into a array of array of value. This method is
	 * the converse of <code>createList</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 *
	 * @param string
	 *            the string
	 * @return an array of array of <code>string</code>
	 * @see #createList
	 */
	protected abstract List<T> parseStringRepresentation(String string);

	/**
	 * Creates and returns a new value row for the table.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 *
	 * @return a new item
	 */
	protected abstract T createNewItem();

	/**
	 * Creates the Add, Remove, Up, and Down button in the given button box.
	 *
	 * @param box
	 *            the box for the buttons
	 */
	private void createButtons(Composite box) {
		addButton = createPushButton(box, "New");
		editButton = createPushButton(box, "Edit");
		selectButton = createPushButton(box, "Select");
		testButton = createPushButton(box, "Test");
		duplicateButton = createPushButton(box, "Duplicate");
		removeButton = createPushButton(box, "Remove");
		upButton = createPushButton(box, "Up");
		downButton = createPushButton(box, "Down");
	}

	private Button createPushButton(Composite parent, String name) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(name);
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(getSelectionListener());
		return button;
	}

	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) table.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	public void createSelectionListener() {
		selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == editButton) {
					editPressed();
				} else if (widget == selectButton) {
					selectPressed();
				} else if (widget == testButton) {
					testPressed();
				} else if (widget == addButton) {
					addPressed();
				} else if (widget == duplicateButton) {
					duplicatePressed();
				} else if (widget == removeButton) {
					removePressed();
				} else if (widget == upButton) {
					upPressed();
				} else if (widget == downButton) {
					downPressed();
				} else if (widget == table) {
					selectionChanged();
				}
			}
		};
	}

	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gridData.horizontalSpan = 2;
		gridData.widthHint = 550;
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(2, false));

		table = getTableControl(composite);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
		table.setLayoutData(gd);

		buttonBox = getButtonBoxControl(composite);
		gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		buttonBox.setLayoutData(gd);
	}

	protected void doLoad() {
		if (table != null) {
			String s = getPreferenceStore().getString(getPreferenceName());
			loadFromStringRepresentation(s);
		}
	}

	protected void loadFromStringRepresentation(String s) {
		table.removeAll();
		currentItems = parseStringRepresentation(s);
		for (DataItem item : currentItems) {
			TableItem tableItem = new TableItem(table, SWT.NONE);
			setFontReflectingSelection(item, tableItem);
			tableItem.setText(item.getColumnValues());
		}
	}

	protected void setFontReflectingSelection(DataItem item, TableItem tableItem) {
		if (item.isSelected()) {
			tableItem.setFont(getFontForSelected());
		} else {
			tableItem.setFont(table.getFont());
		}
	}

	protected Font getFontForSelected() {
		return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
	}

	protected void doLoadDefault() {
		if (table != null) {
			String s = getPreferenceStore().getDefaultString(getPreferenceName());
			loadFromStringRepresentation(s);
		}
	}

	protected void doStore() {
		String s = createStringRepresentation(currentItems);
		if (s != null) {
			getPreferenceStore().setValue(getPreferenceName(), s);
		} 
	}

	public Composite getButtonBoxControl(Composite parent) {
		if (buttonBox == null) {
			buttonBox = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			buttonBox.setLayout(layout);
			createButtons(buttonBox);
			buttonBox.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					editButton = null;
					selectButton = null;
					testButton = null;
					addButton = null;
					duplicateButton = null;
					removeButton = null;
					upButton = null;
					downButton = null;
					buttonBox = null;
				}
			});

		} else {
			checkParent(buttonBox, parent);
		}

		selectionChanged();
		return buttonBox;
	}

	public Table getTableControl(Composite parent) {
		if (table == null) {
			table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL
					| SWT.H_SCROLL | SWT.FULL_SELECTION);
			table.setFont(parent.getFont());
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			table.addSelectionListener(getSelectionListener());
			table.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					table = null;
				}
			});
			table.addListener(SWT.MouseDoubleClick, new Listener() {
				public void handleEvent(Event event) {
					Rectangle clientArea = table.getClientArea();
					Point pt = new Point(event.x, event.y);
					int index = table.getTopIndex();
					while (index < table.getItemCount()) {
						TableItem item = table.getItem(index);
						boolean rowVisible = false;
						for (int i = 0; i < columnNames.length; i++) {
							Rectangle rect = item.getBounds(i);
							if (rect.contains(pt)) {
								table.setSelection(index);
								editPressed();
								return;
							}
							if (!rowVisible && rect.intersects(clientArea)) {
								rowVisible = true;
							}
						}
						if (!rowVisible) {
							return;
						}
						index++;
					}
				}
			});
			for (String columnName : columnNames) {
				TableColumn tableColumn = new TableColumn(table, SWT.LEAD);
				tableColumn.setText(columnName);
				tableColumn.setWidth(100);
			}
			if (columnNames.length > 0) {
				TableLayout layout = new TableLayout();
				if (columnNames.length > 1) {
					for (int i = 0; i < (columnNames.length - 1); i++) {
						layout.addColumnData(new ColumnWeightData(0,
								columnWidths[i], false));

					}
				}
				layout.addColumnData(new ColumnWeightData(100,
						columnWidths[columnNames.length - 1], true));
				table.setLayout(layout);
			}
			final TableEditor editor = new TableEditor(table);
			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
		} else {
			checkParent(table, parent);
		}
		return table;
	}

	public int getNumberOfControls() {
		return 2;
	}

	private SelectionListener getSelectionListener() {
		if (selectionListener == null) {
			createSelectionListener();
		}
		return selectionListener;
	}

	/**
	 * Returns this field editor's shell.
	 * <p>
	 * This method is internal to the framework; subclassers should not call
	 * this method.
	 * </p>
	 *
	 * @return the shell
	 */
	protected Shell getShell() {
		if (addButton == null) {
			return null;
		}
		return addButton.getShell();
	}

	private void addPressed() {
		setPresentsDefaultValue(false);
		T newInputObject = createNewItem();
		if (newInputObject != null) {
			currentItems.add(newInputObject);
			if (currentItems.size() == 1) {
				newInputObject.setSelected(true);
			}
			TableItem tableItem = new TableItem(table, SWT.NONE);
			setFontReflectingSelection(newInputObject, tableItem);
			tableItem.setText(newInputObject.getColumnValues());
			selectionChanged();
		}
	}

	protected void editPressed() {
	}

	protected void testPressed() {
	}

	protected void selectPressed() {
		setPresentsDefaultValue(false);
		int index = table.getSelectionIndex();
		if (index >= 0) {
			for (int i = 0; i < currentItems.size(); i++) {
				T item = currentItems.get(i);
				boolean wasSelected = item.isSelected(); 
				boolean shouldBeSelected = i == index;
				if (wasSelected && !shouldBeSelected) {
					item.setSelected(false);
					table.getItem(i).setFont(table.getFont());
				} else if (!wasSelected && shouldBeSelected) {
					item.setSelected(true);
					table.getItem(i).setFont(getFontForSelected());
				}
			}
		}
	}
	
	private void duplicatePressed() {
		setPresentsDefaultValue(false);
		int index = table.getSelectionIndex();
		int target = index + 1;

		if (index >= 0) {
			TableItem[] selection = table.getSelection();
			Assert.isTrue(selection.length == 1);
			T newItem = (T) currentItems.get(index).clone();
			newItem.setSelected(false);
			currentItems.add(target, newItem);
			TableItem tableItem = new TableItem(table, SWT.NONE, target);
			setFontReflectingSelection(newItem, tableItem);
			tableItem.setText(newItem.getColumnValues());
			table.setSelection(target);
		}
		selectionChanged();
	}

	private void removePressed() {
		setPresentsDefaultValue(false);
		int index = table.getSelectionIndex();
		if (index >= 0) {
			table.remove(index);
			currentItems.remove(index);
			if (currentItems.size() == 1) {
				currentItems.get(0).setSelected(true);
				setFontReflectingSelection(currentItems.get(0), table.getItem(0));
			}
			selectionChanged();
		}
	}

	private void upPressed() {
		swap(true);
	}

	private void downPressed() {
		swap(false);
	}

	/**
	 * Invoked when the selection in the list has changed.
	 *
	 * <p>
	 * The default implementation of this method utilizes the selection index
	 * and the size of the list to toggle the enabled state of the up, down and
	 * remove buttons.
	 * </p>
	 *
	 * <p>
	 * Subclasses may override.
	 * </p>
	 *
	 */
	protected void selectionChanged() {
		int index = table.getSelectionIndex();
		int size = table.getItemCount();

		selectButton.setEnabled(index >= 0 && !currentItems.get(index).isSelected());
		editButton.setEnabled(index >= 0);
		testButton.setEnabled(index >= 0);
		duplicateButton.setEnabled(index >= 0);
		removeButton.setEnabled(index >= 0);
		upButton.setEnabled(size > 1 && index > 0);
		downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
	}

	public void setFocus() {
		if (table != null) {
			table.setFocus();
		}
	}

	private void swap(boolean up) {
		setPresentsDefaultValue(false);
		int index = table.getSelectionIndex();
		int target = up ? index - 1 : index + 1;

		if (index >= 0) {
			TableItem[] selection = table.getSelection();
			Assert.isTrue(selection.length == 1);
			String[] values = new String[columnNames.length];
			for (int j = 0; j < columnNames.length; j++) {
				values[j] = selection[0].getText(j);
			}
			table.remove(index);
			TableItem tableItem = new TableItem(table, SWT.NONE, target);
			tableItem.setText(values);
			table.setSelection(target);
			
			T temp = currentItems.get(index);
			currentItems.set(index, currentItems.get(target));
			currentItems.set(target, temp);
		}
		selectionChanged();
	}

	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getTableControl(parent).setEnabled(enabled);
		addButton.setEnabled(enabled);
		editButton.setEnabled(enabled);
		duplicateButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
		upButton.setEnabled(enabled);
		downButton.setEnabled(enabled);
	}

}