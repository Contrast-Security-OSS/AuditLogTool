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
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class OtherPreferencePage extends PreferencePage {

    private Combo termStartMonthCombo;
    Pattern ptn = Pattern.compile("^[0-9]{2}-[0-9]{2}$");
    public static String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Oct", "Nov", "Dec" };
    public static String[] WEEKDAYS = { "日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日" };
    private List<Button> weekDayBtns = new ArrayList<Button>();
    private Text auditLogLimitTxt;
    private Text auditLogSleepTxt;

    public OtherPreferencePage() {
        super("その他設定");
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

        Group protectGrp = new Group(composite, SWT.NONE);
        GridLayout protectGrpLt = new GridLayout(2, false);
        protectGrpLt.marginHeight = 10;
        protectGrpLt.marginWidth = 15;
        protectGrpLt.horizontalSpacing = 10;
        protectGrpLt.verticalSpacing = 15;
        protectGrp.setLayout(protectGrpLt);
        GridData protectGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        protectGrp.setLayoutData(protectGrpGrDt);
        protectGrp.setText("監査ログ取得期間の設定");

        new Label(protectGrp, SWT.LEFT).setText("期首：");
        termStartMonthCombo = new Combo(protectGrp, SWT.DROP_DOWN | SWT.READ_ONLY);
        termStartMonthCombo.setItems(MONTHS);
        termStartMonthCombo.setText(ps.getString(PreferenceConstants.TERM_START_MONTH));

        Group weekDayGrp = new Group(protectGrp, SWT.NONE);
        GridLayout weekDayGrpLt = new GridLayout(7, false);
        weekDayGrpLt.horizontalSpacing = 10;
        weekDayGrp.setLayout(weekDayGrpLt);
        GridData weekDayGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        weekDayGrpGrDt.horizontalSpan = 2;
        weekDayGrp.setLayoutData(weekDayGrpGrDt);
        weekDayGrp.setText("週の開始");
        int weekDayIdx = 0;
        for (String weekDay : WEEKDAYS) {
            Button weekDayBtn = new Button(weekDayGrp, SWT.RADIO);
            weekDayBtn.setText(weekDay);
            if (ps.getInt(PreferenceConstants.START_WEEKDAY) == weekDayIdx) {
                weekDayBtn.setSelection(true);
            } else {
                weekDayBtn.setSelection(false);
            }
            weekDayBtns.add(weekDayBtn);
            weekDayIdx++;
        }

        // ========== 監査ログ取得ごとスリープ ========== //
        Group sleepGrp = new Group(composite, SWT.NONE);
        GridLayout sleepGrpLt = new GridLayout(2, false);
        sleepGrpLt.marginWidth = 15;
        sleepGrpLt.horizontalSpacing = 10;
        sleepGrp.setLayout(sleepGrpLt);
        GridData sleepGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        sleepGrp.setLayoutData(sleepGrpGrDt);
        sleepGrp.setText("API呼び出し制御");

        new Label(sleepGrp, SWT.LEFT).setText("呼び出し1回あたりの取得数（1000までは動作確認済み）：");
        auditLogLimitTxt = new Text(sleepGrp, SWT.BORDER);
        auditLogLimitTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        auditLogLimitTxt.setText(ps.getString(PreferenceConstants.LIMIT_AUDITLOG));

        new Label(sleepGrp, SWT.LEFT).setText("呼び出し間隔スリープ（ミリ秒）：");
        auditLogSleepTxt = new Text(sleepGrp, SWT.BORDER);
        auditLogSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        auditLogSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_AUDITLOG));

        Label descLbl = new Label(sleepGrp, SWT.LEFT);
        GridData descLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        descLblGrDt.horizontalSpan = 2;
        descLbl.setLayoutData(descLblGrDt);
        descLbl.setText("※ 取得数とスリープの組み合わせで、監査ログの取得に要する時間を調整できます。");

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
            public void widgetSelected(SelectionEvent e) {
                termStartMonthCombo.setText(ps.getDefaultString(PreferenceConstants.TERM_START_MONTH));
                for (Button btn : weekDayBtns) {
                    btn.setSelection(false);
                }
                Button btn = weekDayBtns.get(ps.getDefaultInt(PreferenceConstants.START_WEEKDAY));
                btn.setSelection(true);
                auditLogLimitTxt.setText(ps.getDefaultString(PreferenceConstants.LIMIT_AUDITLOG));
                auditLogSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_AUDITLOG));
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
        if (this.auditLogSleepTxt.getText().isEmpty()) {
            errors.add("・脆弱性取得間隔スリープを指定してください。");
        } else {
            if (!StringUtils.isNumeric(this.auditLogSleepTxt.getText())) {
                errors.add("・脆弱性取得間隔スリープは数値を指定してください。");
            }
        }
        ps.setValue(PreferenceConstants.LIMIT_AUDITLOG, this.auditLogLimitTxt.getText());
        ps.setValue(PreferenceConstants.SLEEP_AUDITLOG, this.auditLogSleepTxt.getText());
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "その他設定", String.join("\r\n", errors));
            return false;
        } else {
            ps.setValue(PreferenceConstants.TERM_START_MONTH, this.termStartMonthCombo.getText());
            int weekDaySelection = 0;
            for (Button btn : weekDayBtns) {
                if (btn.getSelection()) {
                    break;
                }
                weekDaySelection++;
            }
            ps.setValue(PreferenceConstants.START_WEEKDAY, weekDaySelection);
        }
        return true;
    }
}
