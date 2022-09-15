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

package com.contrastsecurity.auditlogtool;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.exec.OS;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.yaml.snakeyaml.Yaml;

import com.contrastsecurity.auditlogtool.exception.ApiException;
import com.contrastsecurity.auditlogtool.exception.NonApiException;
import com.contrastsecurity.auditlogtool.model.AuditLog;
import com.contrastsecurity.auditlogtool.model.AuditLogCSVColumn;
import com.contrastsecurity.auditlogtool.model.ContrastSecurityYaml;
import com.contrastsecurity.auditlogtool.model.Filter;
import com.contrastsecurity.auditlogtool.model.Organization;
import com.contrastsecurity.auditlogtool.preference.AboutPage;
import com.contrastsecurity.auditlogtool.preference.AuditLogCSVColumnPreferencePage;
import com.contrastsecurity.auditlogtool.preference.BasePreferencePage;
import com.contrastsecurity.auditlogtool.preference.CSVPreferencePage;
import com.contrastsecurity.auditlogtool.preference.ConnectionPreferencePage;
import com.contrastsecurity.auditlogtool.preference.MyPreferenceDialog;
import com.contrastsecurity.auditlogtool.preference.OtherPreferencePage;
import com.contrastsecurity.auditlogtool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Main implements PropertyChangeListener {

    public static final String WINDOW_TITLE = "AuditLogTool - %s";
    // 以下のMASTER_PASSWORDはプロキシパスワードを保存する際に暗号化で使用するパスワードです。
    // 本ツールをリリース用にコンパイルする際はchangemeを別の文字列に置き換えてください。
    public static final String MASTER_PASSWORD = "changeme!";

    // 各出力ファイルの文字コード
    public static final String CSV_WIN_ENCODING = "Shift_JIS";
    public static final String CSV_MAC_ENCODING = "UTF-8";
    public static final String FILE_ENCODING = "UTF-8";

    public static final int MINIMUM_SIZE_WIDTH = 800;
    public static final int MINIMUM_SIZE_WIDTH_MAC = 880;
    public static final int MINIMUM_SIZE_HEIGHT = 640;

    private AuditLogToolShell shell;

    private Button auditLogLoadBtn;

    private Button settingBtn;

    private Label statusBar;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd(E)");

    private Map<FilterEnum, Set<Filter>> auditLogFilterMap;

    private boolean isSortDesc;

    // AuditLog
    private Label auditLogCount;
    private List<Button> auditLogCreatedRadios = new ArrayList<Button>();
    private Button auditLogTermHalf1st;
    private Button auditLogTermHalf2nd;
    private Button auditLogTerm30days;
    private Button auditLogTermYesterday;
    private Button auditLogTermToday;
    private Button auditLogTermLastWeek;
    private Button auditLogTermThisWeek;
    private Button auditLogTermPeriod;
    private Text auditLogCreatedFilterTxt;
    private Date frCreatedDate;
    private Date toCreatedDate;
    private Table auditLogTable;
    private List<AuditLog> auditLogs;
    private List<AuditLog> filteredAuditLogs = new ArrayList<AuditLog>();
    private Map<AuditLogCreatedDateFilterEnum, Date> auditLogCreatedFilterMap;

    private PreferenceStore ps;

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    Logger logger = LogManager.getLogger("auditlogtool");

    /**
     * @param args
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.initialize();
        main.createPart();
    }

    private void initialize() {
        try {
            String homeDir = System.getProperty("user.home");
            this.ps = new PreferenceStore(homeDir + "\\auditlogtool.properties");
            if (OS.isFamilyMac()) {
                this.ps = new PreferenceStore(homeDir + "/auditlogtool.properties");
            }
            try {
                this.ps.load();
            } catch (FileNotFoundException fnfe) {
                this.ps = new PreferenceStore("auditlogtool.properties");
                this.ps.load();
            }
        } catch (FileNotFoundException fnfe) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.ps.setDefault(PreferenceConstants.IS_SUPERADMIN, "true");
            this.ps.setDefault(PreferenceConstants.IS_CREATEGROUP, "false");
            this.ps.setDefault(PreferenceConstants.GROUP_NAME, "GgHTWED8kZdQU76c");
            this.ps.setDefault(PreferenceConstants.SHOW_CREATEGROUP_LOG, "false");
            this.ps.setDefault(PreferenceConstants.PROXY_AUTH, "none");
            this.ps.setDefault(PreferenceConstants.CONNECTION_TIMEOUT, 3000);
            this.ps.setDefault(PreferenceConstants.SOCKET_TIMEOUT, 3000);

            this.ps.setDefault(PreferenceConstants.TERM_START_MONTH, "Jan");
            this.ps.setDefault(PreferenceConstants.START_WEEKDAY, 1); // 月曜日
            this.ps.setDefault(PreferenceConstants.AUDITLOG_CREATED_DATE_FILTER, 0);

            this.ps.setDefault(PreferenceConstants.CSV_COLUMN_AUDITLOG, AuditLogCSVColmunEnum.defaultValuesStr());
            this.ps.setDefault(PreferenceConstants.LIMIT_AUDITLOG, 100);
            this.ps.setDefault(PreferenceConstants.SLEEP_AUDITLOG, 300);
            this.ps.setDefault(PreferenceConstants.CSV_OUT_HEADER_AUDITLOG, true);
            this.ps.setDefault(PreferenceConstants.CSV_FILE_FORMAT_AUDITLOG, "'auditlog'_yyyy-MM-dd_HHmmss");

            this.ps.setDefault(PreferenceConstants.OPENED_MAIN_TAB_IDX, 0);
            this.ps.setDefault(PreferenceConstants.OPENED_SUB_TAB_IDX, 0);

            Yaml yaml = new Yaml();
            InputStream is = new FileInputStream("contrast_security.yaml");
            ContrastSecurityYaml contrastSecurityYaml = yaml.loadAs(is, ContrastSecurityYaml.class);
            is.close();
            this.ps.setDefault(PreferenceConstants.CONTRAST_URL, contrastSecurityYaml.getUrl());
            this.ps.setDefault(PreferenceConstants.USERNAME, contrastSecurityYaml.getUserName());
            this.ps.setDefault(PreferenceConstants.SERVICE_KEY, contrastSecurityYaml.getServiceKey());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    private void createPart() {
        Display display = new Display();
        shell = new AuditLogToolShell(display, this);
        if (OS.isFamilyMac()) {
            shell.setMinimumSize(MINIMUM_SIZE_WIDTH_MAC, MINIMUM_SIZE_HEIGHT);
        } else {
            shell.setMinimumSize(MINIMUM_SIZE_WIDTH, MINIMUM_SIZE_HEIGHT);
        }
        Image[] imageArray = new Image[5];
        imageArray[0] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon16.png"));
        imageArray[1] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon24.png"));
        imageArray[2] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon32.png"));
        imageArray[3] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon48.png"));
        imageArray[4] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon128.png"));
        shell.setImages(imageArray);
        Window.setDefaultImages(imageArray);
        setWindowTitle();
        shell.addShellListener(new ShellListener() {
            @Override
            public void shellIconified(ShellEvent event) {
            }

            @Override
            public void shellDeiconified(ShellEvent event) {
            }

            @Override
            public void shellDeactivated(ShellEvent event) {
            }

            @Override
            public void shellClosed(ShellEvent event) {
                ps.setValue(PreferenceConstants.MEM_WIDTH, shell.getSize().x);
                ps.setValue(PreferenceConstants.MEM_HEIGHT, shell.getSize().y);
                ps.setValue(PreferenceConstants.PROXY_TMP_USER, "");
                ps.setValue(PreferenceConstants.PROXY_TMP_PASS, "");
                for (Button termBtn : auditLogCreatedRadios) {
                    if (termBtn.getSelection()) {
                        ps.setValue(PreferenceConstants.AUDITLOG_CREATED_DATE_FILTER, auditLogCreatedRadios.indexOf(termBtn));
                    }
                }
                try {
                    ps.save();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            @Override
            public void shellActivated(ShellEvent event) {
                boolean ngRequiredFields = false;
                String url = ps.getString(PreferenceConstants.CONTRAST_URL);
                String usr = ps.getString(PreferenceConstants.USERNAME);
                boolean isSuperAdmin = ps.getBoolean(PreferenceConstants.IS_SUPERADMIN);
                String svc = ps.getString(PreferenceConstants.SERVICE_KEY);
                if (isSuperAdmin) {
                    String api = ps.getString(PreferenceConstants.API_KEY);
                    if (url.isEmpty() || usr.isEmpty() || svc.isEmpty() || api.isEmpty()) {
                        ngRequiredFields = true;
                    }
                } else {
                    if (url.isEmpty() || usr.isEmpty() || svc.isEmpty()) {
                        ngRequiredFields = true;
                    }
                }
                List<Organization> orgs = getValidOrganizations();
                if (ngRequiredFields || (!isSuperAdmin && orgs.isEmpty())) {
                    auditLogLoadBtn.setEnabled(false);
                    settingBtn.setText("このボタンから基本設定を行ってください。");
                    uiReset();
                } else {
                    auditLogLoadBtn.setEnabled(true);
                    settingBtn.setText("設定");
                }
                updateProtectOption();
                setWindowTitle();
                if (ps.getBoolean(PreferenceConstants.PROXY_YUKO) && ps.getString(PreferenceConstants.PROXY_AUTH).equals("input")) {
                    String proxy_usr = ps.getString(PreferenceConstants.PROXY_TMP_USER);
                    String proxy_pwd = ps.getString(PreferenceConstants.PROXY_TMP_PASS);
                    if (proxy_usr == null || proxy_usr.isEmpty() || proxy_pwd == null || proxy_pwd.isEmpty()) {
                        ProxyAuthDialog proxyAuthDialog = new ProxyAuthDialog(shell);
                        int result = proxyAuthDialog.open();
                        if (IDialogConstants.CANCEL_ID == result) {
                            ps.setValue(PreferenceConstants.PROXY_AUTH, "none");
                        } else {
                            ps.setValue(PreferenceConstants.PROXY_TMP_USER, proxyAuthDialog.getUsername());
                            ps.setValue(PreferenceConstants.PROXY_TMP_PASS, proxyAuthDialog.getPassword());
                        }
                    }
                }
            }
        });

        Listener listener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.stateMask == SWT.CTRL) {
                    int num = Character.getNumericValue(event.character);
                    if (num > -1) {
                        support.firePropertyChange("userswitch", 0, num);
                    }
                }
            }
        };
        display.addFilter(SWT.KeyUp, listener);

        GridLayout baseLayout = new GridLayout(1, false);
        baseLayout.marginWidth = 8;
        baseLayout.marginBottom = 0;
        baseLayout.verticalSpacing = 8;
        shell.setLayout(baseLayout);

        Group auditLogListGrp = new Group(shell, SWT.NONE);
        auditLogListGrp.setLayout(new GridLayout(3, false));
        GridData auditLogListGrpGrDt = new GridData(GridData.FILL_BOTH);
        auditLogListGrpGrDt.minimumHeight = 200;
        auditLogListGrp.setLayoutData(auditLogListGrpGrDt);

        Composite auditLogTermGrp = new Composite(auditLogListGrp, SWT.NONE);
        auditLogTermGrp.setLayout(new GridLayout(10, false));
        GridData auditLogTermGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        auditLogTermGrp.setLayoutData(auditLogTermGrpGrDt);

        new Label(auditLogTermGrp, SWT.LEFT).setText("取得期間：");
        // =============== 取得期間選択ラジオボタン ===============
        // 上半期
        auditLogTermHalf1st = new Button(auditLogTermGrp, SWT.RADIO);
        auditLogTermHalf1st.setText("上半期");
        auditLogCreatedRadios.add(auditLogTermHalf1st);
        auditLogTermHalf1st.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                frCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.HALF_1ST_START);
                toCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.HALF_1ST_END);
                detectedDateLabelUpdate();
            }

        });
        // 下半期
        auditLogTermHalf2nd = new Button(auditLogTermGrp, SWT.RADIO);
        auditLogTermHalf2nd.setText("下半期");
        auditLogCreatedRadios.add(auditLogTermHalf2nd);
        auditLogTermHalf2nd.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                frCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.HALF_2ND_START);
                toCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.HALF_2ND_END);
                detectedDateLabelUpdate();
            }

        });
        // 直近30日間
        auditLogTerm30days = new Button(auditLogTermGrp, SWT.RADIO);
        auditLogTerm30days.setText("直近30日間");
        auditLogCreatedRadios.add(auditLogTerm30days);
        auditLogTerm30days.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                frCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.BEFORE_30_DAYS);
                toCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.TODAY);
                detectedDateLabelUpdate();
            }
        });
        // 昨日
        auditLogTermYesterday = new Button(auditLogTermGrp, SWT.RADIO);
        auditLogTermYesterday.setText("昨日");
        auditLogCreatedRadios.add(auditLogTermYesterday);
        auditLogTermYesterday.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                frCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.YESTERDAY);
                toCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.YESTERDAY);
                detectedDateLabelUpdate();
            }
        });
        // 今日
        auditLogTermToday = new Button(auditLogTermGrp, SWT.RADIO);
        auditLogTermToday.setText("今日");
        auditLogCreatedRadios.add(auditLogTermToday);
        auditLogTermToday.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                frCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.TODAY);
                toCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.TODAY);
                detectedDateLabelUpdate();
            }
        });
        // 先週
        auditLogTermLastWeek = new Button(auditLogTermGrp, SWT.RADIO);
        auditLogTermLastWeek.setText("先週");
        auditLogCreatedRadios.add(auditLogTermLastWeek);
        auditLogTermLastWeek.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                frCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.LAST_WEEK_START);
                toCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.LAST_WEEK_END);
                detectedDateLabelUpdate();
            }
        });
        // 今週
        auditLogTermThisWeek = new Button(auditLogTermGrp, SWT.RADIO);
        auditLogTermThisWeek.setText("今週");
        auditLogCreatedRadios.add(auditLogTermThisWeek);
        auditLogTermThisWeek.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                frCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.THIS_WEEK_START);
                toCreatedDate = auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.THIS_WEEK_END);
                detectedDateLabelUpdate();
            }
        });
        // 任意機関
        auditLogTermPeriod = new Button(auditLogTermGrp, SWT.RADIO);
        auditLogTermPeriod.setText("任意");
        auditLogCreatedRadios.add(auditLogTermPeriod);
        auditLogTermPeriod.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });
        auditLogCreatedFilterTxt = new Text(auditLogTermGrp, SWT.BORDER);
        auditLogCreatedFilterTxt.setText("");
        auditLogCreatedFilterTxt.setEditable(false);
        auditLogCreatedFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        auditLogCreatedFilterTxt.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event e) {
                if (!auditLogTermPeriod.getSelection()) {
                    return;
                }
                FilterLastDetectedDialog filterDialog = new FilterLastDetectedDialog(shell, frCreatedDate, toCreatedDate);
                int result = filterDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    auditLogLoadBtn.setFocus();
                    return;
                }
                frCreatedDate = filterDialog.getFrDate();
                toCreatedDate = filterDialog.getToDate();
                detectedDateLabelUpdate();
                if (!auditLogCreatedFilterTxt.getText().isEmpty()) {
                    for (Button rdo : auditLogCreatedRadios) {
                        rdo.setSelection(false);
                    }
                    auditLogTermPeriod.setSelection(true);
                }
                auditLogLoadBtn.setFocus();
            }
        });
        for (Button termBtn : this.auditLogCreatedRadios) {
            updateProtectOption();
            termBtn.setSelection(false);
            if (this.auditLogCreatedRadios.indexOf(termBtn) == this.ps.getInt(PreferenceConstants.AUDITLOG_CREATED_DATE_FILTER)) {
                termBtn.setSelection(true);
                Event event = new Event();
                event.widget = termBtn;
                event.type = SWT.Selection;
                termBtn.notifyListeners(SWT.Selection, event);
            }
        }
        detectedDateLabelUpdate();

        auditLogLoadBtn = new Button(auditLogListGrp, SWT.PUSH);
        GridData auditLogLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        auditLogLoadBtnGrDt.horizontalSpan = 3;
        auditLogLoadBtnGrDt.heightHint = 50;
        auditLogLoadBtn.setLayoutData(auditLogLoadBtnGrDt);
        auditLogLoadBtn.setText("取得");
        auditLogLoadBtn.setToolTipText("監査ログ一覧を取得します。");
        auditLogLoadBtn.setFont(new Font(display, "ＭＳ ゴシック", 20, SWT.NORMAL));
        auditLogLoadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                filteredAuditLogs.clear();
                auditLogTable.clearAll();
                auditLogTable.removeAll();
                Date[] frToDate = getFrToDetectedDate();
                if (frToDate.length != 2) {
                    MessageDialog.openError(shell, "監査ログ一覧の取得", "取得期間を設定してください。");
                    return;
                }
                AuditLogsGetWithProgress progress = new AuditLogsGetWithProgress(shell, ps, getValidOrganizations(), frToDate[0], frToDate[1]);
                ProgressMonitorDialog progDialog = new AuditLogGetProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                    isSortDesc = true;
                    auditLogs = progress.getAllAuditLogs();
                    Collections.sort(auditLogs, new Comparator<AuditLog>() {
                        @Override
                        public int compare(AuditLog e1, AuditLog e2) {
                            return e2.getDate().compareTo(e1.getDate());
                        }
                    });
                    filteredAuditLogs.addAll(auditLogs);
                    for (AuditLog auditLog : auditLogs) {
                        addColToAuditLogTable(auditLog, -1);
                    }
                    auditLogFilterMap = progress.getFilterMap();
                    auditLogCount.setText(String.format("%d/%d", filteredAuditLogs.size(), auditLogs.size()));
                    List<Organization> errorOrgs = progress.getErrorOrgs();
                    if (!errorOrgs.isEmpty()) {
                        String errorOrgsStr = errorOrgs.stream().map(org -> org.getName()).collect(Collectors.joining("\r\n", "- ", ""));
                        MessageDialog.openWarning(shell, "監査ログ一覧の取得", String.format("監査ログを取得しましたが、一部の組織では監査ログを取得できていません。権限やグループの設定などご確認ください。\r\n%s", errorOrgsStr));
                    } else {
                        MessageDialog.openInformation(shell, "監査ログ一覧の取得", "監査ログを取得しました。");
                    }
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    logger.error(trace);
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "監査ログ一覧の取得", String.format("TeamServerからエラーが返されました。\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "監査ログ一覧の取得", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", errorMsg));
                    } else {
                        MessageDialog.openError(shell, "監査ログ一覧の取得", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", errorMsg));
                    }
                } catch (InterruptedException e) {
                    MessageDialog.openInformation(shell, "監査ログ一覧の取得", e.getMessage());
                }
            }
        });

        this.auditLogCount = new Label(auditLogListGrp, SWT.RIGHT);
        GridData auditLogCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        auditLogCountGrDt.minimumHeight = 12;
        auditLogCountGrDt.minimumWidth = 30;
        auditLogCountGrDt.heightHint = 12;
        auditLogCountGrDt.widthHint = 30;
        this.auditLogCount.setLayoutData(auditLogCountGrDt);
        this.auditLogCount.setFont(new Font(display, "ＭＳ ゴシック", 10, SWT.NORMAL));
        this.auditLogCount.setText("0/0");

        auditLogTable = new Table(auditLogListGrp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        tableGrDt.horizontalSpan = 3;
        auditLogTable.setLayoutData(tableGrDt);
        auditLogTable.setLinesVisible(true);
        auditLogTable.setHeaderVisible(true);
        Menu menuTable = new Menu(auditLogTable);
        auditLogTable.setMenu(menuTable);

        MenuItem miExp = new MenuItem(menuTable, SWT.NONE);
        miExp.setText("CSVエクスポート");
        miExp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] selectIndexes = auditLogTable.getSelectionIndices();
                List<List<String>> csvList = new ArrayList<List<String>>();
                String csvFileFormat = ps.getString(PreferenceConstants.CSV_FILE_FORMAT_AUDITLOG);
                if (csvFileFormat == null || csvFileFormat.isEmpty()) {
                    csvFileFormat = ps.getDefaultString(PreferenceConstants.CSV_FILE_FORMAT_AUDITLOG);
                }
                String timestamp = new SimpleDateFormat(csvFileFormat).format(new Date());
                String currentPath = System.getProperty("user.dir");
                String filePath = timestamp + ".csv";
                if (OS.isFamilyMac()) {
                    if (currentPath.contains(".app/Contents/Java")) {
                        filePath = "../../../" + timestamp + ".csv";
                    }
                }
                String csv_encoding = Main.CSV_WIN_ENCODING;
                if (OS.isFamilyMac()) {
                    csv_encoding = Main.CSV_MAC_ENCODING;
                }
                String columnJsonStr = ps.getString(PreferenceConstants.CSV_COLUMN_AUDITLOG);
                List<AuditLogCSVColumn> columnList = null;
                if (columnJsonStr.trim().length() > 0) {
                    try {
                        columnList = new Gson().fromJson(columnJsonStr, new TypeToken<List<AuditLogCSVColumn>>() {
                        }.getType());
                    } catch (JsonSyntaxException jse) {
                        MessageDialog.openError(shell, "監査ログ出力項目の読み込み", String.format("監査ログ出力項目の内容に問題があります。\r\n%s", columnJsonStr));
                        columnList = new ArrayList<AuditLogCSVColumn>();
                    }
                } else {
                    columnList = new ArrayList<AuditLogCSVColumn>();
                    for (AuditLogCSVColmunEnum colEnum : AuditLogCSVColmunEnum.sortedValues()) {
                        columnList.add(new AuditLogCSVColumn(colEnum));
                    }
                }
                for (int idx : selectIndexes) {
                    List<String> csvLineList = new ArrayList<String>();
                    AuditLog auditLog = filteredAuditLogs.get(idx);
                    for (AuditLogCSVColumn csvColumn : columnList) {
                        if (!csvColumn.isValid()) {
                            continue;
                        }
                        switch (csvColumn.getColumn()) {
                            case AUDITLOG_01:
                                // ==================== 01. 作成日時 ====================
                                csvLineList.add(auditLog.getDateStr());
                                break;
                            case AUDITLOG_02:
                                // ==================== 02. 組織名 ====================
                                csvLineList.add(auditLog.getOrganization().getName());
                                break;
                            case AUDITLOG_03:
                                // ==================== 03. ユーザー名 ====================
                                csvLineList.add(auditLog.getUserName());
                                break;
                            case AUDITLOG_04:
                                // ==================== 04. メッセージ ====================
                                csvLineList.add(auditLog.getMessage());
                                break;
                        }
                    }
                    csvList.add(csvLineList);
                }
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), csv_encoding))) {
                    CSVPrinter printer = CSVFormat.EXCEL.print(bw);
                    if (ps.getBoolean(PreferenceConstants.CSV_OUT_HEADER_AUDITLOG)) {
                        List<String> csvHeaderList = new ArrayList<String>();
                        for (AuditLogCSVColumn csvColumn : columnList) {
                            if (csvColumn.isValid()) {
                                csvHeaderList.add(csvColumn.getColumn().getCulumn());
                            }
                        }
                        printer.printRecord(csvHeaderList);
                    }
                    for (List<String> csvLine : csvList) {
                        printer.printRecord(csvLine);
                    }
                    MessageDialog.openInformation(shell, "監査ログ一覧のエクスポート", "csvファイルをエクスポートしました。");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        MenuItem miSelectAll = new MenuItem(menuTable, SWT.NONE);
        miSelectAll.setText("すべて選択（Ctrl + A）");
        miSelectAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                auditLogTable.selectAll();
            }
        });

        auditLogTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.stateMask == SWT.CTRL && e.keyCode == 'a') {
                    auditLogTable.selectAll();
                    e.doit = false;
                }
            }
        });

        TableColumn column0 = new TableColumn(auditLogTable, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(auditLogTable, SWT.CENTER);
        column1.setWidth(150);
        column1.setText("作成日時");
        column1.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                isSortDesc = !isSortDesc;
                auditLogTable.clearAll();
                auditLogTable.removeAll();
                if (isSortDesc) {
                    Collections.reverse(auditLogs);
                    Collections.reverse(filteredAuditLogs);
                } else {
                    Collections.sort(auditLogs, new Comparator<AuditLog>() {
                        @Override
                        public int compare(AuditLog e1, AuditLog e2) {
                            return e1.getDate().compareTo(e2.getDate());
                        }
                    });
                    Collections.sort(filteredAuditLogs, new Comparator<AuditLog>() {
                        @Override
                        public int compare(AuditLog e1, AuditLog e2) {
                            return e1.getDate().compareTo(e2.getDate());
                        }
                    });
                }
                for (AuditLog auditLog : filteredAuditLogs) {
                    addColToAuditLogTable(auditLog, -1);
                }
            }
        });
        TableColumn column2 = new TableColumn(auditLogTable, SWT.LEFT);
        column2.setWidth(200);
        column2.setText("組織名");
        TableColumn column3 = new TableColumn(auditLogTable, SWT.LEFT);
        column3.setWidth(200);
        column3.setText("ユーザー名");
        TableColumn column4 = new TableColumn(auditLogTable, SWT.LEFT);
        column4.setWidth(800);
        column4.setText("メッセージ");

        Button auditLogFilterBtn = new Button(auditLogListGrp, SWT.PUSH);
        GridData auditLogFilterBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        auditLogFilterBtnGrDt.horizontalSpan = 3;
        auditLogFilterBtn.setLayoutData(auditLogFilterBtnGrDt);
        auditLogFilterBtn.setText("フィルター");
        auditLogFilterBtn.setToolTipText("監査ログのフィルタリングを行います。");
        auditLogFilterBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (auditLogFilterMap == null) {
                    MessageDialog.openInformation(shell, "監査ログフィルター", "監査ログ一覧を読み込んでください。");
                    return;
                }
                AuditFilterDialog filterDialog = new AuditFilterDialog(shell, auditLogFilterMap);
                filterDialog.addPropertyChangeListener(shell.getMain());
                int result = filterDialog.open();
                if (IDialogConstants.OK_ID != result) {
                    return;
                }
            }
        });

        Composite bottomBtnGrp = new Composite(shell, SWT.NONE);
        GridLayout bottomBtnGrpLt = new GridLayout();
        bottomBtnGrpLt.numColumns = 1;
        bottomBtnGrpLt.makeColumnsEqualWidth = false;
        bottomBtnGrpLt.marginHeight = 0;
        bottomBtnGrp.setLayout(bottomBtnGrpLt);
        GridData bottomBtnGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        bottomBtnGrp.setLayoutData(bottomBtnGrpGrDt);

        // ========== 設定ボタン ==========
        settingBtn = new Button(bottomBtnGrp, SWT.PUSH);
        settingBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        settingBtn.setText("設定");
        settingBtn.setToolTipText("動作に必要な設定を行います。");
        settingBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                PreferenceManager mgr = new PreferenceManager();
                PreferenceNode baseNode = new PreferenceNode("base", new BasePreferencePage(shell));
                PreferenceNode connectionNode = new PreferenceNode("connection", new ConnectionPreferencePage());
                PreferenceNode otherNode = new PreferenceNode("other", new OtherPreferencePage());
                PreferenceNode csvNode = new PreferenceNode("csv", new CSVPreferencePage());
                PreferenceNode evtCsvColumnNode = new PreferenceNode("evtcsvcolumn", new AuditLogCSVColumnPreferencePage());
                mgr.addToRoot(baseNode);
                mgr.addToRoot(connectionNode);
                mgr.addToRoot(otherNode);
                mgr.addToRoot(csvNode);
                mgr.addTo(csvNode.getId(), evtCsvColumnNode);
                PreferenceNode aboutNode = new PreferenceNode("about", new AboutPage());
                mgr.addToRoot(aboutNode);
                PreferenceDialog dialog = new MyPreferenceDialog(shell, mgr);
                dialog.setPreferenceStore(ps);
                dialog.open();
                try {
                    ps.save();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        this.statusBar = new Label(shell, SWT.RIGHT);
        GridData statusBarGrDt = new GridData(GridData.FILL_HORIZONTAL);
        statusBarGrDt.minimumHeight = 11;
        statusBarGrDt.heightHint = 11;
        this.statusBar.setLayoutData(statusBarGrDt);
        this.statusBar.setFont(new Font(display, "ＭＳ ゴシック", 9, SWT.NORMAL));
        this.statusBar.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

        uiUpdate();
        int width = this.ps.getInt(PreferenceConstants.MEM_WIDTH);
        int height = this.ps.getInt(PreferenceConstants.MEM_HEIGHT);
        if (width > 0 && height > 0) {
            shell.setSize(width, height);
        } else {
            shell.setSize(MINIMUM_SIZE_WIDTH, MINIMUM_SIZE_HEIGHT);
            // shell.pack();
        }
        shell.open();
        try {
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            String trace = stringWriter.toString();
            logger.error(trace);
        }
        display.dispose();
    }

    private void detectedDateLabelUpdate() {
        if (frCreatedDate != null && toCreatedDate != null) {
            auditLogCreatedFilterTxt.setText(String.format("%s ～ %s", sdf.format(frCreatedDate), sdf.format(toCreatedDate)));
        } else if (frCreatedDate != null) {
            auditLogCreatedFilterTxt.setText(String.format("%s ～", sdf.format(frCreatedDate)));
        } else if (toCreatedDate != null) {
            auditLogCreatedFilterTxt.setText(String.format("～ %s", sdf.format(toCreatedDate)));
        } else {
            auditLogCreatedFilterTxt.setText("");
        }
    }

    private void addColToAuditLogTable(AuditLog audit, int index) {
        if (audit == null) {
            return;
        }
        TableItem item = null;
        if (index > 0) {
            item = new TableItem(auditLogTable, SWT.CENTER, index);
        } else {
            item = new TableItem(auditLogTable, SWT.CENTER);
        }
        item.setText(1, audit.getDateStr());
        item.setText(2, audit.getOrganization().getName());
        item.setText(3, audit.getUserName());
        item.setText(4, audit.getMessage());
    }

    private void uiReset() {
    }

    private void uiUpdate() {
    }

    public PreferenceStore getPreferenceStore() {
        return ps;
    }

    public Organization getValidOrganization() {
        String orgJsonStr = ps.getString(PreferenceConstants.TARGET_ORGS);
        if (orgJsonStr.trim().length() > 0) {
            try {
                List<Organization> orgList = new Gson().fromJson(orgJsonStr, new TypeToken<List<Organization>>() {
                }.getType());
                for (Organization org : orgList) {
                    if (org != null && org.isValid()) {
                        return org;
                    }
                }
            } catch (JsonSyntaxException e) {
                return null;
            }
        }
        return null;
    }

    public List<Organization> getValidOrganizations() {
        List<Organization> orgs = new ArrayList<Organization>();
        String orgJsonStr = ps.getString(PreferenceConstants.TARGET_ORGS);
        if (orgJsonStr.trim().length() > 0) {
            try {
                List<Organization> orgList = new Gson().fromJson(orgJsonStr, new TypeToken<List<Organization>>() {
                }.getType());
                for (Organization org : orgList) {
                    if (org != null && org.isValid()) {
                        orgs.add(org);
                    }
                }
            } catch (JsonSyntaxException e) {
                return orgs;
            }
        }
        return orgs;
    }

    private void updateProtectOption() {
        this.auditLogCreatedFilterMap = getAuditLogCreatedDateMap();
        auditLogTermToday.setToolTipText(sdf.format(this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.TODAY)));
        auditLogTermYesterday.setToolTipText(sdf.format(this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.YESTERDAY)));
        auditLogTerm30days.setToolTipText(String.format("%s ～ %s", sdf.format(this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.BEFORE_30_DAYS)),
                sdf.format(this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.TODAY))));
        auditLogTermLastWeek.setToolTipText(String.format("%s ～ %s", sdf.format(this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.LAST_WEEK_START)),
                sdf.format(this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.LAST_WEEK_END))));
        auditLogTermThisWeek.setToolTipText(String.format("%s ～ %s", sdf.format(this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.THIS_WEEK_START)),
                sdf.format(this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.THIS_WEEK_END))));
        auditLogTermHalf1st.setToolTipText(String.format("%s ～ %s", sdf.format(this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.HALF_1ST_START)),
                sdf.format(this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.HALF_1ST_END))));
        auditLogTermHalf2nd.setToolTipText(String.format("%s ～ %s", sdf.format(this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.HALF_2ND_START)),
                sdf.format(auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.HALF_2ND_END))));
    }

    private Date[] getFrToDetectedDate() {
        int idx = -1;
        for (Button termBtn : this.auditLogCreatedRadios) {
            if (termBtn.getSelection()) {
                idx = auditLogCreatedRadios.indexOf(termBtn);
                break;
            }
        }
        if (idx < 0) {
            idx = 0;
        }
        Date frDate = null;
        Date toDate = null;
        switch (idx) {
            case 0: // 上半期
                frDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.HALF_1ST_START);
                toDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.HALF_1ST_END);
                break;
            case 1: // 下半期
                frDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.HALF_2ND_START);
                toDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.HALF_2ND_END);
                break;
            case 2: // 30days
                frDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.BEFORE_30_DAYS);
                toDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.TODAY);
                break;
            case 3: // Yesterday
                frDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.YESTERDAY);
                toDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.YESTERDAY);
                break;
            case 4: // Today
                frDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.TODAY);
                toDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.TODAY);
                break;
            case 5: // LastWeek
                frDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.LAST_WEEK_START);
                toDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.LAST_WEEK_END);
                break;
            case 6: // ThisWeek
                frDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.THIS_WEEK_START);
                toDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.THIS_WEEK_END);
                break;
            case 7: // Specify
                if (frCreatedDate == null || toCreatedDate == null) {
                    return new Date[] {};
                }
                return new Date[] { frCreatedDate, toCreatedDate };
            default:
                frDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.BEFORE_30_DAYS);
                toDate = this.auditLogCreatedFilterMap.get(AuditLogCreatedDateFilterEnum.TODAY);
        }
        // Date frDate = Date.from(frLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        // Calendar cal = Calendar.getInstance();
        // cal.set(toLocalDate.getYear(), toLocalDate.getMonthValue() - 1, toLocalDate.getDayOfMonth(), 23, 59, 59);
        // Date toDate = cal.getTime();
        return new Date[] { frDate, toDate };
    }

    public Map<AuditLogCreatedDateFilterEnum, LocalDate> getAuditLogCreatedDateMapOld() {
        Map<AuditLogCreatedDateFilterEnum, LocalDate> map = new HashMap<AuditLogCreatedDateFilterEnum, LocalDate>();
        LocalDate today = LocalDate.now();

        map.put(AuditLogCreatedDateFilterEnum.TODAY, today);
        map.put(AuditLogCreatedDateFilterEnum.YESTERDAY, today.minusDays(1));
        map.put(AuditLogCreatedDateFilterEnum.BEFORE_30_DAYS, today.minusDays(30));
        LocalDate lastWeekStart = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
        lastWeekStart = lastWeekStart.minusDays(7 - ps.getInt(PreferenceConstants.START_WEEKDAY));
        if (lastWeekStart.plusDays(7).isAfter(today)) {
            lastWeekStart = lastWeekStart.minusDays(7);
        }
        map.put(AuditLogCreatedDateFilterEnum.LAST_WEEK_START, lastWeekStart);
        map.put(AuditLogCreatedDateFilterEnum.LAST_WEEK_END, lastWeekStart.plusDays(6));
        map.put(AuditLogCreatedDateFilterEnum.THIS_WEEK_START, lastWeekStart.plusDays(7));
        map.put(AuditLogCreatedDateFilterEnum.THIS_WEEK_END, lastWeekStart.plusDays(13));

        int termStartMonth = IntStream.range(0, OtherPreferencePage.MONTHS.length)
                .filter(i -> ps.getString(PreferenceConstants.TERM_START_MONTH).equals(OtherPreferencePage.MONTHS[i])).findFirst().orElse(-1);
        int half_1st_month_s = ++termStartMonth;
        int thisYear = today.getYear();
        int thisMonth = today.getMonthValue();
        // half 1st start
        LocalDate half_1st_month_s_date = null;
        // if (half_1st_month_s + 5 < thisMonth) { // 元の仕様の場合はこのコメント解除
        half_1st_month_s_date = LocalDate.of(thisYear, half_1st_month_s, 1);
        // } else { // 元の仕様の場合はこのコメント解除
        // half_1st_month_s_date = LocalDate.of(thisYear - 1, half_1st_month_s, 1); // 元の仕様の場合はこのコメント解除
        // } // 元の仕様の場合はこのコメント解除
        map.put(AuditLogCreatedDateFilterEnum.HALF_1ST_START, half_1st_month_s_date);
        // half 1st end
        // LocalDate half_1st_month_e_date = half_1st_month_s_date.plusMonths(6).minusDays(1);
        map.put(AuditLogCreatedDateFilterEnum.HALF_1ST_END, half_1st_month_s_date.plusMonths(6).minusDays(1));

        // half 2nd start
        LocalDate half_2nd_month_s_date = half_1st_month_s_date.plusMonths(6);
        // half 2nd end
        LocalDate half_2nd_month_e_date = half_2nd_month_s_date.plusMonths(6).minusDays(1);
        int todayNum = Integer.valueOf(today.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        int termEndNum = Integer.valueOf(half_2nd_month_e_date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        // if (todayNum < termEndNum) { // 元の仕様の場合はこのコメント解除
        // half_2nd_month_s_date = half_2nd_month_s_date.minusYears(1); // 元の仕様の場合はこのコメント解除
        // half_2nd_month_e_date = half_2nd_month_e_date.minusYears(1); // 元の仕様の場合はこのコメント解除
        // } // 元の仕様の場合はこのコメント解除
        map.put(AuditLogCreatedDateFilterEnum.HALF_2ND_START, half_2nd_month_s_date);
        map.put(AuditLogCreatedDateFilterEnum.HALF_2ND_END, half_2nd_month_e_date);
        return map;
    }

    public Map<AuditLogCreatedDateFilterEnum, Date> getAuditLogCreatedDateMap() {
        Map<AuditLogCreatedDateFilterEnum, Date> map = new HashMap<AuditLogCreatedDateFilterEnum, Date>();
        LocalDate today = LocalDate.now();

        map.put(AuditLogCreatedDateFilterEnum.TODAY, Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        map.put(AuditLogCreatedDateFilterEnum.YESTERDAY, Date.from(today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        map.put(AuditLogCreatedDateFilterEnum.BEFORE_30_DAYS, Date.from(today.minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        LocalDate lastWeekStart = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
        lastWeekStart = lastWeekStart.minusDays(7 - ps.getInt(PreferenceConstants.START_WEEKDAY));
        if (lastWeekStart.plusDays(7).isAfter(today)) {
            lastWeekStart = lastWeekStart.minusDays(7);
        }
        map.put(AuditLogCreatedDateFilterEnum.LAST_WEEK_START, Date.from(lastWeekStart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        map.put(AuditLogCreatedDateFilterEnum.LAST_WEEK_END, Date.from(lastWeekStart.plusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        map.put(AuditLogCreatedDateFilterEnum.THIS_WEEK_START, Date.from(lastWeekStart.plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        map.put(AuditLogCreatedDateFilterEnum.THIS_WEEK_END, Date.from(lastWeekStart.plusDays(13).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        int termStartMonth = IntStream.range(0, OtherPreferencePage.MONTHS.length)
                .filter(i -> ps.getString(PreferenceConstants.TERM_START_MONTH).equals(OtherPreferencePage.MONTHS[i])).findFirst().orElse(-1);
        int half_1st_month_s = ++termStartMonth;
        int thisYear = today.getYear();
        // int thisMonth = today.getMonthValue(); // 元の仕様の場合はこのコメント解除
        // half 1st start
        LocalDate half_1st_month_s_date = null;
        // if (half_1st_month_s + 5 < thisMonth) { // 元の仕様の場合はこのコメント解除
        half_1st_month_s_date = LocalDate.of(thisYear, half_1st_month_s, 1);
        // } else { // 元の仕様の場合はこのコメント解除
        // half_1st_month_s_date = LocalDate.of(thisYear - 1, half_1st_month_s, 1); // 元の仕様の場合はこのコメント解除
        // } // 元の仕様の場合はこのコメント解除
        map.put(AuditLogCreatedDateFilterEnum.HALF_1ST_START, Date.from(half_1st_month_s_date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        // half 1st end
        // LocalDate half_1st_month_e_date = half_1st_month_s_date.plusMonths(6).minusDays(1);
        map.put(AuditLogCreatedDateFilterEnum.HALF_1ST_END, Date.from(half_1st_month_s_date.plusMonths(6).minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        // half 2nd start
        LocalDate half_2nd_month_s_date = half_1st_month_s_date.plusMonths(6);
        // half 2nd end
        LocalDate half_2nd_month_e_date = half_2nd_month_s_date.plusMonths(6).minusDays(1);
        // int todayNum = Integer.valueOf(today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))); // 元の仕様の場合はこのコメント解除
        // int termEndNum = Integer.valueOf(half_2nd_month_e_date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))); // 元の仕様の場合はこのコメント解除
        // if (todayNum < termEndNum) { // 元の仕様の場合はこのコメント解除
        // half_2nd_month_s_date = half_2nd_month_s_date.minusYears(1); // 元の仕様の場合はこのコメント解除
        // half_2nd_month_e_date = half_2nd_month_e_date.minusYears(1); // 元の仕様の場合はこのコメント解除
        // } // 元の仕様の場合はこのコメント解除
        map.put(AuditLogCreatedDateFilterEnum.HALF_2ND_START, Date.from(half_2nd_month_s_date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        map.put(AuditLogCreatedDateFilterEnum.HALF_2ND_END, Date.from(half_2nd_month_e_date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        return map;
    }

    public void setWindowTitle() {
        String text = null;
        List<Organization> validOrgs = getValidOrganizations();
        if (!validOrgs.isEmpty()) {
            List<String> orgNameList = new ArrayList<String>();
            for (Organization validOrg : validOrgs) {
                orgNameList.add(validOrg.getName());
            }
            text = String.join(", ", orgNameList);
        }
        boolean isSuperAdmin = ps.getBoolean(PreferenceConstants.IS_SUPERADMIN);
        if (isSuperAdmin) {
            this.shell.setText(String.format(WINDOW_TITLE, "SuperAdmin"));
        } else {
            if (text == null || text.isEmpty()) {
                this.shell.setText(String.format(WINDOW_TITLE, "組織未設定"));
            } else {
                this.shell.setText(String.format(WINDOW_TITLE, text));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("auditFilter".equals(event.getPropertyName())) {
            Map<FilterEnum, Set<Filter>> filterMap = (Map<FilterEnum, Set<Filter>>) event.getNewValue();
            auditLogTable.clearAll();
            auditLogTable.removeAll();
            filteredAuditLogs.clear();
            if (isSortDesc) {
                Collections.reverse(auditLogs);
            } else {
                Collections.sort(auditLogs, new Comparator<AuditLog>() {
                    @Override
                    public int compare(AuditLog e1, AuditLog e2) {
                        return e1.getDate().compareTo(e2.getDate());
                    }
                });
            }
            // メッセージ
            String keywordInclude = "";
            Set<Filter> msgIncludeSet = filterMap.get(FilterEnum.MESSAGE_INCLUDE);
            if (!msgIncludeSet.isEmpty()) {
                keywordInclude = msgIncludeSet.iterator().next().getLabel();
            }
            String keywordExclude = "";
            Set<Filter> msgExcludeSet = filterMap.get(FilterEnum.MESSAGE_EXCLUDE);
            if (!msgExcludeSet.isEmpty()) {
                keywordExclude = msgExcludeSet.iterator().next().getLabel();
            }
            for (AuditLog auditLog : auditLogs) {
                boolean lostFlg = false;
                // 組織名
                for (Filter filter : filterMap.get(FilterEnum.ORG_NAME)) {
                    if (auditLog.isSuperAdmin()) {
                        if (filter.getLabel().equals("SuperAdmin")) {
                            if (!filter.isValid()) {
                                lostFlg |= true;
                            }
                        }
                    } else {
                        if (auditLog.getOrganization().getName().equals(filter.getLabel())) {
                            if (!filter.isValid()) {
                                lostFlg |= true;
                            }
                        }
                    }
                }
                // ユーザー名
                for (Filter filter : filterMap.get(FilterEnum.USER_NAME)) {
                    if (auditLog.getUserName().equals(filter.getLabel())) {
                        if (!filter.isValid()) {
                            lostFlg |= true;
                        }
                    }
                }
                // メッセージ含む
                if (!StringUtils.containsIgnoreCase(auditLog.getMessage(), keywordInclude)) {
                    lostFlg |= true;
                }
                // メッセージ含まない
                if (!keywordExclude.isEmpty() && StringUtils.containsIgnoreCase(auditLog.getMessage(), keywordExclude)) {
                    lostFlg |= true;
                }
                if (!lostFlg) {
                    addColToAuditLogTable(auditLog, -1);
                    filteredAuditLogs.add(auditLog);
                }
            }
            auditLogCount.setText(String.format("%d/%d", filteredAuditLogs.size(), auditLogs.size()));
        } else if ("tsv".equals(event.getPropertyName())) {
            System.out.println("tsv main");
        }

    }

    /**
     * @param listener
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    /**
     * @param listener
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }
}
