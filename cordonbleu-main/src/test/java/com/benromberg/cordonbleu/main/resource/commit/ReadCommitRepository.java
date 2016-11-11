package com.benromberg.cordonbleu.main.resource.commit;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadCommitRepository {
    @JsonProperty
    private final String name;

    @JsonProperty
    private final List<String> branches;

    @JsonCreator
    public ReadCommitRepository(String name, List<String> branches) {
        this.name = name;
        this.branches = branches;
    }

    public String getName() {
        return name;
    }

    public List<String> getBranches() {
        return branches;
    }

}
