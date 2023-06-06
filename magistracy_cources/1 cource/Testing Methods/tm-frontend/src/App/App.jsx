import React from 'react';
import { Router, Route } from 'react-router-dom';
import { connect } from 'react-redux';

import Alert from 'react-bootstrap/Alert';

import '../../node_modules/bootstrap/dist/css/bootstrap.min.css';
import "./App.css";

import { history } from '../_helpers';
import { actions } from '../_actions';
import { PrivateRoute } from '../_components';
import { HomePage } from '../HomePage';
import { AdminPage, AddPlanPage, AddCustomerPage } from '../AdminPage';
import { CustomerPage, TopUpBalancePage } from '../CustomerPage';
import { LoginPage } from '../LoginPage';

class App extends React.Component {
    constructor(props) {
        super(props);

        const { dispatch } = this.props;
        history.listen((location, action) => {
            // clear alert on location change
            dispatch(actions.clear());
        });
    }

    render() {
        const { alert } = this.props;
        return (
            <div className="jumbotron">
                <div className="container">
                    <Alert variant="danger" fade="false" show={alert.message}>
                        {alert.message}
                    </Alert>

                    <Router history={history}>
                        <div>
                            <PrivateRoute exact path="/" component={HomePage} />
                            <PrivateRoute path="/admin" component={AdminPage} />
                            <PrivateRoute path="/add-customer" component={AddCustomerPage} />
                            <PrivateRoute path="/add-plan" component={AddPlanPage} />
                            <PrivateRoute path="/customer" component={CustomerPage} />
                            <PrivateRoute path="/top-up-balance" component={TopUpBalancePage} />
                            <Route path="/login" component={LoginPage} />
                        </div>
                    </Router>
                </div>
            </div>
        );
    }
}

function mapStateToProps(state) {
    return {
        alert: state.alert
    };
}

const connectedApp = connect(mapStateToProps)(App);
export { connectedApp as App };
