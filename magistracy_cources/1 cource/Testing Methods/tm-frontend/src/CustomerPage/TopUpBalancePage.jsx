import React from 'react';
import { Button, FormGroup, FormControl, FormLabel, ButtonToolbar, ButtonGroup } from "react-bootstrap";
import { connect } from 'react-redux';
import { history } from '../_helpers';
import { customerActions } from "../_actions";

class TopUpBalancePage extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            money: 0
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {}

    handleChange(event) {
        const { name, value } = event.target;

        this.setState({[name]: parseInt(value)})
    }

    handleSubmit(event) {
        event.preventDefault();

        const { dispatch } = this.props;

        dispatch(customerActions.topUpBalance(
            this.state.money
        ));
    }

    render() {
        const handleClick = () => {
            history.push("/customer");
        }

        return (
            <form onSubmit={this.handleSubmit}>
                <h3>Top up balance</h3>

                <FormGroup>
                    <FormLabel>Money</FormLabel>
                    <FormControl type="number" name="money" value={this.state.money} onChange={this.handleChange} />
                </FormGroup>

                <ButtonToolbar aria-label="Toolbar with button groups">
                    <ButtonGroup className="mr-2" aria-label="First group">
                        <Button onClick={handleClick}>Back</Button>
                    </ButtonGroup>
                    <ButtonGroup className="mr-2" aria-label="Second group">
                        <Button type="submit" className="btn btn-primary btn-block">Submit</Button>
                    </ButtonGroup>
                </ButtonToolbar>
            </form>
        );
    }
}

function mapStateToProps(state) {
    return {};
}

const connectedTopUpBalancePage = connect(mapStateToProps)(TopUpBalancePage);
export { connectedTopUpBalancePage as TopUpBalancePage };
