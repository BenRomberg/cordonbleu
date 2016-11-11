<style lang="sass">
.email-alias {
  padding-bottom: 5px;
}
.has-feedback .form-control-feedback {
  top: 25px;
  right: 0;
}
.help-block:empty {
  margin: 0;
}
.profile-image-18 {
  border-radius: 4px;
  width: 18px;
  height: 18px;
}
.profile-image-32 {
  border-radius: 6px;
  width: 32px;
  height: 32px;
}
</style>

<template lang="jade">
  div.inset
    div.centering-root(v-if="!user")
      div.centering-wrapper
        div.centering Not logged in.
    form#profile-form.form-horizontal(v-else data-toggle="validator")
      div.form-group.has-feedback
        label.col-sm-2.control-label(for="profile-form-name") Name
        div.col-sm-5
          input#profile-form-name.form-control(:pattern="sharedConfig.namePattern", :maxlength="sharedConfig.nameMaximumLength" placeholder="Name" required v-model="user.name" data-error="Username may contain only letters, numbers, underscores, or hyphens.")
          span.help-block.with-errors
      div.form-group
        label.col-sm-2.control-label Avatar
        div.col-sm-5 {{{user | toAvatar 32}}} Please change your avatar on <a href="http://gravatar.com/" target="_blank">gravatar.com</a> with the email address below.
      div.form-group.has-feedback
        label.col-sm-2.control-label(for="profile-form-email") Email
        div.col-sm-5
          input#profile-form-email.form-control(type="email" placeholder="Email" required v-model="user.email")
      div.form-group.has-feedback
        label.col-sm-2.control-label Email Aliases
        div.col-sm-5
          div.input-group.email-alias(v-for="(index, alias) in user.emailAliases" track-by="$index")
            input.form-control(type="email" placeholder="Email Alias" required v-model="alias")
            span.input-group-btn
              button.btn.btn-danger(type="button" @click="removeAlias(index)") <span class="fa fa-trash"></span>
          button.btn.btn-default(type="button" @click="addAlias()") Add Alias
      div.form-group
        div.col-sm-offset-2.col-sm-5
          div.btn-group(role="group")
            button.btn.btn-primary(type="submit" @click.prevent="saveUser()") Save
            button.btn.btn-default(type="button" @click="resetUser()") Reset
</template>

<script lang="babel">
import * as Store from '../store'

module.exports = {
  data: function() {
    return {
      user: null,
      newAliasEmail: null
    }
  },
  vuex: {
    actions: {
      loginUser: Store.loginUser, ajaxPost: Store.ajaxPost, showPopover: Store.showPopover
    },
    getters: {
      loggedInUser: Store.loggedInUser
    }
  },
  watch: {
    'loggedInUser': function(newValue) {
      this.resetUser()
    }
  },
  ready: function() {
    this.resetUser()
  },
  methods: {
    removeAlias: function(index) {
      ga('send', 'event', 'profile', 'removeAlias')
      this.user.emailAliases.splice(index, 1)
      this.validateForm()
    },
    addAlias: function() {
      ga('send', 'event', 'profile', 'addAlias')
      this.user.emailAliases.push('')
      this.validateForm()
    },
    saveUser: function() {
      ga('send', 'event', 'profile', 'saveUser')
      if (this.user.name === '') {
        this.user.name = null
      }
      this.ajaxPost('/user', {
        name: this.user.name,
        email: this.user.email,
        emailAliases: this.user.emailAliases
      }, data => this.loginUser(data), {
        409: () => this.showPopover('#profile-form-email', { content: 'Email is already taken.' })
      })
    },
    resetUser: function() {
      ga('send', 'event', 'profile', 'resetUser')
      this.user = JSON.parse(JSON.stringify(this.loggedInUser))
      this.validateForm()
    },
    validateForm: function() {
      this.$nextTick(() => $('#profile-form').validator('validate'))
    }
  }
}
</script>
