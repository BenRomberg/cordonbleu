<style lang="sass">
#commit-list {
  float: left;
  margin-right: 4px;
}

#commit-detail {
  overflow: auto;
}
</style>

<template lang="jade">
  div
    template(v-if="activeTeam")
      div#commit-list
        commit-list
      div#commit-detail
        router-view(@update-commit="updateCommit")
</template>

<script lang="babel">
import * as Store from '../store'
var CommitListView = require('./CommitList.vue')

module.exports = {
  components: {
    'commit-list': CommitListView
  },
  vuex: {
    getters: {
      activeTeam: Store.activeTeam
    },
  },
  methods: {
    updateCommit: function(commit) {
      this.$broadcast('update-commit-broadcast', commit)
    }
  }
}
</script>
