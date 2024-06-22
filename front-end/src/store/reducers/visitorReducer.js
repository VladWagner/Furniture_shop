import {visitorActionsTypes} from "../../infrastrucutre/actionsTypes";

const initialState = {
    fingerprint: '',
    error: null
}

// Изменение состояния хранилища в зависимости от заданного действия
export const visitorReducer = function (state = initialState,action) {

    if (action === undefined || action.type === undefined)
        return state;

    switch (action.type) {
        case visitorActionsTypes.COUNT_VISIT:

            if (action.payload === undefined)
                return state;

            return {...state, fingerprint: action.payload};

        // Обработка ошибки
        case visitorActionsTypes.VISITOR_ERROR:

            if (action.payload === undefined)
                return state;

            return {...state, error: action.payload};
        default:
            return state;
    }
}