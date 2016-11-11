<style lang="sass">
$notificationPopupHeight: 500px;

.notification-popover {
  max-height: $notificationPopupHeight;
  padding: 0;
}
.notification-popover .popover-content {
  padding: 0;
}
#notification-content {
  max-height: $notificationPopupHeight - 4px;
  overflow-x: hidden;
  overflow-y: auto;
}
#notification-content .list-group-item {
  border-left: none;
  border-right: none;
}
#notification-content .list-group-item:first-child {
  border-top: none;
}
#notification-content .list-group-item:last-child {
  border-bottom: none;
}
</style>

<template lang="jade">
  div.btn-group
    button#notification-button.btn(:class="notificationPrompts > 0 ? 'btn-warning' : 'btn-info'" @click="toggleNotifications($event)")
      | <span class="fa fa-bell"></span> <span class="badge">{{notificationPrompts}}</span>
    slot
    div#notification-popover-content.hidden-popover-content
      div#notification-content(@scroll="notificationScrolling.scroll()")
        a.list-group-item(v-for="notification in notificationItems" v-link="{ name: 'commitDetail', params: { commitHash: notification.commit.hash, teamName: notification.commit.teamName }}" ":class"="{ 'list-group-item-warning': notification.prompt, 'list-group-item-success': notification.commit.approved, 'active': this.$route.params.commitHash === notification.commit.hash }")
          div(:title="notification.commit.author | toCommitAuthor")
            <span class="fa fa-fw fa-code"></span> <b>{{notification.commit.hash.substring(0, 6)}} from {{{notification.commit.author | toCommitAuthorWithAvatar}}}</b>
          div(:title="notification.commit.message") {{notification.commit.message}}
          div
            | <b>{{{notification.lastAction.user | toUserWithAvatar}}}</b>&nbsp;
            template(v-if="notification.lastAction.type === 'COMMENT'")
              | <span class="fa fa-comment"></span> commented
            template(v-if="notification.lastAction.type === 'APPROVE'")
              | <span class="fa fa-thumbs-up"></span> approved
            | &nbsp;<span class="fa fa-clock-o"></span> {{{notification.lastAction.time | toTimeAgoSpan}}}
        div.list-group-item(v-if="notificationItems.length === 0") No notifications available.
</template>

<script lang="babel">
import Vuex, * as Store from '../store'

module.exports = {
  data: function() {
    return {
    }
  },
  vuex: {
    getters: {
      loggedInUser: Store.loggedInUser,
      notificationItems: Store.notificationItems,
      notificationPrompts: Store.notificationPrompts,
      notificationScrolling: Store.notificationScrolling,
    },
    actions: {
      onKeyDown: Store.onKeyDown,
      togglePopover: Store.togglePopover,
      updateNotifications: Store.updateNotifications
    }
  },
  ready: function() {
    this.onKeyDown('n', () => this.toggleNotifications('#notification-button'))
    $('body').on('inserted.bs.popover', (event) => {
      if (event.target.id === 'notification-button') {
        $('.notification-popover .popover-content').append($('#notification-popover-content').children())
      }
    })
    $('body').on('hide.bs.popover', (event) => {
      if (event.target.id === 'notification-button') {
        $('#notification-popover-content').append($('.notification-popover .popover-content').children())
      }
    })
    this.runInInterval('notificationRefresh', 60, () => {
      if (this.loggedInUser) {
        this.updateNotifications(true)
      }
    })
  },
  methods: {
    toggleNotifications: function(eventOrSelector) {
      this.togglePopover(eventOrSelector, {
        html: true,
        content: ' '
      }, null, 'popover-info notification-popover', () => ga('send', 'event', 'notifications', 'show', null, this.notificationPrompts))
    },
  }
}
</script>
