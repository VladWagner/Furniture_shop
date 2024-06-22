import {userActionsTypes} from "../../infrastrucutre/actionsTypes";

const initialState = {
    accessToken: localStorage.getItem("access_token") ?? null,
    id: null,
    userName: null,
    email: null,
    snp: null,
    isAuth: false,
    isAuthenticatingYet: false,
    isConfirmed: false,
    roles: [],
    profilePhoto: null,

    adminPanelVisited: false,
    error: "Нет прав для доступа"
}


// Изменение состояния хранилища в зависимости от заданного действия
// В редуктор нужно задавать именно такое аргументы:
export const userReducer = function userReducer(state = initialState,action) {

    if (action === undefined || action.type === undefined)
        return state;

    let payload = action.payload;
    switch (action.type) {
        case userActionsTypes.LOGIN:

            if (action.payload === undefined)
                return state;

            return {...state, accessToken: payload.accessToken,/* roles: payload.roles,*/ isAuth: payload.isAuthenticated, isAuthenticatingYet: false};

        case userActionsTypes.SET_ACCESS_TOKEN:

            if (action.payload === undefined)
                return state;

            return {...state,isAuth: true};

        case userActionsTypes.AUTHENTICATE:

            if (action.payload === undefined)
                return state;

            console.log(`Action payload: ${payload}`)

            return {...state,isAuth: payload, isAuthenticatingYet: false};

        // Подтверждение почты
        case userActionsTypes.CONFIRM:

            if (action.payload === undefined)
                return state;

            return {...state, isConfirmed: payload};

        // Задание аватара пользователя
        case userActionsTypes.GET_USER_DETAILED_INFO:

            if (action.payload === undefined)
                return state;

            console.log(`Payload in reducer (user details)`);
            console.dir(payload);

            return {...state, id: payload.id,
                userName: payload.userLogin,
                email: payload.email,
                snp: payload.name,
                isConfirmed: payload.isConfirmed,
                roles: [payload.role.roleName],
                profilePhoto: payload.profilePhoto};

        case userActionsTypes.LOGOUT:
            return {accessToken: null,isConfirmed: false,isAuth: false,roles: [], error: null};

        // Идёт ли сейчас процесс аутентификации
        case userActionsTypes.SET_IS_AUTHENTICATING:

            return {...state, isAuthenticatingYet: true};

        // Задать почту, на которую отправлен код подтверждения
        case userActionsTypes.SET_EMAIL_FOR_CONFIRMATION:

            if (action.payload === undefined)
                return state;

            return {...state, email: payload};

        // Обработка ошибки
        case userActionsTypes.USER_ERROR:

            if (action.payload === undefined)
                return state;

            return {...state, error: action.payload};
        default:
            return state;
    }
}