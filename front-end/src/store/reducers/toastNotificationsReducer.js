import {toastActionsTypes, visitorActionsTypes} from "../../infrastrucutre/actionsTypes";
import {getRandom} from "../../infrastrucutre/utils";
import {toastsContainerPositions, toastsTypes} from "../../infrastrucutre/constants";

const initialState = {
    containerPosition: toastsContainerPositions.TOP,
    toasts: [],
    toastsPaged: new Set(),
    error: null
}

// Изменение состояния хранилища в зависимости от заданного действия
export const toastNotificationsReducer = function (state = initialState, action) {

    if (action === undefined || action.type === undefined)
        return state;

    const payload = action.payload;
    let toastIdx = -1;

    switch (action.type) {
        case toastActionsTypes.CREATE_TOAST_NOTIFICATION:

            if (payload === undefined)
                return state;

            // Создать toast с id == времени жизни страницы
            state.toasts.push({
                id: Math.round(window.performance.now() * 10),
                type: payload.toast_type,
                message: payload.message,
                timeout: payload.timeout
            })

            if (state.toastsPaged.size < 3)
                state.toastsPaged.add(state.toasts.length - 1);

            console.log(`Массив сообщений в toastReducer. И массив порционного вывода:`)
            console.dir(state);

            return {...state};

        // Удалить toast
        case toastActionsTypes.REMOVE_TOAST_NOTIFICATION:

            if (payload === undefined)
                return state;


            toastIdx = state.toasts.findIndex(t => t.id === payload);

            /*for (let i = 0; i < state.toasts.length; i++) {
                if (state.toasts[i].id === payload) {
                    toastIdx = i;
                    break
                }
            }*/

            if (toastIdx < 0)
                return state;

            console.log(`toastPagedIdx: ${state.toastsPaged.has(toastIdx)}. toastIdx: ${toastIdx}`)

            state.toasts.splice(toastIdx, 1);

            // Уменьшить кол-во элементов в множестве индексов сообщений в соответствии с размером основного массива toasts
            if (state.toastsPaged.size >= state.toasts.length && state.toasts.length > 0) {
                state.toastsPaged.forEach((v1, v2, set) => {
                    if (!state.toasts[v1])
                        set.delete(v1);
                });
            }

            console.log(`(DEL) массив порционного вывода сообщений:`)
            console.dir(state);

            return {...state};

        case toastActionsTypes.SET_CONTAINER_POSITION:

            // Если заданы некорректные значения
            if (!payload || (payload !== toastsContainerPositions.TOP &&  payload !== toastsContainerPositions.BOTTOM))
                return state;

            return {...state, containerPosition: payload}

        default:
            return state;
    }
}