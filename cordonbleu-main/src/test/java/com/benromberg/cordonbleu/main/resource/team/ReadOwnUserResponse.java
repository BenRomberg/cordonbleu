package com.benromberg.cordonbleu.main.resource.team;

import java.util.List;

import com.benromberg.cordonbleu.main.permission.GlobalPermission;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadOwnUserResponse {
    @JsonProperty
    private final String id;

    @JsonProperty
    private final String name;

    @JsonProperty
    private final String email;

    @JsonProperty
    private final List<String> emailAliases;

    @JsonProperty
    private final boolean admin;

    @JsonProperty
    private final boolean inactive;

    @JsonProperty
    private final List<GlobalPermission> globalPermissions;

    @JsonProperty
    private final List<ReadUserTeamResponse> teams;

    @JsonCreator
    public ReadOwnUserResponse(String id, String name, String email, List<String> emailAliases, boolean admin,
            boolean inactive, List<GlobalPermission> globalPermissions, List<ReadUserTeamResponse> teams) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.emailAliases = emailAliases;
        this.admin = admin;
        this.inactive = inactive;
        this.globalPermissions = globalPermissions;
        this.teams = teams;
    }

    public List<GlobalPermission> getGlobalPermissions() {
        return globalPermissions;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public List<String> getEmailAliases() {
        return emailAliases;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isInactive() {
        return inactive;
    }

    public List<ReadUserTeamResponse> getTeams() {
        return teams;
    }
}
