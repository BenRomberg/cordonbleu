package com.benromberg.cordonbleu.main.resource.team;

import com.benromberg.cordonbleu.data.model.TeamFlag;

import java.util.Set;

import com.benromberg.cordonbleu.main.permission.TeamPermission;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadActiveTeamResponse {
    @JsonProperty
    private final String id;

    @JsonProperty
    private final String name;

    @JsonProperty
    private final Set<TeamPermission> permissions;

    @JsonProperty
    private final Set<TeamFlag> flags;

    @JsonProperty
    private final String publicKey;

    @JsonProperty
    private final ReadFilterResponse filters;

    @JsonCreator
    public ReadActiveTeamResponse(String id, String name, Set<TeamPermission> permissions, Set<TeamFlag> flags,
            String publicKey, ReadFilterResponse filters) {
        this.id = id;
        this.name = name;
        this.permissions = permissions;
        this.flags = flags;
        this.publicKey = publicKey;
        this.filters = filters;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<TeamPermission> getPermissions() {
        return permissions;
    }

    public Set<TeamFlag> getFlags() {
        return flags;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public ReadFilterResponse getFilters() {
        return filters;
    }
}
