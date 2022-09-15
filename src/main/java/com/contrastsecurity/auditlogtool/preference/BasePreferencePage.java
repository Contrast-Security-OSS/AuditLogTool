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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.auditlogtool.AuditLogToolShell;
import com.contrastsecurity.auditlogtool.ProxyAuthDialog;
import com.contrastsecurity.auditlogtool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class BasePreferencePage extends PreferencePage {

    private Text contrastUrlTxt;
    private Text userNameTxt;
    private Text serviceKeyTxt;
    private Button superAdminYes;
    private Button superAdminNo;
    private Text orgIdTxt;
    private Text apiKeyTxt;
    private Button groupCreateYes;
    private Button groupCreateNo;
    private Text groupNameTxt;
    private Button showCreateGroupLogFlg;

    private List<Organization> orgList;
    private List<Button> checkBoxList = new ArrayList<Button>();
    private List<Integer> selectedIdxes = new ArrayList<Integer>();
    private Table table;
    private Button addBtn;
    private Button rmvBtn;
    private AuditLogToolShell shell;

    Logger logger = LogManager.getLogger("auditlogtool");

    public BasePreferencePage(AuditLogToolShell shell) {
        super("基本設定");
        this.shell = shell;
    }

    @Override
    protected Control createContents(Composite parent) {
        IPreferenceStore ps = getPreferenceStore();

        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout compositeLt = new GridLayout(1, false);
        compositeLt.marginHeight = 15;
        compositeLt.marginWidth = 5;
        compositeLt.horizontalSpacing = 10;
        compositeLt.verticalSpacing = 20;
        composite.setLayout(compositeLt);

        Composite baseGrp = new Composite(composite, SWT.NONE);
        GridLayout baseGrpLt = new GridLayout(2, false);
        baseGrpLt.marginWidth = 15;
        baseGrpLt.horizontalSpacing = 10;
        baseGrp.setLayout(baseGrpLt);
        GridData baseGrpLtGrDt = new GridData(GridData.FILL_BOTH);
        baseGrp.setLayoutData(baseGrpLtGrDt);

        new Label(baseGrp, SWT.LEFT).setText("Contrast URL：");
        contrastUrlTxt = new Text(baseGrp, SWT.BORDER);
        contrastUrlTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        contrastUrlTxt.setText(ps.getString(PreferenceConstants.CONTRAST_URL));
        contrastUrlTxt.setMessage("http://xxx.xxx.xxx.xxx/Contrast");
        contrastUrlTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                contrastUrlTxt.selectAll();
            }
        });
        contrastUrlTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String contrastUrlStr = contrastUrlTxt.getText().trim();
                String userNameStr = userNameTxt.getText().trim();
                String serviceKeyStr = serviceKeyTxt.getText().trim();
                if (contrastUrlStr.isEmpty() || userNameStr.isEmpty() || serviceKeyStr.isEmpty()) {
                    addBtn.setEnabled(false);
                } else {
                    addBtn.setEnabled(true);
                }
            }
        });

        new Label(baseGrp, SWT.LEFT).setText("Username：");
        userNameTxt = new Text(baseGrp, SWT.BORDER);
        userNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userNameTxt.setText(ps.getString(PreferenceConstants.USERNAME));
        userNameTxt.setMessage("メールアドレス");
        userNameTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                userNameTxt.selectAll();
            }
        });
        userNameTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String contrastUrlStr = contrastUrlTxt.getText().trim();
                String userNameStr = userNameTxt.getText().trim();
                String serviceKeyStr = serviceKeyTxt.getText().trim();
                if (contrastUrlStr.isEmpty() || userNameStr.isEmpty() || serviceKeyStr.isEmpty()) {
                    addBtn.setEnabled(false);
                } else {
                    addBtn.setEnabled(true);
                }
            }
        });

        new Label(baseGrp, SWT.LEFT).setText("Service Key：");
        serviceKeyTxt = new Text(baseGrp, SWT.BORDER);
        serviceKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        serviceKeyTxt.setText(ps.getString(PreferenceConstants.SERVICE_KEY));
        serviceKeyTxt.setMessage("個人のサービスキー");
        serviceKeyTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                serviceKeyTxt.selectAll();
            }
        });
        serviceKeyTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String contrastUrlStr = contrastUrlTxt.getText().trim();
                String serviceKeyStr = serviceKeyTxt.getText().trim();
                String userNameStr = userNameTxt.getText().trim();
                if (contrastUrlStr.isEmpty() || serviceKeyStr.isEmpty() || userNameStr.isEmpty()) {
                    addBtn.setEnabled(false);
                } else {
                    addBtn.setEnabled(true);
                }
            }
        });

        // ========== SuperAdmin true/false ========== //
        Label superAdminLbl = new Label(baseGrp, SWT.LEFT);
        GridData superAdminLblGrDt = new GridData();
        superAdminLbl.setLayoutData(superAdminLblGrDt);
        superAdminLbl.setText("SuperAdminですか？");

        Composite superAdminTypeGrp = new Composite(baseGrp, SWT.NONE);
        GridLayout superAdminTypeGrpGrpLt = new GridLayout(2, false);
        superAdminTypeGrp.setLayout(superAdminTypeGrpGrpLt);
        GridData superAdminTypeGrpGrDt = new GridData();
        superAdminTypeGrp.setLayoutData(superAdminTypeGrpGrDt);

        superAdminYes = new Button(superAdminTypeGrp, SWT.RADIO);
        superAdminYes.setText("はい");

        superAdminNo = new Button(superAdminTypeGrp, SWT.RADIO);
        superAdminNo.setText("いいえ");

        Composite stackComposite = new Composite(baseGrp, SWT.NONE);
        StackLayout stackCompositeLt = new StackLayout();
        stackComposite.setLayout(stackCompositeLt);
        GridData stackCompositeGrDt = new GridData(GridData.FILL_BOTH);
        stackCompositeGrDt.horizontalSpan = 3;
        stackComposite.setLayoutData(stackCompositeGrDt);

        Composite comp1 = new Composite(stackComposite, SWT.NONE);
        GridLayout comp1Lt = new GridLayout(1, false);
        comp1Lt.marginWidth = 0;
        comp1Lt.verticalSpacing = 10;
        comp1.setLayout(comp1Lt);
        comp1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Group apiKeyGrp = new Group(comp1, SWT.NONE);
        GridLayout apiKeyGrpLt = new GridLayout(2, false);
        apiKeyGrpLt.marginWidth = 10;
        apiKeyGrpLt.horizontalSpacing = 10;
        apiKeyGrp.setLayout(apiKeyGrpLt);
        GridData apiKeyGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        apiKeyGrp.setLayoutData(apiKeyGrpGrDt);
        apiKeyGrp.setText("組織");

        new Label(apiKeyGrp, SWT.LEFT).setText("組織ID：");
        orgIdTxt = new Text(apiKeyGrp, SWT.BORDER);
        orgIdTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        orgIdTxt.setText(ps.getString(PreferenceConstants.ORG_ID));
        orgIdTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                orgIdTxt.selectAll();
            }
        });

        new Label(apiKeyGrp, SWT.LEFT).setText("API Key：");
        apiKeyTxt = new Text(apiKeyGrp, SWT.BORDER);
        apiKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        apiKeyTxt.setText(ps.getString(PreferenceConstants.API_KEY));
        apiKeyTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                apiKeyTxt.selectAll();
            }
        });

        // ========== 一時グループ ========== //
        Group tempGroupGrp = new Group(comp1, SWT.NONE);
        GridLayout tempGroupGrpLt = new GridLayout(2, false);
        tempGroupGrpLt.marginWidth = 10;
        tempGroupGrpLt.horizontalSpacing = 10;
        tempGroupGrp.setLayout(tempGroupGrpLt);
        GridData tempGroupGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        tempGroupGrp.setLayoutData(tempGroupGrpGrDt);
        tempGroupGrp.setText("グループの一時作成");

        Text createGroupDescText = new Text(tempGroupGrp, SWT.MULTI | SWT.READ_ONLY);
        GridData createGroupDescTextGrDt = new GridData();
        createGroupDescTextGrDt.horizontalSpan = 2;
        createGroupDescTextGrDt.horizontalAlignment = SWT.FILL;
        createGroupDescTextGrDt.grabExcessHorizontalSpace = true;
        createGroupDescTextGrDt.verticalAlignment = SWT.FILL;
        createGroupDescTextGrDt.grabExcessVerticalSpace = true;
        createGroupDescText.setLayoutData(createGroupDescTextGrDt);
        createGroupDescText.setFont(new Font(shell.getDisplay(), "ＭＳ Ｐゴシック", 9, SWT.NORMAL));
        createGroupDescText.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        List<String> strList = new ArrayList<String>();
        strList.add("各組織の監査ログを取得するには、それらの組織にAdmin権限を持つユーザーとしてグループに属している必要があります。");
        strList.add("グループの一時作成を使用することで監査ログ取得時のみ各組織のAdmin権限を持たせることができます。");
        strList.add("すでにView権限などの権限の低いグループに属しているユーザーの場合、Admin権限とはならないためご注意ください。");
        createGroupDescText.setText(String.join("\r\n", strList));

        Label groupCreateLbl = new Label(tempGroupGrp, SWT.LEFT);
        GridData groupCreateLblGrDt = new GridData();
        groupCreateLbl.setLayoutData(groupCreateLblGrDt);
        groupCreateLbl.setText("作成しますか？");

        Composite groupCreateTypeGrp = new Composite(tempGroupGrp, SWT.NONE);
        GridLayout groupCreateTypeGrpGrpLt = new GridLayout(2, false);
        groupCreateTypeGrp.setLayout(groupCreateTypeGrpGrpLt);
        GridData groupCreateTypeGrpGrDt = new GridData();
        groupCreateTypeGrp.setLayoutData(groupCreateTypeGrpGrDt);

        groupCreateYes = new Button(groupCreateTypeGrp, SWT.RADIO);
        groupCreateYes.setText("はい");

        groupCreateNo = new Button(groupCreateTypeGrp, SWT.RADIO);
        groupCreateNo.setText("いいえ");

        new Label(tempGroupGrp, SWT.LEFT).setText("グループ名：");
        groupNameTxt = new Text(tempGroupGrp, SWT.BORDER);
        groupNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        groupNameTxt.setText(ps.getString(PreferenceConstants.GROUP_NAME));
        groupNameTxt.setEnabled(false);
        groupNameTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                groupNameTxt.selectAll();
            }
        });

        Composite showGroupCreateTypeGrp = new Composite(tempGroupGrp, SWT.NONE);
        GridLayout showGroupCreateTypeGrpGrpLt = new GridLayout(1, false);
        showGroupCreateTypeGrp.setLayout(showGroupCreateTypeGrpGrpLt);
        GridData showGroupCreateTypeGrpGrDt = new GridData();
        showGroupCreateTypeGrpGrDt.horizontalSpan = 2;
        showGroupCreateTypeGrp.setLayoutData(showGroupCreateTypeGrpGrDt);

        showCreateGroupLogFlg = new Button(showGroupCreateTypeGrp, SWT.CHECK);
        GridData showCreateGroupLogFlgGrDt = new GridData(GridData.FILL_HORIZONTAL);
        showCreateGroupLogFlgGrDt.horizontalSpan = 2;
        showCreateGroupLogFlg.setLayoutData(showCreateGroupLogFlgGrDt);
        showCreateGroupLogFlg.setText("グループの一時作成に関するログを表示する");
        if (ps.getBoolean(PreferenceConstants.SHOW_CREATEGROUP_LOG)) {
            showCreateGroupLogFlg.setSelection(true);
        }

        if (ps.getBoolean(PreferenceConstants.IS_CREATEGROUP)) {
            groupCreateYes.setSelection(true);
        } else {
            groupCreateNo.setSelection(true);
        }

        // groupCreateYes.addSelectionListener(new SelectionAdapter() {
        // @Override
        // public void widgetSelected(SelectionEvent e) {
        // Button source = (Button) e.getSource();
        // if (source.getSelection()) {
        // groupNameTxt.setEnabled(true);
        // }
        // }
        // });
        //
        // groupCreateNo.addSelectionListener(new SelectionAdapter() {
        // @Override
        // public void widgetSelected(SelectionEvent e) {
        // Button source = (Button) e.getSource();
        // if (source.getSelection()) {
        // groupNameTxt.setEnabled(false);
        // }
        // }
        // });

        // apiKeyTxt.addModifyListener(new ModifyListener() {
        // @Override
        // public void modifyText(ModifyEvent e) {
        // String contrastUrlStr = contrastUrlTxt.getText();
        // String serviceKeyStr = serviceKeyTxt.getText();
        // String userNameStr = userNameTxt.getText();
        // String apiKeyStr = apiKeyTxt.getText();
        // if (contrastUrlStr.isEmpty() || serviceKeyStr.isEmpty() || userNameStr.isEmpty() || apiKeyStr.isEmpty()) {
        // addBtn.setEnabled(false);
        // } else {
        // addBtn.setEnabled(true);
        // }
        // }
        // });

        Composite comp2 = new Composite(stackComposite, SWT.NONE);
        GridLayout comp2Lt = new GridLayout(1, false);
        comp2Lt.marginWidth = 0;
        comp2.setLayout(comp2Lt);
        comp2.setLayoutData(new GridData(GridData.FILL_BOTH));

        // ========== 組織テーブル ========== //
        Group orgTableGrp = new Group(comp2, SWT.NONE);
        GridLayout orgTableGrpLt = new GridLayout(2, false);
        // orgTableGrpLt.marginWidth = 10;
        orgTableGrpLt.horizontalSpacing = 10;
        orgTableGrp.setLayout(orgTableGrpLt);
        GridData orgTableGrpGrDt = new GridData(GridData.FILL_BOTH);
        orgTableGrp.setLayoutData(orgTableGrpGrDt);
        orgTableGrp.setText("組織一覧");

        String orgJsonStr = ps.getString(PreferenceConstants.TARGET_ORGS);
        if (orgJsonStr.trim().length() > 0) {
            try {
                orgList = new Gson().fromJson(orgJsonStr, new TypeToken<List<Organization>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                MessageDialog.openError(getShell(), "組織設定の読み込み", String.format("組織設定の内容に問題があります。\r\n%s", orgJsonStr));
                orgList = new ArrayList<Organization>();
            }
        } else {
            orgList = new ArrayList<Organization>();
        }
        // Clean up ここから
        List<Integer> irregularIndexes = new ArrayList<Integer>();
        for (int i = 0; i < orgList.size(); i++) {
            Object obj = orgList.get(i);
            if (!(obj instanceof Organization)) {
                irregularIndexes.add(i);
            }
        }
        int[] irregularArray = irregularIndexes.stream().mapToInt(i -> i).toArray();
        for (int i = irregularArray.length - 1; i >= 0; i--) {
            orgList.remove(i);
        }
        // Clean up ここまで

        table = new Table(orgTableGrp, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        // tableGrDt.horizontalSpan = 2;
        table.setLayoutData(tableGrDt);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableColumn column0 = new TableColumn(table, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(table, SWT.CENTER);
        column1.setWidth(50);
        column1.setText("有効");
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setWidth(150);
        column2.setText("組織名");
        TableColumn column3 = new TableColumn(table, SWT.CENTER);
        column3.setWidth(250);
        column3.setText("組織ID");
        TableColumn column4 = new TableColumn(table, SWT.CENTER);
        column4.setWidth(250);
        column4.setText("API Key");

        for (Organization org : orgList) {
            this.addOrgToTable(org);
        }
        for (Button button : checkBoxList) {
            if (button.getSelection()) {
                this.selectedIdxes.add(checkBoxList.indexOf(button));
            }
        }

        if (ps.getString(PreferenceConstants.IS_SUPERADMIN).equals("true")) {
            superAdminYes.setSelection(true);
            stackCompositeLt.topControl = comp1;
            stackComposite.layout();
        } else {
            superAdminNo.setSelection(true);
            stackCompositeLt.topControl = comp2;
            stackComposite.layout();
        }

        superAdminYes.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button source = (Button) e.getSource();
                if (source.getSelection()) {
                    stackCompositeLt.topControl = comp1;
                    stackComposite.layout();
                }
            }
        });

        superAdminNo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button source = (Button) e.getSource();
                if (source.getSelection()) {
                    stackCompositeLt.topControl = comp2;
                    stackComposite.layout();
                }
            }
        });

        Composite orgButtonGrp = new Composite(orgTableGrp, SWT.NONE);
        orgButtonGrp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        orgButtonGrp.setLayout(new GridLayout(1, true));

        addBtn = new Button(orgButtonGrp, SWT.NULL);
        addBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addBtn.setText("追加");
        if (contrastUrlTxt.getText().trim().isEmpty() || userNameTxt.getText().trim().isEmpty() || serviceKeyTxt.getText().trim().isEmpty()) {
            addBtn.setEnabled(false);
        } else {
            addBtn.setEnabled(true);
        }
        addBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (ps.getBoolean(PreferenceConstants.PROXY_YUKO) && ps.getString(PreferenceConstants.PROXY_AUTH).equals("input")) {
                    String usr = ps.getString(PreferenceConstants.PROXY_TMP_USER);
                    String pwd = ps.getString(PreferenceConstants.PROXY_TMP_PASS);
                    if (usr == null || usr.isEmpty() || pwd == null || pwd.isEmpty()) {
                        ProxyAuthDialog proxyAuthDialog = new ProxyAuthDialog(getShell());
                        int result = proxyAuthDialog.open();
                        if (IDialogConstants.CANCEL_ID == result) {
                            ps.setValue(PreferenceConstants.PROXY_AUTH, "none");
                        } else {
                            ps.setValue(PreferenceConstants.PROXY_TMP_USER, proxyAuthDialog.getUsername());
                            ps.setValue(PreferenceConstants.PROXY_TMP_PASS, proxyAuthDialog.getPassword());
                        }
                    }
                }
                OrganizationDialog orgDialog = null;
                orgDialog = new OrganizationDialog(shell, ps, contrastUrlTxt.getText().trim(), userNameTxt.getText().trim(), serviceKeyTxt.getText().trim());
                int result = orgDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
                Organization rtnOrg = orgDialog.getOrg();
                if (rtnOrg == null) {
                    return;
                }
                // String path = pathDialog.getDirPath();
                if (orgList.contains(rtnOrg)) {
                    MessageDialog.openError(composite.getShell(), "組織の追加", "すでに設定されている組織です。");
                    return;
                }
                orgList.add(rtnOrg);
                addOrgToTable(rtnOrg);
                if (orgList.size() == 1) {
                    checkBoxList.get(0).setSelection(true);
                    rtnOrg.setValid(true);
                }
                for (Button button : checkBoxList) {
                    if (button.getSelection()) {
                        selectedIdxes.add(checkBoxList.indexOf(button));
                    }
                }
            }
        });

        rmvBtn = new Button(orgButtonGrp, SWT.NULL);
        rmvBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rmvBtn.setText("削除");
        rmvBtn.setEnabled(false);
        rmvBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] indexes = table.getSelectionIndices();
                for (int i = indexes.length - 1; i >= 0; i--) {
                    orgList.remove(indexes[i]);
                }
                for (Button button : checkBoxList) {
                    button.dispose();
                }
                checkBoxList.clear();
                table.clearAll();
                table.removeAll();
                for (Organization org : orgList) {
                    addOrgToTable(org);
                }
                if (checkBoxList.isEmpty()) {
                    selectedIdxes.clear();
                } else {
                    for (Button button : checkBoxList) {
                        if (button.getSelection()) {
                            selectedIdxes.add(checkBoxList.indexOf(button));
                        }
                    }
                }
            }
        });

        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = table.getSelectionIndex();
                if (index < 0) {
                    rmvBtn.setEnabled(false);
                } else {
                    rmvBtn.setEnabled(true);
                }
            }
        });

        Label connectionHint = new Label(orgTableGrp, SWT.LEFT);
        connectionHint.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        connectionHint.setText("※ プロキシ経由など接続に関する設定が必要な場合は「接続設定」で事前に設定を済ませておいてください。");

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
        defaultBtn.setText("デフォルトに戻す");
        defaultBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ps.getDefaultString(PreferenceConstants.IS_CREATEGROUP).equals("true")) {
                    groupCreateYes.setSelection(true);
                    groupCreateNo.setSelection(false);
                } else {
                    groupCreateYes.setSelection(false);
                    groupCreateNo.setSelection(true);
                }
                groupNameTxt.setText(ps.getDefaultString(PreferenceConstants.GROUP_NAME));
                showCreateGroupLogFlg.setSelection(ps.getDefaultBoolean(PreferenceConstants.SHOW_CREATEGROUP_LOG));
            }
        });

        Button applyBtn = new Button(buttonGrp, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.widthHint = 90;
        applyBtn.setLayoutData(applyBtnGrDt);
        applyBtn.setText("適用");
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
        String url = this.contrastUrlTxt.getText().trim();
        String usr = this.userNameTxt.getText().trim();
        String svc = this.serviceKeyTxt.getText().trim();
        if (url.isEmpty() || usr.isEmpty() || svc.isEmpty()) {
            if (!this.orgList.isEmpty()) {
                errors.add("・組織一覧に設定が残っている場合、Contrast URL, Username, Service Keyはブランクにできません。\r\nこれらをブランクにする場合は組織一覧の設定をすべて削除してください。");
                contrastUrlTxt.setText(ps.getString(PreferenceConstants.CONTRAST_URL));
                serviceKeyTxt.setText(ps.getString(PreferenceConstants.SERVICE_KEY));
                userNameTxt.setText(ps.getString(PreferenceConstants.USERNAME));
            }
        }
        ps.setValue(PreferenceConstants.CONTRAST_URL, url);
        ps.setValue(PreferenceConstants.USERNAME, usr);
        for (Organization org : this.orgList) {
            org.setValid(false);
            for (Integer selectedIdx : selectedIdxes) {
                TableItem selectedItem = table.getItem(selectedIdx);
                if (org.getOrganization_uuid().equals(selectedItem.getText(3))) {
                    org.setValid(true);
                }
            }
        }
        ps.setValue(PreferenceConstants.SERVICE_KEY, svc);

        if (superAdminYes.getSelection()) {
            ps.setValue(PreferenceConstants.IS_SUPERADMIN, "true");
        } else {
            ps.setValue(PreferenceConstants.IS_SUPERADMIN, "false");
        }
        ps.setValue(PreferenceConstants.API_KEY, this.apiKeyTxt.getText());
        ps.setValue(PreferenceConstants.ORG_ID, this.orgIdTxt.getText());
        if (groupCreateYes.getSelection()) {
            ps.setValue(PreferenceConstants.IS_CREATEGROUP, "true");
        } else {
            ps.setValue(PreferenceConstants.IS_CREATEGROUP, "false");
        }
        ps.setValue(PreferenceConstants.GROUP_NAME, this.groupNameTxt.getText());
        ps.setValue(PreferenceConstants.SHOW_CREATEGROUP_LOG, this.showCreateGroupLogFlg.getSelection());
        ps.setValue(PreferenceConstants.TARGET_ORGS, new Gson().toJson(this.orgList));
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "基本設定", String.join("\r\n", errors));
            return false;
        }
        return true;
    }

    private void addOrgToTable(Organization org) {
        if (org == null) {
            return;
        }
        TableEditor editor = new TableEditor(table);
        Button button = new Button(table, SWT.CHECK);
        if (org.isValid()) {
            button.setSelection(true);
        }
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedIdxes.clear();
                for (Button button : checkBoxList) {
                    if (button.getSelection()) {
                        selectedIdxes.add(checkBoxList.indexOf(button));
                    }
                }
            }
        });
        button.pack();
        TableItem item = new TableItem(table, SWT.CENTER);
        editor.minimumWidth = button.getSize().x;
        editor.horizontalAlignment = SWT.CENTER;
        editor.setEditor(button, item, 1);
        checkBoxList.add(button);
        item.setText(2, org.getName());
        item.setText(3, org.getOrganization_uuid());
        item.setText(4, org.getApikey());
    }
}
