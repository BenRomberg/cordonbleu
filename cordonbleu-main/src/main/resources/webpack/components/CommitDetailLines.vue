<style lang="sass">
@import "../variables";

$lineHeight: 18px;

.line-number {
  float: left;
  text-align: center;
  width: 22px;
  font-size: $codeFontSize;
  height: $lineHeight;
}
div.line-number {
  white-space: pre;
  font-family: Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  width: $lineNumberWidth;
}
div.line-number:before {
  content: attr(data-linenumber);
}
.code-line {
  height: $lineHeight;
}
.comment .panel-body {
  padding: 10px 15px;
}
.comment-link {
  cursor: pointer;
  padding: 0;
  margin-right: 5px;
  background: none;
  border: none;
  color: #ddd;
}
.comment-link:focus {
  outline: none;
}
.comment-link:hover .fa-comment-o, .comment-link.has-comment .fa-comment-o, .comment-link .fa-comment {
  display: none;
}
.comment-link:hover .fa-comment, .comment-link.has-comment .fa-comment {
  display: inline;
  color: #000;
}
.comment-reply {
  padding-top: 10px;
}
</style>

<template lang="jade">
  div
    template(v-for="cluster in clusters")
      template(v-if="cluster.spacer")
        spacer(:file="file", :spacer="cluster.spacer")
      template(v-if="cluster.commentLine && (cluster.commentLine.comments.length > 0 || newCommentLine === cluster.commentLine)")
        div.inset-vertical.comment-cluster
          div.comment.panel.panel-primary(v-for="comment in cluster.commentLine.comments")
            div.panel-heading.panel-heading-nowrap
              div.panel-heading-tail(v-if="isUserLoggedIn(comment.user) && hasTeamPermissionComment")
                div.btn-group(role="group")
                  button.btn.btn-xs.btn-warning(@click="confirmDeleteComment(comment, cluster.commentLine, $event)") Delete
                  button.btn.btn-xs.btn-default(@click="toggleEditComment(comment, cluster.commentLine)") Edit
              div {{{comment.user | toUserWithAvatar}}} {{{comment.created | toTimeAgoSpan}}}
            div.panel.panel-body
              template(v-if="editingComment === comment")
                div.form-group
                  textarea#comment-text.form-control(v-model="commentText" rows="5")
                div.btn-group(role="group")
                  button.btn.btn-primary(@click="editComment()") Save
                  button.btn.btn-default(@click="toggleEditComment(comment, cluster.commentLine)") Cancel
              template(v-else) {{{comment.textAsHtml}}}
          div.comment-reply
            template(v-if="newCommentLine === cluster.commentLine")
              div.form-group
                textarea#comment-text.form-control(v-model="commentText" rows="5")
              div.btn-group(role="group")
                button.btn.btn-primary(@click="addComment()") Comment
                button.btn.btn-default(@click="toggleAddComment(cluster, $event)") Cancel
            template(v-else)
              button.btn.btn-primary(@click="toggleAddComment(cluster, $event)") Reply
      template(v-if="cluster.lines")
        div(:style="'height: ' + 18 * cluster.lines.length + 'px'", :class="{ 'line-cluster': !fromSpacer }")
          template(v-if="cluster.visible || isLocationHashTarget(cluster) || fromSpacer")
            template(v-for="codeLine in cluster.lines")
              template(v-if="fromSpacer")
                div.line-number(:data-linenumber="codeLine.beforeLineNumber | ifPositive")
                div.line-number(:data-linenumber="codeLine.afterLineNumber | ifPositive")
              template(v-else)
                a(:id="'line-' + linkName(codeLine)", :name="linkName(codeLine)")
                a(:href="'#' + linkName(codeLine)")
                  div.line-number(:data-linenumber="codeLine.beforeLineNumber | ifPositive")
                  div.line-number(:data-linenumber="codeLine.afterLineNumber | ifPositive")
              button.line-number.fa.comment-link(@click="toggleAddComment(cluster, $event, codeLine)", :class="{ 'has-comment': hasComment(codeLine) }")
                <span class="fa-comment"></span>
                <span class="fa-comment-o"></span>
              div.code.code-line(:class="codeLine.status | lowercase") {{{codeLine.highlightedCode}}}
</template>

<script lang="babel">
import * as Store from '../store'
import * as DomHelper from '../classes/DomHelper'
var CommitDetailSpacerView = require('./CommitDetailSpacer.vue')

