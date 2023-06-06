import * as ActionTypes from '../_actions/me-actions'

const initialState = {
    contact: {},
    error: null
};

export function me(state = initialState, action) {
    switch (action.type) {
        case ActionTypes.ME_SUCCESS:
            return Object.assign({}, state, {
                contact: action.contact
            });

        case ActionTypes.ME_FAILURE:
            return Object.assign({}, state, {
                error: action.error
            });

        default:
            return state
    }
}
