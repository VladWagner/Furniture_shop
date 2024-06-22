import {visitorActionsTypes} from "../../infrastrucutre/actionsTypes";
import * as utils from "../../infrastrucutre/utils";
import {instanceWithoutInterceptor} from "../../infrastrucutre/axiosInterceptor";
import {logout} from "./userActions";

export const countVisit = function () {
    return async (dispatch) => {
        // Получить fingerprint пользователя
        let fingerprint = await utils.getFingerprint();

        // Проверить наличие записи о посещении в local store
        let lastVisit = localStorage.getItem("last_visit");

        // Прошло >= 24h от последнего посещения
        let goneGeDay = lastVisit !== undefined && utils.datesDiffGe(new Date(), new Date(lastVisit), 24, 'h')

        if (goneGeDay) {
            // Отправка запроса через axios для увеличения счётчика
            instanceWithoutInterceptor.post("/api/stat/daily_visits/increase_counter").then();

            localStorage.setItem("last_visit", new Date().toString())
        }

        // Изменение состояния
        let action = {
            type: visitorActionsTypes.COUNT_VISIT,
            payload: fingerprint
        }
        dispatch(action)
    }
};



