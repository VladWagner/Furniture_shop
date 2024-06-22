import {combineReducers, configureStore, createStore} from '@reduxjs/toolkit';
import { applyMiddleware } from 'redux';
import {thunk} from "redux-thunk"
import {userReducer} from './reducers/userReducer'
import {visitorReducer} from "./reducers/visitorReducer";
import {categoriesReducer} from "./reducers/categoriesReducer";
import {cartReducer} from "./reducers/cartReducer";
import {toastNotificationsReducer} from "./reducers/toastNotificationsReducer";

const reducersMap = {
    user: userReducer,
    visitor: visitorReducer,
    categories: categoriesReducer,
    cart: cartReducer,
    toasts: toastNotificationsReducer
};

//let store = configureStore(reducersMap);

// Временное решение через deprecated createStore пока не будут созданы редукторы
const rootReducer = combineReducers(reducersMap);

const store = createStore(rootReducer, applyMiddleware(thunk));

export default store;