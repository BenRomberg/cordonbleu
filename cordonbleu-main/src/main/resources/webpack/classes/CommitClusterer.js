module.exports = function() {
  this.clusterCommit = function(commit) {
    commit.files.forEach(file => {
      file.clusters = this.clusterCommitLines(file.codeLines)
    })
    return commit
  }

  this.clusterCommitLines = function(lines) {
    var clusters = []
    var currentLineCluster = []
    var pushCurrentLineCluster = () => {
      if (currentLineCluster.length > 0) {
        clusters.push({ lines: currentLineCluster, visible: false })
        currentLineCluster = []
      }
    }
    lines.forEach(line => {
      if (line.spacer) {
        pushCurrentLineCluster()
        clusters.push({ spacer: line.spacer })
        return
      }
      currentLineCluster.push(line.line)
      if (line.line.comments.length > 0) {
        pushCurrentLineCluster()
        clusters.push({ commentLine: line.line })
      }
    })
    pushCurrentLineCluster()
    return clusters
  }
}
