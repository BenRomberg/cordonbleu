<style lang="sass">
#users-list {
  width: auto;
}
</style>

<template lang="jade">
  div.inset(v-if="loggedInUser")
    table#users-list.table.table-bordered.table-striped.table-hover
      tr
        th Name
        th Email
        th Email Aliases
        th Admin
        th Inactive
      tr(v-for="user in users")
        td {{user.name}}
        td {{user.email}}
        td: div(v-for="emailAlias in user.emailAliases") {{emailAlias}}
        td: input(type="checkbox" v-model="user.admin" @change="toggleFlag('ADMIN', user, user.admin)", :disabled="user.id === loggedInUser.id")
        td: input(type="checkbox" v-model="user.inactive" @change="toggleFlag('INACTIVE', user, user.inactive)")
</template>

<script lang="babel">
import * as Store from '../store'

module.exports = {
  data: function() {
    return {
      users: []
    }
  },
  vuex: {
    actions: {
      ajaxGet: Store.ajaxGet, ajaxPost: Store.ajaxPost
    },
    getters: {
      loggedInUser: Store.loggedInUser
    },
  },
  ready: function() {
    this.ajaxGet('/admin/user', null, data => this.users = data)
  },
  methods: {
    toggleFlag: function(flag, user, flagValue) {
      this.ajaxPost('/admin/user/updateFlag', { userId: user.id, flag: flag, flagValue: flagValue }, data => this.users = data)
    }
  }
}
</script>
