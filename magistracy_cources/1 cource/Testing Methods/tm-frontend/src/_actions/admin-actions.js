import { apiService } from '../_services';
import { actions } from './';
import { history } from '../_helpers';

export const GET_CUSTOMERS_REQUEST = 'GET_CUSTOMERS_REQUEST'
export const GET_CUSTOMERS_SUCCESS = 'GET_CUSTOMERS_SUCCESS'
export const GET_CUSTOMERS_FAILURE = 'GET_CUSTOMERS_FAILURE'

export const CREATE_CUSTOMER_REQUEST = 'CREATE_CUSTOMER_REQUEST'
export const CREATE_CUSTOMER_SUCCESS = 'CREATE_CUSTOMER_SUCCESS'
export const CREATE_CUSTOMER_FAILURE = 'CREATE_CUSTOMER_FAILURE'

export const DELETE_CUSTOMER_REQUEST = 'DELETE_CUSTOMER_REQUEST'
export const DELETE_CUSTOMER_SUCCESS = 'DELETE_CUSTOMER_SUCCESS'
export const DELETE_CUSTOMER_FAILURE = 'DELETE_CUSTOMER_FAILURE'

export const GET_PLANS_REQUEST = 'GET_PLANS_REQUEST'
export const GET_PLANS_SUCCESS = 'GET_PLANS_SUCCESS'
export const GET_PLANS_FAILURE = 'GET_PLANS_FAILURE'

export const CREATE_PLAN_REQUEST = 'CREATE_PLAN_REQUEST'
export const CREATE_PLAN_SUCCESS = 'CREATE_PLAN_SUCCESS'
export const CREATE_PLAN_FAILURE = 'CREATE_PLAN_FAILURE'

export const DELETE_PLAN_REQUEST = 'DELETE_PLAN_REQUEST'
export const DELETE_PLAN_SUCCESS = 'DELETE_PLAN_SUCCESS'
export const DELETE_PLAN_FAILURE = 'DELETE_PLAN_FAILURE'

export const adminActions = {
    getCustomers,
    createCustomer,
    deleteCustomer,

    getPlans,
    createPlan,
    deletePlan
};

function getCustomers() {
    return dispatch => {
        dispatch(request());

        apiService.getCustomers()
            .then(
                customers => dispatch(success(customers)),
                error => dispatch(failure(error))
            );
    };

    function request() { return { type: GET_CUSTOMERS_REQUEST } }
    function success(customers) { return { type: GET_CUSTOMERS_SUCCESS, customers } }
    function failure(error) { return { type: GET_CUSTOMERS_FAILURE, error } }
}

function createCustomer(username, pass, firstName, lastName) {
    return dispatch => {
        dispatch(request());

        apiService.createCustomer(username, pass, firstName, lastName)
            .then(
                createdCustomer => {
                    dispatch(success(createdCustomer));

                    history.push('/admin');
                },
                error => {
                    dispatch(failure(error));

                    dispatch(actions.error(error));
                }
            );
    };

    function request() { return { type: CREATE_CUSTOMER_REQUEST } }
    function success(createdCustomer) { return { type: CREATE_CUSTOMER_SUCCESS, createdCustomer } }
    function failure(error) { return { type: CREATE_CUSTOMER_FAILURE, error } }
}

function deleteCustomer(id) {
    return dispatch => {
        dispatch(request());

        apiService.deleteCustomer(id)
            .then(
                () => {
                    dispatch(success(id));
                },
                error => {
                    dispatch(failure(error));

                    dispatch(actions.error(error));
                }
            );
    };

    function request() { return { type: DELETE_CUSTOMER_REQUEST } }
    function success(id) { return { type: DELETE_CUSTOMER_SUCCESS, id } }
    function failure(error) { return { type: DELETE_CUSTOMER_FAILURE, error } }
}

function getPlans() {
    return dispatch => {
        dispatch(request());

        apiService.getPlans()
            .then(
                plans => dispatch(success(plans)),
                error => dispatch(failure(error))
            );
    };

    function request() { return { type: GET_PLANS_REQUEST } }
    function success(plans) { return { type: GET_PLANS_SUCCESS, plans } }
    function failure(error) { return { type: GET_PLANS_FAILURE, error } }
}

function createPlan(name, details, fee) {
    return dispatch => {
        dispatch(request());

        apiService.createPlan(name, details, fee)
            .then(
                createdPlan => {
                    dispatch(success(createdPlan));

                    history.push('/admin');
                },
                error => {
                    dispatch(failure(error));

                    dispatch(actions.error(error));
                }
            );
    };

    function request() { return { type: CREATE_PLAN_REQUEST } }
    function success(createdPlan) { return { type: CREATE_PLAN_SUCCESS, createdPlan } }
    function failure(error) { return { type: CREATE_PLAN_FAILURE, error } }
}

function deletePlan(id) {
    return dispatch => {
        dispatch(request());

        apiService.deletePlan(id)
            .then(
                () => {
                    dispatch(success(id));
                },
                error => {
                    dispatch(failure(error));

                    dispatch(actions.error(error));
                }
            );
    };

    function request() { return { type: DELETE_PLAN_REQUEST } }
    function success(id) { return { type: DELETE_PLAN_SUCCESS, id } }
    function failure(error) { return { type: DELETE_PLAN_FAILURE, error } }
}
