package com.benromberg.cordonbleu.main.permission;

import javax.ws.rs.ForbiddenException;

public class UserManagementPermissionGuard {
    public void guardManageUsers(UserWithPermissions user) {
        if (!user.hasGlobalPermission(GlobalPermission.MANAGE_USERS)) {
            throw new ForbiddenException();
        }
    }
}
