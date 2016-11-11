package com.benromberg.cordonbleu.main.permission;

public enum GlobalPermission {
    MANAGE_USERS {
        @Override
        public boolean grantTo(UserWithPermissions user) {
            return user.isAdmin();
        }
    };

    public abstract boolean grantTo(UserWithPermissions user);
}
