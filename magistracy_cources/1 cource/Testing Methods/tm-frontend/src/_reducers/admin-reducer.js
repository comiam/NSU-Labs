import * as ActionTypes from '../_actions/admin-actions'

const initialState = {
    customers: [],
    plans: [],
    error: null,
    createdCustomer: null
};

export function admin(state = initialState, action) {
    switch (action.type) {
        case ActionTypes.GET_CUSTOMERS_SUCCESS:
            return Object.assign({}, state, {
                customers: action.customers
            });

        case ActionTypes.GET_CUSTOMERS_FAILURE:
            return Object.assign({}, state, {
                error: action.error
            });

        case ActionTypes.CREATE_CUSTOMER_REQUEST:
            return Object.assign({}, state, {
                createdCustomer: action.createdCustomer
            });

        case ActionTypes.DELETE_CUSTOMER_SUCCESS:
            let customers = [];

            state.customers.forEach(customer => {
                if (customer.id !== action.id) {
                    customers.push(customer);
                }
            });

            return Object.assign({}, state, {
                customers: customers
            });

        case ActionTypes.GET_PLANS_SUCCESS:
            return Object.assign({}, state, {
                plans: action.plans
            });

        case ActionTypes.GET_PLANS_FAILURE:
            return Object.assign({}, state, {
                error: action.error
            });

        case ActionTypes.DELETE_PLAN_SUCCESS:
            let plans = [];

            state.plans.forEach(plan => {
                if (plan.id !== action.id) {
                    plans.push(plan);
                }
            });

            return Object.assign({}, state, {
                plans: plans
            });

        default:
            return state
    }
}
