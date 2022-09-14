package com.contrastsecurity.auditlogtool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class AuditLogGetProgressMonitorDialog extends ProgressMonitorDialog {

    public AuditLogGetProgressMonitorDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("監査ログの取得");
    }

}
