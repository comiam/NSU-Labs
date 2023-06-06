import React from 'react';
import { Route, Redirect } from 'react-router-dom';

export const PrivateRoute = ({ component: Component, ...rest }) => (
    <Route {...rest} render = { props => {
        let account = localStorage.getItem('account');

        if (!account) {
            return <Redirect to={{ pathname: '/login', state: { from: props.location } }} />;
        }

        if (rest.path !== '/') {
            return <Component {...props} />;
        }

        account = JSON.parse(account);

        if (account.authorities.includes("ADMIN")) {
            return <Redirect to={{ pathname: '/admin', state: { from: props.location } }} />;
        }

        if (account.authorities.includes("CUSTOMER")) {
            return <Redirect to={{ pathname: '/customer', state: { from: props.location } }} />;
        }

        return <Component {...props} />;
    }} />
)
