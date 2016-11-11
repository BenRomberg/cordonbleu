import { ajaxGet } from './ajax'
import { loggedInUser } from './user'

const hasTeamPermission = function (state, permission) {
  return state.team.activeTeam.permissions.indexOf(permission) >= 0
}

const state = {
  activeTeam: null
}

const mutations = {
  SET_ACTIVE_TEAM (state, team) {
    state.activeTeam = team
  },
  UNSET_ACTIVE_TEAM (state) {
    state.activeTeam = null
  }
}

export function activeTeam (state) {
  return state.team.activeTeam
}
export function hasTeamPermissionApprove (state) {
  return hasTeamPermission(state, 'APPROVE')
}
export function hasTeamPermissionComment (state) {
  return hasTeamPermission(state, 'COMMENT')
}
export function hasTeamPermissionManage (state) {
  return hasTeamPermission(state, 'MANAGE')
}
export function teams (state) {
  const teams = []
  const user = loggedInUser(state)
  if (user) {
    teams.push(...user.teams)
  }
  if (state.team.activeTeam && !teams.some(team => team.id === state.team.activeTeam.id)) {
    teams.unshift(state.team.activeTeam)
  }
  return teams
}

export const onTeamParameterChange = function ({ dispatch }, teamParameter) {
  if (!teamParameter) {
    dispatch('UNSET_ACTIVE_TEAM')
    return
  }
  ajaxGet({ dispatch }, '/team', { name: teamParameter }, data => dispatch('SET_ACTIVE_TEAM', data), {
    404: () => dispatch('UNSET_ACTIVE_TEAM')
  })
}
export const refreshActiveTeam = function({ dispatch, state }) {
  if (state.team.activeTeam) {
    onTeamParameterChange({ dispatch }, state.team.activeTeam.name)
  }
}
export const setActiveTeam = function ({ dispatch }, team) {
  dispatch('SET_ACTIVE_TEAM', team)
}

export default {
  state, mutations
}
