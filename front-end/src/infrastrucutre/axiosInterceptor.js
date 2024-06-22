import axios from 'axios';
import Cookies from 'js-cookie';
import history from "./browserHistory";
import {setAccessToken, logout, setError} from '../store/actions/userActions'
import store from "../store/createStore";
import {Constants} from "./constants";

export const instance = axios.create({

    baseURL: 'http://' + Constants.localIp + Constants.localAppAddr,
    //baseURL: 'http://' + Constants.localIp + ':8080'
});

export const instanceWithoutInterceptor = axios.create({

    baseURL: 'http://' + Constants.localIp + Constants.localAppAddr,
});

/*instance.interceptors.request.use((config) => {
    // Получить текущий домен
    config.headers['Origin'] = window.location.origin;
    return config;
});*/

// Задать перехватчик ответа с определённым статусом

instance.interceptors.response.use(
    (resp) => resp,
    async (error) => {

        // Исходный запрос
        let originalRequest = error.config;

        if (error.response === undefined)
            return Promise.reject(error);

        // Хранилище пользователя
        const userState = store.getState().user;

        console.log(`Response error code: ${error.response.status}`);

        // Получить refresh token
        let refreshToken = localStorage.getItem("refresh_token") ?? sessionStorage.getItem("refresh_token");

        // Если access токен не валиден и это не ошибка поторной попытки запроса
        if (error.response.status === 444 && !originalRequest._retry) {

            console.log('444 STATUS HANDLING')

            // Установить флаг, что это повторная попытка
            originalRequest._retry = true;

            // Если нет токена обновления, тогда требуем аутентификацию
            if (!refreshToken){
                //history.push('/login');
                return Promise.reject(error)
            }

            let accessToken;
            try {
                accessToken = await requireAccessToken(refreshToken);
            } catch (e) {
                console.log(`Error with access token: ${e}`)
            }

            if (!accessToken){
                // Выполнить действие выхода из учётной записи
                store.dispatch(logout(refreshToken));

                // Принудительно перенаправить на маршрут авторизации
                //history.push('/login');
                return Promise.reject(error)
            }

            // Задать access токен в хранилище
            if (!userState.accessToken || userState.accessToken !== accessToken)
                store.dispatch(setAccessToken(accessToken));

            // Изменить заголовок запроса
            originalRequest.headers['Authorization'] = `Bearer ${accessToken}`;

            // Повторить оригинальный запрос
            return instance(originalRequest);


        }
        // Токен аутентификации задан не был, а для endpoint'a нужна аутентифицакия
        else if (error.response.status === 403 && !originalRequest._retry) {

            // Установить флаг, что это повторная попытка
            //originalRequest._retry = true;

            let accessToken = localStorage.getItem("access_token");

            // Если access токена нет ни в local storage, ни в userState
            if (!accessToken || accessToken.length === 0)
            {
                // Если нет токена обновления, тогда требуем аутентификацию
                if (!refreshToken) {
                    //history.push('/login');
                    return Promise.reject(error)
                }

                try {
                    accessToken = await requireAccessToken(refreshToken);

                } catch (e) {

                    // Здесь должен быть logout с очисткой refresh_token, различных флагов созданных для аутентифицированного пользоателя

                }

                if (!accessToken){

                    // Выполнить действие выхода из учётной записи
                    store.dispatch(logout(refreshToken));

                    // Принудительно перенаправить на маршрут авторизации
                    //history.push('/login');
                    return Promise.reject(error)
                }
            }

            // Задать access токен в хранилище
            if (!userState.accessToken || userState.accessToken !== accessToken)
                store.dispatch(setAccessToken(accessToken));

            // Изменить заголовок запроса
            originalRequest.headers['Authorization'] = `Bearer ${accessToken}`;

            // Повторить оригинальный запрос
            return instance(originalRequest);

        }
        // Если пользователь аутентифицироан, но у него недостаточно прав доступа
        else if (error.response.status === 443){
            store.dispatch(setError("Для доступа к данному функционалу у вас недостаточно прав!"));
        }
        // else if


        // Если запрос возвратился с ошибкой не связанной с авторизацией (либо кто-то очень хитрый и поменял access токен)
        return Promise.reject(error)

    }
)

async function requireAccessToken(refreshToken) {

    /*if (Cookies.get("refresh_token"))
        document.cookie = 'refresh_token=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';

    Cookies.set('refresh_token', refreshToken, { sameSite: 'None', secure: true });*/

    try {
        const resp = await axios.get(`http://${Constants.localIp}`+`${Constants.localAppAddr}/api/auth/get_access_token`, {
            headers: {
                "refresh_token": refreshToken
            }
        });

        if (resp.status !== 200 || !resp.data.access_token) {

            return Promise.reject(null);
        }

        return resp.data.access_token;

    } catch (error) {
        console.log(`Error while access_token request: ${error.response?.data?.message}`);
        return Promise.reject(error);
    }
}

