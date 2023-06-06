import { apiService } from '../_services';
import { actions } from './';
import { history } from '../_helpers';

export const LOGIN_REQUEST = 'LOGIN_REQUEST'
export const LOGIN_SUCCESS = 'LOGIN_SUCCESS'
export const LOGIN_FAILURE = 'LOGIN_FAILURE'

export const LOGOUT = 'LOGOUT'

export const authActions = {
    login,
    logout
};

function login(username, password) {
    return dispatch => {
        dispatch(request());

        apiService.login(username, password)
            .then(
                account => {
                    // store user details and jwt token in local storage to keep user logged in between page refreshes.
                    localStorage.setItem('account', JSON.stringify(account));

                    if (!account || !account.authorities) {
                        let error = "No authorities in account...";
                        dispatch(failure(error));
                        dispatch(actions.error(error));
                        history.push('/');
                        return;
                    }

                    if (account.authorities.includes("ADMIN")) {
                        dispatch(success(account));
                        history.push('/admin');
                        return;
                    }

                    if (account.authorities.includes("CUSTOMER")) {
                        dispatch(success(account));
                        history.push('/customer');
                        return;
                    }

                    let error = "Unknown authority...";
                    dispatch(failure(error));
                    dispatch(actions.error(error));

                    history.push('/');
                },
                error => {
                    dispatch(failure(error));
                    dispatch(actions.error(error));
                }
            );
    };

    function request() { return { type: LOGIN_REQUEST } }
    function success(account) { return { type: LOGIN_SUCCESS, account } }
    function failure(error) { return { type: LOGIN_FAILURE, error } }
}

function logout() {
    // remove user from local storage to log user out
    localStorage.removeItem('account');

    return { type: LOGOUT };
}
