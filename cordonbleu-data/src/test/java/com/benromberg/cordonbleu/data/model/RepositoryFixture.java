package com.benromberg.cordonbleu.data.model;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Team;

public interface RepositoryFixture extends TeamFixture {
    String REPOSITORY_NAME = "repository-name";
    String REPOSITORY_URL = "repository.url";
    String REPOSITORY_TYPE = "repository.git";

    CodeRepositoryMetadata REPOSITORY = new RepositoryBuilder().build();
    String REPOSITORY_ID = REPOSITORY.getId();

    default RepositoryBuilder repository() {
        return new RepositoryBuilder();
    }

    static class RepositoryBuilder {
        private String name = REPOSITORY_NAME;
        private String sourceUrl = REPOSITORY_URL;
        private String type = REPOSITORY_TYPE;
        private Team team = TEAM;

        public RepositoryBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RepositoryBuilder sourceUrl(String sourceUrl) {
            this.sourceUrl = sourceUrl;
            return this;
        }

        public RepositoryBuilder team(Team team) {
            this.team = team;
            return this;
        }

        public CodeRepositoryMetadata build() {
            return new CodeRepositoryMetadata(sourceUrl, name, team, type);
        }
    }
}
