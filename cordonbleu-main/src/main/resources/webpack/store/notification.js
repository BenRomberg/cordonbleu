import * as DomHelper from '../classes/DomHelper'
import { ajaxGet } from './ajax'
var EndlessScolling = require('../classes/EndlessScrolling.js')
var NOTIFICATION_LIMIT = 20

const initializeNotificationScrolling = function({ dispatch, state }) {
  const scrolling = new EndlessScolling(
    'notification-content',
    NOTIFICATION_LIMIT,
    () => state.notification.items.length,
    newLimit => updateNotifications({ dispatch, state }, false, newLimit))
  dispatch('INITIALIZE_SCROLLING', scrolling)
}

const state = {
  items: [],
  prompts: 0,
  scrolling: null
}

const mutations = {
  UPDATE_NOTIFICATIONS (state, items, prompts) {
    state.items = items
    state.prompts = prompts
  },
  INITIALIZE_SCROLLING (state, scrolling) {
    state.scrolling = scrolling
  }
}

export function notificationItems (state) {
  return state.notification.items
}
export function notificationPrompts (state) {
  return state.notification.prompts
}
export function notificationScrolling (state) {
  return state.notification.scrolling
}

export const updateNotifications = function({ dispatch, state }, hide, limit) {
  if (!state.notification.scrolling) {
    initializeNotificationScrolling({ dispatch, state })
  }
  ajaxGet({ dispatch }, '/commit/notifications', { limit: limit || state.notification.scrolling.getCurrentLimit() }, data => {
    dispatch('UPDATE_NOTIFICATIONS', data.notifications, data.totalPrompts)
    DomHelper.waitForElement('notification-content', state.notification.scrolling.scroll)
  }, {
    401: () => {}
  }, hide)
}

export default {
  state, mutations
}
