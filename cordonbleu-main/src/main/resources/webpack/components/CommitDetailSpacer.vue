<style lang="sass">
@import "../variables";

.line-number-spacer {
  width: $lineNumberWidth * 2;
}
</style>

<template lang="jade">
  div
    template(v-if="clusters")
      code-lines(:file="file", :clusters="clusters", :from-spacer="true", :index="null")
    template(v-else)
      button.btn.btn-xs.btn-primary.fa.fa-sort.line-number-spacer(@click="fillSpacer(file, spacer)")
</template>

<script lang="babel">
import * as Store from '../store'
var CommitClusterer = require('../classes/CommitClusterer.js')

module.exports = {
  props: ['file', 'spacer'],
  data: function() {
    return {
      clusters: null
    }
  },
  vuex: {
    getters: {
      activeTeam: Store.activeTeam
    },
    actions: {
      ajaxGet: Store.ajaxGet
    }
  },
  methods: {
    fillSpacer: function(file, spacer) {
      ga('send', 'event', 'commitSpacer', 'expand')
      var spacerParameters = {
        hash: this.$route.params.commitHash,
        teamId: this.activeTeam.id,
        beforePath: file.beforePath,
        afterPath: file.afterPath,
        beginIndex: spacer.beginIndex,
        endIndex: spacer.endIndex
      }
      this.ajaxGet('/commit/detail/spacerLines', spacerParameters, data => this.clusters = new CommitClusterer().clusterCommitLines(data))
    }
  }
}
</script>
