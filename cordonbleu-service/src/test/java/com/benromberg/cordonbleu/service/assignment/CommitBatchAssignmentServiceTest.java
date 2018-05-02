package com.benromberg.cordonbleu.service.assignment;

import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitFixture;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.service.assignment.CommitBatchAssignment;
import com.benromberg.cordonbleu.service.assignment.CommitBatchAssignmentService;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CommitBatchAssignmentServiceTest implements CommitFixture {

    private static final CommitAuthor COMMIT_AUTHOR_1 = new CommitAuthor("one", "one@mail.com");
    private static final CommitId COMMIT_ID_1 = new CommitId("one-hash", TEAM);
    private static final Commit COMMIT_1 = new CommitBuilder().id(COMMIT_ID_1).author(COMMIT_AUTHOR_1).build();
    private static final CommitId COMMIT_ID_11 = new CommitId("one-one-hash", TEAM);
    private static final Commit COMMIT_11 = new CommitBuilder().id(COMMIT_ID_11).author(COMMIT_AUTHOR_1).build();

    private static final CommitAuthor COMMIT_AUTHOR_2 = new CommitAuthor("two", "two@mail.com");
    private static final CommitId COMMIT_ID_2 = new CommitId("two-hash", TEAM);
    private static final Commit COMMIT_2 = new CommitBuilder().id(COMMIT_ID_2).author(COMMIT_AUTHOR_2).build();

    private static final CommitAuthor COMMIT_AUTHOR_3 = new CommitAuthor("three", "three@mail.com");
    private static final CommitId COMMIT_ID_3 = new CommitId("three-hash", TEAM);
    private static final Commit COMMIT_3 = new CommitBuilder().id(COMMIT_ID_3).author(COMMIT_AUTHOR_3).build();

    private static final User USER_1 = new User("user-one-email", "commit-one-user", "password-one");
    private static final User USER_2 = new User("user-two-email", "commit-two-user", "password-two");
    private static final User USER_3 = new User("user-three-email", "commit-three-user", "password-three");

    private final CommitBatchAssignmentService service = new CommitBatchAssignmentService();

    @Test
    public void generateBatch_ForEmptyCommitsList_ThrowsIllegalArgumentException() {
        assertThatThrownBy(
                () -> service.generateCommitBatchAssignments(Collections.emptyList(), Collections.singletonList(USER_1))).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    public void generateBatch_ForEmptyUsersList_ThrowsIllegalArgumentException() {
        assertThatThrownBy(
                () -> service.generateCommitBatchAssignments(Collections.emptyList(), Collections.singletonList(USER_1))).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    public void generateBatch_ForOneUserOneCommit_ReturnsOneCommitBatchAssignment() {
        List<CommitBatchAssignment> result = service.generateCommitBatchAssignments(Collections.singletonList(COMMIT_1),
                Collections.singletonList(USER_1));

        assertThat(result).hasOnlyOneElementSatisfying(item -> assertBatchAssignment(item, USER_1, COMMIT_AUTHOR_1, COMMIT_ID_1));
    }

    @Test
    public void generateBatch_ForOneUserMultipleCommitsOfSameAuthor_ReturnsOneCommitBatchAssignment() {
        List<CommitBatchAssignment> result = service.generateCommitBatchAssignments(Arrays.asList(COMMIT_1, COMMIT_11),
                Collections.singletonList(USER_1));

        assertThat(result).hasOnlyOneElementSatisfying(
                item -> assertBatchAssignment(item, USER_1, COMMIT_AUTHOR_1, COMMIT_ID_1, COMMIT_ID_11));
    }

    @Test
    public void generateBatch_ForOneUserMultipleCommits_ReturnsOneCommitBatchAssignmentPerAuthor() {
        List<CommitBatchAssignment> result = service.generateCommitBatchAssignments(Arrays.asList(COMMIT_1, COMMIT_11, COMMIT_2),
                Collections.singletonList(USER_1));

        assertThat(result).hasSize(2);
        assertThat(result).anySatisfy(item -> assertBatchAssignment(item, USER_1, COMMIT_AUTHOR_1, COMMIT_ID_1, COMMIT_ID_11));
        assertThat(result).anySatisfy(item -> assertBatchAssignment(item, USER_1, COMMIT_AUTHOR_2, COMMIT_ID_2));
    }

    @Test
    public void generateBatch_ForThreeUsersThreeAuthors_ReturnsThreeCommitBatchAssignments() {
        List<CommitBatchAssignment> result = service.generateCommitBatchAssignments(Arrays.asList(COMMIT_1, COMMIT_11, COMMIT_2, COMMIT_3),
                Arrays.asList(USER_1, USER_2, USER_3));

        assertThat(result).hasSize(3);
        assertThat(result).anySatisfy(item -> assertUserIs(item, USER_1));
        assertThat(result).anySatisfy(item -> assertUserIs(item, USER_2));
        assertThat(result).anySatisfy(item -> assertUserIs(item, USER_3));
        assertThat(result).anySatisfy(item -> assertCommits(item, COMMIT_AUTHOR_1, COMMIT_ID_1, COMMIT_ID_11));
        assertThat(result).anySatisfy(item -> assertCommits(item, COMMIT_AUTHOR_2, COMMIT_ID_2));
        assertThat(result).anySatisfy(item -> assertCommits(item, COMMIT_AUTHOR_3, COMMIT_ID_3));
    }

    @Test
    public void generateBatch_ForTwoUsersThreeAuthors_ReturnsTwoCommitBatchAssignments() {
        List<CommitBatchAssignment> result = service.generateCommitBatchAssignments(Arrays.asList(COMMIT_1, COMMIT_11, COMMIT_2, COMMIT_3),
                Arrays.asList(USER_1, USER_2));

        assertThat(result).hasSize(3);
        assertThat(result).anySatisfy(item -> assertUserIs(item, USER_1));
        assertThat(result).anySatisfy(item -> assertUserIs(item, USER_2));
        assertThat(result).anySatisfy(item -> assertCommits(item, COMMIT_AUTHOR_1, COMMIT_ID_1, COMMIT_ID_11));
        assertThat(result).anySatisfy(item -> assertCommits(item, COMMIT_AUTHOR_2, COMMIT_ID_2));
        assertThat(result).anySatisfy(item -> assertCommits(item, COMMIT_AUTHOR_3, COMMIT_ID_3));
    }

    private void assertBatchAssignment(CommitBatchAssignment commitBatchAssignment, User user, CommitAuthor commitAuthor,
            CommitId... commitIds) {
        assertUserIs(commitBatchAssignment, user);
        assertCommits(commitBatchAssignment, commitAuthor, commitIds);
    }

    private void assertUserIs(CommitBatchAssignment commitBatchAssignment, User user) {
        assertThat(commitBatchAssignment.getAssignee()).isEqualToComparingFieldByField(user);
    }

    private void assertCommits(CommitBatchAssignment commitBatchAssignment, CommitAuthor commitAuthor, CommitId... commitIds) {
        assertThat(commitBatchAssignment.getCommits()).extracting(Commit::getId).containsExactly(commitIds);
        assertThat(commitBatchAssignment.getCommitAuthor()).isEqualToComparingFieldByField(commitAuthor);
    }
}