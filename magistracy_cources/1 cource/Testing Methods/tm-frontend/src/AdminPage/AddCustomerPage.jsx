import React from 'react';
import { Button, FormGroup, FormControl, FormLabel, ButtonToolbar, ButtonGroup } from "react-bootstrap";
import { connect } from 'react-redux';
import { history } from '../_helpers';
import { adminActions } from "../_actions";

class AddCustomerPage extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            firstName: '',
            lastName: '',
            login: '',
            pass: ''
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {}

    handleChange(event) {
        const { name, value } = event.target;

        this.setState({[name]: value})
    }

    handleSubmit(event) {
        event.preventDefault();

        const { dispatch } = this.props;

        dispatch(adminActions.createCustomer(
            this.state.login,
            this.state.pass,
            this.state.firstName,
            this.state.lastName));
    }

    render() {
        const handleClick = () => {
            history.push("/admin");
        }

        // Лабораторная *: обратите внимание на то что пароль не скрыт.
        // <input type="password" name="password" value={this.state.password} onChange={this.handleChange} />
        return (
            <form onSubmit={this.handleSubmit}>
                <h3>Crete new customer</h3>
                <FormGroup>
                    <FormLabel>First name</FormLabel>
                    <FormControl type="text" name="firstName" value={this.state.firstName} onChange={this.handleChange} />
                </FormGroup>

                <FormGroup>
                    <FormLabel>Last name</FormLabel>
                    <FormControl type="text" name="lastName" value={this.state.lastName} onChange={this.handleChange} />
                </FormGroup>

                <FormGroup>
                    <FormLabel>Email address</FormLabel>
                    <FormControl type="text" name="login" value={this.state.login} onChange={this.handleChange} />
                </FormGroup>

                <FormGroup>
                    <FormLabel>Password</FormLabel>
                    <FormControl type="text" name="pass" value={this.state.pass} onChange={this.handleChange} />
                </FormGroup>

                <ButtonToolbar aria-label="Toolbar with button groups">
                    <ButtonGroup className="mr-2" aria-label="First group">
                        <Button onClick={handleClick}>Back</Button>
                    </ButtonGroup>
                    <ButtonGroup className="mr-2" aria-label="Second group">
                        <Button type="submit" className="btn btn-primary btn-block">Create</Button>
                    </ButtonGroup>
                </ButtonToolbar>
            </form>
        );
    }
}

function mapStateToProps(state) {
    return {};
}

const connectedAddCustomerPage = connect(mapStateToProps)(AddCustomerPage);
export { connectedAddCustomerPage as AddCustomerPage };
