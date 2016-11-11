import { ajaxGet } from './ajax'
import { showPopover } from './popover'
import { refreshActiveTeam } from './team'

const state = {
  loggedInUser: null
}

const mutations = {
  USER_LOGIN (state, user) {
    state.loggedInUser = user
  },
  USER_LOGOUT (state) {
    state.loggedInUser = null
  }
}

export function loggedInUser (state) {
  return state.user.loggedInUser
}
export function hasGlobalPermissionManageUsers (state) {
  if (!state.user.loggedInUser) {
    return false
  }
  return state.user.loggedInUser.globalPermissions.indexOf('MANAGE_USERS') >= 0
}

export const loginUser = function ({ dispatch, state }, user) {
  dispatch('USER_LOGIN', user)
  refreshActiveTeam({ dispatch, state })
}
export const logoutUser = function ({ dispatch }) {
  dispatch('USER_LOGOUT')
}
export const isUserLoggedIn = function ({ state }, user) {
  return state.user.loggedInUser && state.user.loggedInUser.id === user.id
}
export const restoreUserFromSession = function ({ dispatch }) {
  if (Cookies.get('session')) {
    ajaxGet({ dispatch }, '/user', null, data => dispatch('USER_LOGIN', data), {
      401: () => Cookies.remove('session')
    })
    return true
  }
  return false
}
export const requireLogin = function ({ dispatch, state }, event, reason, container) {
  if (state.user.loggedInUser) {
    return false
  }
  if (event) {
    showPopover({ dispatch, state }, event, {
      title: 'Login required',
      content: 'Please login in order to ' + reason + '.',
      container: container
    })
  }
  return true
}

export default {
  state, mutations
}
