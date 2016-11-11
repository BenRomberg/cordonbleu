<style lang="sass">
</style>

<template lang="jade">
  div
    div.form-group.has-feedback
      label.col-sm-2.control-label(for="team-name") Name
      div.col-sm-5
        input#team-name.form-control(v-model="team.name" placeholder="Name", :pattern="sharedConfig.namePattern", :maxlength="sharedConfig.nameMaximumLength" required data-error="Team Name may contain only letters, numbers, underscores, or hyphens.")
        span.help-block.with-errors
    div.form-group
      label.col-sm-2.control-label(for="team-name") Visibility
      div.col-sm-5
        label.radio-inline
          input(type="radio", :value="false" v-model="isPrivate", :disabled="!visibility")
          | Public
        label.radio-inline
          input(type="radio", :value="true" v-model="isPrivate", :disabled="!visibility")
          | Private
    div.form-group(v-if="!isPrivate")
      label.col-sm-2.control-label(for="team-name") Permissions
      div.col-sm-5
        div.checkbox
          label
            input(type="checkbox" v-model="team.flags" value="COMMENT_MEMBER_ONLY")
            | Only Team-Members can comment
        div.checkbox
          label
            input(type="checkbox" v-model="team.flags" value="APPROVE_MEMBER_ONLY")
            | Only Team-Members can approve
</template>

<script lang="babel">
module.exports = {
  props: ['team', 'visibility'],
  computed: {
    isPrivate: {
      get: function() {
        return this.team.flags.indexOf('PRIVATE') > -1
      },
      set: function(newValue) {
        var index = this.team.flags.indexOf('PRIVATE')
        if (newValue && index < 0) {
          this.team.flags.push('PRIVATE')
        }
        if (!newValue && index > -1) {
          this.team.flags.splice(index, 1)
        }
      }
    }
  }
}
</script>
