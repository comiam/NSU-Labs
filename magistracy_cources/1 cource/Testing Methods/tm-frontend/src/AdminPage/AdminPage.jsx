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
import { history } from '../_helpers';

import { meActions, adminActions } from '../_actions';

class AdminPage extends React.Component {
    constructor(props) {
        super(props);
    }

    componentDidMount() {
        this.props.dispatch(meActions.me());
        this.props.dispatch(adminActions.getCustomers());
        this.props.dispatch(adminActions.getPlans());
    }

    render() {
        const { contact, customers, plans } = this.props;

        const handleOnAddCustomer = () => {
            history.push("/add-customer");
        }

        const handleOnRowDeleteCustomer = (oldData) =>
            new Promise((resolve) => {
                setTimeout(() => {
                    resolve();

                    this.props.dispatch(adminActions.deleteCustomer(oldData.id));
                }, 600);
            })

        const handleOnAddPlan = () => {
            history.push("/add-plan");
        }

        const handleOnRowDeletePlan = (oldData) =>
            new Promise((resolve) => {
                setTimeout(() => {
                    resolve();

                    this.props.dispatch(adminActions.deletePlan(oldData.id));
                }, 600);
            })


        return (
            <div className="col-md-32 col-md-offset-0">
                <h2>Hi {contact.login}!</h2>

                <p>
                    <Link to="/login">Logout</Link>
                </p>

                <MaterialTable
                    columns={[
                        { title: "Login", field: "login" },
                        { title: "First Name", field: "first_name" },
                        { title: "Last Name", field: "last_name" },
                        { title: "Balance", field: "balance", type: "numeric" }
                    ]}
                    data={customers}
                    title="Customers"
                    editable={{
                        onRowDelete: handleOnRowDeleteCustomer
                    }}
                    actions={[
                        {
                            icon: AddBox,
                            tooltip: "Add Customer",
                            position: "toolbar",
                            onClick: handleOnAddCustomer
                        }
                    ]}
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
                        { title: "Login", field: "name" },
                        { title: "Details", field: "details" },
                        { title: "Fee", field: "fee", type: "numeric" }
                    ]}
                    data={plans}
                    title="Plans"
                    editable={{
                        onRowDelete: handleOnRowDeletePlan
                    }}
                    actions={[
                        {
                            icon: AddBox,
                            tooltip: "Add plan",
                            position: "toolbar",
                            onClick: handleOnAddPlan
                        }
                    ]}
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
            </div>
        );
    }
}

function mapStateToProps(state) {
    return {
        contact: state.me.contact,
        customers: state.admin.customers,
        plans: state.admin.plans
    };
}

const connectedAdminPage = connect(mapStateToProps)(AdminPage);
export { connectedAdminPage as AdminPage };
