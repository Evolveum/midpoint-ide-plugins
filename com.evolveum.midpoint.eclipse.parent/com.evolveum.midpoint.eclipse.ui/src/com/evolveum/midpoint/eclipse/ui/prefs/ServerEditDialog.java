package com.evolveum.midpoint.eclipse.ui.prefs;

import java.io.File;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ServerEditDialog extends TitleAreaDialog {

	private Text txtName;
	private Text txtUrl;
	private Text txtLogin;
	private Text txtPassword;
	private Text txtShortName;
	private Text txtProperties;
	private Text txtLogFile;
	private Button btnTestConnection;
	private Button btnBrowsePropertiesFile;
	private Button btnBrowseLogFile;
	
	private ServerInfo newDataItem;
	private ServerInfo existingDataItem;
	private boolean createNew;

	public ServerEditDialog(Shell parentShell, ServerInfo dataItemToEdit, boolean createNew) {
		super(parentShell);
		existingDataItem = dataItemToEdit;
		this.createNew = createNew;
	}
	
	public static ServerEditDialog createEdit(Shell shell, ServerInfo data) {
		return new ServerEditDialog(shell, data, false);
	}

	public static ServerEditDialog createNew(Shell shell, ServerInfo data) {
		return new ServerEditDialog(shell, data, true);
	}


	@Override
	public void create() {
		super.create();
		if (createNew) {
			setTitle("Add a midPoint server");
		} else {
			setTitle("Edit a midPoint server");
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(4, false);
		container.setLayout(layout);

		createName(container);
		createUrl(container);
		createLogin(container);
		createPassword(container);
		createShortName(container);
		createPropertiesFile(container);
		createLogFile(container);

		return area;
	}

	private void createName(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Name");

		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = GridData.FILL;

		txtName = new Text(container, SWT.BORDER);
		txtName.setLayoutData(gd);
		if (existingDataItem != null) {
			txtName.setText(existingDataItem.getName());
		}
	}

	private void createUrl(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("URL");

		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = GridData.FILL;
		
		txtUrl = new Text(container, SWT.BORDER);
		txtUrl.setLayoutData(gd);
		if (existingDataItem != null) {
			txtUrl.setText(existingDataItem.getUrl());
		}
	}

	private void createLogin(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Login");

		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = GridData.FILL;
		
		txtLogin = new Text(container, SWT.BORDER);
		txtLogin.setLayoutData(gd);
		if (existingDataItem != null) {
			txtLogin.setText(existingDataItem.getLogin());
		}
	}

	private void createPassword(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Password");

		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = GridData.FILL;
		
		txtPassword = new Text(container, SWT.BORDER);
		txtPassword.setLayoutData(gd);
		txtPassword.setEchoChar('*');
		if (existingDataItem != null) {
			txtPassword.setText(existingDataItem.getPassword());
		}
		
		GridData gd2 = new GridData();
		gd2.horizontalAlignment = GridData.FILL;
		btnTestConnection = new Button(container, SWT.PUSH);
        btnTestConnection.setText("Test connection");
        btnTestConnection.setLayoutData(gd2);
        btnTestConnection.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent evt) {
                PluginPreferences.testConnection(txtName.getText(), txtUrl.getText(), txtLogin.getText(), txtPassword.getText());
            }
        });
        btnTestConnection.addDisposeListener(event -> btnTestConnection = null);		
	}
	
	private void createShortName(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Short name");

		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = GridData.FILL;
		
		txtShortName = new Text(container, SWT.BORDER);
		txtShortName.setLayoutData(gd);
		if (existingDataItem != null) {
			txtShortName.setText(existingDataItem.getShortName());
		}
	}

	private void createPropertiesFile(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Properties file");

		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = GridData.FILL;
		
		txtProperties = new Text(container, SWT.BORDER);
		txtProperties.setLayoutData(gd);
		
		GridData gd2 = new GridData();
		gd2.horizontalAlignment = GridData.FILL;
		btnBrowsePropertiesFile = new Button(container, SWT.PUSH);
        btnBrowsePropertiesFile.setText("Browse...");
        btnBrowsePropertiesFile.setLayoutData(gd2);
        btnBrowsePropertiesFile.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent evt) {
                String newValue = changePressed();
                if (newValue != null) {
                    txtProperties.setText(newValue);
                }
            }
        	protected String changePressed() {
                File f = new File(txtProperties.getText());
                if (!f.exists()) {
        			f = null;
        		}
                File d = getFile(f);
                if (d == null) {
        			return null;
        		}

                return d.getAbsolutePath();
            }

        });        
        btnBrowsePropertiesFile.addDisposeListener(event -> btnBrowsePropertiesFile = null);		
	}

	private void createLogFile(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Log file (if local)");

		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = GridData.FILL;
		
		txtLogFile = new Text(container, SWT.BORDER);
		txtLogFile.setLayoutData(gd);
		
		GridData gd2 = new GridData();
		gd2.horizontalAlignment = GridData.FILL;
		btnBrowseLogFile = new Button(container, SWT.PUSH);
		btnBrowseLogFile.setText("Browse...");
		btnBrowseLogFile.setLayoutData(gd2);
		btnBrowseLogFile.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent evt) {
                String newValue = changePressed();
                if (newValue != null) {
                	txtLogFile.setText(newValue);
                }
            }
        	protected String changePressed() {
                File f = new File(txtLogFile.getText());
                if (!f.exists()) {
        			f = null;
        		}
                File d = getFile(f);
                if (d == null) {
        			return null;
        		}
                return d.getAbsolutePath();
            }

        });        
		btnBrowseLogFile.addDisposeListener(event -> btnBrowseLogFile = null);		
	}

	private File getFile(File startingDirectory) {

        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.SHEET);
        if (startingDirectory != null) {
			dialog.setFileName(startingDirectory.getPath());
		}
        String file = dialog.open();
        if (file != null) {
            file = file.trim();
            if (file.length() > 0) {
				return new File(file);
			}
        }

        return null;
    }


	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	private void saveInput() {
		newDataItem = new ServerInfo();
		newDataItem.setName(txtName.getText());
		newDataItem.setUrl(txtUrl.getText());
		newDataItem.setLogin(txtLogin.getText());
		newDataItem.setPassword(txtPassword.getText());
		newDataItem.setShortName(txtShortName.getText());
		newDataItem.setPropertiesFile(txtProperties.getText());
		newDataItem.setLogFile(txtLogFile.getText());
		if (existingDataItem != null) {
			newDataItem.setSelected(existingDataItem.isSelected());
		}
	}
	
	public ServerInfo getServerDataItem() {
		return newDataItem;
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

}