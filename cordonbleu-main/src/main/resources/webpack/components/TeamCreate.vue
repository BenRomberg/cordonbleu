<style lang="sass">
</style>

<template lang="jade">
  div.inset
    form#team-form.form-horizontal(data-toggle="validator")
      team-form(:team.sync="team", :visibility="true")
      div.form-group
        div.col-sm-offset-2.col-sm-5
          button.btn.btn-primary(type="submit" @click.prevent="createTeam($event)") Create Team
</template>

<script lang="babel">
import * as Store from '../store'
var TeamFormView = require('./TeamForm.vue')

module.exports = {
  components: {
    'team-form': TeamFormView
  },
  data: function() {
    return {
      team: {
        name: '',
        flags: []
      }
    }
  },
  vuex: {
    actions: {
      ajaxPost: Store.ajaxPost, loginUser: Store.loginUser, requireLogin: Store.requireLogin, showPopover: Store.showPopover
    }
  },
  ready: function() {
    $('#team-form').validator()
  },
  methods: {
    createTeam: function(event) {
      if (this.requireLogin(event, 'create a team')) {
        ga('send', 'event', 'team', 'create', 'requireLogin')
        return
      }
      this.ajaxPost('/team', this.team, data => {
        ga('send', 'event', 'team', 'create', 'success')
        this.loginUser(data)
        this.$route.router.go({ name: 'commits', params: { teamName: this.team.name } })
      }, {
        409: () => {
          ga('send', 'event', 'team', 'create', 'duplicateName')
          this.showPopover('#team-name', {
            title: 'Duplicate name',
            content: 'A team with the same name already exists.'
          })
        }
      })
    },
  }
}
</script>
