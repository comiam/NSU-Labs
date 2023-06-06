import { apiService } from '../_services';

export const ME_REQUEST = 'ME_REQUEST'
export const ME_SUCCESS = 'ME_SUCCESS'
export const ME_FAILURE = 'ME_FAILURE'

export const meActions = {
    me
};

function me() {
    return dispatch => {
        dispatch(request());

        apiService.me()
            .then(
                contact => dispatch(success(contact)),
                error => dispatch(failure(error))
            );
    };

    function request() { return { type: ME_REQUEST } }
    function success(contact) { return { type: ME_SUCCESS, contact } }
    function failure(error) { return { type: ME_FAILURE, error } }
}
