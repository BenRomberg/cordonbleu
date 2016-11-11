package com.benromberg.cordonbleu.data.dao;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import com.benromberg.cordonbleu.data.model.UserFixture;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityExistsException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserFlag;
import com.benromberg.cordonbleu.data.model.UserTeam;
import com.benromberg.cordonbleu.data.model.UserTeamFlag;
import com.benromberg.cordonbleu.data.validation.ValidationFailedException;

public class UserDaoTest implements UserFixture {
    private static final String TOO_LONG_NAME = "user-name-with-more-than-16-characters";
    private static final String OTHER_EMAIL = "other@email.com";
    private static final String ZZZ_EMAIL = "zzz@email.com";
    private static final User ZZZ_USER_WITH_NAME = new User(ZZZ_EMAIL, "zzz", "pw");
    private static final User OTHER_USER = new User(OTHER_EMAIL, "other", "pw");
    private static final String NEW_EMAIL = "new@email.com";
    private static final String NEW_NAME = "newName";
    private static final String ALIAS_EMAIL = "alias@email.com";

    @Rule
    public DaoRule databaseRule = new DaoRule().withTeam();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final UserDao dao = databaseRule.createUserDao();

    @Test
    public void insertedUser_CanBeFoundByEmail() throws Exception {
        dao.insert(USER);
        User user = dao.findByEmail(USER_EMAIL).get();
        assertThat(user.getEmail()).isEqualTo(USER_EMAIL);
        assertThat(user.getEncryptedPassword()).isEqualTo(USER_PASSWORD);
        assertThat(user.getName()).isEqualTo(USER_NAME);
        assertThat(user.getEmailAliases()).isEmpty();
    }

    @Test
    public void insertedUser_CanBeFoundByEmailOrAlias() throws Exception {
        dao.insert(USER);
        List<User> users = dao.findByEmailOrAlias(USER_EMAIL);
        assertThat(users).extracting(User::getEmail).containsExactly(USER_EMAIL);
    }

    @Test
    public void insertedUser_WithEmailAlias_CanBeFoundByEmailOrAlias() throws Exception {
        dao.insert(USER);
        dao.update(USER, USER_NAME, USER_EMAIL, asList(ALIAS_EMAIL));
        List<User> users = dao.findByEmailOrAlias(ALIAS_EMAIL);
        assertThat(users).extracting(User::getEmail).containsExactly(USER_EMAIL);
    }

    @Test
    public void insertedUser_WithEmailAlias_CanBeFoundByEmailOrAlias_CaseInsensitive() throws Exception {
        dao.insert(USER);
        dao.update(USER, USER_NAME, USER_EMAIL, asList(ALIAS_EMAIL.toUpperCase()));
        List<User> users = dao.findByEmailOrAlias(ALIAS_EMAIL);
        assertThat(users).extracting(User::getEmail).containsExactly(USER_EMAIL);
    }

    @Test
    public void insertedUser_CanBeFoundByIds() throws Exception {
        dao.insert(USER);
        List<User> users = dao.findByIds(asList(USER.getId()));
        assertThat(users).extracting(User::getEmail).containsExactly(USER_EMAIL);
    }

    @Test
    public void insertedUser_CannotHaveTooLongName() throws Exception {
        expectedException.expect(ValidationFailedException.class);
        dao.insert(user().name(TOO_LONG_NAME).build());
    }

    @Test
    public void insertedUser_CannotHaveEmptyName() throws Exception {
        expectedException.expect(ValidationFailedException.class);
        dao.insert(user().name("").build());
    }

    @Test
    public void insertedUser_CannotHaveIllegalCharacters() throws Exception {
        expectedException.expect(ValidationFailedException.class);
        dao.insert(user().name("[]").build());
    }

    @Test
    public void insertedUser_CannotHaveEmptyEmail() throws Exception {
        expectedException.expect(ValidationFailedException.class);
        dao.insert(user().email("").build());
    }

    @Test
    public void insertUser_WithDuplicateEmailAddress_IsRejected() throws Exception {
        dao.insert(USER);
        expectedException.expect(EntityExistsException.class);
        dao.insert(user().name("other-name").build());
    }

    @Test
    public void insertUser_WithDuplicateEmailAddress_CaseInsensitive_IsRejected() throws Exception {
        dao.insert(USER);
        expectedException.expect(EntityExistsException.class);
        dao.insert(user().email(USER_EMAIL.toUpperCase()).build());
    }

