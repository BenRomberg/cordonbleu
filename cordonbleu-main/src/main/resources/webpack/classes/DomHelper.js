export const waitForElement = function(id, callback) {
  var waitOrExecute = () => {
    if (!document.getElementById(id)) {
      window.requestAnimationFrame(waitOrExecute)
      return
    }
    callback()
  }
  waitOrExecute()
}
