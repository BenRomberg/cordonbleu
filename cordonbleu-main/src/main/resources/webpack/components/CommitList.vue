<style lang="sass">
@import "../variables";

#show-approved-checkbox {
  margin-top: 4px;
  margin-bottom: 0;
}
.multiselect {
  margin: 2px;
}
.badge {
  padding: 3px 6px;
}

#commit-list {
  width: $tableWidth;
}
#commit-list-container {
  position: absolute;
  top: 140px;
  bottom: 0;
  width: $tableWidth;
  overflow-y: scroll;
  cursor: default;
}
#commit-list-container td {
  text-overflow: ellipsis;
  max-width: $repositoriesWidth;
}
#commit-list-container td, #commit-list-container th {
  overflow: hidden;
  padding: 4px;
  white-space:nowrap;
}
#refresh-commit-list {
  text-align: center;
}
tr.removed>td {
  background-image:
  url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNYtWrVfwAG/gL+OCIFVwAAAABJRU5ErkJggg==');
  background-repeat: repeat-x;
  background-position: 50% 50%;
}
td.icon {
  overflow: hidden;
  text-align: center;
  max-width: $iconWidth;
}
th.author {
  width: $authorWidth;
}
th.hash {
  width: $hashWidth;
}
th.created {
  width: $createdWidth;
}
th.repositories {
  width: $repositoriesWidth;
}
th.icon {
  width: $iconWidth;
  text-align: center;
}
</style>

<template lang="jade">
  div
    div.inset
      select#repository-dropdown.btn-primary(multiple="multiple")
      select#author-dropdown.btn-primary(multiple="multiple")
      div#show-approved-checkbox.checkbox
        label <input type="checkbox" v-model="showApproved" @change="updateApproved()"> Show Approved
      div#show-collective-review-checkbox.checkbox
        label <input type="checkbox" v-model="showCollectiveReviewOnly" @change="updateCollectiveReview()"> Show Collective Review
      div#refresh-commit-list(v-if="refreshCount > 0")
        button.btn.btn-info.btn-xs(@click="updateList()")
          <span class="badge">{{refreshCount}}</span> new commit{{refreshCount | toPluralS}} available
    div#commit-list-container(@scroll="endlessScrolling.scroll()")
      table.table.table-striped.table-hover.table-condensed
        tr
          th.author Author
          th.hash Commit
          th.created Time Ago
          th.repositories Repositories
          th.icon <span class="fa fa-comment"></span>
          th.icon <span class="fa fa-check-square"></span>
        tr(v-for="commit in commits" v-link="{ name: 'commitDetail', params: { commitHash: commit.hash, teamName: $route.params.teamName }}", :class="{ 'info': commit.hash === $route.params.commitHash, 'removed': commit.removed }")
          td(:title="commit.author | toCommitAuthor") {{{commit.author | toCommitAuthorWithAvatar}}}
          td(:title="commit.hash") {{commit.hash.substring(0, 6)}}
          td(:title="commit.created | toFullTime") {{commit.created | toTimeAgo true}}
          td(:title="commit.repositories.join(', ')") {{commit.repositories}}
          td.icon <span class="badge">{{commit.numComments | ifPositive}}</span>
          td.icon <span class="fa" :class="commit.approved ? 'fa-check' : ''"></span>
</template>

<script lang="babel">
import * as Store from '../store'
import * as DomHelper from '../classes/DomHelper'
var EndlessScolling = require('../classes/EndlessScrolling.js')
var LIMIT = 50

