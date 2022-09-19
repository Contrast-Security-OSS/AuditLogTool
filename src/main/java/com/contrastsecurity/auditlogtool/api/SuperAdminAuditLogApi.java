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

package com.contrastsecurity.auditlogtool.api;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.auditlogtool.json.AuditLogJson;
import com.contrastsecurity.auditlogtool.model.AuditLog;
import com.contrastsecurity.auditlogtool.model.Organization;
import com.contrastsecurity.auditlogtool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SuperAdminAuditLogApi extends AuditLogApi {

    public SuperAdminAuditLogApi(Shell shell, IPreferenceStore ps, Organization org, Date startDate, Date endDate, int offset) {
        super(shell, ps, org, startDate, endDate, offset);
    }

    @Override
    protected String getUrl() {
        int limit = this.ps.getInt(PreferenceConstants.LIMIT_AUDITLOG);
        if (this.startDate != null && this.endDate != null) {
            String fr = sdf.format(this.startDate);
            String to = sdf.format(this.endDate);
            return String.format("%s/api/ng/superadmin/security/audit-logs?startDate=%s&endDate=%s&limit=%d&offset=%d", this.contrastUrl, fr, to, limit, this.offset); //$NON-NLS-1$
        } else {
            return String.format("%s/api/ng/superadmin/security/audit-logs?limit=%d&offset=%d", this.contrastUrl, limit, this.offset); //$NON-NLS-1$
        }
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type contType = new TypeToken<AuditLogJson>() {
        }.getType();
        AuditLogJson auditJson = gson.fromJson(response, contType);
        this.totalCount = auditJson.getTotal();
        List<AuditLog> audits = auditJson.getLogs();
        for (AuditLog audit : audits) {
            audit.setSuperAdmin(true);
            // Organization
            audit.setOrganization(this.org);
            // DateTime
            LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(audit.getDate())), ZoneId.systemDefault());
            audit.setDateStr(datetimeformatter.format(ldt));
            // UserName
            Matcher m1 = userPtn1.matcher(audit.getMessage());
            Matcher m2 = userPtn2.matcher(audit.getMessage());
            Matcher m3 = userPtn3.matcher(audit.getMessage());
            Matcher m4 = userPtn4.matcher(audit.getMessage());
            Matcher m5 = userPtn5.matcher(audit.getMessage());
            Matcher m6 = userPtn6.matcher(audit.getMessage());
            Matcher m7 = userPtn7.matcher(audit.getMessage());
            if (m1.find()) {
                audit.setUserName(m1.group(1).replaceAll("'", "")); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (m2.find()) {
                audit.setUserName(m2.group(1));
            } else if (m3.find()) {
                audit.setUserName(m3.group(1));
            } else if (m4.find()) {
                audit.setUserName(m4.group(1));
            } else if (m5.find()) {
                audit.setUserName(m5.group(1));
            } else if (m6.find()) {
                audit.setUserName(m6.group(1).replaceAll("'", "")); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (m7.find()) {
                audit.setUserName(m7.group(1).replaceAll("'", "")); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                audit.setUserName("SYSTEM"); //$NON-NLS-1$
            }
        }
        return audits;
    }

}
