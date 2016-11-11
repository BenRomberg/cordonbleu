package com.benromberg.cordonbleu.main.permission;

import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.UserTeam;

public enum TeamPermission {
    VIEW {
        @Override
        public boolean grantTo(UserWithPermissions user, Team team) {
            return !team.isPrivate() || user.belongsTo(team);
        }
    },
    COMMENT {
        @Override
        public boolean grantTo(UserWithPermissions user, Team team) {
            return (!team.isPrivate() && !team.isCommentMemberOnly() && user.isKnown()) || user.belongsTo(team);
        }
    },
    APPROVE {
        @Override
        public boolean grantTo(UserWithPermissions user, Team team) {
            return (!team.isPrivate() && !team.isApproveMemberOnly() && user.isKnown()) || user.belongsTo(team);
        }
    },
    MANAGE {
        @Override
        public boolean grantTo(UserWithPermissions user, Team team) {
            return user.getTeam(team).map(UserTeam::isOwner).orElse(false);
        }
    };

    public abstract boolean grantTo(UserWithPermissions user, Team team);
}
