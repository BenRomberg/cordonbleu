import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

import ajax from './store/ajax'
import keys from './store/keys'
import notification from './store/notification'
import popover from './store/popover'
import team from './store/team'
import user from './store/user'

export * from './store/ajax'
export * from './store/keys'
export * from './store/notification'
export * from './store/popover'
export * from './store/team'
export * from './store/user'

export default new Vuex.Store({
  modules: {
    ajax, keys, notification, popover, team, user
  }
})