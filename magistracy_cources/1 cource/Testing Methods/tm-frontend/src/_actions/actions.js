export const ALERT_SUCCESS = 'ALERT_SUCCESS'
export const ALERT_ERROR   = 'ALERT_ERROR'
export const ALERT_CLEAR   = 'ALERT_CLEAR'

export const actions = {
    success,
    error,
    clear
};

function success(message) {
    return { type: ALERT_SUCCESS, message };
}

function error(message) {
    return { type: ALERT_ERROR, message };
}

function clear() {
    return { type: ALERT_CLEAR };
}