    @Test
    public void insertUser_WithDuplicateName_IsRejected() throws Exception {
        dao.insert(USER);
        expectedException.expect(EntityExistsException.class);
        dao.insert(user().email(OTHER_EMAIL).build());
    }

    @Test
    public void insertUser_WithDuplicateName_CaseInsensitive_IsRejected() throws Exception {
        dao.insert(USER);
        expectedException.expect(EntityExistsException.class);
        dao.insert(user().name(USER_NAME.toUpperCase()).email(OTHER_EMAIL).build());
    }

    @Test
    public void updatedUser_HasUpdatedValues() throws Exception {
        dao.insert(USER);
        User user = dao.update(USER, NEW_NAME, NEW_EMAIL, asList(ALIAS_EMAIL)).get();
        assertThat(user.getName()).isEqualTo(NEW_NAME);
        assertThat(user.getEmailAliases()).containsExactly(ALIAS_EMAIL);
    }

    @Test
    public void updatedUser_CannotHaveTooLongName() throws Exception {
        dao.insert(USER);
        expectedException.expect(ValidationFailedException.class);
        dao.update(USER, TOO_LONG_NAME, USER_EMAIL, asList(ALIAS_EMAIL));
    }

    @Test
    public void updatedUser_WithEmptyEmailAlias_IsRejected() throws Exception {
        dao.insert(USER);
        expectedException.expect(ValidationFailedException.class);
        dao.update(USER, USER_NAME, USER_EMAIL, asList(""));
    }

    @Test
    public void updateUser_WithDuplicateEmailAddress_IsRejected() throws Exception {
        dao.insert(USER);
        dao.insert(OTHER_USER);
        expectedException.expect(EntityExistsException.class);
        dao.update(OTHER_USER, OTHER_USER.getName(), USER_EMAIL, OTHER_USER.getEmailAliases());
    }

    @Test
    public void updateUser_WithDuplicateEmailAddress_CaseInsensitive_IsRejected() throws Exception {
        dao.insert(USER);
        dao.insert(OTHER_USER);
        expectedException.expect(EntityExistsException.class);
        dao.update(OTHER_USER, OTHER_USER.getName(), USER_EMAIL.toUpperCase(), OTHER_USER.getEmailAliases());
    }

    @Test
    public void updateUser_WithDuplicateName_IsRejected() throws Exception {
        dao.insert(USER);
        dao.insert(OTHER_USER);
        expectedException.expect(EntityExistsException.class);
        dao.update(OTHER_USER, USER_NAME, OTHER_USER.getEmail(), OTHER_USER.getEmailAliases());
    }

    @Test
    public void updateUser_WithDuplicateName_CaseInsensitive_IsRejected() throws Exception {
        dao.insert(USER);
        dao.insert(OTHER_USER);
        expectedException.expect(EntityExistsException.class);
        dao.update(OTHER_USER, USER_NAME.toUpperCase(), OTHER_USER.getEmail(), OTHER_USER.getEmailAliases());
    }

    @Test
    public void updateFlag_WithMissingUser_ReturnsEmptyUser() throws Exception {
        Optional<User> user = dao.updateFlag("non-existing-id", UserFlag.ADMIN, true);
        assertThat(user).isEmpty();
    }

    @Test
    public void updateFlag_WithExistingUserWithoutFlag_ReturnsUserWithFlag() throws Exception {
        dao.insert(USER);
        User user = dao.updateFlag(USER.getId(), UserFlag.ADMIN, true).get();
        assertThat(user.isAdmin()).isTrue();
    }

    @Test
    public void updateFlag_WithExistingUserWithFlag_ReturnsUserWithoutFlag() throws Exception {
        dao.insert(USER);
        dao.updateFlag(USER.getId(), UserFlag.ADMIN, true);
        User user = dao.updateFlag(USER.getId(), UserFlag.ADMIN, false).get();
        assertThat(user.isAdmin()).isFalse();
    }

    @Test
    public void updateFlag_WithExistingUserAndFlag_ReturnsUnchangedUser() throws Exception {
        dao.insert(USER);
        dao.updateFlag(USER.getId(), UserFlag.ADMIN, true);
        User user = dao.updateFlag(USER.getId(), UserFlag.ADMIN, true).get();
        assertThat(user.getFlags()).containsExactly(UserFlag.ADMIN);
    }

