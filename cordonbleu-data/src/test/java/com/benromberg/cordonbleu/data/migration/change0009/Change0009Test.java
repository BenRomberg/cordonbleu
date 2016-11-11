package com.benromberg.cordonbleu.data.migration.change0009;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.migration.ChangeRule;
import com.benromberg.cordonbleu.data.migration.TestCollection;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.migration.change0009.Change0009;

public class Change0009Test {
    private static final String COMMIT_HIGHLIGHT_CACHE_COLLECTION = "commitHighlightCache";
    private static final String DEFAULT_TEAM_NAME = "default-team";
    private static final String COMMIT_HASH_1 = "commit-hash-1";
    private static final String COMMIT_HASH_2 = "commit-hash-2";
    private static final String REPOSITORY_ID_2 = "repo-id-2";
    private static final String REPOSITORY_ID_1 = "repo-id-1";
    private static final String OTHER_FIELD_1 = "other field 1";
    private static final String OTHER_FIELD_2 = "other field 2";
    private static final String REPOSITORY_COLLECTION = "codeRepositoryMetadata";
    private static final String TEAM_COLLECTION = "team";
    private static final String COMMIT_COLLECTION = "commit";

    @Rule
    public ChangeRule changeRule = new ChangeRule(Change0009.class);

    @Test
    public void withoutRepositories_NoTeamIsAdded() throws Exception {
        changeRule.runChanges();

        TestCollection<TeamAfter> collectionAfter = changeRule.getCollection(TEAM_COLLECTION, TeamAfter.class);
        List<TeamAfter> teamsAfter = collectionAfter.find().toArray();
        assertThat(teamsAfter).isEmpty();
    }

    @Test
    public void withExistingRepositories_DefaultTeamIsAdded() throws Exception {
        TestCollection<RepositoryBefore> repositoryCollectionBefore = changeRule.getCollection(REPOSITORY_COLLECTION,
                RepositoryBefore.class);
        repositoryCollectionBefore.insert(new RepositoryBefore(REPOSITORY_ID_1, OTHER_FIELD_1));
        repositoryCollectionBefore.insert(new RepositoryBefore(REPOSITORY_ID_2, OTHER_FIELD_2));

        changeRule.runChanges();

        TeamAfter team = assertDefaultTeamInitialization();
        TestCollection<RepositoryAfter> repositoryCollectionAfter = changeRule.getCollection(REPOSITORY_COLLECTION,
                RepositoryAfter.class);
        List<RepositoryAfter> repositoriesAfter = repositoryCollectionAfter.find().toArray();
        assertThat(repositoriesAfter).extracting(RepositoryAfter::getOtherField, RepositoryAfter::getTeam)
                .containsOnly(tuple(OTHER_FIELD_1, team.getId()), tuple(OTHER_FIELD_2, team.getId()));
    }

    @Test
    public void withExistingCommits_DefaultTeamIsAdded() throws Exception {
        setupRepositoryForDefaultTeamInitialization();
        TestCollection<CommitBefore> commitCollectionBefore = changeRule.getCollection(COMMIT_COLLECTION,
                CommitBefore.class);
        commitCollectionBefore.insert(new CommitBefore(COMMIT_HASH_1, OTHER_FIELD_1));
        commitCollectionBefore.insert(new CommitBefore(COMMIT_HASH_2, OTHER_FIELD_2));

        changeRule.runChanges();

        TeamAfter team = assertDefaultTeamInitialization();
        TestCollection<CommitAfter> commitCollectionAfter = changeRule.getCollection(COMMIT_COLLECTION,
                CommitAfter.class);
        List<CommitAfter> commitsAfter = commitCollectionAfter.find().toArray();
        assertThat(commitsAfter).extracting(CommitAfter::getOtherField, commit -> commit.getId().getHash(),
                commit -> commit.getId().getTeam()).containsOnly(tuple(OTHER_FIELD_1, COMMIT_HASH_1, team.getId()),
                tuple(OTHER_FIELD_2, COMMIT_HASH_2, team.getId()));
    }

    @Test
    public void withExistingCommitHighlightCaches_DefaultTeamIsAdded() throws Exception {
        setupRepositoryForDefaultTeamInitialization();
        TestCollection<CommitHighlightCacheBefore> commitHighlightCollectionBefore = changeRule.getCollection(
                COMMIT_HIGHLIGHT_CACHE_COLLECTION, CommitHighlightCacheBefore.class);
        commitHighlightCollectionBefore.insert(new CommitHighlightCacheBefore(COMMIT_HASH_1, OTHER_FIELD_1));
        commitHighlightCollectionBefore.insert(new CommitHighlightCacheBefore(COMMIT_HASH_2, OTHER_FIELD_2));

        changeRule.runChanges();

        TeamAfter team = assertDefaultTeamInitialization();
        TestCollection<CommitHighlightCacheAfter> commitHighlightCollectionAfter = changeRule.getCollection(
                COMMIT_HIGHLIGHT_CACHE_COLLECTION, CommitHighlightCacheAfter.class);
        List<CommitHighlightCacheAfter> commitHighlightsAfter = commitHighlightCollectionAfter.find().toArray();
        assertThat(commitHighlightsAfter).extracting(CommitHighlightCacheAfter::getOtherField,
                commitHighlight -> commitHighlight.getId().getHash(), commit -> commit.getId().getTeam()).containsOnly(
                tuple(OTHER_FIELD_1, COMMIT_HASH_1, team.getId()), tuple(OTHER_FIELD_2, COMMIT_HASH_2, team.getId()));
    }

    private TeamAfter assertDefaultTeamInitialization() {
        TestCollection<TeamAfter> collectionAfter = changeRule.getCollection(TEAM_COLLECTION, TeamAfter.class);
        List<TeamAfter> teamsAfter = collectionAfter.find().toArray();
        assertThat(teamsAfter).extracting(TeamAfter::getName).containsExactly(DEFAULT_TEAM_NAME);
        TeamAfter team = teamsAfter.get(0);
        return team;
    }

    private void setupRepositoryForDefaultTeamInitialization() {
        TestCollection<RepositoryBefore> repositoryCollectionBefore = changeRule.getCollection(REPOSITORY_COLLECTION,
                RepositoryBefore.class);
        repositoryCollectionBefore.insert(new RepositoryBefore(REPOSITORY_ID_1, OTHER_FIELD_1));
    }
}
