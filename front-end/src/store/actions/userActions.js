import {userActionsTypes} from "../../infrastrucutre/actionsTypes";
import * as utils from "../../infrastrucutre/utils";
import {instance, instanceWithoutInterceptor} from "../../infrastrucutre/axiosInterceptor";
import {UserDTO} from "../../api/dto/request/userDto";
import {Constants} from "../../infrastrucutre/constants";
import {useActions} from "../../hooks/useActions";
import {cleanStateOnLogout} from "./cartActions";


export const register = function (snp, username, email, password) {
    return async (dispatch) => {

        // Изменение состояния. Задать почту, на которую был отправлен код подтверждения
        let action = {
            type: userActionsTypes.SET_EMAIL_FOR_CONFIRMATION,
            payload: email ? email : null
        }
        dispatch(action);

        /*{
                    name: snp,
                        login: username,
                    email: email,
                    password: password
                }*/
        const formData = new FormData();

        formData.append("user", new Blob([JSON.stringify({
            name: snp,
            login: username,
            email: email,
            password: password
        })], {
            type: "application/json"
        })
    );


        // Отправка запроса через axios
        return instanceWithoutInterceptor.post("/api/auth/register", formData/*, {
            headers: {
                'Content-Type': "multipart/form-data"
            }
        }*/).then(resp => {

            return Promise.resolve(resp);

        })
        .catch(error => {

            let errorMsg = error.response?.data?.message;
            console.log(`Error while authentication: ${errorMsg}`);

            return Promise.reject(errorMsg);
        })

    }
};

export const login = function (login, password) {
    return async (dispatch) => {
        // Отправка запроса через axios
        return instanceWithoutInterceptor.post("/api/auth/login", {
            login: login,
            password: password
        }).then(resp => {
            const data = resp.data;

            // Вытащить JWT
            const refresh_token = data?.refresh_token;
            const access_token = data?.access_token;

            if (!refresh_token || !access_token)
                return "ошибка аутентификации!"

            localStorage.setItem("refresh_token", refresh_token);

            // Изменение состояния
            let action = {
                type: userActionsTypes.LOGIN,
                payload: {
                    isAuthenticated: true,
                    accessToken: access_token
                }
            }
            dispatch(action);

            return Promise.resolve("authenticated");

        })
        .catch(error => {

            let errorMsg = error.response?.data?.message;
            console.log(`Error while authentication: ${errorMsg}`);

            return Promise.reject(errorMsg);
        })

    }
};

export const authenticate = function () {
    return async (dispatch) => {
        const key = "refresh_token";
        // Получить refresh token из LS || Session store
        let refreshToken = localStorage.getItem(key) ?? sessionStorage.getItem(key);

        if (!refreshToken)
            return;

        let action = {
            type: userActionsTypes.SET_IS_AUTHENTICATING,
        }
        dispatch(action);

        return instanceWithoutInterceptor.get("/api/auth/validate_refresh_token", {
            headers: {
                "refresh_token": refreshToken
            }
        }).then(resp => {

            // Токен не валиден
            if (resp.status === 442) {
                localStorage.removeItem(key);
                sessionStorage.removeItem(key);
                return;
            }

            // Изменение состояния
            let action = {
                type: userActionsTypes.AUTHENTICATE,
                payload: true
            }
            dispatch(action);

            return true;
        })
        .catch(error => {
            console.log(`Error: ${error.response?.data?.message}`);

            let action = {
                type: userActionsTypes.AUTHENTICATE,
                payload: false
            }
            dispatch(action);

            return false;
        })

    }
};

export const confirm = function (verificationToken) {

    if (typeof verificationToken !== 'string')
        throw new Error("Токен подтверждения почты должен быть в строковом типе")

    return async (dispatch) => {
        // Отправка запроса через axios


        // Изменение состояния
        let action = {
            type: userActionsTypes.CONFIRM,
            payload: true
        }
        console.log(`\nAction: ${action.type}`)
        dispatch(action);
    }
};

export const getUserDetailedInfo = function () {

    return async (dispatch) => {
        // Отправка запроса через axios
        try {

            try {
                const reps = await instance.get("/api/users/get_authed_detailed_info")

                let userDto = UserDTO.readFromResponseData(reps.data);

                //console.log('User DTO:');
                //console.dir(userDto);

                // Изменение пользователя
                let action = {
                    type: userActionsTypes.GET_USER_DETAILED_INFO,
                    payload: userDto
                }
                dispatch(action);

            } catch (error) {

                console.log("При получении информации об авторизированном пользователе возникла ошибка: ");
                console.log(error.response ? error?.response?.data?.message : error);

            }


        } catch (e) {

        }
    }
};

export const setAccessToken = function (accessToken) {
    return async (dispatch) => {

        let {sub, user_id, is_confirmed, roles} = utils.parseJWT(accessToken).payload;

        console.log(`Token in action: ${accessToken}`);

        if (user_id && is_confirmed && roles)
            localStorage.setItem("access_token", accessToken)

        console.log(`Roles: ${roles}`);
        console.log(`InConfirmed: ${is_confirmed}`);

        // Изменение состояния
        let action = {
            type: userActionsTypes.SET_ACCESS_TOKEN,
            payload: {token: accessToken, userName: sub, user_id, is_confirmed, roles}
        }
        dispatch(action)
    }
};

export const logout = function (refreshToken) {
    return async (dispatch) => {

        // Очистить состояние корзины
        cleanStateOnLogout();

        // Отправка запроса через axios для выхода конкрентного пользователя из учётной записи

        // Изменение состояния
        let action = {
            type: userActionsTypes.LOGOUT,
            payload: undefined
        }
        dispatch(action)
    }
};

export const setEmailForConfirmation = function (email) {
    return async (dispatch) => {
        // Изменение состояния
        let action = {
            type: userActionsTypes.SET_EMAIL_FOR_CONFIRMATION,
            payload: email
        }
        dispatch(action)
    }
};

export const setError = function (errorMessage) {
    return async (dispatch) => {
        // Изменение состояния
        let action = {
            type: userActionsTypes.USER_ERROR,
            payload: errorMessage
        }
        dispatch(action)
    }
};

