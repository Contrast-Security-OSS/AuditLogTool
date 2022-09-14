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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CSVPreferencePage extends PreferencePage {

    private Text evtCSVFileFmtTxt;

    public CSVPreferencePage() {
        super("CSV出力設定");
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

        Group csvFileFmtGrp = new Group(composite, SWT.NONE);
        GridLayout csvFileFmtGrpLt = new GridLayout(1, false);
        csvFileFmtGrpLt.marginWidth = 10;
        csvFileFmtGrpLt.marginHeight = 10;
        csvFileFmtGrpLt.horizontalSpacing = 5;
        csvFileFmtGrpLt.verticalSpacing = 10;
        csvFileFmtGrp.setLayout(csvFileFmtGrpLt);
        GridData csvFileFmtGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // csvFileFmtGrpGrDt.horizontalSpan = 2;
        csvFileFmtGrp.setLayoutData(csvFileFmtGrpGrDt);
        csvFileFmtGrp.setText("CSV出力ファイルフォーマット（フォルダ名にも適用されます）");

        Group evtCSVFileFmtGrp = new Group(csvFileFmtGrp, SWT.NONE);
        GridLayout evtCSVFileFmtGrpLt = new GridLayout(1, false);
        evtCSVFileFmtGrpLt.marginWidth = 10;
        evtCSVFileFmtGrpLt.marginHeight = 10;
        evtCSVFileFmtGrpLt.horizontalSpacing = 10;
        evtCSVFileFmtGrp.setLayout(evtCSVFileFmtGrpLt);
        GridData evtCSVFileFmtGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // evtCSVFileFmtGrpGrDt.horizontalSpan = 2;
        evtCSVFileFmtGrp.setLayoutData(evtCSVFileFmtGrpGrDt);
        evtCSVFileFmtGrp.setText("監査ログ");

        evtCSVFileFmtTxt = new Text(evtCSVFileFmtGrp, SWT.BORDER);
        evtCSVFileFmtTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        evtCSVFileFmtTxt.setText(ps.getString(PreferenceConstants.CSV_FILE_FORMAT_AUDITLOG));
        evtCSVFileFmtTxt.setMessage(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_AUDITLOG));

        Label csvFileFormatHint = new Label(csvFileFmtGrp, SWT.LEFT);
        GridData csvFileFormatHintGrDt = new GridData(GridData.FILL_HORIZONTAL);
        csvFileFormatHint.setLayoutData(csvFileFormatHintGrDt);
        csvFileFormatHint.setText("※ java.text.SimpleDateFormatの書式としてください。\r\n例) 'vul_'yyyy-MM-dd_HHmmss、'lib_'yyyy-MM-dd_HHmmss");

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
                evtCSVFileFmtTxt.setText(ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_AUDITLOG));
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
        ps.setValue(PreferenceConstants.CSV_FILE_FORMAT_AUDITLOG, this.evtCSVFileFmtTxt.getText());
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "その他設定", String.join("\r\n", errors));
            return false;
        }
        return true;
    }
}
