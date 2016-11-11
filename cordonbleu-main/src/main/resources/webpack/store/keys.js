const state = {
  keyMap: {}
}

const mutations = {
  ADD_KEY (state, key, callback) {
    state.keyMap[key.toUpperCase().charCodeAt()] = { key: key, callback: callback }
  }
}

export const installKeyHandler = function ({ state }) {
  $(document).keydown(event => {
    if (['TEXTAREA', 'INPUT'].indexOf(event.target.tagName) >= 0 || event.ctrlKey || event.altKey || event.metaKey) {
      return
    }
    if (state.keys.keyMap[event.which]) {
      state.keys.keyMap[event.which].callback()
      ga('send', 'event', 'shortcut', state.keys.keyMap[event.which].key)
    }
  })
}
export const onKeyDown = function({ dispatch }, key, callback) {
  dispatch('ADD_KEY', key, callback)
}

export default {
  state, mutations
}
