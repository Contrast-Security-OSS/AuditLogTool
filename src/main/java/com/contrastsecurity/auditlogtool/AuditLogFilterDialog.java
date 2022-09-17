/*
 * MIT License
 * Copyright (c) 2015-2019 Tabocom
 *
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
 */

package com.contrastsecurity.auditlogtool;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.auditlogtool.model.Filter;

public class AuditLogFilterDialog extends Dialog {

    private Map<FilterEnum, Set<Filter>> filterMap;
    private CheckboxTableViewer orgNameViewer;
    private CheckboxTableViewer userNameViewer;
    private Text messageIncludeFilter;
    private Text messageExcludeFilter;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public AuditLogFilterDialog(Shell parentShell, Map<FilterEnum, Set<Filter>> filterMap) {
        super(parentShell);
        this.filterMap = filterMap;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout compositeLt = new GridLayout(3, false);
        compositeLt.marginWidth = 25;
        compositeLt.marginHeight = 5;
        compositeLt.horizontalSpacing = 5;
        composite.setLayout(compositeLt);
        GridData compositeGrDt = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(compositeGrDt);

        // #################### 組織名 #################### //
        Group orgNameGrp = new Group(composite, SWT.NONE);
        GridLayout orgNameGrpLt = new GridLayout(2, false);
        orgNameGrpLt.marginWidth = 10;
        orgNameGrpLt.marginHeight = 10;
        orgNameGrp.setLayout(orgNameGrpLt);
        GridData orgNameGrpGrDt = new GridData(GridData.FILL_BOTH);
        orgNameGrpGrDt.minimumWidth = 200;
        orgNameGrp.setLayoutData(orgNameGrpGrDt);
        orgNameGrp.setText("組織名");

        final Table orgNameTable = new Table(orgNameGrp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData orgNameTableGrDt = new GridData(GridData.FILL_BOTH);
        orgNameTableGrDt.horizontalSpan = 2;
        orgNameTable.setLayoutData(orgNameTableGrDt);
        orgNameViewer = new CheckboxTableViewer(orgNameTable);
        orgNameViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });
        orgNameViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                checkStateUpdate();
            }
        });
        orgNameTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int df = orgNameTable.getSelectionIndex();
                orgNameViewer.setChecked(orgNameViewer.getElementAt(df), !orgNameViewer.getChecked(orgNameViewer.getElementAt(df)));
                checkStateUpdate();
            }
        });

        List<String> orgNameLabelList = new ArrayList<String>();
        List<String> orgNameValidLabelList = new ArrayList<String>();
        for (Filter filter : filterMap.get(FilterEnum.ORG_NAME)) {
            orgNameLabelList.add(filter.getLabel());
            if (filter.isValid()) {
                orgNameValidLabelList.add(filter.getLabel());
            } else {
            }
        }
        // if (orgNameValidLabelList.isEmpty()) {
        // orgNameValidLabelList.addAll(orgNameLabelList);
        // }
        orgNameViewer.setContentProvider(new ArrayContentProvider());
        orgNameViewer.setInput(orgNameLabelList);
        orgNameViewer.setCheckedElements(orgNameValidLabelList.toArray());

        final Button orgNameBulkOffBtn = new Button(orgNameGrp, SWT.NULL);
        GridData orgNameBulkOffBtnGrDt = new GridData();
        orgNameBulkOffBtnGrDt.minimumWidth = 50;
        orgNameBulkOffBtn.setLayoutData(orgNameBulkOffBtnGrDt);
        orgNameBulkOffBtn.setText("すべてoff");
        orgNameBulkOffBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                orgNameViewer.setCheckedElements(new ArrayList<String>().toArray());
                orgNameViewer.refresh();
                checkStateUpdate();
            }
        });

        final Button orgNameBulkOnBtn = new Button(orgNameGrp, SWT.NULL);
        GridData orgNameBulkOnBtnGrDt = new GridData();
        orgNameBulkOnBtnGrDt.minimumWidth = 50;
        orgNameBulkOnBtn.setLayoutData(orgNameBulkOnBtnGrDt);
        orgNameBulkOnBtn.setText("すべてon");
        orgNameBulkOnBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                orgNameValidLabelList.addAll(orgNameLabelList);
                orgNameViewer.setCheckedElements(orgNameValidLabelList.toArray());
                orgNameViewer.refresh();
                checkStateUpdate();
            }
        });

        // final Button orgNameBulkBtn = new Button(orgNameGrp, SWT.CHECK);
        // orgNameBulkBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // orgNameBulkBtn.setText("すべて");
        // if (orgNameValidLabelList.isEmpty()) {
        // orgNameBulkBtn.setSelection(false);
        // } else {
        // orgNameBulkBtn.setSelection(true);
        // }
        // orgNameBulkBtn.addSelectionListener(new SelectionAdapter() {
        // @Override
        // public void widgetSelected(SelectionEvent e) {
        // if (orgNameBulkBtn.getSelection()) {
        // orgNameValidLabelList.addAll(orgNameLabelList);
        // orgNameViewer.setCheckedElements(orgNameValidLabelList.toArray());
        // orgNameViewer.refresh();
        // } else {
        // orgNameViewer.setCheckedElements(new ArrayList<String>().toArray());
        // orgNameViewer.refresh();
        // }
        // checkStateUpdate();
        // }
        // });

        // #################### ユーザー名 #################### //
        Group userNameGrp = new Group(composite, SWT.NONE);
        GridLayout userNameGrpLt = new GridLayout(2, false);
        userNameGrpLt.marginWidth = 10;
        userNameGrpLt.marginHeight = 10;
        userNameGrp.setLayout(userNameGrpLt);
        GridData userNameGrpGrDt = new GridData(GridData.FILL_BOTH);
        userNameGrpGrDt.minimumWidth = 200;
        userNameGrp.setLayoutData(userNameGrpGrDt);
        userNameGrp.setText("ユーザー名");

        final Table userNameTable = new Table(userNameGrp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData userNameTableGrDt = new GridData(GridData.FILL_BOTH);
        userNameTableGrDt.horizontalSpan = 2;
        userNameTable.setLayoutData(userNameTableGrDt);
        userNameViewer = new CheckboxTableViewer(userNameTable);
        userNameViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });
        userNameViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                checkStateUpdate();
            }
        });
        userNameTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int df = userNameTable.getSelectionIndex();
                userNameViewer.setChecked(userNameViewer.getElementAt(df), !userNameViewer.getChecked(userNameViewer.getElementAt(df)));
                checkStateUpdate();
            }
        });
        List<String> userNameLabelList = new ArrayList<String>();
        List<String> userNameValidLabelList = new ArrayList<String>();
        for (Filter filter : filterMap.get(FilterEnum.USER_NAME)) {
            userNameLabelList.add(filter.getLabel());
            if (filter.isValid()) {
                userNameValidLabelList.add(filter.getLabel());
            } else {
            }
        }
        // if (userNameValidLabelList.isEmpty()) {
        // userNameValidLabelList.addAll(userNameLabelList);
        // }
        userNameViewer.setContentProvider(new ArrayContentProvider());
        userNameViewer.setInput(userNameLabelList);
        userNameViewer.setCheckedElements(userNameValidLabelList.toArray());

        final Button userNameBulkOffBtn = new Button(userNameGrp, SWT.NULL);
        GridData userNameBulkOffBtnGrDt = new GridData();
        userNameBulkOffBtnGrDt.minimumWidth = 50;
        userNameBulkOffBtn.setLayoutData(userNameBulkOffBtnGrDt);
        userNameBulkOffBtn.setText("すべてoff");
        userNameBulkOffBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                userNameViewer.setCheckedElements(new ArrayList<String>().toArray());
                userNameViewer.refresh();
                checkStateUpdate();
            }
        });

        final Button userNameBulkOnBtn = new Button(userNameGrp, SWT.NULL);
        GridData userNameBulkOnBtnGrDt = new GridData();
        userNameBulkOnBtnGrDt.minimumWidth = 50;
        userNameBulkOnBtn.setLayoutData(userNameBulkOnBtnGrDt);
        userNameBulkOnBtn.setText("すべてon");
        userNameBulkOnBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                userNameValidLabelList.addAll(userNameLabelList);
                userNameViewer.setCheckedElements(userNameValidLabelList.toArray());
                userNameViewer.refresh();
                checkStateUpdate();
            }
        });

        // final Button userNameBulkBtn = new Button(userNameGrp, SWT.CHECK);
        // userNameBulkBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // userNameBulkBtn.setText("すべて");
        // if (userNameValidLabelList.isEmpty()) {
        // userNameBulkBtn.setSelection(false);
        // } else {
        // userNameBulkBtn.setSelection(true);
        // }
        // userNameBulkBtn.addSelectionListener(new SelectionAdapter() {
        // @Override
        // public void widgetSelected(SelectionEvent e) {
        // if (userNameBulkBtn.getSelection()) {
        // userNameValidLabelList.addAll(userNameLabelList);
        // userNameViewer.setCheckedElements(userNameValidLabelList.toArray());
        // userNameViewer.refresh();
        // } else {
        // userNameViewer.setCheckedElements(new ArrayList<String>().toArray());
        // userNameViewer.refresh();
        // }
        // checkStateUpdate();
        // }
        // });

        // #################### メッセージ #################### //
        String keywordInclude = "";
        Set<Filter> msgIncludeSet = filterMap.get(FilterEnum.MESSAGE_INCLUDE);
        if (msgIncludeSet != null && !msgIncludeSet.isEmpty()) {
            keywordInclude = msgIncludeSet.iterator().next().getLabel();
        }
        String keywordExclude = "";
        Set<Filter> msgExcludeSet = filterMap.get(FilterEnum.MESSAGE_EXCLUDE);
        if (msgExcludeSet != null && !msgExcludeSet.isEmpty()) {
            keywordExclude = msgExcludeSet.iterator().next().getLabel();
        }

        Group messageGrp = new Group(composite, SWT.NONE);
        GridLayout messageGrpLt = new GridLayout(2, false);
        messageGrpLt.marginWidth = 10;
        messageGrpLt.marginHeight = 10;
        messageGrp.setLayout(messageGrpLt);
        GridData messageGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        messageGrpGrDt.horizontalSpan = 2;
        messageGrp.setLayoutData(messageGrpGrDt);
        messageGrp.setText("メッセージ");

        messageIncludeFilter = new Text(messageGrp, SWT.BORDER);
        messageIncludeFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        messageIncludeFilter.setMessage("この文字列が含まれている監査ログを対象とします...");
        messageIncludeFilter.setText(keywordInclude);
        messageIncludeFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                checkStateUpdate();
            }
        });
        new Label(messageGrp, SWT.LEFT).setText("を含む");

        messageExcludeFilter = new Text(messageGrp, SWT.BORDER);
        messageExcludeFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        messageExcludeFilter.setMessage("この文字列が含まれていない監査ログを対象とします...");
        messageExcludeFilter.setText(keywordExclude);
        messageExcludeFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                checkStateUpdate();
            }
        });
        new Label(messageGrp, SWT.LEFT).setText("を含まない");

        return composite;
    }

    private void checkStateUpdate() {
        // 組織名
        Object[] orgNameItems = orgNameViewer.getCheckedElements();
        List<String> orgNameStrItems = new ArrayList<String>();
        for (Object item : orgNameItems) {
            orgNameStrItems.add((String) item);
        }
        for (Filter filter : filterMap.get(FilterEnum.ORG_NAME)) {
            if (orgNameStrItems.contains(filter.getLabel())) {
                filter.setValid(true);
            } else {
                filter.setValid(false);
            }
        }
        // ユーザー名
        Object[] userNameItems = userNameViewer.getCheckedElements();
        List<String> userNameStrItems = new ArrayList<String>();
        for (Object item : userNameItems) {
            userNameStrItems.add((String) item);
        }
        for (Filter filter : filterMap.get(FilterEnum.USER_NAME)) {
            if (userNameStrItems.contains(filter.getLabel())) {
                filter.setValid(true);
            } else {
                filter.setValid(false);
            }
        }
        // メッセージ
        // 含む
        Set<Filter> msgIncludeFilterSet = new LinkedHashSet<Filter>();
        msgIncludeFilterSet.add(new Filter(messageIncludeFilter.getText()));
        filterMap.put(FilterEnum.MESSAGE_INCLUDE, msgIncludeFilterSet);
        // 含まない
        Set<Filter> msgExcludeFilterSet = new LinkedHashSet<Filter>();
        msgExcludeFilterSet.add(new Filter(messageExcludeFilter.getText()));
        filterMap.put(FilterEnum.MESSAGE_EXCLUDE, msgExcludeFilterSet);

        support.firePropertyChange("auditFilter", null, filterMap);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "閉じる", true);
    }

    @Override
    protected void okPressed() {
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        return new Point(720, 480);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("監査ログフィルター");
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }
}
