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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.auditlogtool.api.Api;
import com.contrastsecurity.auditlogtool.api.ApiKeyApi;
import com.contrastsecurity.auditlogtool.api.GroupCreateApi;
import com.contrastsecurity.auditlogtool.api.GroupDeleteApi;
import com.contrastsecurity.auditlogtool.api.GroupsApi;
import com.contrastsecurity.auditlogtool.api.OrganizationAuditLogApi;
import com.contrastsecurity.auditlogtool.api.OrganizationsApi;
import com.contrastsecurity.auditlogtool.api.SuperAdminAuditLogApi;
import com.contrastsecurity.auditlogtool.exception.ApiException;
import com.contrastsecurity.auditlogtool.exception.NonApiException;
import com.contrastsecurity.auditlogtool.model.AuditLog;
import com.contrastsecurity.auditlogtool.model.Filter;
import com.contrastsecurity.auditlogtool.model.Group;
import com.contrastsecurity.auditlogtool.model.Organization;
import com.contrastsecurity.auditlogtool.preference.PreferenceConstants;

public class AuditLogsGetWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private List<Organization> orgs;
    private Date frDetectedDate;
    private Date toDetectedDate;
    private List<AuditLog> allAuditLogs;
    private List<Organization> errorOrgs;
    private Set<Filter> organicationFilterSet = new LinkedHashSet<Filter>();
    private Set<Filter> userNameFilterSet = new LinkedHashSet<Filter>();
    private int addedGroupId;

    Logger logger = LogManager.getLogger("auditlogtool");

    public AuditLogsGetWithProgress(Shell shell, PreferenceStore ps, List<Organization> orgs, Date frDate, Date toDate) {
        this.shell = shell;
        this.ps = ps;
        this.orgs = orgs;
        this.frDetectedDate = frDate;
        this.toDetectedDate = toDate;
        this.allAuditLogs = new ArrayList<AuditLog>();
        this.errorOrgs = new ArrayList<Organization>();
        this.addedGroupId = -1;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        int sleepAudit = this.ps.getInt(PreferenceConstants.SLEEP_AUDITLOG);
        boolean isSuperAdmin = this.ps.getBoolean(PreferenceConstants.IS_SUPERADMIN);
        Organization baseOrg = new Organization();
        baseOrg.setOrganization_uuid(this.ps.getString(PreferenceConstants.ORG_ID));
        baseOrg.setApikey(this.ps.getString(PreferenceConstants.API_KEY));

        try {
            monitor.setTaskName("監査ログの取得を開始しています...");
            Thread.sleep(100);
            monitor.beginTask("監査ログの取得を開始しています...", 100);
            Thread.sleep(100);
            if (isSuperAdmin) {
                SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 30);
                sub1Monitor.beginTask("", 100);
                try {
                    SubProgressMonitor sub1_1Monitor = new SubProgressMonitor(sub1Monitor, this.ps.getBoolean(PreferenceConstants.IS_CREATEGROUP) ? 60 : 80);
                    sub1Monitor.subTask("SuperAdminの監査ログを取得...");
                    List<AuditLog> audits = new ArrayList<AuditLog>();
                    Api superAdminAuditApi = new SuperAdminAuditLogApi(this.shell, this.ps, baseOrg, frDetectedDate, toDetectedDate, 0);
                    Api organizationsApi = new OrganizationsApi(this.shell, this.ps, baseOrg);
                    Api groupsApi = new GroupsApi(this.shell, this.ps, baseOrg);
                    List<AuditLog> tmpAudits = (List<AuditLog>) superAdminAuditApi.get();
                    int totalAuditLogCount = superAdminAuditApi.getTotalCount();
                    sub1_1Monitor.beginTask("", totalAuditLogCount);
                    audits.addAll(tmpAudits);
                    sub1_1Monitor.worked(tmpAudits.size());
                    boolean auditLogIncompleteFlg = false;
                    auditLogIncompleteFlg = totalAuditLogCount > audits.size();
                    while (auditLogIncompleteFlg) {
                        Thread.sleep(sleepAudit);
                        if (monitor.isCanceled()) {
                            throw new InterruptedException("キャンセルされました。");
                        }
                        superAdminAuditApi = new SuperAdminAuditLogApi(this.shell, this.ps, baseOrg, frDetectedDate, toDetectedDate, audits.size());
                        tmpAudits = (List<AuditLog>) superAdminAuditApi.get();
                        audits.addAll(tmpAudits);
                        sub1_1Monitor.worked(tmpAudits.size());
                        auditLogIncompleteFlg = totalAuditLogCount > audits.size();
                    }
                    this.allAuditLogs.addAll(audits);
                    sub1_1Monitor.done();

                    if (monitor.isCanceled()) {
                        throw new InterruptedException("キャンセルされました。");
                    }
                    Thread.sleep(100);
                    sub1Monitor.subTask("組織一覧を取得します...");
                    SubProgressMonitor sub1_2Monitor = new SubProgressMonitor(sub1Monitor, 10);
                    sub1_2Monitor.beginTask("", 100);
                    List<Organization> organizations = (List<Organization>) organizationsApi.get();
                    sub1_2Monitor.worked(100);
                    sub1_2Monitor.done();
                    Thread.sleep(100);
                    if (this.ps.getBoolean(PreferenceConstants.IS_CREATEGROUP)) {
                        SubProgressMonitor sub1_3Monitor = new SubProgressMonitor(sub1Monitor, 20);
                        sub1Monitor.subTask("一時グループを作成しています...");
                        sub1_3Monitor.beginTask("", 100);
                        List<Group> groups = (List<Group>) groupsApi.get();
                        int groupId = -1;
                        for (Group cg : groups) {
                            if (cg.getName().equals(this.ps.getString(PreferenceConstants.GROUP_NAME))) {
                                groupId = cg.getGroup_id();
                            }
                        }
                        if (groupId > -1) {
                            sub1_3Monitor.done();
                            throw new ApiException("すでにグループが存在しています。");
                        }
                        sub1_3Monitor.worked(50);
                        Api groupCreateApi = new GroupCreateApi(this.shell, this.ps, baseOrg, organizations);
                        String rtnMsg = (String) groupCreateApi.post();
                        if (rtnMsg.equals("true")) {
                            groups = (List<Group>) groupsApi.get();
                            groupId = -1;
                            for (Group cg : groups) {
                                if (cg.getName().equals(this.ps.getString(PreferenceConstants.GROUP_NAME))) {
                                    groupId = cg.getGroup_id();
                                }
                            }
                            if (groupId > -1) {
                                this.addedGroupId = groupId;
                            }
                        }
                        sub1_3Monitor.worked(50);
                        sub1_3Monitor.done();
                    }
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("キャンセルされました。");
                    }
                    Thread.sleep(100);
                    SubProgressMonitor sub1_3Monitor = new SubProgressMonitor(sub1Monitor, 10);
                    sub1Monitor.subTask("各組織のAPI Keyを取得します...");
                    sub1_3Monitor.beginTask("", organizations.size());
                    for (Organization org : organizations) {
                        Api apiKeyApi = new ApiKeyApi(this.shell, this.ps, baseOrg, org.getOrganization_uuid());
                        org.setApikey((String) apiKeyApi.get());
                        sub1_3Monitor.worked(1);
                        Thread.sleep(300);
                    }
                    this.orgs = organizations;
                    sub1_3Monitor.done();
                    sub1Monitor.done();
                } catch (InterruptedException ie) {
                    throw ie;
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                }
            }
            Thread.sleep(500);
            if (monitor.isCanceled()) {
                throw new InterruptedException("キャンセルされました。");
            }
            SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, isSuperAdmin ? 70 : 100);
            monitor.subTask("各組織の監査ログを取得...");
            sub2Monitor.beginTask("", this.orgs.size() * 100);
            for (Organization org : this.orgs) {
                sub2Monitor.subTask(String.format("各組織の監査ログを取得...%s", org.getName()));
                try {
                    SubProgressMonitor sub2_1Monitor = new SubProgressMonitor(sub2Monitor, 100);
                    // 監査ログを読み込み
                    List<AuditLog> audits = new ArrayList<AuditLog>();
                    Api auditApi = new OrganizationAuditLogApi(this.shell, this.ps, org, frDetectedDate, toDetectedDate, 0);
                    auditApi.setIgnoreStatusCodes(new ArrayList(Arrays.asList(401, 403)));
                    Object rtnObj = auditApi.get();
                    if (rtnObj == null) {
                        errorOrgs.add(org);
                        continue;
                    }
                    List<AuditLog> tmpAudits = (List<AuditLog>) rtnObj;
                    int totalAuditLogCount = auditApi.getTotalCount();
                    sub2_1Monitor.beginTask("", totalAuditLogCount);
                    audits.addAll(tmpAudits);
                    sub2_1Monitor.worked(tmpAudits.size());
                    boolean auditLogIncompleteFlg = false;
                    auditLogIncompleteFlg = totalAuditLogCount > audits.size();
                    while (auditLogIncompleteFlg) {
                        Thread.sleep(sleepAudit);
                        if (monitor.isCanceled()) {
                            throw new InterruptedException("キャンセルされました。");
                        }
                        auditApi = new OrganizationAuditLogApi(this.shell, this.ps, org, frDetectedDate, toDetectedDate, audits.size());
                        auditApi.setIgnoreStatusCodes(new ArrayList(Arrays.asList(403)));
                        tmpAudits = (List<AuditLog>) auditApi.get();
                        audits.addAll(tmpAudits);
                        sub2_1Monitor.worked(tmpAudits.size());
                        auditLogIncompleteFlg = totalAuditLogCount > audits.size();
                    }
                    this.allAuditLogs.addAll(audits);
                    sub2Monitor.worked(1);
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    throw ie;
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                }
                Thread.sleep(500);
            }
            sub2Monitor.done();
            monitor.done();
        } catch (Exception e) {
            throw e;
        } finally {
            if (isSuperAdmin) {
                if (this.ps.getBoolean(PreferenceConstants.IS_CREATEGROUP) && this.addedGroupId > 0) {
                    Api groupDeleteApi = new GroupDeleteApi(this.shell, this.ps, baseOrg, this.addedGroupId);
                    String rtnMsg;
                    try {
                        rtnMsg = (String) groupDeleteApi.delete();
                        if (!rtnMsg.equals("true")) {
                            throw new InvocationTargetException(new NonApiException("グループの削除に失敗しました。"));
                        }
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            }
        }
    }

    public List<AuditLog> getAllAuditLogs() {
        if (!this.ps.getBoolean(PreferenceConstants.SHOW_CREATEGROUP_LOG)) {
            String groupName = this.ps.getString(PreferenceConstants.GROUP_NAME);
            List<AuditLog> hideAuditLogs = new ArrayList<AuditLog>();
            for (AuditLog auditLog : this.allAuditLogs) {
                if (!auditLog.getMessage().contains(groupName)) {
                    hideAuditLogs.add(auditLog);
                }
            }
            return hideAuditLogs;
        }
        return this.allAuditLogs;
    }

    public List<Organization> getErrorOrgs() {
        return errorOrgs;
    }

    public Map<FilterEnum, Set<Filter>> getFilterMap() {
        for (AuditLog audit : this.allAuditLogs) {
            if (audit.isSuperAdmin()) {
                organicationFilterSet.add(new Filter("SuperAdmin"));
            } else {
                organicationFilterSet.add(new Filter(audit.getOrganization().getName()));
            }
            userNameFilterSet.add(new Filter(audit.getUserName()));
        }
        Map<FilterEnum, Set<Filter>> filterMap = new HashMap<FilterEnum, Set<Filter>>();
        filterMap.put(FilterEnum.ORG_NAME, organicationFilterSet);
        filterMap.put(FilterEnum.USER_NAME, userNameFilterSet);
        return filterMap;
    }

}
