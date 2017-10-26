package com.benromberg.cordonbleu.data.model;

import static java.util.Arrays.asList;

import java.util.List;

import com.benromberg.cordonbleu.data.util.RandomIdGenerator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeRepositoryMetadata extends NamedEntity {
    public static final String TEAM_PROPERTY = "team";
    public static final String FLAGS_PROPERTY = "flags";
    public static final String TYPE_GIT = "git";
    public static final String TYPE_SVN = "svn";

    @JsonProperty
    private final String sourceUrl;

    @JsonProperty(required = false)
    private final String type;

    @JsonProperty(FLAGS_PROPERTY)
    private final List<RepositoryFlag> flags;

    @JsonProperty(TEAM_PROPERTY)
    private final Team team;

    public CodeRepositoryMetadata(String sourceUrl, @JsonProperty(NAME_PROPERTY) String name, Team team, String type) {
        this(RandomIdGenerator.generate(), sourceUrl, name, asList(), team, type);
    }

    @JsonCreator
    private CodeRepositoryMetadata(String id, String sourceUrl, @JsonProperty(NAME_PROPERTY) String name,
            @JsonProperty(FLAGS_PROPERTY) List<RepositoryFlag> flags, Team team, String type) {
        super(id, name);
        this.sourceUrl = sourceUrl;
        this.flags = flags;
        this.team = team;
        this.type= type;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public boolean isRemoveOnNextUpdate() {
        return flags.contains(RepositoryFlag.REMOVE_ON_NEXT_UPDATE);
    }

    public Team getTeam() {
        return team;
    }

    public String getType() {
        return type;
    }
}
