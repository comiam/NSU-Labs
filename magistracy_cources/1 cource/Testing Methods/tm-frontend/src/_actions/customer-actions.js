import { apiService } from '../_services';
import { actions } from './';
import {history} from "../_helpers";

export const GET_AVAILABLE_SUBSCRIPTIONS_REQUEST = 'GET_AVAILABLE_SUBSCRIPTIONS_REQUEST'
export const GET_AVAILABLE_SUBSCRIPTIONS_SUCCESS = 'GET_AVAILABLE_SUBSCRIPTIONS_SUCCESS'
export const GET_AVAILABLE_SUBSCRIPTIONS_FAILURE = 'GET_AVAILABLE_SUBSCRIPTIONS_FAILURE'

export const CREATE_SUBSCRIPTION_REQUEST = 'CREATE_SUBSCRIPTION_REQUEST'
export const CREATE_SUBSCRIPTION_SUCCESS = 'CREATE_SUBSCRIPTION_SUCCESS'
export const CREATE_SUBSCRIPTION_FAILURE = 'CREATE_SUBSCRIPTION_FAILURE'

export const DELETE_SUBSCRIPTION_REQUEST = 'DELETE_SUBSCRIPTION_REQUEST'
export const DELETE_SUBSCRIPTION_SUCCESS = 'DELETE_SUBSCRIPTION_SUCCESS'
export const DELETE_SUBSCRIPTION_FAILURE = 'DELETE_SUBSCRIPTION_FAILURE'

export const GET_AVAILABLE_PLANS_REQUEST = 'GET_AVAILABLE_PLANS_REQUEST'
export const GET_AVAILABLE_PLANS_SUCCESS = 'GET_AVAILABLE_PLANS_SUCCESS'
export const GET_AVAILABLE_PLANS_FAILURE = 'GET_AVAILABLE_PLANS_FAILURE'

export const TOP_UP_BALANCE_REQUEST = 'TOP_UP_BALANCE_REQUEST'
export const TOP_UP_BALANCE_SUCCESS = 'TOP_UP_BALANCE_SUCCESS'
export const TOP_UP_BALANCE_FAILURE = 'TOP_UP_BALANCE_FAILURE'

export const customerActions = {
    getAvailableSubscriptions,
    createSubscription,
    deleteSubscription,

    getAvailablePlans,

    topUpBalance
};

function getAvailableSubscriptions() {
    return dispatch => {
        dispatch(request());

        apiService.getAvailableSubscriptions()
            .then(
                availableSubscriptions => dispatch(success(availableSubscriptions)),
                error => dispatch(failure(error))
            );
    };

    function request() { return { type: GET_AVAILABLE_SUBSCRIPTIONS_REQUEST } }
    function success(availableSubscriptions) { return { type: GET_AVAILABLE_SUBSCRIPTIONS_SUCCESS, availableSubscriptions } }
    function failure(error) { return { type: GET_AVAILABLE_SUBSCRIPTIONS_FAILURE, error } }
}

function createSubscription(planId) {
    return dispatch => {
        dispatch(request());

        apiService.createSubscription(planId)
            .then(
                createdSubscription => {
                    dispatch(success(createdSubscription));

                    dispatch(getAvailablePlans());

                    dispatch(getAvailableSubscriptions());
                },
                error => {
                    dispatch(failure(error));

                    dispatch(actions.error(error));
                }
            );
    };

    function request() { return { type: CREATE_SUBSCRIPTION_REQUEST } }
    function success(createdSubscription) { return { type: CREATE_SUBSCRIPTION_SUCCESS, createdSubscription } }
    function failure(error) { return { type: CREATE_SUBSCRIPTION_FAILURE, error } }
}

function deleteSubscription(id) {
    return dispatch => {
        dispatch(request());

        apiService.deleteSubscription(id)
            .then(
                () => {
                    dispatch(success(id));

                    dispatch(getAvailablePlans());

                    dispatch(getAvailableSubscriptions());
                },
                error => {
                    dispatch(failure(error));

                    dispatch(actions.error(error));
                }
            );
    };

    function request() { return { type: DELETE_SUBSCRIPTION_REQUEST } }
    function success(id) { return { type: DELETE_SUBSCRIPTION_SUCCESS, id } }
    function failure(error) { return { type: DELETE_SUBSCRIPTION_FAILURE, error } }
}

function getAvailablePlans() {
    return dispatch => {
        dispatch(request());

        apiService.getAvailablePlans()
            .then(
                availablePlans => dispatch(success(availablePlans)),
                error => dispatch(failure(error))
            );
    };

    function request() { return { type: GET_AVAILABLE_PLANS_REQUEST } }
    function success(availablePlans) { return { type: GET_AVAILABLE_PLANS_SUCCESS, availablePlans } }
    function failure(error) { return { type: GET_AVAILABLE_PLANS_FAILURE, error } }
}

function topUpBalance(money) {
    return dispatch => {
        dispatch(request());

        apiService.topUpBalance(money)
            .then(
                () => {
                    dispatch(success());

                    history.push('/customer');
                },
                error => dispatch(failure(error))
            );
    };

    function request() { return { type: TOP_UP_BALANCE_REQUEST } }
    function success() { return { type: TOP_UP_BALANCE_SUCCESS } }
    function failure(error) { return { type: TOP_UP_BALANCE_FAILURE, error } }
}
