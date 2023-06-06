import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';

//import registerServiceWorker from './registerServiceWorker';

import { store } from './_helpers';
import { App } from './App';

render(
    <Provider store={store}>
        <App />
    </Provider>,
    document.getElementById('root')
);

//registerServiceWorker();