    @Test
    public void findByEmail_FindsUser() throws Exception {
        dao.insert(USER);
        User user = dao.findByEmail(USER_EMAIL).get();
        assertThat(user.getId()).isEqualTo(USER.getId());
    }

    @Test
    public void findByEmail_WithUppercaseEmail_FindsUser() throws Exception {
        dao.insert(USER);
        User user = dao.findByEmail(USER_EMAIL.toUpperCase()).get();
        assertThat(user.getId()).isEqualTo(USER.getId());
    }

    @Test
    public void findAllUsers_ReturnsUsersSortedByName() throws Exception {
        dao.insert(USER);
        dao.insert(OTHER_USER);
        dao.insert(ZZZ_USER_WITH_NAME);
        List<User> allUsers = dao.findAll();
        assertThat(allUsers).extracting(User::getEmail).containsExactly(OTHER_EMAIL, USER_EMAIL, ZZZ_EMAIL);
    }

    @Test
    public void findByFlag_HavingAdminFlag_ReturnsAdminUsers() throws Exception {
        dao.insert(USER);
        dao.updateFlag(USER.getId(), UserFlag.ADMIN, true);
        dao.updateFlag(USER.getId(), UserFlag.INACTIVE, true);
        dao.insert(OTHER_USER);
        List<User> adminUsers = dao.findByFlag(UserFlag.ADMIN, true);
        assertThat(adminUsers).extracting(User::getEmail).containsExactly(USER_EMAIL);
    }

    @Test
    public void findByFlag_HavingNoAdminFlag_ReturnsRegularUsers() throws Exception {
        dao.insert(USER);
        dao.updateFlag(USER.getId(), UserFlag.ADMIN, true);
        dao.updateFlag(USER.getId(), UserFlag.INACTIVE, true);
        dao.insert(OTHER_USER);
        List<User> adminUsers = dao.findByFlag(UserFlag.ADMIN, false);
        assertThat(adminUsers).extracting(User::getEmail).containsExactly(OTHER_EMAIL);
    }

    @Test
    public void findByFlag_ReturnsUsersSortedByName() throws Exception {
        dao.insert(USER);
        dao.insert(OTHER_USER);
        dao.insert(ZZZ_USER_WITH_NAME);
        List<User> allUsers = dao.findByFlag(UserFlag.ADMIN, false);
        assertThat(allUsers).extracting(User::getEmail).containsExactly(OTHER_EMAIL, USER_EMAIL, ZZZ_EMAIL);
    }

    @Test
    public void addTeam_AddsTeamToUser() throws Exception {
        User user = addUserWithTeam(USER).get();
        assertThat(user.getTeams()).extracting(UserTeam::getTeam, UserTeam::isOwner)
                .containsExactly(tuple(TEAM, false));
    }

    @Test
    public void addTeam_WithSameTeamTwice_OnlyAddsItOnce() throws Exception {
        User user = addUserWithTeam(USER).get();
        dao.addTeam(USER_ID, TEAM);
        assertThat(user.getTeams()).extracting(UserTeam::getTeam, UserTeam::isOwner)
                .containsExactly(tuple(TEAM, false));
    }

    @Test
    public void removeTeam_RemovesTeamFromUser() throws Exception {
        addUserWithTeam(USER);
        User user = dao.removeTeam(USER_ID, TEAM).get();
        assertThat(user.getTeams()).isEmpty();
    }

    @Test
    public void removeTeam_WithoutTeamOnUser_ReturnsEmpty() throws Exception {
        Optional<User> user = dao.removeTeam(USER_ID, TEAM);
        assertThat(user).isEmpty();
    }

    @Test
    public void updateTeamFlag_WithMissingUser_ReturnsEmptyUser() throws Exception {
        Optional<User> user = dao.updateTeamFlag("non-existing-id", TEAM, UserTeamFlag.OWNER, true);
        assertThat(user).isEmpty();
    }

    @Test
    public void updateTeamFlag_WithUserNotPartOfTheTeam_ReturnsEmptyUser() throws Exception {
        dao.insert(USER);
        Optional<User> user = dao.updateTeamFlag(USER_ID, TEAM, UserTeamFlag.OWNER, true);
        assertThat(user).isEmpty();
    }

    @Test
    public void updateTeamFlag_WithExistingUserWithoutFlag_ReturnsUserWithFlag() throws Exception {
        addUserWithTeam(USER);
        User user = dao.updateTeamFlag(USER_ID, TEAM, UserTeamFlag.OWNER, true).get();
        assertThat(user.getTeams()).extracting(UserTeam::isOwner).containsExactly(true);
    }

