<style lang="sass">
@import "../variables";
@import "../sharedWithEmail";

$headerHeight: 87px;

#commit-details {
  top: $headerHeight;
  left: $tableWidth + 4px;
  overflow-y: auto;
  margin: 0;
}
#refresh-commit-detail {
  position: absolute;
  top: $headerHeight + 2px;
  left: $tableWidth + 4px;
  right: 0;
  text-align: center;
}
#more-comments {
  position: absolute;
  bottom: 4px;
  left: $tableWidth + 4px;
  right: 0;
  text-align: center;
}
#commit-header {
  padding: 10px 15px;
  width: 100%;
}
.panel-heading-nowrap > div {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.panel-heading-tail {
  padding-left: 8px;
  text-align: right;
  float: right;
}
.panel-heading-tail > button {
  margin-bottom: 6px;
}
.popover {
  color: #000;
}
.alert-default {
    color: #555;
    background-color: #fff;
    border-color: #ddd;
}

.panel-heading a {
  cursor: pointer;
}
.panel-heading a:after {
    font: normal normal normal 14px/1 FontAwesome;
    content: "\f062";
    float: right;
    color: #999;
}
.panel-heading a.collapsed:after {
    content: "\f063";
}

.commit-file-before, .commit-file-before.panel-heading {
  background-color: #ffeaea;
}
.commit-file-after, .commit-file-after.panel-heading {
  background-color: #eaffea;
}
.file-content {
  overflow-x: auto;
}

#commit-message, code {
  overflow: auto;
  display: inline-block;
}
#commit-message {
  display: block;
  width: 100%;
  margin-bottom: 5px;
}
code {
  background: none;
  max-width: 100%;
  vertical-align: bottom;
  padding: 0 4px;
  margin-bottom: -1px;
}
.primary-detail {
  font-weight: 700;
  margin-right: 10px;
}
.gap-detail {
  margin-right: 20px; 
}
.code-review-btn {
  margin-left : 10px;
}
</style>

<template lang="jade">
  div
    div#commit-details.centering-root(v-if="!commit")
      div.centering-wrapper
        div.centering Loading...
    template(v-else)
      div#commit-details.centering-root(v-if="commit === 'notfound'")
        div.centering-wrapper
          div.centering Commit {{$route.params.commitHash}} was not found.
      div#commit-details.centering-root(v-if="commit === 'removed'")
        div.centering-wrapper
          div.centering Commit {{$route.params.commitHash}} has been removed from the repository.
      template(v-else)
        div#commit-header.alert.panel-heading-nowrap(:class="{ 'alert-default': !commit.approval, 'alert-success': commit.approval }")
          div.panel-heading-tail
            template(v-if="commit.approval")
              button.btn.btn-warning(@click="revertCommitApproval($event)") <span class="fa fa-undo"></span> Undo Approval
            template(v-else)
              button#approve-button.btn.btn-success(@click="approveCommit($event)") <span class="fa fa-thumbs-up"></span> Approve
            template(v-if="commit.collectiveReview")
              button.btn.btn-info.code-review-btn(@click="proposeToCollectiveReview($event, false)") <span class="fa fa-users"></span> Undo Propose to Collective Review              
            template(v-else)
              button.btn.btn-warning.code-review-btn(@click="proposeToCollectiveReview($event, true)") <span class="fa fa-users"></span> Propose to Collective Review
          div
            <span class="fa fa-fw fa-code"></span> <span class="primary-detail">{{commit.hash}}</span>
            br
            template(v-for="repository in commit.repositories")
              <span class="fa fa-fw fa-database"></span> <span class="primary-detail">{{repository.name}}</span>
              <span class="fa fa-code-fork"></span> <span class="gap-detail">{{repository.branches.join(', ')}}</span>
          div.panel-heading-tail(v-if="commit.approval")
            <span class="fa fa-thumbs-up"></span> by <span class="primary-detail">{{{commit.approval.approver | toUserWithAvatar}}}</span>
            <span class="fa fa-clock-o"></span> {{{commit.approval.time | toTimeAgoSpan}}}
          div
            <span class="primary-detail">{{{commit.author | toCommitAuthorWithAvatar}}}</span>
            <span class="fa fa-clock-o"></span> {{{commit.created | toTimeAgoSpan}}}           
        div#commit-details.panel-group.centering-root(@scroll="scrollCommitView()")
          div#commit-message.code.well {{{commit.messageAsHtml}}}
          div.panel.panel-default(v-for="(index, file) in commit.files")
            div.panel-heading(:class="file.pathFragments.length == 1 ? 'commit-file-' + file.pathFragments[0].status.toLowerCase() : ''")
              h4.panel-title
                <span v-for="fragment in file.pathFragments" :class="'commit-file-' + fragment.status.toLowerCase()">{{fragment.text}}</span>
            div.panel.panel-body.file-content
              code-lines(:file="file", :clusters="file.clusters", :from-spacer="false", :index="index")
        div#refresh-commit-detail(v-if="refreshCount > 0")
          button.btn.btn-info.btn-xs(@click="updateCommit()")
            <span class="badge">{{refreshCount}}</span> new comment{{refreshCount | toPluralS}} available
        div#more-comments(v-if="moreComments > 0")
          button.btn.btn-info.btn-xs(@click="scrollToNextComment()")
            <span class="badge">{{moreComments}}</span> more conversation{{moreComments | toPluralS}} <span class="fa fa-arrow-down"></span>
</template>

<script lang="babel">
import * as Store from '../store'
import * as DomHelper from '../classes/DomHelper'
var CommitClusterer = require('../classes/CommitClusterer.js')

