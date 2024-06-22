import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import "./styles/styles.css"
import {Provider} from "react-redux";
import store from "./store/createStore"
import {BrowserRouter} from "react-router-dom";
import 'typeface-roboto';
import {AutoAuthComponent} from "./components/AutoAuthComponent";
import ToastContainerMy from "./components/ToastNotifications/ToastContainerMy";


/*Создать объект reactDOMRoot*/
const root = ReactDOM.createRoot(document.getElementById('app'));
root.render(
    <Provider store={store}>
        <ToastContainerMy/>
        <App/>
    </Provider>
);


