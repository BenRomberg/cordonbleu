<style lang="sass">
</style>

<template lang="jade">
  span
    div.navbar-form.navbar-right(v-if="loggedInUser")
      user-buttons
        button.btn.btn-info.dropdown-toggle(data-toggle="dropdown" aria-haspopup="true" aria-expanded="false")
          | {{{loggedInUser | toUserWithAvatar}}} <span class="caret"></span>
        ul.dropdown-menu
          li: a(v-link="{ name: 'profile' }") Profile
          li(v-if="hasGlobalPermissionManageUsers"): a(v-link="{ name: 'user-management' }") User Management
          li: a(@click="logout()") Logout
    div.navbar-form.navbar-right(v-show="!loggedInUser")
      form#login-form.form-inline(role="form" data-toggle="validator")
        div.form-group.has-feedback
          input#login-register-email.form-control(type="email" v-model="email" placeholder="Email" required)
          span.glyphicon.form-control-feedback(aria-hidden="true")
        div.form-group.has-feedback
          input#login-register-password.form-control.has-feedback(type="password" v-model="password" placeholder="Password" required :data-minlength="sharedConfig.passwordMinimumLength")
          span.glyphicon.form-control-feedback(aria-hidden="true")
        div.form-group
          button#login-register-button.btn.btn-info.popover-info(type="submit" @click.prevent="loginOrRegister()") Login/Register
</template>

<script lang="babel">
import Vuex, * as Store from '../store'
import AppNotificationView from './AppNotification.vue'

module.exports = {
  components: {
    'user-buttons': AppNotificationView,
  },
  data: function() {
    return {
      email: null,
      password: null
    }
  },
  computed: {
    loading: function() {
      return this.$route.params.teamName && !this.activeTeam
    },
  },
  vuex: {
    getters: {
      activeTeam: Store.activeTeam,
      hasGlobalPermissionManageUsers: Store.hasGlobalPermissionManageUsers,
      loggedInUser: Store.loggedInUser,
    },
    actions: {
      ajaxPost: Store.ajaxPost,
      loginUser: Store.loginUser,
      logoutUser: Store.logoutUser,
      restoreUserFromSession: Store.restoreUserFromSession,
      showConfirmationPopover: Store.showConfirmationPopover,
      showPopover: Store.showPopover,
      updateNotifications: Store.updateNotifications
    },
  },
  created: function() {
    if (this.restoreUserFromSession()) {
      this.updateNotifications()
    }
  },
  ready: function() {
    $('#login-form').validator()
  },
  methods: {
    loginOrRegister: function() {
      if ($('#login-register-button').hasClass('disabled')) {
        return
      }
      this.ajaxPost('/user/login', { email: this.email, password: this.password }, data => this.loginUser(data), {
        404: () => this.confirmRegistration(),
        403: () => this.userInactive(),
        401: () => this.wrongPassword()
      })
    },
    userInactive: function() {
      ga('send', 'event', 'loginRegister', 'userInactive')
      this.password = null
      this.showPopover('#login-register-email', {
        content: 'User has been set to inactive and may not login.'
      })
    },
    loginUser: function(user) {
      ga('send', 'event', 'loginRegister', 'login')
      this.loginUser(user)
      this.password = null
      this.updateNotifications()
    },
    wrongPassword: function() {
      ga('send', 'event', 'loginRegister', 'wrongPassword')
      this.password = null
      this.showPopover('#login-register-password', {
        content: 'Wrong password.'
      })
    },
    confirmRegistration: function() {
      ga('send', 'event', 'loginRegister', 'confirmRegistration')
      this.showConfirmationPopover('#login-register-button', 'Register', 'primary', this.register, {
        title: 'Confirm registration',
        content: 'No user with your email-address was found. Please choose a username to register:' +
          '<div class="form-group has-feedback">' +
            '<input id="register-form-name" class="form-control" pattern="' + this.sharedConfig.namePattern + '" maxlength="' + this.sharedConfig.nameMaximumLength + '" placeholder="Name" required data-error="Username may contain only letters, numbers, underscores, or hyphens.">' +
            '<span class="help-block with-errors"></span>' +
          '</div>'
      })
      $('#confirmation-form').validator()
    },
    register: function() {
      ga('send', 'event', 'loginRegister', 'register')
      this.ajaxPost('/user/register', { email: this.email, password: this.password, name: $('#register-form-name').val() }, data => this.loginUser(data))
    },
    logout: function() {
      ga('send', 'event', 'loginRegister', 'logout')
      Cookies.remove('session')
      this.logoutUser()
      this.$router.go({ name: 'welcome' })
    },
  },
}
</script>
