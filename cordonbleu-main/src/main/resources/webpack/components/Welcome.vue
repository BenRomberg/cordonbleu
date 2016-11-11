<style lang="sass">
#team-list {
  float: left;
}
</style>

<template lang="jade">
  div#team-list.inset
    h4 Public Teams
    div.list-group
      a.list-group-item(v-for="team in publicTeams", v-link="{ name: 'commits', params: { teamName: team.name } }") {{team.name}}
</template>

<script lang="babel">
import * as Store from '../store'

module.exports = {
  data: function() {
    return {
      publicTeams: []
    }
  },
  vuex: {
    actions: {
      ajaxGet: Store.ajaxGet
    }
  },
  ready: function() {
    this.ajaxGet('/team/public', null, data => this.publicTeams = data)
  }
}
</script>
