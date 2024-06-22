import {toastActionsTypes} from "../../infrastrucutre/actionsTypes";
import * as utils from "../../infrastrucutre/utils";
import {toastsTypes} from "../../infrastrucutre/constants";


export const createToast = function (message, type, timeout = 0) {
    return (dispatch) => {

        // Изменение состояния
        let action = {
            type: toastActionsTypes.CREATE_TOAST_NOTIFICATION,
            payload: {
                toast_type: type,
                message: message,
                timeout: timeout
            }
        }
        dispatch(action)
    }
};

export const removeToast = function (id) {
    return (dispatch) => {

        // Изменение состояния
        let action = {
            type: toastActionsTypes.REMOVE_TOAST_NOTIFICATION,
            payload: id
        }
        dispatch(action)
    }
};

export const setToastsContainerPosition = function (position) {
    return (dispatch) => {

        // Изменение состояния
        let action = {
            type: toastActionsTypes.SET_CONTAINER_POSITION,
            payload: position
        }
        dispatch(action)
    }
};




