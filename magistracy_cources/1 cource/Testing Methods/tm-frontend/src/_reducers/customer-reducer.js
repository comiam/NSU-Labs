import * as ActionTypes from '../_actions/customer-actions'

const initialState = {
    availableSubscriptions: [],
    availablePlans: [],
    error: null,
    createdSubscription: null
};

export function customer(state = initialState, action) {
    switch (action.type) {
        case ActionTypes.GET_AVAILABLE_SUBSCRIPTIONS_SUCCESS:
            return Object.assign({}, state, {
                availableSubscriptions: action.availableSubscriptions
            });

        case ActionTypes.GET_AVAILABLE_SUBSCRIPTIONS_FAILURE:
            return Object.assign({}, state, {
                error: action.error
            });

        case ActionTypes.GET_AVAILABLE_PLANS_SUCCESS:
            return Object.assign({}, state, {
                availablePlans: action.availablePlans
            });

        case ActionTypes.GET_AVAILABLE_PLANS_FAILURE:
            return Object.assign({}, state, {
                error: action.error
            });

        case ActionTypes.CREATE_SUBSCRIPTION_FAILURE:
            return Object.assign({}, state, {
                error: action.error
            });

        case ActionTypes.DELETE_SUBSCRIPTION_FAILURE:
            return Object.assign({}, state, {
                error: action.error
            });

        case ActionTypes.TOP_UP_BALANCE_FAILURE:
            return Object.assign({}, state, {
                error: action.error
            });

        default:
            return state
    }
}
