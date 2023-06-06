import * as ActionTypes from '../_actions/auth-actions'

let account = JSON.parse(localStorage.getItem('account'));
const initialState = account ? {loggedIn: true, account} : {};

export function auth(state = initialState, action) {
    switch (action.type) {
        case ActionTypes.LOGIN_REQUEST:
            return {
                loggingIn: true
            };
        case ActionTypes.LOGIN_SUCCESS:
            return {
                loggedIn: true,
                account: action.account
            };
        case ActionTypes.LOGIN_FAILURE:
            return {};
        case ActionTypes.LOGOUT:
            return {};
        default:
            return state
    }
}
