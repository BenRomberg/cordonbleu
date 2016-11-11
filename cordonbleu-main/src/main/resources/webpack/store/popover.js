const ensurePopover = function ({ dispatch, state }, eventOrSelector, options, additionalHideSelector, popoverClass, showCallback) {
  var element = typeof(eventOrSelector) === 'string' ? $(eventOrSelector) : $(eventOrSelector.currentTarget)
  if (state.popover.popovers.indexOf(element[0]) < 0) {
    dispatch('ADD_POPOVER', element[0])
    var additionalClass = popoverClass ? ' ' + popoverClass : ''
    options['template'] = '<div class="popover' + additionalClass + '" role="tooltip"><div class="arrow"></div><h3 class="popover-title"></h3><div class="popover-content"></div></div>'
    options['trigger'] = 'manual'
    options['placement'] = options['placement'] || 'bottom'
    options['container'] = options['container'] || 'body'
    options['viewport'] = options['container']
    element.popover(options)
    if (showCallback) {
      element.one('show.bs.popover', showCallback)
    }
    element.one('hidden.bs.popover', () => {
      dispatch('REMOVE_POPOVER', element[0])
      element.popover('destroy')
    })
  }
  var isOnElement = (containingElement, target) => containingElement.get(0) === target || containingElement.has(target).length
  $('#app' + (additionalHideSelector ? ', ' + additionalHideSelector : '')).on('click', function(event) {
    if (isOnElement(element, event.target) || isOnElement($('.popover'), event.target)) {
      return
    }
    $(this).off(event)
    element.popover('hide')
  })
  return element
}

const state = {
  popovers: []
}

const mutations = {
  ADD_POPOVER (state, element) {
    state.popovers.push(element)
  },
  REMOVE_POPOVER (state, element) {
    state.popovers.splice(state.popovers.indexOf(element), 1)
  }
}


export const showPopover = function ({ dispatch, state }, eventOrSelector, options, additionalHideSelector, popoverClass, showCallback) {
  return ensurePopover({ dispatch, state }, eventOrSelector, options, additionalHideSelector, popoverClass, showCallback).popover('show')
}
export const togglePopover = function ({ dispatch, state }, eventOrSelector, options, additionalHideSelector, popoverClass, showCallback) {
  return ensurePopover({ dispatch, state }, eventOrSelector, options, additionalHideSelector, popoverClass, showCallback).popover('toggle')
}
export const showConfirmationPopover = function ({ dispatch, state }, eventOrSelector, confirmButtonLabel, confirmButtonClass, callback, options) {
  options['html'] = true
  options['content'] = 
    '<form id="confirmation-form" class="form-inline" data-toggle="validator">' +
      options['content'] +
      '<div class="btn-group popover-button-group">' +
        '<button id="cancel-button" class="btn btn-default" type="button">Cancel</button>' +
        '<button id="confirm-button" class="btn btn-' + confirmButtonClass + '" type="submit">' + confirmButtonLabel + '</button>' +
      '</div>' +
    '</form>'
  var element = showPopover({ dispatch, state }, eventOrSelector, options)
  $('#confirm-button').on('click', event => {
    element.popover('hide')
    event.preventDefault()
    callback()
  })
  $('#cancel-button').on('click', () => element.popover('hide'))
}

export default {
  state, mutations
}
