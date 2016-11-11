<style lang="sass">
#ajax-loading-indicator {
  position: absolute;
  left: 0;
  right: 0;
  text-align: center;
}
#content {
  position: absolute;
  top: 56px;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: auto;
}

.navbar {
  background-color: #337ab7; /* also change in index.html */
  background-image: none;
  margin-bottom: 4px;
}

.navbar-default .navbar-brand {
  color: #fff;
  font-weight: bold;
}

.navbar .nav > li > a {
  color: #fff;
}

.navbar-default .navbar-nav a.active, .navbar-default .navbar-nav a.active:hover, .navbar-default .navbar-nav a.active:focus {
  color: #555;
  background-color: #fff;
  background-image: none;
}

#help-button {
  margin-left: 15px;
}

#shortcuts ul {
  padding-left: 10px;
}
#shortcuts li {
  white-space: nowrap;
  list-style-type: none;
}

.dropdown-menu>li>a {
  cursor: pointer;
}
.user-inactive {
  text-decoration: line-through;
}
</style>

<template lang="jade">
  div.fullheight
    div.centering-root(v-if="loading")
      div.centering-wrapper
        div.centering Loading...
    template(v-else)
      nav#navigation.navbar.navbar-default
        div.navbar-header
          button.navbar-toggle.collapsed(type="button" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false")
            span.sr-only Toggle navigation
            span.icon-bar
            span.icon-bar
            span.icon-bar
          a.navbar-brand(v-link="'/'") Cordon Bleu
        div.collapse.navbar-collapse
          ul.nav.navbar-nav
            li.dropdown
              a.dropdown-toggle(data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false")
                | <span class="fa fa-users"></span> {{activeTeam ? activeTeam.name : 'Teams'}} <span class="caret"></span>
              ul.dropdown-menu
                li(v-for="team in teams" v-link-active)
                  a(v-link="{ name: 'commits', params: { teamName: team.name } }")
                    | <span class="fa fa-fw fa-users"></span> {{team.name}}
                li.divider(role="separator")
                li: a(v-link="{ name: 'create-team' }") <span class="fa fa-fw fa-plus"></span> Create new Team
            template(v-if="activeTeam")
              li: a(v-link="{ name: 'commits', activeClass: 'ignore' }", :class="{ 'active': !$route.fullPath.startsWith('/team/:teamName/settings') }")
                | <span class="fa fa-code"></span> Commits
              li.dropdown(v-if="hasTeamPermissionManage")
                a.dropdown-toggle(data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false",
                  :class="{ 'active': $route.fullPath.startsWith('/team/:teamName/settings') }")
                  <span class="fa fa-cogs"></span> Team Settings <span class="caret"></span>
                ul.dropdown-menu
                  li(v-link-active): a(v-link="{ name: 'team-settings' }")
                    | <span class="fa fa-fw fa-cog"></span> General Settings
                  li(v-link-active): a(v-link="{ name: 'repositories' }")
                    | <span class="fa fa-fw fa-database"></span> Repositories
                  li(v-link-active): a(v-link="{ name: 'team-members' }")
                    | <span class="fa fa-fw fa-user-plus"></span> Members
          button#help-button.btn.btn-info.navbar-btn(type="button" @click="toggleHelp($event)")
            | <span class="fa fa-life-ring"></span> Help
          user-navigation
      div#content
        router-view
      div#ajax-loading-indicator
        span.label.label-info(v-if="!newWebsiteVersion && ajaxLoading > 0") Loading...
        span.label.label-danger(v-if="!newWebsiteVersion && ajaxLoading === 0 && ajaxError > 0") Error connecting to Server!
        span.h3(v-if="newWebsiteVersion" @click="reloadPage()")
          a.label.label-warning There is a new version available. Please save your work and click here to reload the page.
      div#help-popover-content.hidden-popover-content
        div#shortcuts Shortcuts:
          ul
            li: <kbd><b>a</b></kbd> <span class="fa fa-fw fa-thumbs-up"></span> to approve a commit
            li: <kbd><b>w</b></kbd> <span class="fa fa-fw fa-arrow-circle-up"></span> to go up one commit
            li: <kbd><b>s</b></kbd> <span class="fa fa-fw fa-arrow-circle-down"></span> to go down one commit
            li: <kbd><b>c</b></kbd> <span class="fa fa-fw fa-arrow-down"></span> to go down one conversation
            li: <kbd><b>n</b></kbd> <span class="fa fa-fw fa-bell"></span> to toggle your notifications
            li: <kbd><b>h</b></kbd> <span class="fa fa-fw fa-life-ring"></span> to toggle this help
          | Hold <b>Shift</b> to exclusively select an<br/>author/repository in the dropdown.
</template>

<script lang="babel">
import Vuex, * as Store from '../store'
import AppUserView from './AppUser.vue'

module.exports = {
  components: {
    'user-navigation': AppUserView
  },
  data: function() {
    return {
    }
  },
  computed: {
    loading: function() {
      return this.$route.params.teamName && !this.activeTeam
    }
  },
  vuex: {
    getters: {
      activeTeam: Store.activeTeam,
      ajaxError: Store.ajaxError,
      ajaxLoading: Store.ajaxLoading,
      hasTeamPermissionManage: Store.hasTeamPermissionManage,
      newWebsiteVersion: Store.newWebsiteVersion,
      teams: Store.teams,
    },
    actions: {
      installKeyHandler: Store.installKeyHandler,
      onKeyDown: Store.onKeyDown,
      onTeamParameterChange: Store.onTeamParameterChange,
      togglePopover: Store.togglePopover,
    }
  },
  watch: {
    '$route.params.teamName': function(newValue) {
      this.onTeamParameterChange(newValue)
    }
  },
  created: function() {
    this.onTeamParameterChange(this.$route.params.teamName)
  },
  ready: function() {
    this.installKeyHandler()
    this.onKeyDown('h', () => this.toggleHelp('#help-button'))
  },
  methods: {
    reloadPage: function() {
      location.reload(true)
    },
    toggleHelp: function(eventOrSelector) {
      this.togglePopover(eventOrSelector, {
        html: true,
        content: document.getElementById('help-popover-content').innerHTML
      }, null, 'popover-info help-popover', () => ga('send', 'event', 'help', 'show'))
    }
  },
  store: Vuex,
}
</script>
