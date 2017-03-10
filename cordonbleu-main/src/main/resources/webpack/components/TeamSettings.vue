<style lang="sass">
</style>

<template lang="jade">
  div.inset
    form#team-form.form-horizontal(data-toggle="validator")
      team-form(:team.sync="team", :visibility="false")
      div.form-group
        div.col-sm-offset-2.col-sm-5
          div.btn-group(role="group")
            button.btn.btn-primary(type="submit" @click.prevent="saveTeam()") Save
            button.btn.btn-default(type="button" @click="resetTeam()") Reset
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
      team: JSON.parse(JSON.stringify(this.activeTeam))
    }
  },
  vuex: {
    getters: {
      activeTeam: Store.activeTeam
    },
    actions: {
      ajaxPost: Store.ajaxPost,
      setActiveTeam: Store.setActiveTeam,
      showPopover: Store.showPopover
    }
  },
  ready: function() {
    this.resetTeam()
  },
  methods: {
    saveTeam: function(event) {
      delete this.team.permissions
      delete this.team.publicKey
      this.ajaxPost('/team/update', {
        flags: this.team.flags,
        id: this.team.id,
        name: this.team.name
      }, data => {
        ga('send', 'event', 'team', 'update', 'success')
        this.setActiveTeam(data)
        this.$router.go({ name: 'team-settings', params: { teamName: data.name } })
      }, {
        409: () => {
          ga('send', 'event', 'team', 'update', 'duplicateName')
          this.showPopover('#team-name', {
            title: 'Duplicate name',
            content: 'A team with the same name already exists.'
          })
        }
      })
    },
    resetTeam: function() {
      $('#team-form').validator()
      this.team = JSON.parse(JSON.stringify(this.activeTeam))
    }
  }
}
</script>
