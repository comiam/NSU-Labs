import { createBrowserHistory } from 'history';

export const history = createBrowserHistory({ basename: process.env.REACT_APP_ROUTER_BASE });
