<style lang="sass">
#repository-list {
  width: auto;
  margin-top: 20px;
}
#public-key {
  word-break: break-all;
}
</style>

<template lang="jade">
  div.inset
    form.form-inline
      div.form-group
        label.sr-only(for="repository-name") Name
        input#repository-name.form-control(v-model="repositoryName" placeholder="Name")
      div.form-group
        select#repository-type.form-control(v-model="repositoryType" placeholder="SCM Type")
            option git
            option svn
      div.form-group
        label.sr-only(for="repository-source-url") Source URL
        input#repository-source-url.form-control(v-model="repositorySourceUrl" placeholder="Source URL" size=50)
      button.btn.btn-primary(type="submit" @click.prevent="addRepository($event)") Add
    table#repository-list.table.table-bordered.table-striped.table-hover
      tr
        th Name
        th Type
        th Source URL
        th
      tr(v-for="repository in repositories")
        td {{repository.name}}
        td {{repository.type}}
        td {{repository.sourceUrl}}
        td
          button.btn.btn-danger.btn-sm.fa.fa-trash(@click="confirmDeleteRepository(repository, $event)")
    div
      h3 Public Key
      | In order to use private repositories, you may need to add this SSH Public Key in order to access it (see instructions for <a href="https://help.github.com/articles/adding-a-new-ssh-key-to-your-github-account/">GitHub</a> or <a href="https://confluence.atlassian.com/bitbucket/add-an-ssh-key-to-an-account-302811853.html">Bitbucket</a>).
      div#public-key.well {{activeTeam.publicKey}}
</template>

<script lang="babel">
import * as Store from '../store'

module.exports = {
  data: function() {
    return {
      repositories: [],
      repositoryName: null,
      repositorySourceUrl: null
    }
  },
  vuex: {
    getters: {
      activeTeam: Store.activeTeam
    },
    actions: {
      ajaxGet: Store.ajaxGet,
      ajaxPost: Store.ajaxPost,
      showConfirmationPopover: Store.showConfirmationPopover,
      showPopover: Store.showPopover
    }
  },
  ready: function() {
    this.ajaxGet('/repository/list', { teamId: this.activeTeam.id }, data => this.repositories = data)
  },
  methods: {
    addRepository: function(event) {
      this.ajaxPost('/repository/add', {
        teamId: this.activeTeam.id,
        name: this.repositoryName,
        type: this.repositoryType,
        sourceUrl: this.repositorySourceUrl
      }, data => {
        ga('send', 'event', 'repository', 'add', 'success')
        this.repositories = data
      }, {
        409: () => {
          ga('send', 'event', 'repository', 'add', 'duplicateName')
          this.showPopover('#repository-name', {
            title: 'Duplicate name',
            content: 'A repository with the same name already exists. (If it was just deleted, please wait a minute until the deletion is finished.)'
          })
        }
      })
    },
    confirmDeleteRepository: function(repository, event) {
      ga('send', 'event', 'repository', 'delete', 'confirm')
      this.showConfirmationPopover(event, 'Delete', 'danger', () => this.deleteRepository(repository), {
        title: 'Confirm deleting repository',
        content: 'Do you really want to delete repository "' + repository.name + '"?'
      })
    },
    deleteRepository: function(repository) {
      ga('send', 'event', 'repository', 'delete', 'success')
      this.ajaxPost('/repository/delete', {
        id: repository.id,
        teamId: this.activeTeam.id
      }, data => this.repositories = data)
    }
  }
}
</script>
