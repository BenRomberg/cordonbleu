package com.benromberg.cordonbleu.service.team;

import com.benromberg.cordonbleu.data.dao.TeamDao;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.model.Team;
import com.benromberg.cordonbleu.data.model.TeamFlag;
import com.benromberg.cordonbleu.data.model.TeamKeyPair;
import com.benromberg.cordonbleu.data.model.User;
import com.benromberg.cordonbleu.data.model.UserTeamFlag;
import com.benromberg.cordonbleu.data.util.KeyPair;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import com.benromberg.cordonbleu.service.coderepository.keypair.SshKeyPairGenerator;

public class TeamService {
    private final TeamDao dao;
    private final UserDao userDao;
    private final SshKeyPairGenerator keyPairGenerator;

    @Inject
    public TeamService(TeamDao dao, UserDao userDao, SshKeyPairGenerator keyPairGenerator) {
        this.dao = dao;
        this.userDao = userDao;
        this.keyPairGenerator = keyPairGenerator;
    }

    public List<Team> findPublicTeams() {
        return dao.findPublic();
    }

    public Optional<Team> findPublicTeamByName(String name) {
        return dao.findByName(name).filter(team -> !team.isPrivate());
    }

    public Optional<Team> findById(String teamId) {
        return dao.findById(teamId);
    }

    public User createTeam(String teamName, Set<TeamFlag> teamFlags, User owner) {
        KeyPair keyPair = keyPairGenerator.generate();
        Team team = new Team(teamName, teamFlags, new TeamKeyPair(keyPair.getPrivateKey(), keyPair.getPublicKey()));
        dao.insert(team);
        userDao.addTeam(owner.getId(), team);
        return userDao.updateTeamFlag(owner.getId(), team, UserTeamFlag.OWNER, true).get();
    }

    public List<User> findMembers(Team team) {
        return userDao.findTeamMembers(team);
    }

    public void addMember(Team team, String userName) {
        User user = userDao.findByName(userName).get();
        userDao.addTeam(user.getId(), team);
    }

    public void removeMember(Team team, String userId) {
        userDao.removeTeam(userId, team);
    }

    public void updateMemberFlag(Team team, String userId, UserTeamFlag flag, boolean flagValue) {
        userDao.updateTeamFlag(userId, team, flag, flagValue);
    }

    public Optional<Team> updateTeam(User user, Team team, String name, Set<TeamFlag> flags) {
        Optional<Team> updatedTeam = dao.updateTeam(team, name, flags);
        userDao.invalidate(user.getId());
        return updatedTeam;
    }
}
