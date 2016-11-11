package com.benromberg.cordonbleu.data.model;

import static java.util.Arrays.asList;

import java.util.List;

import com.benromberg.cordonbleu.data.util.RandomIdGenerator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeRepositoryMetadata extends NamedEntity {
    public static final String TEAM_PROPERTY = "team";
    public static final String FLAGS_PROPERTY = "flags";

    @JsonProperty
    private final String sourceUrl;

    @JsonProperty(FLAGS_PROPERTY)
    private final List<RepositoryFlag> flags;

    @JsonProperty(TEAM_PROPERTY)
    private final Team team;

    public CodeRepositoryMetadata(String sourceUrl, @JsonProperty(NAME_PROPERTY) String name, Team team) {
        this(RandomIdGenerator.generate(), sourceUrl, name, asList(), team);
    }

    @JsonCreator
    private CodeRepositoryMetadata(String id, String sourceUrl, @JsonProperty(NAME_PROPERTY) String name,
            @JsonProperty(FLAGS_PROPERTY) List<RepositoryFlag> flags, Team team) {
        super(id, name);
        this.sourceUrl = sourceUrl;
        this.flags = flags;
        this.team = team;
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
}
