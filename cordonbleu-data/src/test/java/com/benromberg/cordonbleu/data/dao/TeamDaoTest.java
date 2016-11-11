package com.benromberg.cordonbleu.data.dao;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.benromberg.cordonbleu.data.model.TeamFixture;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityExistsException;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.TeamFlag;
import com.benromberg.cordonbleu.data.validation.ValidationFailedException;

public class TeamDaoTest implements TeamFixture {
    private static final String NEW_TEAM_NAME = "new-team-name";

    private static final String UPPERCASE_TEAM_NAME = "UpperCaseName";

    @Rule
    public DaoRule databaseRule = new DaoRule();

    private final TeamDao teamDao = databaseRule.createTeamDao();

    @Test
    public void findByName_WithWrongName_ReturnsEmpty() throws Exception {
        teamDao.insert(TEAM);
        Optional<Team> foundTeam = teamDao.findByName("wrong name");
        assertThat(foundTeam).isEmpty();
    }

    @Test
    public void findByName_WithCorrectName_ReturnsTeam() throws Exception {
        teamDao.insert(TEAM);
        Team foundTeam = teamDao.findByName(TEAM_NAME).get();
        assertThat(foundTeam.getId()).isEqualTo(TEAM_ID);
        assertThat(foundTeam.getName()).isEqualTo(TEAM_NAME);
        assertThat(foundTeam.getFlags()).isEmpty();
        assertThat(foundTeam.getKeyPair().getPrivateKey()).isEqualTo(TEAM_PRIVATE_KEY);
        assertThat(foundTeam.getKeyPair().getPublicKey()).isEqualTo(TEAM_PUBLIC_KEY);
    }

    @Test
    public void findByName_WithUppercaseName_ReturnsTeam() throws Exception {
        teamDao.insert(team().name(UPPERCASE_TEAM_NAME).build());
        Team foundTeam = teamDao.findByName(UPPERCASE_TEAM_NAME).get();
        assertThat(foundTeam.getName()).isEqualTo(UPPERCASE_TEAM_NAME);
    }

    @Test
    public void findPublic_WithPublicTeam_ReturnsTeam() throws Exception {
        teamDao.insert(TEAM);
        List<Team> foundTeams = teamDao.findPublic();
        assertThat(foundTeams).containsExactly(TEAM);
    }

    @Test
    public void findPublic_WithPrivateTeam_ReturnsEmptyList() throws Exception {
        teamDao.insert(TEAM);
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        List<Team> foundTeams = teamDao.findPublic();
        assertThat(foundTeams).isEmpty();
    }

    @Test
    public void findPublic_WithPrivateTeamHavingAdditionalFlags_ReturnsEmptyList() throws Exception {
        teamDao.insert(TEAM);
        teamDao.updateFlag(TEAM_ID, TeamFlag.PRIVATE, true);
        teamDao.updateFlag(TEAM_ID, TeamFlag.APPROVE_MEMBER_ONLY, true);
        List<Team> foundTeams = teamDao.findPublic();
        assertThat(foundTeams).isEmpty();
    }

    @Test
    public void updateTeam_WithMissingTeam_ReturnsEmpty() throws Exception {
        Optional<Team> team = teamDao.updateTeam(TEAM, NEW_TEAM_NAME, emptySet());
        assertThat(team).isEmpty();
    }

    @Test
    public void updateTeam_WithExistingTeam_UpdatesFields() throws Exception {
        teamDao.insert(TEAM);
        Team team = teamDao.updateTeam(TEAM, NEW_TEAM_NAME, EnumSet.of(TeamFlag.PRIVATE)).get();
        assertThat(team.getName()).isEqualTo(NEW_TEAM_NAME);
        assertThat(team.getFlags()).containsOnly(TeamFlag.PRIVATE);
    }

    @Test
    public void updateTeam_WithDuplicateName_ThrowsException() throws Exception {
        teamDao.insert(TEAM);
        teamDao.insert(team().name(NEW_TEAM_NAME).build());
        assertThatThrownBy(() -> teamDao.updateTeam(TEAM, NEW_TEAM_NAME, EnumSet.of(TeamFlag.PRIVATE))).isInstanceOf(
                EntityExistsException.class);
    }

    @Test
    public void updateTeam_WithInvalidName_ThrowsException() throws Exception {
        teamDao.insert(TEAM);
        assertThatThrownBy(() -> teamDao.updateTeam(TEAM, "invalid name", emptySet())).isInstanceOf(
                ValidationFailedException.class);
    }
}
