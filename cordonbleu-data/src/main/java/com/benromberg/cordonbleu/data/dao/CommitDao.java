package com.benromberg.cordonbleu.data.dao;

import static com.benromberg.cordonbleu.data.model.Commit.AUTHOR_PROPERTY;
import static com.benromberg.cordonbleu.data.model.Commit.REPOSITORIES_PROPERTY;
import static com.benromberg.cordonbleu.data.model.CommitRepository.REPOSITORY_PROPERTY;
import static com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueValue.uniqueProperty;
import static com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueValue.uniqueValue;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.mongojack.Aggregation.Expression.path;
import com.benromberg.cordonbleu.util.CollectionUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mongojack.Aggregation;
import org.mongojack.Aggregation.Expression;
import org.mongojack.Aggregation.Group;
import org.mongojack.Aggregation.Group.Accumulator;
import org.mongojack.Aggregation.Project;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBUpdate;
import org.mongojack.DBUpdate.Builder;

import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Comment;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitApproval;
import com.benromberg.cordonbleu.data.model.CommitAuthor;
import com.benromberg.cordonbleu.data.model.CommitId;
import com.benromberg.cordonbleu.data.model.CommitRepository;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserTeam;
import com.benromberg.cordonbleu.data.util.jackson.CaseInsensitiveUniqueValue;
import com.benromberg.cordonbleu.data.util.jackson.CustomModule;
import com.mongodb.DBObject;

@Singleton
public class CommitDao extends MongoDao<CommitId, Commit> {
    public static final String COLLECTION_NAME = "commit";

    private static final String SORT_PROPERTY_HASH = ID_PROPERTY + "." + CommitId.HASH_PROPERTY;
    private static final String SORT_PROPERTY_CREATED = Commit.CREATED_PROPERTY;

    @Inject
    public CommitDao(DatabaseWithMigration db, CodeRepositoryMetadataDao repositoryDao, UserDao userDao, TeamDao teamDao) {
        super(db, CommitId.class, Commit.class, COLLECTION_NAME, createCustomModule(repositoryDao, userDao, teamDao));
        createIndex(REPOSITORIES_PROPERTY + "." + REPOSITORY_PROPERTY);
    }

    private static CustomModule createCustomModule(CodeRepositoryMetadataDao repositoryDao, UserDao userDao,
            TeamDao teamDao) {
        CustomModule customModule = new CustomModule();
        customModule.addSerializer(CodeRepositoryMetadata.class, new EntitySerializer<>());
        customModule.addDeserializer(CodeRepositoryMetadata.class, new EntityDeserializer<>(repositoryDao));
        customModule.addSerializer(User.class, new EntitySerializer<>());
        customModule.addDeserializer(User.class, new EntityDeserializer<>(userDao));
        customModule.addSerializer(Team.class, new EntitySerializer<>());
        customModule.addDeserializer(Team.class, new EntityDeserializer<>(teamDao));
        return customModule;
    }

    public List<Commit> findByRepositories(List<CodeRepositoryMetadata> repositories) {
        return findAndSort(
                DBQuery.elemMatch(Commit.REPOSITORIES_PROPERTY,
                        DBQuery.in(CommitRepository.REPOSITORY_PROPERTY, repositories))).toArray();
    }

    private DBCursor<Commit> findAndSort(Query query) {
        return find(query).sort(object(SORT_PROPERTY_CREATED, -1).append(SORT_PROPERTY_HASH, 1));
    }

    public Optional<Commit> addComment(CommitId commitId, Comment comment) {
        DBObject updateAndSort = object("$each", asList(comment)).append("$sort", object(Comment.CREATED_PROPERTY, 1));
        Builder update = DBUpdate.push(Commit.COMMENTS_PROPERTY, updateAndSort);
        return update(commitId, update);
    }

    public Optional<Commit> updateApproval(CommitId id, Optional<CommitApproval> approval) {
        return update(id, DBUpdate.set(Commit.APPROVAL_PROPERTY, approval));
    }

    public Optional<Commit> updateProposetoCollectiveReview(CommitId id, boolean value) {
        return update(id, DBUpdate.set(Commit.COLLECTIVE_REVIEW, value));
    }
    
    public Optional<Commit> updateComment(CommitId commitId, String commentId, String text) {
        Query find = DBQuery.is(ID_PROPERTY, commitId).is(Commit.COMMENTS_PROPERTY + "." + ID_PROPERTY, commentId);
        Builder update = DBUpdate.set(Commit.COMMENTS_PROPERTY + ".$." + Comment.TEXT_PROPERTY, text);
        return update(find, update);
    }

    public Optional<Commit> removeComment(CommitId commitId, String commentId) {
        Builder update = DBUpdate.pull(Commit.COMMENTS_PROPERTY, object(ID_PROPERTY, commentId));
        return update(commitId, update);
    }

