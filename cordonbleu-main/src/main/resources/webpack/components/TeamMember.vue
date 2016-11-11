<style lang="sass">
#members-list {
  width: auto;
  margin-top: 20px;
}
</style>

<template lang="jade">
  div.inset
    form.form-inline
      div.form-group
        label.sr-only(for="member-name") Name
        input#member-name.form-control(type="text" v-model="memberName" placeholder="User Name")
      button.btn.btn-primary(type="submit" @click.prevent="addMember($event)") Add
    table#members-list.table.table-bordered.table-striped.table-hover
      tr
        th Name
        th Email
        th Email Aliases
        th Owner
        th
      tr(v-for="member in members")
        td {{member.name}}
        td {{member.email}}
        td: div(v-for="emailAlias in member.emailAliases") {{emailAlias}}
        td: input(type="checkbox" v-model="member.owner" @change="toggleFlag('OWNER', member, member.owner)", :disabled="member.id === loggedInUser.id")
        td: button.btn.btn-danger.btn-sm.fa.fa-trash(@click="confirmDeleteMember(member, $event)")
</template>

<script lang="babel">
import * as Store from '../store'

module.exports = {
  data: function() {
    return {
      members: [],
      memberName: null
    }
  },
  vuex: {
    getters: {
      activeTeam: Store.activeTeam,
      loggedInUser: Store.loggedInUser
    },
    actions: {
      ajaxGet: Store.ajaxGet,
      ajaxPost: Store.ajaxPost,
      showConfirmationPopover: Store.showConfirmationPopover,
      showPopover: Store.showPopover
    }
  },
  ready: function() {
    this.ajaxGet('/team/members', { teamId: this.activeTeam.id }, data => this.members = data)
    $('#member-name').textcomplete([{
      match: new RegExp('^(.+)$'),
      index: 1,
      search: (term, callback) => {
        this.ajaxGet('/user/autocomplete', { prefix: term }, data => callback(data))
      },
      replace: user => user.name,
      template: user => user.name
    }])
  },
  methods: {
    toggleFlag: function(flag, member, flagValue) {
      this.ajaxPost('/team/members/updateFlag', {
        userId: member.id,
        teamId: this.activeTeam.id,
        flag: flag,
        flagValue: flagValue
      }, data => this.members = data)
    },
    addMember: function(event) {
      this.ajaxPost('/team/members/add', {
        teamId: this.activeTeam.id,
        userName: this.memberName
      }, data => this.members = data, {
        404: () => {
          this.showPopover('#member-name', {
            title: 'User Name not found',
            content: 'A User with Name "' + this.memberName + '" was not found.'
          })
        }
      })
    },
    confirmDeleteMember: function(member, event) {
      this.showConfirmationPopover(event, 'Remove Team Member', 'danger', () => this.deleteMember(member), {
        title: 'Confirm removing member',
        content: 'Do you really want to remove team member "' + member.name + '"?'
      })
    },
    deleteMember: function(member) {
      this.ajaxPost('/team/members/remove', {
        userId: member.id,
        teamId: this.activeTeam.id
      }, data => this.members = data)
    }
  }
}
</script>
