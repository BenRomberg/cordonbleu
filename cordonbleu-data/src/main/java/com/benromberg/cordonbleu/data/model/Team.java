package com.benromberg.cordonbleu.data.model;

import java.util.Set;

import com.benromberg.cordonbleu.data.util.RandomIdGenerator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Team extends NamedEntity {
    public static final String FLAGS_PROPERTY = "flags";

    @JsonProperty(FLAGS_PROPERTY)
    private final Set<TeamFlag> flags;

    @JsonProperty
    private final TeamKeyPair keyPair;

    @JsonCreator
    private Team(String id, @JsonProperty(NAME_PROPERTY) String name,
            @JsonProperty(FLAGS_PROPERTY) Set<TeamFlag> flags, TeamKeyPair keyPair) {
        super(id, name);
        this.flags = flags;
        this.keyPair = keyPair;
    }

    public Team(String name, Set<TeamFlag> flags, TeamKeyPair keyPair) {
        this(RandomIdGenerator.generate(), name, flags, keyPair);
    }

    public boolean isPrivate() {
        return flags.contains(TeamFlag.PRIVATE);
    }

    public boolean isCommentMemberOnly() {
        return flags.contains(TeamFlag.COMMENT_MEMBER_ONLY);
    }

    public boolean isApproveMemberOnly() {
        return flags.contains(TeamFlag.APPROVE_MEMBER_ONLY);
    }

    public Set<TeamFlag> getFlags() {
        return flags;
    }

    public TeamKeyPair getKeyPair() {
        return keyPair;
    }
}