    public List<CommitAuthor> findAuthors(Team team) {
        // equivalent to
        // db.commit.aggregate([
        // { $match: { "_id.team": team.getId() } },
        // { $group: { _id: "$author.email.unique", author: { $first: "$author" } } },
        // { $project: { _id: 0, name: "$author.name", email: { value: "$author.email.value", unique: "$_id" } } }
        // ])
        // unfortunately not supported by Fongo, so only tested manually
        Map<String, Accumulator> groupAccumulator = singletonMap("author", Group.first("author"));
        Map<String, Expression<?>> emailProjetion = new HashMap<>();
        emailProjetion.put("value", path("author", "email", "value"));
        emailProjetion.put("unique", path("_id"));
        List<CommitAuthor> authors = aggregate(
                Aggregation
                        .match(DBQuery.is(ID_PROPERTY + "." + CommitId.TEAM_PROPERTY, team.getId()))
                        .group(Expression.path("author", "email", "unique"), groupAccumulator)
                        .then(Project.field("name", path("author", "name"))
                                .set("email", Expression.object(emailProjetion)).excludeId()), CommitAuthor.class)
                .results();
        return CollectionUtil.sortCaseInsensitive(authors, CommitAuthor::getName);
    }

    public List<Commit> findByFilter(CommitFilter commitFilter) {
        List<String> userAndAuthorEmails = collectUniqueUserEmails(commitFilter.getUsers());
        userAndAuthorEmails.addAll(commitFilter.getAuthors().stream().map(author -> uniqueValue(author.getEmail()))
                .collect(toList()));
        Query repositoriesQuery = DBQuery.elemMatch(REPOSITORIES_PROPERTY,
                DBQuery.in(CommitRepository.REPOSITORY_PROPERTY, commitFilter.getRepositories()));
        Query usersAndAuthorsQuery = DBQuery.in(AUTHOR_PROPERTY + "." + uniqueProperty(CommitAuthor.EMAIL_PROPERTY),
                userAndAuthorEmails);
        Query query = DBQuery.and(repositoriesQuery, usersAndAuthorsQuery);
        if (commitFilter.getLastCommitHash().isPresent()) {
            Commit lastCommit = findById(new CommitId(commitFilter.getLastCommitHash().get(), commitFilter.getTeam()))
                    .get();
            query = query.and(DBQuery.or(
                    DBQuery.is(SORT_PROPERTY_CREATED, lastCommit.getCreated()).greaterThan(SORT_PROPERTY_HASH,
                            lastCommit.getId().getHash()),
                    DBQuery.lessThan(SORT_PROPERTY_CREATED, lastCommit.getCreated())));
        }
        if (!commitFilter.isApproved()) {
            query = query.and(DBQuery.is(Commit.APPROVAL_PROPERTY, convertToDbObject(Optional.empty())));
        }
        if (commitFilter.isCollectiveReview()) {
            query = query.and(DBQuery.is(Commit.COLLECTIVE_REVIEW, true));
        }
        return findAndSort(query).limit(commitFilter.getLimit()).toArray();
    }

    private List<String> collectUniqueUserEmails(List<User> users) {
        return users.stream()
                .flatMap(user -> Stream.concat(Stream.of(user.getEmail()), user.getEmailAliases().stream()))
                .map(CaseInsensitiveUniqueValue::uniqueValue).collect(toList());
    }

    public void removeOrphaned(List<CodeRepositoryMetadata> existingRepositories) {
        updateMulti(
                DBQuery.elemMatch(REPOSITORIES_PROPERTY,
                        DBQuery.notIn(CommitRepository.REPOSITORY_PROPERTY, existingRepositories)),
                DBUpdate.pull(
                        REPOSITORIES_PROPERTY,
                        object(CommitRepository.REPOSITORY_PROPERTY,
                                object("$nin", convertToDbObject(existingRepositories)))));
        remove(DBQuery.size(REPOSITORIES_PROPERTY, 0));
    }

    public List<Commit> findNotifications(User user, int limit) {
        List<String> userEmails = collectUniqueUserEmails(asList(user));
        Query withinTeamsOfUser = DBQuery.in(ID_PROPERTY + "." + CommitId.TEAM_PROPERTY,
                user.getTeams().stream().map(UserTeam::getTeam).collect(toList()));
        Query ownCommitCommented = DBQuery.in(AUTHOR_PROPERTY + "." + uniqueProperty(CommitAuthor.EMAIL_PROPERTY),
                userEmails).notEquals(Commit.COMMENTS_PROPERTY, asList());
        Query commentedOtherCommit = DBQuery.elemMatch(Commit.COMMENTS_PROPERTY,
                DBQuery.is(Comment.USER_PROPERTY, user));
        return find(withinTeamsOfUser.or(ownCommitCommented, commentedOtherCommit))
                .sort(object(Commit.COMMENTS_PROPERTY + "." + Comment.CREATED_PROPERTY, -1)).limit(limit).toArray();
    }

    public Commit insertOrUpdateRepository(Commit commit, CommitRepository repository) {
        return insertOrUpdate(commit.getId(),
                update().push(REPOSITORIES_PROPERTY, repository).setOnInsert(commit, REPOSITORIES_PROPERTY));
    }

    public Optional<Commit> updateAsRemoved(CommitId commitId) {
        return update(commitId, DBUpdate.set(Commit.REMOVED_PROPERTY, true));
    }
}
