/*
 * MIT License
 * Copyright (c) 2020 Contrast Security Japan G.K.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package com.contrastsecurity.auditlogtool.preference;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.jasypt.util.text.BasicTextEncryptor;

import com.contrastsecurity.auditlogtool.Main;
import com.contrastsecurity.auditlogtool.Messages;

public class ConnectionPreferencePage extends PreferencePage {

    private Button validFlg;
    private Text hostTxt;
    private Text portTxt;
    private Button authNone;
    private Button authInput;
    private Button authSave;
    private Text userTxt;
    private Text passTxt;
    private Button ignoreSSLCertCheckFlg;
    private Text connectionTimeoutTxt;
    private Text socketTimeoutTxt;

    public ConnectionPreferencePage() {
        super(Messages.getString("ConnectionPreferencePage.connection_settings_title")); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(Composite parent) {
        IPreferenceStore ps = getPreferenceStore();
        ps.addPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(PreferenceConstants.PROXY_AUTH)) {
                    if (event.getNewValue().equals("none")) { //$NON-NLS-1$
                        if (!authNone.isDisposed()) {
                            authNone.setSelection(true);
                            authInput.setSelection(false);
                            authSave.setSelection(false);
                            userTxt.setText(""); //$NON-NLS-1$
                            userTxt.setEnabled(false);
                            passTxt.setText(""); //$NON-NLS-1$
                            passTxt.setEnabled(false);
                        }
                    }
                }
            }
        });

        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout compositeLt = new GridLayout(1, false);
        compositeLt.marginHeight = 15;
        compositeLt.marginWidth = 5;
        compositeLt.horizontalSpacing = 10;
        compositeLt.verticalSpacing = 20;
        composite.setLayout(compositeLt);

        Group proxyGrp = new Group(composite, SWT.NONE);
        GridLayout proxyGrpLt = new GridLayout(1, false);
        proxyGrpLt.marginHeight = 10;
        proxyGrpLt.marginWidth = 10;
        proxyGrpLt.horizontalSpacing = 10;
        proxyGrp.setLayout(proxyGrpLt);
        GridData proxyGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        proxyGrp.setLayoutData(proxyGrpGrDt);
        proxyGrp.setText(Messages.getString("ConnectionPreferencePage.proxy_settings_group_title")); //$NON-NLS-1$

        validFlg = new Button(proxyGrp, SWT.CHECK);
        // GridData validFlgGrDt = new GridData();
        // validFlgGrDt.horizontalSpan = 4;
        // validFlg.setLayoutData(validFlgGrDt);
        validFlg.setText(Messages.getString("ConnectionPreferencePage.valid_proxy")); //$NON-NLS-1$
        if (ps.getBoolean(PreferenceConstants.PROXY_YUKO)) {
            validFlg.setSelection(true);
        }

        Group proxyHostGrp = new Group(proxyGrp, SWT.NONE);
        GridLayout proxyHostGrppLt = new GridLayout(4, false);
        proxyHostGrppLt.marginWidth = 10;
        proxyHostGrppLt.horizontalSpacing = 10;
        proxyHostGrp.setLayout(proxyHostGrppLt);
        GridData proxyHostGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // proxyGrpGrDt.horizontalSpan = 4;
        proxyHostGrp.setLayoutData(proxyHostGrpGrDt);

        // ========== ホスト ========== //
        new Label(proxyHostGrp, SWT.LEFT).setText(Messages.getString("ConnectionPreferencePage.proxy_host")); //$NON-NLS-1$
        hostTxt = new Text(proxyHostGrp, SWT.BORDER);
        hostTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        hostTxt.setText(ps.getString(PreferenceConstants.PROXY_HOST));
        hostTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                hostTxt.selectAll();
            }
        });

        // ========== ポート ========== //
        new Label(proxyHostGrp, SWT.LEFT).setText(Messages.getString("ConnectionPreferencePage.proxy_port")); //$NON-NLS-1$
        portTxt = new Text(proxyHostGrp, SWT.BORDER);
        GridData portTxtGrDt = new GridData();
        portTxtGrDt.widthHint = 100;
        portTxt.setLayoutData(portTxtGrDt);
        portTxt.setText(ps.getString(PreferenceConstants.PROXY_PORT));
        portTxt.setTextLimit(5);
        portTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                portTxt.selectAll();
            }
        });

        Group proxyAuthGrp = new Group(proxyGrp, SWT.NONE);
        GridLayout proxyAuthGrpLt = new GridLayout(2, false);
        proxyAuthGrpLt.marginWidth = 15;
        proxyAuthGrpLt.horizontalSpacing = 10;
        proxyAuthGrp.setLayout(proxyAuthGrpLt);
        GridData proxyAuthGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // dirGrpGrDt.horizontalSpan = 4;
        proxyAuthGrp.setLayoutData(proxyAuthGrpGrDt);
        proxyAuthGrp.setText(Messages.getString("ConnectionPreferencePage.proxy_authentication")); //$NON-NLS-1$

        // ========== Save or Input ========== //
        Composite authInputTypeGrp = new Composite(proxyAuthGrp, SWT.NONE);
        GridLayout authInputTypeGrpLt = new GridLayout(1, false);
        authInputTypeGrpLt.marginWidth = 0;
        authInputTypeGrpLt.marginBottom = -5;
        authInputTypeGrpLt.verticalSpacing = 10;
        authInputTypeGrp.setLayout(authInputTypeGrpLt);
        GridData authInputTypeGrpGrDt = new GridData();
        authInputTypeGrpGrDt.horizontalSpan = 2;
        authInputTypeGrp.setLayoutData(authInputTypeGrpGrDt);

        authNone = new Button(authInputTypeGrp, SWT.RADIO);
        authNone.setText(Messages.getString("ConnectionPreferencePage.without_authentication")); //$NON-NLS-1$
        authNone.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button source = (Button) e.getSource();
                if (source.getSelection()) {
                    userTxt.setText(""); //$NON-NLS-1$
                    userTxt.setEnabled(false);
                    passTxt.setText(""); //$NON-NLS-1$
                    passTxt.setEnabled(false);
                }
            }
        });

        authInput = new Button(authInputTypeGrp, SWT.RADIO);
        authInput.setText(Messages.getString("ConnectionPreferencePage.proxy_password_needs_input")); //$NON-NLS-1$
        authInput.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button source = (Button) e.getSource();
                if (source.getSelection()) {
                    userTxt.setText(""); //$NON-NLS-1$
                    userTxt.setEnabled(false);
                    passTxt.setText(""); //$NON-NLS-1$
                    passTxt.setEnabled(false);
                }
            }
        });

        authSave = new Button(authInputTypeGrp, SWT.RADIO);
        authSave.setText(Messages.getString("ConnectionPreferencePage.proxy_password_memory")); //$NON-NLS-1$
        authSave.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button source = (Button) e.getSource();
                if (source.getSelection()) {
                    userTxt.setEnabled(true);
                    passTxt.setEnabled(true);
                }
            }

        });

        Composite authGrp = new Composite(proxyAuthGrp, SWT.NONE);
        GridLayout authGrpLt = new GridLayout(2, false);
        authGrpLt.marginTop = -5;
        authGrpLt.marginLeft = 12;
        authGrp.setLayout(authGrpLt);
        GridData authGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        authGrp.setLayoutData(authGrpGrDt);
        // ========== ユーザー ========== //
        new Label(authGrp, SWT.LEFT).setText(Messages.getString("ConnectionPreferencePage.proxy_user_label")); //$NON-NLS-1$
        userTxt = new Text(authGrp, SWT.BORDER);
        userTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userTxt.setText(ps.getString(PreferenceConstants.PROXY_USER));
        userTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                userTxt.selectAll();
            }
        });

        // ========== パスワード ========== //
        new Label(authGrp, SWT.LEFT).setText(Messages.getString("ConnectionPreferencePage.proxy_password_label")); //$NON-NLS-1$
        passTxt = new Text(authGrp, SWT.BORDER);
        passTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        passTxt.setEchoChar('*');
        passTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                passTxt.selectAll();
            }
        });

        if (ps.getString(PreferenceConstants.PROXY_AUTH).equals("input")) { //$NON-NLS-1$
            authInput.setSelection(true);
            userTxt.setText(""); //$NON-NLS-1$
            userTxt.setEnabled(false);
            passTxt.setText(""); //$NON-NLS-1$
            passTxt.setEnabled(false);
        } else if (ps.getString(PreferenceConstants.PROXY_AUTH).equals("save")) { //$NON-NLS-1$
            authSave.setSelection(true);
            userTxt.setEnabled(true);
            passTxt.setEnabled(true);
            BasicTextEncryptor encryptor = new BasicTextEncryptor();
            encryptor.setPassword(Main.MASTER_PASSWORD);
            try {
                passTxt.setText(encryptor.decrypt(ps.getString(PreferenceConstants.PROXY_PASS)));
            } catch (Exception e) {
                MessageDialog.openError(getShell(), Messages.getString("ConnectionPreferencePage.connection_settings_title"), //$NON-NLS-1$
                        Messages.getString("ConnectionPreferencePage.proxy_password_decrypt_fail_message")); //$NON-NLS-1$
                passTxt.setText(""); //$NON-NLS-1$
            }
        } else {
            authNone.setSelection(true);
            userTxt.setText(""); //$NON-NLS-1$
            userTxt.setEnabled(false);
            passTxt.setText(""); //$NON-NLS-1$
            passTxt.setEnabled(false);
        }

        Group sslCertGrp = new Group(composite, SWT.NONE);
        GridLayout sslCertGrpLt = new GridLayout(1, false);
        sslCertGrpLt.marginWidth = 15;
        sslCertGrpLt.horizontalSpacing = 10;
        sslCertGrp.setLayout(sslCertGrpLt);
        GridData sslCertGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        sslCertGrp.setLayoutData(sslCertGrpGrDt);
        sslCertGrp.setText(Messages.getString("ConnectionPreferencePage.ssl_check_group_title")); //$NON-NLS-1$

        // ========== 証明書検証回避 ========== //
        ignoreSSLCertCheckFlg = new Button(sslCertGrp, SWT.CHECK);
        // GridData ignoreSSLCertCheckFlgGrDt = new GridData();
        // ignoreSSLCertCheckFlgGrDt.horizontalSpan = 4;
        // ignoreSSLCertCheckFlg.setLayoutData(ignoreSSLCertCheckFlgGrDt);
        ignoreSSLCertCheckFlg.setText(Messages.getString("ConnectionPreferencePage.ssl_check_skip")); //$NON-NLS-1$
        if (ps.getBoolean(PreferenceConstants.IGNORE_SSLCERT_CHECK)) {
            ignoreSSLCertCheckFlg.setSelection(true);
        }

        Group timeoutGrp = new Group(composite, SWT.NONE);
        GridLayout timeoutGrpLt = new GridLayout(2, false);
        timeoutGrpLt.marginWidth = 15;
        timeoutGrpLt.horizontalSpacing = 10;
        timeoutGrp.setLayout(timeoutGrpLt);
        GridData timeoutGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        timeoutGrp.setLayoutData(timeoutGrpGrDt);
        timeoutGrp.setText(Messages.getString("ConnectionPreferencePage.timeout_group_title")); //$NON-NLS-1$

        // ========== ConnetionTimeout ========== //
        new Label(timeoutGrp, SWT.LEFT).setText(Messages.getString("ConnectionPreferencePage.connect_timeout_label")); //$NON-NLS-1$
        connectionTimeoutTxt = new Text(timeoutGrp, SWT.BORDER);
        connectionTimeoutTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        connectionTimeoutTxt.setText(ps.getString(PreferenceConstants.CONNECTION_TIMEOUT));
        connectionTimeoutTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                connectionTimeoutTxt.selectAll();
            }
        });
        // ========== SocketTimeout ========== //
        new Label(timeoutGrp, SWT.LEFT).setText(Messages.getString("ConnectionPreferencePage.socket_timeout_label")); //$NON-NLS-1$
        socketTimeoutTxt = new Text(timeoutGrp, SWT.BORDER);
        socketTimeoutTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        socketTimeoutTxt.setText(ps.getString(PreferenceConstants.SOCKET_TIMEOUT));
        socketTimeoutTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                socketTimeoutTxt.selectAll();
            }
        });

        Composite buttonGrp = new Composite(parent, SWT.NONE);
        GridLayout buttonGrpLt = new GridLayout(2, false);
        buttonGrpLt.marginHeight = 15;
        buttonGrpLt.marginWidth = 5;
        buttonGrpLt.horizontalSpacing = 7;
        buttonGrpLt.verticalSpacing = 20;
        buttonGrp.setLayout(buttonGrpLt);
        GridData buttonGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        buttonGrpGrDt.horizontalAlignment = SWT.END;
        buttonGrp.setLayoutData(buttonGrpGrDt);

        Button defaultBtn = new Button(buttonGrp, SWT.NULL);
        GridData defaultBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        defaultBtnGrDt.widthHint = 90;
        defaultBtn.setLayoutData(defaultBtnGrDt);
        defaultBtn.setText(Messages.getString("ConnectionPreferencePage.restore_defaults_button_title")); //$NON-NLS-1$
        defaultBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                connectionTimeoutTxt.setText(ps.getDefaultString(PreferenceConstants.CONNECTION_TIMEOUT));
                socketTimeoutTxt.setText(ps.getDefaultString(PreferenceConstants.SOCKET_TIMEOUT));
            }
        });

        Button applyBtn = new Button(buttonGrp, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.widthHint = 90;
        applyBtn.setLayoutData(applyBtnGrDt);
        applyBtn.setText(Messages.getString("ConnectionPreferencePage.apply_button_title")); //$NON-NLS-1$
        applyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                performOk();
            }
        });

        noDefaultAndApplyButton();
        return composite;
    }

    @Override
    public boolean performOk() {
        IPreferenceStore ps = getPreferenceStore();
        if (ps == null) {
            return true;
        }
        List<String> errors = new ArrayList<String>();
        ps.setValue(PreferenceConstants.PROXY_YUKO, this.validFlg.getSelection());
        if (this.validFlg.getSelection()) {
            if (this.hostTxt.getText().isEmpty()) {
                errors.add(Messages.getString("ConnectionPreferencePage.perform_error_proxy_host_empty")); //$NON-NLS-1$
            }
        }
        ps.setValue(PreferenceConstants.PROXY_HOST, this.hostTxt.getText());
        if (this.validFlg.getSelection()) {
            if (this.portTxt.getText().isEmpty()) {
                errors.add(Messages.getString("ConnectionPreferencePage.perform_error_proxy_port_empty")); //$NON-NLS-1$
            } else {
                if (!StringUtils.isNumeric(this.portTxt.getText())) {
                    errors.add(Messages.getString("ConnectionPreferencePage.perform_error_proxy_port_numeric")); //$NON-NLS-1$
                }
            }
        }
        ps.setValue(PreferenceConstants.PROXY_PORT, this.portTxt.getText());
        if (authInput.getSelection()) {
            ps.setValue(PreferenceConstants.PROXY_AUTH, "input"); //$NON-NLS-1$
            ps.setValue(PreferenceConstants.PROXY_USER, ""); //$NON-NLS-1$
            ps.setValue(PreferenceConstants.PROXY_PASS, ""); //$NON-NLS-1$
        } else if (authSave.getSelection()) {
            ps.setValue(PreferenceConstants.PROXY_AUTH, "save"); //$NON-NLS-1$
            if (this.userTxt.getText().isEmpty() || this.passTxt.getText().isEmpty()) {
                errors.add(Messages.getString("ConnectionPreferencePage.perform_error_user_password_empty")); //$NON-NLS-1$
            } else {
                ps.setValue(PreferenceConstants.PROXY_USER, this.userTxt.getText());
                BasicTextEncryptor encryptor = new BasicTextEncryptor();
                encryptor.setPassword(Main.MASTER_PASSWORD);
                ps.setValue(PreferenceConstants.PROXY_PASS, encryptor.encrypt(this.passTxt.getText()));
                ps.setValue(PreferenceConstants.PROXY_TMP_USER, ""); //$NON-NLS-1$
                ps.setValue(PreferenceConstants.PROXY_TMP_PASS, ""); //$NON-NLS-1$
            }
        } else {
            ps.setValue(PreferenceConstants.PROXY_AUTH, "none"); //$NON-NLS-1$
            ps.setValue(PreferenceConstants.PROXY_USER, ""); //$NON-NLS-1$
            ps.setValue(PreferenceConstants.PROXY_PASS, ""); //$NON-NLS-1$
            ps.setValue(PreferenceConstants.PROXY_TMP_USER, ""); //$NON-NLS-1$
            ps.setValue(PreferenceConstants.PROXY_TMP_PASS, ""); //$NON-NLS-1$
        }
        ps.setValue(PreferenceConstants.IGNORE_SSLCERT_CHECK, this.ignoreSSLCertCheckFlg.getSelection());

        if (this.connectionTimeoutTxt.getText().isEmpty()) {
            errors.add(Messages.getString("ConnectionPreferencePage.perform_error_connect_timeout_empty")); //$NON-NLS-1$
        } else {
            if (!StringUtils.isNumeric(this.connectionTimeoutTxt.getText())) {
                errors.add(Messages.getString("ConnectionPreferencePage.perform_error_connect_timeout_numeric")); //$NON-NLS-1$
            } else {
                ps.setValue(PreferenceConstants.CONNECTION_TIMEOUT, this.connectionTimeoutTxt.getText());
            }
        }

        if (this.socketTimeoutTxt.getText().isEmpty()) {
            errors.add(Messages.getString("ConnectionPreferencePage.perform_error_socket_timeout_empty")); //$NON-NLS-1$
        } else {
            if (!StringUtils.isNumeric(this.socketTimeoutTxt.getText())) {
                errors.add(Messages.getString("ConnectionPreferencePage.perform_error_socket_timeout_numeric")); //$NON-NLS-1$
            } else {
                ps.setValue(PreferenceConstants.SOCKET_TIMEOUT, this.socketTimeoutTxt.getText());
            }
        }
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), Messages.getString("ConnectionPreferencePage.connection_settings_title"), String.join("\r\n", errors)); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        return true;
    }
}
