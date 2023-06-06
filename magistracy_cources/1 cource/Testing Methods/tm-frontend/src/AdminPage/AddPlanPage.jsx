import React from 'react';
import { Button, FormGroup, FormControl, FormLabel, ButtonToolbar, ButtonGroup } from "react-bootstrap";
import { connect } from 'react-redux';
import { history } from '../_helpers';
import { adminActions } from "../_actions";

class AddPlanPage extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            name: '',
            description: '',
            fee: 0
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

        dispatch(adminActions.createPlan(
            this.state.name,
            this.state.details,
            this.state.fee));
    }

    render() {
        const handleClick = () => {
            history.push("/admin");
        }

        return (
            <form onSubmit={this.handleSubmit}>
                <h3>Create new plan</h3>

                <FormGroup>
                    <FormLabel>Name</FormLabel>
                    <FormControl type="text" name="name" value={this.state.name} onChange={this.handleChange} />
                </FormGroup>

                <FormGroup>
                    <FormLabel>Details</FormLabel>
                    <FormControl type="text" name="details" value={this.state.details} onChange={this.handleChange} />
                </FormGroup>

                <FormGroup>
                    <FormLabel>Fee</FormLabel>
                    <FormControl type="number" name="fee" value={this.state.fee} onChange={this.handleChange} />
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

const connectedAddPlanPage = connect(mapStateToProps)(AddPlanPage);
export { connectedAddPlanPage as AddPlanPage };
