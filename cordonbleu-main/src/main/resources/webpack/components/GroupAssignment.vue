<style>
.commit-author {
  margin-left: 10px;
}
</style>

<template lang="jade">
  div.inset
    div.well.well-sm Select a group of users to which all recent commits will be assigned. Only commits that are not assigned, not approved and created less than 15 days ago will be considered.
    form#team-form.form-horizontal(data-toggle="validator")
      div
        div.form-group.has-feedback
          label.col-sm-2.control-label(for="assignee-select") Users to assign to
          div.col-sm-5
            select#assignee-select(multiple="multiple")
            span.help-block.with-errors
      div.form-group
        div.col-sm-offset-2.col-sm-5
          button.btn.btn-primary(type="submit" @click.prevent="groupAssign()") Assign recent commits
      div(v-if="assignmentResponse")
        div(v-if="assignmentResponse.length == 0")
          hr
          p No commits were assigned.
        div(v-else)
          div(v-for="assignment in assignmentResponse")
            hr
            <span class="primary-detail">{{{assignment.assignee | toUserWithAvatar}}}</span> was assigned to recent commits of
            <span class="primary-detail commit-author">{{{assignment.commitAuthor | toCommitAuthorWithAvatar}}}</span>
            ul
              li(v-for="commit in assignment.commits") <span class="code">{{commit._id.hash}}</span>
</template>

<script lang="babel">
import * as Store from '../store'
import * as DomHelper from '../classes/DomHelper'

module.exports = {
  data: function() {
    return {
      assignmentResponse: null
    }
  },
  vuex: {
    getters: {
      activeTeam: Store.activeTeam,
      loggedInUser: Store.loggedInUser
    },
    actions: {
      ajaxPost: Store.ajaxPost
    }
  },
  ready: function() {
    this.setupAssignmentMultiselect()
  },
  methods: {
    setupAssignmentMultiselect: function() {
      $('#assignee-select').multiselect({
        maxHeight : 800,
        buttonWidth : 400,
        inheritClass : true,
        enableHTML: true,
        enableFiltering : true,
        enableCaseInsensitiveFiltering : true,
        includeSelectAllOption: true
      })

      var filterToOption = (filter, valueFunc, labelFunc, enabled) => {
        return filter.map(value => {
          return { value: valueFunc(value), label: labelFunc(value), selected: false }
        })
      }
      var userOptions = filterToOption(this.activeTeam.filters.users, user => user.id, this.$options.filters.toUserWithAvatar)
      $('#assignee-select').multiselect('dataprovider', userOptions)
    },
    groupAssign: function(event) {
      var optionToFilter = selector => ($(selector).val() || [])
      var selectedUserIds = optionToFilter("#assignee-select")

      this.ajaxPost('/groupAssignment', {
        teamId: this.activeTeam.id,
        userIds: selectedUserIds
      }, data => this.assignmentResponse = data)
    }
  }
}
</script>