module.exports = {
  data: function() {
    return {
      commit: null,
      refreshCount: 0,
      moreComments: 0
    }
  },
  vuex: {
    getters: {
      activeTeam: Store.activeTeam,
      hasTeamPermissionApprove: Store.hasTeamPermissionApprove
    },
    actions: {
      ajaxGet: Store.ajaxGet,
      ajaxPost: Store.ajaxPost,
      onKeyDown: Store.onKeyDown,
      requireLogin: Store.requireLogin,
      showPopover: Store.showPopover,
      updateNotifications: Store.updateNotifications
    }
  },
  watch: {
    commit: {
      handler: function (newValue, oldValue) {
        this.$dispatch('update-commit', newValue)
      },
      deep: true
    }
  },
  components: {
    'commit-header': {}
  },
  created: function() {
    this.updateCommit()
  },
  ready: function() {  
    this.onKeyDown('a', () => {
      if (!this.commit.approval) {
        this.approveCommit('#approve-button')
      }
    })
    this.onKeyDown('c', this.scrollToNextComment)
    this.runInInterval('commitDetailRefresh', 60, () => {
      this.fetchCommit(data => this.refreshCount = this.calcNumComments(data) - this.calcNumComments(this.commit))
    })
    $(window).resize(this.scrollCommitView);
  },
  methods: {
    scrollCommitView: function() {
      this.updateMoreComments()
      this.updateClusterVisibility()
    },
    updateMoreComments: function() {
      var height = this.getDetailsHeightMinusPeekBuffer()
      this.moreComments = $('.comment-cluster').get().filter(element => $(element).position().top > height).length
    },
    scrollToNextComment: function() {
      var height = this.getDetailsHeightMinusPeekBuffer()
      var scrollTo = Math.min(...$('.comment-cluster').get().map(element => $(element).position().top).filter(topOffset => topOffset > height))
      document.getElementById('commit-details').scrollTop += scrollTo
    },
    getDetailsHeightMinusPeekBuffer: function() {
      var height = $('#commit-details').height()
      return height - Math.min(height, 70)
    },
    updateCommit: function() {
      this.refreshCount = 0
      this.fetchCommit(data => {
        this.commit = new CommitClusterer().clusterCommit(data)
        DomHelper.waitForElement('commit-message', () => {
          this.scrollCommitView()
          if (window.location.hash) {
            window.location.href = window.location.hash
          }
        })
      })
    },
    updateClusterVisibility: function() {
      if (!this.commit) {
        return
      }
      var containerHeight = document.getElementById('commit-details').getBoundingClientRect().height
      var lineClusters = []
      this.commit.files.forEach(file => lineClusters.push(...file.clusters.filter(cluster => cluster.lines)))
      var lineClusterDivs = $('.line-cluster')
      for (var i = 0; i < Math.min(lineClusters.length, lineClusterDivs.length); i++) {
        var lineClusterDiv = $(lineClusterDivs[i])
        lineClusters[i].visible = lineClusterDiv.position().top + lineClusterDiv.height() >= 0
                                && lineClusterDiv.position().top <= containerHeight
      }
    },
    fetchCommit: function(callback) {
      this.ajaxGet('/commit/detail', { hash: this.$route.params.commitHash, teamId: this.activeTeam.id }, callback, {
        404: () => this.commit = 'notfound',
        410: () => this.commit = 'removed'
      }, true)
    },
    approveCommit: function(eventOrSelector) {
      if (this.requireLogin(eventOrSelector, 'approve this commit')) {
        ga('send', 'event', 'commit', 'approve', 'requireLogin')
        return
      }
      if (this.requireTeamMembership(eventOrSelector)) {
        ga('send', 'event', 'commit', 'approve', 'requireMembership')
        return
      }
      ga('send', 'event', 'commit', 'approve', 'success')
      this.ajaxPost('/commit/approve', {
        hash: this.$route.params.commitHash,
        teamId: this.activeTeam.id
      }, data => this.commit.approval = data)
      this.updateNotifications()
    },
    proposeToCollectiveReview: function(eventOrSelector, trueOrFalse) {
		if (this.requireLogin(eventOrSelector, 'approve this commit')) {
	        ga('send', 'event', 'commit', 'proposeToCollectiveReview', 'requireLogin')
	        return
	    }
	    if (this.requireTeamMembership(eventOrSelector)) {
	        ga('send', 'event', 'commit', 'proposeToCollectiveReview', 'requireMembership')
	        return
	    }
    	ga('send', 'event', 'commit', 'proposeToCollectiveReview', 'success')
    	
	    this.ajaxPost('/commit/proposeToCollectiveReview', {
	        "hash": this.$route.params.commitHash,
	        "teamId": this.activeTeam.id,
	        "value" : trueOrFalse	        
	    }, data => this.commit.collectiveReview=trueOrFalse)
      
    },
    revertCommitApproval: function(event) {
      if (this.requireLogin(event, 'revert the approval')) {
        ga('send', 'event', 'commit', 'revertApproval', 'requireLogin')
        return
      }
      if (this.requireTeamMembership(event)) {
        ga('send', 'event', 'commit', 'revertApproval', 'requireMembership')
        return
      }
      ga('send', 'event', 'commit', 'revertApproval', 'success')
      this.ajaxPost('/commit/revertApproval', {
        hash: this.$route.params.commitHash,
        teamId: this.activeTeam.id
      }, data => this.commit.approval = null)
      this.updateNotifications()
    },
    requireTeamMembership: function(event) {
      if (this.hasTeamPermissionApprove) {
        return false
      }
      this.showPopover(event, {
        title: 'Team Membership required',
        content: 'Only team-members may approve commits or reject approvals within this team.',
      })
      return true
    }
  },
  route: {
    canReuse: false
  }
}
</script>
