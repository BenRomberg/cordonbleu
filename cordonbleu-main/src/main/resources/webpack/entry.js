var Vue = require('vue')
var Router = require('vue-router')
var App = require('./components/App.vue')
var CommitView = require('./components/Commit.vue')
var WelcomeView = require('./components/Welcome.vue')
var CommitDetailView = require('./components/CommitDetail.vue')
var RepositoryView = require('./components/Repository.vue')
var CommitDetailLinesView = require('./components/CommitDetailLines.vue')
var ProfileView = require('./components/Profile.vue')
var UserManagementView = require('./components/UserManagement.vue')
var TeamMemberView = require('./components/TeamMember.vue')
var TeamCreateView = require('./components/TeamCreate.vue')
var NameContestView = require('./components/NameContest.vue')
var TeamSettingsView = require('./components/TeamSettings.vue')

require('./global.scss')
var sharedConfig = require('json!./sharedConfig.json')

Vue.config.debug = true

Vue.mixin({
  data: function() {
    return {
      sharedConfig: sharedConfig,
      intervals: {}
    }
  },
  detached: function() {
    for (var intervalName in this.intervals) {
      window.clearInterval(this.intervals[intervalName])
      delete this.intervals[intervalName]
    }
  },
  methods: {
    runInInterval: function(name, intervalInSeconds, callback) {
      var interval = window.setInterval(callback.bind(this), intervalInSeconds * 1000)
      this.intervals[name] = interval
    },
    calcNumComments: function(commit) {
      var toSum = (previous, current) => previous + current
      return commit.files.map(file => file.codeLines.filter(line => line.line).map(line => line.line.comments.length).reduce(toSum, 0)).reduce(toSum, 0)
    }
  }
})
Vue.use(Router)
Vue.use(require('vue-resource'))
Vue.http.headers.common['X-WebsiteVersion'] = window.configuration.websiteVersion
Vue.component('code-lines', CommitDetailLinesView)

var localeData = moment().locale(window.navigator.userLanguage || window.navigator.language || 'en').localeData()
var localizedDateFormat = 'ddd, ' + localeData.longDateFormat('L') + ' ' + localeData.longDateFormat('LTS')
var userAvatar = (user, size) => '<img src="http://www.gravatar.com/avatar/' + md5(user.email.toLowerCase()) + '?s=' + size + '&d=retro" class="profile-image-' + size + '" />'
var escapeHtml = html => html.replace(/[&<>]/g, letter => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;' }[letter]))
var commitAuthor = author => author.name + ' <' + author.email + '>'

var timeAgo = (value, withoutAgo) => moment(value, 'x').fromNow(withoutAgo)
var fullTime = value => moment(value, 'x').format(localizedDateFormat)
Vue.filter('toTimeAgo', timeAgo)
Vue.filter('toFullTime', fullTime)
Vue.filter('toTimeAgoSpan', (value, withoutAgo) => '<span title="' + fullTime(value) + '">' + timeAgo(value, withoutAgo) + '</span>')
Vue.filter('ifPositive', value => value > 0 ? value : '')
Vue.filter('toCommitAuthor', commitAuthor)
Vue.filter('toCommitAuthorWithAvatar', author => '<span title="' + escapeHtml(author.name + ' <' + author.email + '>') + '">' + userAvatar(author, 18) + ' ' + escapeHtml(author.name)) + '</span>'
Vue.filter('toUserWithAvatar', user => userAvatar(user, 18) + ' <b' + (user.inactive ? ' class="user-inactive"' : '') + '>' + user.name + '</b>')
Vue.filter('toAvatar', userAvatar)
Vue.filter('toPluralS', value => value > 1 ? 's' : '')

var router = new Router({
  history: true,
  linkActiveClass: 'active'
})

router.map({
  '/': {
    name: 'welcome',
    component: WelcomeView
  },
  '/team/:teamName': {
    name: 'commits',
    component: CommitView,
    subRoutes: {
      '/': {
        component: {
          template: '<div id="commit-details" class="centering-root"><div class="centering-wrapper"><div class="centering">Select a commit on the left to get started!</div></div></div>'
        }
      },
      '/commit/:commitHash': {
        name: 'commitDetail',
        component: CommitDetailView
      }
    }
  },
  '/team/:teamName/settings/general': {
    name: 'team-settings',
    component: TeamSettingsView
  },
  '/team/:teamName/settings/repositories': {
    name: 'repositories',
    component: RepositoryView
  },
  '/team/:teamName/settings/members': {
    name: 'team-members',
    component: TeamMemberView
  },
  '/settings/users': {
    name: 'user-management',
    component: UserManagementView
  },
  '/settings/profile': {
    name: 'profile',
    component: ProfileView
  },
  '/createTeam': {
    name: 'create-team',
    component: TeamCreateView
  },
  '/nameContest': {
    name: 'nameContest',
    component: NameContestView
  }
})

router.afterEach(transition => {
  ga('set', 'page', transition.to.fullPath)
  ga('send', 'pageview')
})

router.redirect({
  '*': '/'
})

router.start(App, '#app')
