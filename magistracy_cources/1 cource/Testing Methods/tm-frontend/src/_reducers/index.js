import { combineReducers } from 'redux';

import { auth } from './auth-reducer';
import { me } from './me-reducer';
import { admin } from './admin-reducer';
import { customer } from './customer-reducer';
import { alert } from './alert-reducer';

const rootReducer = combineReducers({
  auth,
  me,
  admin,
  customer,
  alert
});

export default rootReducer;