module.exports = {
  props: ['file', 'clusters', 'index', 'fromSpacer'],
  data: function() {
    return {
      newCommentLine: null,
      commentText: null,
      editingComment: null,
      editingCommentLine: null
    }
  },
  vuex: {
    getters: {
      activeTeam: Store.activeTeam,
      hasTeamPermissionComment: Store.hasTeamPermissionComment
    },
    actions: {
      ajaxPost: Store.ajaxPost,
      isUserLoggedIn: Store.isUserLoggedIn,
      requireLogin: Store.requireLogin,
      showConfirmationPopover: Store.showConfirmationPopover,
      showPopover: Store.showPopover,
      updateNotifications: Store.updateNotifications
    }
  },
  components: {
    'spacer': CommitDetailSpacerView
  },
  ready: function() {
    $(document).on('click', 'a.vue-enhance', event => {
      this.$route.router.go(event.target.getAttribute('href'))
      event.preventDefault()
    })
  },
  methods: {
    linkName: function(codeLine) {
      return this.index + ',' + this.$options.filters.ifPositive(codeLine.beforeLineNumber) + ',' + this.$options.filters.ifPositive(codeLine.afterLineNumber)
    },
    isLocationHashTarget: function(cluster) {
      if (!window.location.hash) {
        return false
      }
      var [fileIndex, beforeLine, afterLine] = window.location.hash.substring(1).split(',').map(part => parseInt(part, 10) || null)
      if (fileIndex !== this.index) {
        return false
      }
      return cluster.lines.some(line => line.beforeLineNumber === beforeLine && line.afterLineNumber === afterLine)
    },
    openComment: function(initialText) {
      this.commentText = initialText
      DomHelper.waitForElement('comment-text', () => $('#comment-text').focus())
    },
    toggleAddComment: function(cluster, event, line) {
      if (cluster.commentLine) {
        line = cluster.commentLine
      }
      if (this.newCommentLine === line) {
        this.newCommentLine = null
        ga('send', 'event', 'commitComment', 'add', 'abort')
        return
      }
      if (this.requireLogin(event, 'write comments', '#commit-details')) {
        ga('send', 'event', 'commitComment', 'add', 'requireLogin')
        return
      }
      if (!this.hasTeamPermissionComment) {
        ga('send', 'event', 'commitComment', 'add', 'requireMembership')
        this.showPopover(event, {
          title: 'Team Membership required',
          content: 'Only team-members may comment within this team.',
          container: '#commit-details'
        })
        return
      }
      ga('send', 'event', 'commitComment', 'add', 'start')
      if (cluster.lines) {
        this.splitClustersForNewComment(cluster, line)
      }
      this.newCommentLine = line
      this.openComment(null)
      this.activateTextcomplete()
    },
    splitClustersForNewComment: function(cluster, line) {
      var clusterIndex = this.clusters.indexOf(cluster)
      if (this.clusters[clusterIndex + 1] && this.clusters[clusterIndex + 1].commentLine === line) {
        return
      }
      var lineIndex = cluster.lines.indexOf(line)
      var newLineCluster = { lines: cluster.lines.splice(lineIndex + 1), visible: true }
      this.clusters.splice(clusterIndex + 1, 0, { commentLine: line }, newLineCluster)
    },
    hasComment: function(line) {
      return this.newCommentLine === line || line.comments.length > 0
    },
    addComment: function() {
      ga('send', 'event', 'commitComment', 'add', 'finish')
      var commentParameters = {
        commitHash: this.$route.params.commitHash,
        teamId: this.activeTeam.id,
        text: this.commentText,
        beforePath: this.file.beforePath,
        afterPath: this.file.afterPath,
        beforeLineNumber: this.newCommentLine.beforeLineNumber,
        afterLineNumber: this.newCommentLine.afterLineNumber
      }
      this.ajaxPost('/comment/add', commentParameters, data => {
        this.newCommentLine.comments = data
        this.newCommentLine = null
        this.commentText = null
        this.updateNotifications()
      })
    },
    toggleEditComment: function(comment, line) {
      if (this.editingComment === comment) {
        this.editingComment = null
        ga('send', 'event', 'commitComment', 'edit', 'abort')
        return
      }
      ga('send', 'event', 'commitComment', 'edit', 'start')
      this.editingComment = comment
      this.editingCommentLine = line
      this.openComment(comment.text)
      this.activateTextcomplete()
    },
    editComment: function() {
      ga('send', 'event', 'commitComment', 'edit', 'finish')
      var editParameters = {
        commitHash: this.$route.params.commitHash,
        teamId: this.activeTeam.id,
        commentId: this.editingComment.id,
        text: this.commentText
      }
      this.ajaxPost('/comment/edit', editParameters, data => {
        this.editingCommentLine.comments = data
        this.editingComment = null
        this.editingCommentLine = null
        this.commentText = null
      })
    },
    confirmDeleteComment: function(comment, line, event) {
      ga('send', 'event', 'commitComment', 'delete', 'start')
      this.showConfirmationPopover(event, 'Delete', 'danger', () => this.deleteComment(comment, line), {
        title: 'Confirm deleting comment',
        content: 'Do you really want to delete this comment?',
        container: '#commit-details'
      })
    },
    deleteComment: function(comment, line) {
      ga('send', 'event', 'commitComment', 'delete', 'finish')
      var deleteParameters = {
        commitHash: this.$route.params.commitHash,
        teamId: this.activeTeam.id,
        commentId: comment.id
      }
      this.ajaxPost('/comment/delete', deleteParameters, data => {
        line.comments = data
        this.updateNotifications()
      })
    },
    activateTextcomplete: function() {
      this.$nextTick(() => {
        $('#comment-text').textcomplete([{
          match: new RegExp('\\B@(|' + this.sharedConfig.namePattern + ')$'),
          index: 1,
          search: (term, callback) => {
            var words = this.activeTeam.filters.users.map(user => user.name)
            callback($.map(words, word => word.search(new RegExp(term, "i")) !== -1 ? word : null))
          },
          replace: word => '@' + word
        }]).overlay([
          {
            match: new RegExp('\\B@' + this.sharedConfig.namePattern, 'g'),
            css: {
              'background-color': '#ddd'
            }
          }
        ])
      })
    }
  }
}
</script>
