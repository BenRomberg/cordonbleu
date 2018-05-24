package com.benromberg.cordonbleu.data.model;

import com.benromberg.cordonbleu.data.util.RandomIdGenerator;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueDeserializer;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

import static java.util.Collections.emptyList;

public class User extends NamedEntity {
    public static final String TEAMS_PROPERTY = "teams";
    public static final String FLAGS_PROPERTY = "flags";
    public static final String EMAIL_ALIASES_PROPERTY = "emailAliases";
    public static final String EMAIL_PROPERTY = "email";

    @JsonProperty(EMAIL_PROPERTY)
    @JsonSerialize(using = CaseInsensitiveUniqueSerializer.class)
    @JsonDeserialize(using = CaseInsensitiveUniqueDeserializer.class)
    private final String email;

    @JsonProperty
    private final String encryptedPassword;

    @JsonProperty(EMAIL_ALIASES_PROPERTY)
    @JsonSerialize(contentUsing = CaseInsensitiveUniqueSerializer.class)
    @JsonDeserialize(contentUsing = CaseInsensitiveUniqueDeserializer.class)
    private final List<String> emailAliases;

    @JsonProperty(FLAGS_PROPERTY)
    private final List<UserFlag> flags;

    @JsonProperty(TEAMS_PROPERTY)
    private final List<UserTeam> teams;

    @JsonCreator
    private User(String id, @JsonProperty(NAME_PROPERTY) String name, @JsonProperty(EMAIL_PROPERTY) String email, String encryptedPassword,
            @JsonProperty(EMAIL_ALIASES_PROPERTY) List<String> emailAliases, @JsonProperty(FLAGS_PROPERTY) List<UserFlag> flags,
            @JsonProperty(TEAMS_PROPERTY) List<UserTeam> teams) {
        super(id, name);
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.emailAliases = emailAliases;
        this.flags = flags;
        this.teams = teams;
    }

    public User(String email, String name, String encryptedPassword) {
        this(email, name, encryptedPassword, emptyList());
    }

    public User(String email, String name, String encryptedPassword, List<String> emailAliases) {
        this(RandomIdGenerator.generate(), name, email, encryptedPassword, emailAliases, emptyList(), emptyList());
    }

    public List<UserTeam> getTeams() {
        return teams;
    }

    public String getEmail() {
        return email;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public List<String> getEmailAliases() {
        return emailAliases;
    }

    public List<UserFlag> getFlags() {
        return flags;
    }

    public boolean isAdmin() {
        return flags.contains(UserFlag.ADMIN);
    }

    public boolean isInactive() {
        return flags.contains(UserFlag.INACTIVE);
    }
}
