var INITIAL_OFFSET = 0

module.exports = function(containerId, limitIncrement, listLengthCallback, appendCallback) {
  var currentOffset = INITIAL_OFFSET

  var listCouldUseMoreCommits = () => {
    var container = document.getElementById(containerId)
    return (container.scrollTop + container.getBoundingClientRect().height + 200) > container.scrollHeight && (currentOffset + limitIncrement) === listLengthCallback()
  }

  this.scroll = () => {
    if (listCouldUseMoreCommits()) {
      currentOffset += limitIncrement
      appendCallback(this.getCurrentLimit())
    }
  }

  this.reset = () => currentOffset = INITIAL_OFFSET

  this.getCurrentLimit = () => currentOffset + limitIncrement
}