    @Test
    public void updateTeamFlag_WithExistingUserAndFlag_ReturnsUnchangedUser() throws Exception {
        addUserWithTeam(USER);
        dao.updateTeamFlag(USER_ID, TEAM, UserTeamFlag.OWNER, true);
        User user = dao.updateTeamFlag(USER_ID, TEAM, UserTeamFlag.OWNER, true).get();
        assertThat(user.getTeams().get(0).getFlags()).containsExactly(UserTeamFlag.OWNER);
    }

    @Test
    public void updateTeamFlag_WithExistingUserWithFlag_ReturnsUserWithoutFlag() throws Exception {
        addUserWithTeam(USER);
        dao.updateTeamFlag(USER_ID, TEAM, UserTeamFlag.OWNER, true);
        User user = dao.updateTeamFlag(USER_ID, TEAM, UserTeamFlag.OWNER, false).get();
        assertThat(user.getTeams()).extracting(UserTeam::isOwner).containsExactly(false);
    }

    @Test
    public void findTeamMembers_WithoutMembers_ReturnsEmptyList() throws Exception {
        List<User> members = dao.findTeamMembers(TEAM);
        assertThat(members).isEmpty();
    }

    @Test
    public void findTeamMembers_WithMember_ReturnsList() throws Exception {
        addUserWithTeam(USER);
        List<User> members = dao.findTeamMembers(TEAM);
        assertThat(members).extracting(User::getName).containsExactly(USER_NAME);
    }

    @Test
    public void findTeamMembers_WithMultipleMembers_IsSortedByName() throws Exception {
        addUserWithTeam(USER);
        addUserWithTeam(OTHER_USER);
        List<User> members = dao.findTeamMembers(TEAM);
        assertThat(members).extracting(User::getName).containsExactly(OTHER_USER.getName(), USER_NAME);
    }

    @Test
    public void findByNamePrefix_WithoutUsers_IsEmpty() throws Exception {
        List<User> members = dao.findByNamePrefix("", 10);
        assertThat(members).isEmpty();
    }

    @Test
    public void findByNamePrefix_WithUser_ReturnsUser() throws Exception {
        dao.insert(USER);
        List<User> members = dao.findByNamePrefix("", 10);
        assertThat(members).extracting(User::getName).containsExactly(USER_NAME);
    }

    @Test
    public void findByNamePrefix_WithUsers_ObeysLimitAndSortOrder() throws Exception {
        dao.insert(USER);
        dao.insert(OTHER_USER);
        List<User> members = dao.findByNamePrefix("", 1);
        assertThat(members).extracting(User::getName).containsExactly(OTHER_USER.getName());
    }

    @Test
    public void findByNamePrefix_WithUser_CanBeFoundCaseInsensitive() throws Exception {
        dao.insert(USER);
        List<User> members = dao.findByNamePrefix(USER_NAME.toUpperCase(), 10);
        assertThat(members).extracting(User::getName).containsExactly(USER_NAME);
    }

    @Test
    public void findByNamePrefix_WithUser_ButWrongPrefix_IsEmpty() throws Exception {
        dao.insert(USER);
        List<User> members = dao.findByNamePrefix("x", 10);
        assertThat(members).isEmpty();
    }

    @Test
    public void findByName_WithoutUsers_IsEmpty() throws Exception {
        Optional<User> user = dao.findByName(USER_NAME);
        assertThat(user).isEmpty();
    }

    @Test
    public void findByName_WithUser_ReturnsUser() throws Exception {
        dao.insert(USER);
        User user = dao.findByName(USER_NAME).get();
        assertThat(user.getName()).isEqualTo(USER_NAME);
    }

    @Test
    public void findByName_WithUppercaseUser_ReturnsUser() throws Exception {
        dao.insert(USER);
        User user = dao.findByName(USER_NAME.toUpperCase()).get();
        assertThat(user.getName()).isEqualTo(USER_NAME);
    }

    @Test
    public void findByNamePrefix_WithUser_ButWrongName_IsEmpty() throws Exception {
        dao.insert(USER);
        Optional<User> user = dao.findByName("wrong-name");
        assertThat(user).isEmpty();
    }

    private Optional<User> addUserWithTeam(User user) {
        dao.insert(user);
        return dao.addTeam(user.getId(), TEAM);
    }
}
