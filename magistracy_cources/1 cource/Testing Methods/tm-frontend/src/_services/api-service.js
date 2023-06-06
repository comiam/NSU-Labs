import { authHeader } from '../_helpers';

const config = {
    apiUrl: process.env.REACT_APP_BACKEND_URL
};

export const apiService = {
    login,
    me,

    getCustomers,
    createCustomer,
    deleteCustomer,

    getPlans,
    createPlan,
    deletePlan,

    getAvailableSubscriptions,
    createSubscription,
    deleteSubscription,

    getAvailablePlans,

    topUpBalance
};

function login(username, password) {
    const requestOptions = {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            "login": username,
            "pass": password
        })
    };

    return fetch(`${config.apiUrl}/authenticate`, requestOptions).then(handleResponse)
}

function me() {
    const requestOptions = {
        method: 'GET',
        headers: authHeader()
    };

    return fetch(`${config.apiUrl}/me`, requestOptions).then(handleResponse);
}

function getCustomers() {
    const requestOptions = {
        method: 'GET',
        headers: authHeader()
    };

    return fetch(`${config.apiUrl}/customers`, requestOptions).then(handleResponse);
}

function createCustomer(login, pass, firstName, lastName) {
    const requestOptions = {
        method: 'POST',
        headers: { ...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify({
            "first_name": firstName,
            "last_name": lastName,
            "login": login,
            "pass": pass
        })
    };

    return fetch(`${config.apiUrl}/customers`, requestOptions).then(handleResponse)
}

function deleteCustomer(id) {
    const requestOptions = {
        method: 'DELETE',
        headers: { ...authHeader(), 'Content-Type': 'application/json' }
    };

    return fetch(`${config.apiUrl}/customers/${id}`, requestOptions).then(handleResponse)
}

function getPlans() {
    const requestOptions = {
        method: 'GET',
        headers: authHeader()
    };

    return fetch(`${config.apiUrl}/plans`, requestOptions).then(handleResponse);
}

function createPlan(name, details, fee) {
    const requestOptions = {
        method: 'POST',
        headers: { ...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify({
            "name": name,
            "details": details,
            "fee": fee
        })
    };

    return fetch(`${config.apiUrl}/plans`, requestOptions).then(handleResponse)
}

function deletePlan(id) {
    const requestOptions = {
        method: 'DELETE',
        headers: { ...authHeader(), 'Content-Type': 'application/json' }
    };

    return fetch(`${config.apiUrl}/plans/${id}`, requestOptions).then(handleResponse)
}

function getAvailableSubscriptions() {
    const requestOptions = {
        method: 'GET',
        headers: authHeader()
    };

    return fetch(`${config.apiUrl}/available_subscriptions`, requestOptions).then(handleResponse);
}

function createSubscription(planId) {
    const requestOptions = {
        method: 'POST',
        headers: { ...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify({
            "plan_id": planId
        })
    };

    return fetch(`${config.apiUrl}/subscriptions`, requestOptions).then(handleResponse)
}

function deleteSubscription(id) {
    const requestOptions = {
        method: 'DELETE',
        headers: { ...authHeader(), 'Content-Type': 'application/json' }
    };

    return fetch(`${config.apiUrl}/subscriptions/${id}`, requestOptions).then(handleResponse)
}

function getAvailablePlans() {
    const requestOptions = {
        method: 'GET',
        headers: authHeader()
    };

    return fetch(`${config.apiUrl}/available_plans`, requestOptions).then(handleResponse);
}

function topUpBalance(money) {
    const requestOptions = {
        method: 'POST',
        headers: { ...authHeader(), 'Content-Type': 'application/json' },
        body: JSON.stringify({
            "money": money
        })
    };

    return fetch(`${config.apiUrl}/customers/top_up_balance`, requestOptions).then(handleResponse)
}

function handleResponse(response) {
    return response.text().then(text => {
        let jsonData = {};
        try {
            jsonData = text && JSON.parse(text);
        } catch (exception) {
            if (!response.ok) {
                console.log(`Warning: format of response is not JSON. Please fix it... Text: '${text}'.`)
                jsonData.message = text;
            } else {
                throw(exception);
            }
        }

        if (!response.ok) {
            if (response.status === 401) {
                // auto logout if 401 response returned from api
                apiService.logout();
                //location.reload(true);
            }

            const error = jsonData.message || response.statusText;
            return Promise.reject(error);
        }

        return jsonData;
    });
}
