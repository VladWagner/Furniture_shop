import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './app';
import NavBar from "./components/Navigation";
import "./styles/styles.css"


/*Создать объект reactDOMRoot*/
const root = ReactDOM.createRoot(document.getElementById('app'));
root.render(
    <div>
        <NavBar/>
        <h3>Hello world from react app!</h3>
        <App/>
    </div>
);

