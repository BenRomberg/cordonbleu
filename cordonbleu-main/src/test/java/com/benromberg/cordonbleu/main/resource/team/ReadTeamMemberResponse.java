package com.benromberg.cordonbleu.main.resource.team;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadTeamMemberResponse {
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
    private final boolean owner;

    @JsonCreator
    public ReadTeamMemberResponse(String id, String name, String email, List<String> emailAliases, boolean admin,
            boolean inactive, boolean owner) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.emailAliases = emailAliases;
        this.admin = admin;
        this.inactive = inactive;
        this.owner = owner;
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

    public boolean isOwner() {
        return owner;
    }
}
