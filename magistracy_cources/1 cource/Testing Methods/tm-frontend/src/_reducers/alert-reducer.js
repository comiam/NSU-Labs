import * as ActionTypes from '../_actions/actions'

const initialState = {
    message: null,
    type: null
};

export function alert(state = initialState, action) {
    switch (action.type) {
        case ActionTypes.ALERT_SUCCESS:
            return {
                type: 'alert-success',
                message: action.message
            };
        case ActionTypes.ALERT_ERROR:
            return {
                type: 'alert-danger',
                message: action.message
            };
        case ActionTypes.ALERT_CLEAR:
            return {
                type: null,
                message: null
            };
        default:
            return state
    }
}
