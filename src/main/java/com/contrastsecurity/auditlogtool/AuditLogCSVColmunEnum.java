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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.contrastsecurity.auditlogtool.model.AuditLogCSVColumn;
import com.google.gson.Gson;

public enum AuditLogCSVColmunEnum {
    AUDITLOG_01(Messages.getString("AuditLogCSVColmunEnum.created"), 1, false, null, true, ""), //$NON-NLS-1$ //$NON-NLS-2$
    AUDITLOG_02(Messages.getString("AuditLogCSVColmunEnum.organization_name"), 2, false, null, true, ""), //$NON-NLS-1$ //$NON-NLS-2$
    AUDITLOG_03(Messages.getString("AuditLogCSVColmunEnum.username"), 3, false, null, true, ""), //$NON-NLS-1$ //$NON-NLS-2$
    AUDITLOG_04(Messages.getString("AuditLogCSVColmunEnum.message"), 4, false, null, true, ""); //$NON-NLS-1$ //$NON-NLS-2$

    private String culumn;
    private int order;
    private boolean isSeparate;
    private String separate;
    private boolean isDefault;
    private String remarks;

    private AuditLogCSVColmunEnum(String culumn, int order, boolean isSeparate, String separate, boolean isDefault, String remarks) {
        this.culumn = culumn;
        this.order = order;
        this.isSeparate = isSeparate;
        this.separate = separate;
        this.isDefault = isDefault;
        this.remarks = remarks;
    }

    public String getCulumn() {
        return culumn;
    }

    public boolean isSeparate() {
        return isSeparate;
    }

    public String getSeparate() {
        return separate;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getRemarks() {
        return remarks;
    }

    public static AuditLogCSVColmunEnum[] defaultValues() {
        List<AuditLogCSVColmunEnum> list = new ArrayList<AuditLogCSVColmunEnum>();
        for (AuditLogCSVColmunEnum e : AuditLogCSVColmunEnum.sortedValues()) {
            if (e.isDefault) {
                list.add(e);
            }
        }
        return list.toArray(new AuditLogCSVColmunEnum[0]);
    }

    public static String defaultValuesStr() {
        List<AuditLogCSVColumn> list = new ArrayList<AuditLogCSVColumn>();
        for (AuditLogCSVColmunEnum e : AuditLogCSVColmunEnum.sortedValues()) {
            list.add(new AuditLogCSVColumn(e));
        }
        return new Gson().toJson(list);
    }

    public static AuditLogCSVColmunEnum getByName(String column) {
        for (AuditLogCSVColmunEnum value : AuditLogCSVColmunEnum.values()) {
            if (value.getCulumn() == column) {
                return value;
            }
        }
        return null;
    }

    public static List<AuditLogCSVColmunEnum> sortedValues() {
        List<AuditLogCSVColmunEnum> list = Arrays.asList(AuditLogCSVColmunEnum.values());
        Collections.sort(list, new Comparator<AuditLogCSVColmunEnum>() {
            @Override
            public int compare(AuditLogCSVColmunEnum e1, AuditLogCSVColmunEnum e2) {
                return Integer.valueOf(e1.order).compareTo(Integer.valueOf(e2.order));
            }
        });
        return list;
    }

}