module.exports = {
  data: function() {
    return {
      commits: [],
      showApproved: true,
      showCollectiveReviewOnly: false,
      initialized: false,
      shiftKeyPressed: false,
      refreshCount: 0,
      endlessScrolling: new EndlessScolling('commit-list-container', LIMIT, () => this.commits.length, () => this.appendList())
    }
  },
  vuex: {
    getters: {
      activeTeam: Store.activeTeam
    },
    actions: {
      ajaxPost: Store.ajaxPost, onKeyDown: Store.onKeyDown
    }
  },
  watch: {
    'activeTeam.filters': function(newValue) {
      this.initializeList(newValue)
    }
  },
  ready: function() {
    this.onKeyDown('w', () => this.selectCommit(-1))
    this.onKeyDown('s', () => this.selectCommit(1))
    $(document).on('keyup keydown', event => { this.shiftKeyPressed = event.shiftKey })
    $(window).resize(this.endlessScrolling.scroll);

    this.setupMultiselect('#repository-dropdown', 'Repositories')
    this.setupMultiselect('#author-dropdown', 'Authors')

    this.runInInterval('commitListRefresh', 60, () => {
      this.fetchList(data => {
        this.commits = this.commits.map(commit => data.find(newCommit => newCommit.hash === commit.hash) || commit)
        this.refreshCount = data.filter(commit => !this.commits.some(existingCommit => existingCommit.hash === commit.hash)).length
      }, true, this.commits.length + this.refreshCount, true)
    })
    this.initializeList(this.activeTeam.filters)
  },
  methods: {
    updateApproved: function() {
      ga('send', 'event', 'commitList', 'showApproved', this.showApproved)
      this.updateList()
    },
    updateCollectiveReview :function() {
      ga('send', 'event', 'commitList', 'showCollectiveReviewOnly', this.showCollectiveReviewOnly)
      this.updateList()
    },
    initializeList: function(filter) {
      this.restoreFilters(filter)
      this.initialized = true
      this.updateList()
    },
    setupMultiselect: function(selector, plural) {
      $(selector).multiselect({
        maxHeight : 400,
        inheritClass : true,
        buttonWidth : '100%',
        numberDisplayed : 2,
        enableHTML: true,
        enableFiltering : true,
        enableCaseInsensitiveFiltering : true,
        includeSelectAllOption : true,
        enableClickableOptGroups: true,
        selectAllJustVisible: false,
        onDropdownShown: event => {
          // prevent multiselect from doing event.stopPropagation()
          $('div.btn-group').off('keydown.multiselect');
        },
        onChange: (option, checked, select) => {
          if (this.shiftKeyPressed && option.length === 1) {
            ga('send', 'event', 'commitList', 'change', 'shiftPressed')
            $(selector).multiselect('deselectAll', false).multiselect('select', option.val())
          } else {
            ga('send', 'event', 'commitList', 'change', 'normal', option.length)
          }
          this.updateList()
        },
        onSelectAll: () => {
          ga('send', 'event', 'commitList', 'change', 'selectAll')
          this.updateList()
        },
        nonSelectedText : 'No ' + plural,
        allSelectedText : 'All ' + plural
      })
    },
    selectCommit: function(offset) {
      var commitToSelect = null
      for (var i = 0; i < this.commits.length; i++) {
        if (this.commits[i].hash === this.$route.params.commitHash) {
          commitToSelect = this.commits[i + offset]
        }
      }
      if (!commitToSelect) {
        return
      }
      this.$route.router.go({ name: 'commitDetail', params: { commitHash: commitToSelect.hash }})
    },
    updateList: function() {
      if (!this.initialized) {
        return
      }
      this.storeFilters()
      this.endlessScrolling.reset()
      this.refreshCount = 0
      this.fetchList(data => this.commits = data, true)
    },
    storeFilters: function() {
      var selectAllIsEmpty = selector => {
        var selected = $(selector).val()
        if (!selected || selected.length == $(selector).prop('options').length) {
          return []
        }
        return selected
      }
      Lockr.set('#repository-dropdown', selectAllIsEmpty('#repository-dropdown'))
      Lockr.set('#author-dropdown', selectAllIsEmpty('#author-dropdown'))
      Lockr.set('showApproved', this.showApproved)
    },
    restoreFilters: function(data) {
      var filterToOption = (filter, labelFunc, valueFunc, selected) => {
        return filter.map(value => {
          var serializedValue = JSON.stringify(valueFunc(value))
          return { value: serializedValue, label: labelFunc(value), selected: selected.length === 0 || selected.indexOf(serializedValue) >= 0 }
        })
      }
      $('#repository-dropdown').multiselect('dataprovider', filterToOption(data.repositories, repository => repository.name, repository => repository.id, Lockr.get('#repository-dropdown', [])))
      var authorOptions = filterToOption(data.authors, this.$options.filters.toCommitAuthorWithAvatar, author => author, Lockr.get('#author-dropdown', []))
      var userOptions = filterToOption(data.users, this.$options.filters.toUserWithAvatar, user => user.id, Lockr.get('#author-dropdown', []))
      var authorGroups = [
        { label: 'Registered Users', children: userOptions },
        { label: 'Commit Authors', children: authorOptions }
      ]
      $('#author-dropdown').multiselect('dataprovider', authorGroups)
      this.showApproved = Lockr.get('showApproved', true)
    },
    appendList: function() {
      this.fetchList(data => this.commits.push.apply(this.commits, data))
    },
    fetchList: function(callback, fetchUpdates, limit, hide) {
      var optionToFilter = selector => ($(selector).val() || []).map(serializedValue => JSON.parse(serializedValue))
      var authorAllOptions = optionToFilter("#author-dropdown")
      var commitListData = {
        repository: optionToFilter("#repository-dropdown"),
        author: authorAllOptions.filter(author => typeof author === 'object'),
        user: authorAllOptions.filter(author => typeof author === 'string'),
        approved: this.showApproved,
        collectiveReviewOnly: this.showCollectiveReviewOnly,
        lastCommitHash: this.commits.length == 0 || fetchUpdates ? null : this.commits[this.commits.length - 1].hash,
        limit: limit || LIMIT
      }
      this.ajaxPost('/commit/list', commitListData, data => {
        callback(data)
        DomHelper.waitForElement('commit-list-container', this.endlessScrolling.scroll)
      }, {}, hide)
    }
  },
  events: {
    'update-commit-broadcast': function(commit) {
      this.commits.filter(item => item.hash === commit.hash).forEach(item => {
        item.approved = commit.approval !== null
        item.numComments = this.calcNumComments(commit)
      })
    }
  }
}
</script>
