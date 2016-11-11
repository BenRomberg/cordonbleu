import Vue from 'vue'

const ajax = function ({ dispatch }, method, path, body, callback, statusMap, hide) {
  var loadingFactor = hide ? 0 : 1
  dispatch('AJAX_LOADING_START', loadingFactor)
  window.setTimeout(() => dispatch('AJAX_LOADING_DELAY', loadingFactor), 250)
  Vue.http[method]('/api' + path, body, {
    timeout: 60000
  }).then(response => {
    dispatch('AJAX_LOADING_FINISH', loadingFactor)
    callback(response.data, response)
  }, response => {
    dispatch('AJAX_LOADING_FINISH', loadingFactor)
    if (response.status === 412) {
      dispatch('NEW_WEBSITE_VERSION')
      return
    }
    if (statusMap && statusMap[response.status]) {
      statusMap[response.status](response.data, response)
      return
    }
    dispatch('AJAX_LOADING_ERROR', loadingFactor)
    window.setTimeout(() => dispatch('AJAX_LOADING_ERROR_DELAY', loadingFactor), 5000)
    console.log(response)
  })
}

const state = {
  ajaxLoading: 0,
  ajaxError: 0,
  newWebsiteVersion: false
}

const mutations = {
  AJAX_LOADING_START (state, factor) {
    state.ajaxLoading += factor * 2
  },
  AJAX_LOADING_DELAY (state, factor) {
    state.ajaxLoading -= factor
  },
  AJAX_LOADING_FINISH (state, factor) {
    state.ajaxLoading -= factor
  },
  AJAX_LOADING_ERROR (state, factor) {
    state.ajaxError += factor
  },
  AJAX_LOADING_ERROR_DELAY (state, factor) {
    state.ajaxError -= factor
  },
  NEW_WEBSITE_VERSION (state) {
    state.newWebsiteVersion = true
  }
}

export function ajaxLoading (state) {
  return state.ajax.ajaxLoading
}
export function ajaxError (state) {
  return state.ajax.ajaxError
}
export function newWebsiteVersion (state) {
  return state.ajax.newWebsiteVersion
}

export const ajaxGet = function ({ dispatch }, path, queryParameters, callback, statusMap, hide) {
  ajax({ dispatch }, 'get', path, queryParameters, callback, statusMap, hide)
}
export const ajaxPost = function ({ dispatch }, path, body, callback, statusMap, hide) {
  ajax({ dispatch }, 'post', path, body, callback, statusMap, hide)
}

export default {
  state, mutations
}
