import Search from '@material-ui/icons/Search'
import ArrowDownward from '@material-ui/icons/ArrowDownward';
import ViewColumn from '@material-ui/icons/ViewColumn'
import SaveAlt from '@material-ui/icons/SaveAlt'
import ChevronLeft from '@material-ui/icons/ChevronLeft'
import ChevronRight from '@material-ui/icons/ChevronRight'
import FirstPage from '@material-ui/icons/FirstPage'
import LastPage from '@material-ui/icons/LastPage'
import Add from '@material-ui/icons/Add'
import AddBox from '@material-ui/icons/AddBox'
import Check from '@material-ui/icons/Check'
import FilterList from '@material-ui/icons/FilterList'
import Remove from '@material-ui/icons/Remove'
import Delete from '@material-ui/icons/Delete'
import Clear from '@material-ui/icons/Clear'

import MaterialTable from "material-table";

import React from 'react';
import { Link } from 'react-router-dom';
import { connect } from 'react-redux';

import {meActions, customerActions} from '../_actions';

class CustomerPage extends React.Component {
    componentDidMount() {
        this.props.dispatch(meActions.me())
        this.props.dispatch(customerActions.getAvailableSubscriptions());
        this.props.dispatch(customerActions.getAvailablePlans());
    }

    render() {
        const { contact, availableSubscriptions, availablePlans } = this.props;

        const handleOnRowBuyPlan = (oldData) =>
            new Promise((resolve) => {
                setTimeout(() => {
                    resolve();

                    this.props.dispatch(customerActions.createSubscription(oldData.id));
                }, 600);
            })

        const handleOnRowDeleteSubscription = (oldData) =>
            new Promise((resolve) => {
                setTimeout(() => {
                    resolve();

                    this.props.dispatch(customerActions.deleteSubscription(oldData.id));
                }, 600);
            })

        return (
            <div className="col-md-32 col-md-offset-0">
                <h2>Hi {contact.first_name} {contact.last_name}!</h2>
                <h3>Your balance: {contact.balance}</h3>
                <p>
                    <Link to="/top-up-balance">Top up balance</Link>
                </p>
                <p>
                    <Link to="/login">Logout</Link>
                </p>

                <MaterialTable
                    columns={[
                        { title: "Name", field: "plan_name" },
                        { title: "Details", field: "plan_details" },
                        { title: "Fee", field: "plan_fee", type: "numeric"}
                    ]}
                    data={availableSubscriptions}
                    title="Subscriptions"
                    editable={{
                        onRowDelete: handleOnRowDeleteSubscription
                    }}
                    icons={{
                        ViewColumn: ViewColumn,
                        SortArrow: ArrowDownward,
                        Add: Add,
                        AddBox: AddBox,
                        Delete: Delete,
                        Clear: Clear,
                        ResetSearch: Clear,
                        Check: Check,
                        DetailPanel: ChevronRight,
                        Export: SaveAlt,
                        Filter: FilterList,
                        FirstPage: FirstPage,
                        LastPage: LastPage,
                        NextPage: ChevronRight,
                        PreviousPage: ChevronLeft,
                        Search: Search,
                        ThirdStateCheck: Remove,
                    }}
                />

                <MaterialTable
                    columns={[
                        { title: "Name", field: "login" },
                        { title: "Details", field: "details" },
                        { title: "Fee", field: "fee", type: "numeric"}
                    ]}
                    data={availablePlans}
                    title="Plans"
                    editable={{
                        deleteTooltip: () => "Buy Plan",
                        onRowDelete: handleOnRowBuyPlan
                    }}
                    icons={{
                        ViewColumn: ViewColumn,
                        SortArrow: ArrowDownward,
                        Add: Add,
                        AddBox: AddBox,
                        Delete: Add,
                        Clear: Clear,
                        ResetSearch: Clear,
                        Check: Check,
                        DetailPanel: ChevronRight,
                        Export: SaveAlt,
                        Filter: FilterList,
                        FirstPage: FirstPage,
                        LastPage: LastPage,
                        NextPage: ChevronRight,
                        PreviousPage: ChevronLeft,
                        Search: Search,
                        ThirdStateCheck: Remove,
                    }}
                />
            </div>
        );
    }
}

function mapStateToProps(state) {
    return {
        contact: state.me.contact,
        availableSubscriptions: state.customer.availableSubscriptions,
        availablePlans: state.customer.availablePlans
    };
}

const connectedCustomerPage = connect(mapStateToProps)(CustomerPage);
export { connectedCustomerPage as CustomerPage };
