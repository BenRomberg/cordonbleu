package com.benromberg.cordonbleu.data.model;

import static java.util.Arrays.asList;
import com.benromberg.cordonbleu.util.ClockService;

import java.time.LocalDateTime;
import java.util.List;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.CommitRepository;

public interface CommitFixture extends RepositoryFixture {
    String COMMIT_HASH = "commit-hash";
    CommitId COMMIT_ID = new CommitId(COMMIT_HASH, TEAM);
    String COMMIT_AUTHOR_EMAIL = "author@email.com";
    String COMMIT_AUTHOR_NAME = "author-name";
    LocalDateTime COMMIT_CREATED = ClockService.now();
    String COMMIT_MESSAGE = "commit message";
    String COMMIT_BRANCH = "commit-branch";
    List<String> COMMIT_BRANCHES = asList(COMMIT_BRANCH);
    CommitRepository COMMIT_REPOSITORY = new CommitRepository(REPOSITORY, COMMIT_BRANCHES);
    CommitAuthor COMMIT_AUTHOR = new CommitAuthor(COMMIT_AUTHOR_NAME, COMMIT_AUTHOR_EMAIL);
    Commit COMMIT = new CommitBuilder().build();

    default CommitBuilder commit() {
        return new CommitBuilder();
    }

    static class CommitBuilder {
        private CommitId id = COMMIT_ID;
        private List<CommitRepository> repositories = asList(COMMIT_REPOSITORY);
        private CommitAuthor author = COMMIT_AUTHOR;
        private LocalDateTime created = COMMIT_CREATED;
        private String message = COMMIT_MESSAGE;

        public CommitBuilder id(CommitId id) {
            this.id = id;
            return this;
        }

        public CommitBuilder id(String hash) {
            this.id = new CommitId(hash, TEAM);
            return this;
        }

        public CommitBuilder repositories(CommitRepository... repositories) {
            this.repositories = asList(repositories);
            return this;
        }

        public CommitBuilder author(CommitAuthor author) {
            this.author = author;
            return this;
        }

        public CommitBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public CommitBuilder message(String message) {
            this.message = message;
            return this;
        }

        public Commit build() {
            return new Commit(this.id, this.repositories, this.author, this.created, this.message);
        }
    }
}
