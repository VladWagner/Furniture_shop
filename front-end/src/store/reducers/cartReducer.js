import {cartActionsTypes} from "../../infrastrucutre/actionsTypes";
import store from "../createStore";
import {instance} from "../../infrastrucutre/axiosInterceptor";
import {userReducer} from "./userReducer";
import {Basket} from "../../models/basket";
import {forEach} from "react-bootstrap/ElementChildren";

const initialState = {
    pvIdsList: new Map(),
    // Флаг нужен, поскольку изменения в map не всегда фиксируются в зависимостях хуков
    idsMapUpdated: false,
    pvDtoList: null,
    pvCount: 0,
    sum: null,
    error: null,

    // Если на сервере есть корзина, то нужно ли её синхронизировать
    syncServerBasket: false,
    basketDto: null,

    // Флаг необз проведения синхонизации с сервером
    basketToSynchronize: false
}

// Изменение состояния хранилища в зависимости от заданного действия
export const cartReducer = function (state = initialState, action) {

    if (action === undefined || action.type === undefined)
        return state;

    let payload = action.payload;

    switch (action.type) {
        // Загрузить корзину из локального хранилища или с бэка + сбросить ошибки
        case cartActionsTypes.SET_BASKET_STATE:

            if (payload === undefined)
                return state;
            return {
                ...state,
                pvIdsList: payload.pvIdsList ? payload.pvIdsList : new Map(),
                pvDtoList: payload.pvDtoList ? payload.pvDtoList : [],
                sum: payload.sum ? payload.sum : null,
                pvCount: payload.pvCount ?? payload.generalCount ?? state.pvCount,
                syncServerBasket: payload.syncBasket ? payload.syncBasket : state.syncServerBasket,
                basketDto: payload.dto,
                basketToSynchronize: false,
                error: null
            };

        // Добавить вариант товара в корзину
        case cartActionsTypes.ADD_PV_TO_CART:

            if (payload === undefined)
                return state;

            /*state.pvIdsList.add(new PvCartItem(payload.pvId, 1));
            state.pvCount = state.pvIdsList.reduce((sum, pv) => sum + pv.quantity,0)*/

            let prevQuantityAndTime = state.pvIdsList.get(payload.pvId);
            if (prevQuantityAndTime && prevQuantityAndTime.value > 0)
                state.pvIdsList.set(payload.pvId, {...prevQuantityAndTime, value: prevQuantityAndTime.value + 1});
            else {
                state.pvIdsList.set(payload.pvId, {value: 1, timeStamp: Date.now()});
                // Отсортировать ассоциативную коллекцию по дате добавления
                /*state.pvIdsList = new Map([...state.pvIdsList.entries()]
                    .sort((entry1, entry2) => entry1[1].timeStamp - entry2[1].timeStamp));*/
            }


            countPvAmountsSum(state);

            // Сохранить корзину в local storage
            if (!state.syncServerBasket)
                saveBasketToLs(state);

            return {...state, idsMapUpdated: true, basketToSynchronize: payload.basketToSync ? payload.basketToSync : true};

        // Убрать вариант товара из корзины
        case cartActionsTypes.REMOVE_PV_FROM_CART:

            if (payload === undefined)
                return state;


            if (state.pvIdsList.has(payload?.pvId)) {
                state.pvCount = state.pvCount - state.pvIdsList[payload.pvId];
                state.pvIdsList.delete(payload.pvId);

                countPvAmountsSum(state);
                // Сохранить измененную корзину в local storage (если там ещё остались товары)
                if (state.pvIdsList.size > 0 && !state.syncServerBasket)
                    saveBasketToLs(state);
                else if (state.pvIdsList.size === 0 && !state.syncServerBasket)
                    localStorage.removeItem("basket");
            }

            return {...state, basketToSynchronize: payload.basketToSync ? payload.basketToSync : true};
        // Увеличить кол-во едениц варианта тоара в корзине
        case cartActionsTypes.CHANGE_PV_COUNTER:

            if (payload === undefined)
                return state;

            if (!state.pvIdsList.has(payload.pvId))
                return state;

            let prevValue = state.pvIdsList.get(payload.pvId);
            state.pvIdsList.set(payload.pvId, {...prevValue, value: payload.count});

            // Пересчитать общее кол-во вариантов

            countPvAmountsSum(state);

            // Сохранить корзину в local storage
            if (!state.syncServerBasket)
                saveBasketToLs(state);

            return {...state, sum: null, basketDto: null, basketToSynchronize: payload.basketToSync ? payload.basketToSync : true};
        // Очистить состояние и LS при выходе из учётной записи
        case cartActionsTypes.CLEAN_ON_LOGOUT:

            state.pvIdsList = new Map();
            state.pvCount = 0;

            // Сбросить значения в корзине
            saveBasketToLs(state);

            return {...state, basketToSynchronize: false};

        case cartActionsTypes.SYNCHRONIZE_BASKET_WITH_SERVER:

            if (payload === undefined)
                return state;

            // Задать корзину, полученную с сервера
            if (payload === true && state.basketDto){
                console.log('Synchronizing basket. BasketDto:');
                console.dir(state.basketDto)
                //state.pvDtoList = state.basketDto.productVariantsAndCount;
                state.pvIdsList = state.basketDto.productVariantsAndCount.reduce((map, pvAndCount) =>
                    map.set(pvAndCount.productVariantDto.id, {value: pvAndCount.count, timeStamp: 0}), new Map()
                );
                state.sum = state.basketDto.sum;
                state.pvCount = state.basketDto.productVariantsAndCount.reduce((accum, pv) => accum + pv.count, 0);
                state.basketToSynchronize = false;
            }

            return {...state, syncServerBasket: payload}

        case cartActionsTypes.SET_BASKET_TO_SYNC_FLAG:

            if (payload === undefined)
                return state;

            return {...state, basketToSynchronize: payload}

        case cartActionsTypes.ERROR:

            if (payload === undefined)
                return state;

            return {...state, error: payload}
        default:
            return state;
    }
}

const saveBasketToLs = (state) => {

    let basketModel = new Basket({
        pvItems: Array.from(state.pvIdsList.entries()),
        count: state.pvCount
    });


    localStorage.setItem('basket', JSON.stringify(basketModel));
}

// Пересчитать общее кол-во вариантов в ассоциативной коллекции
function countPvAmountsSum(state) {
    let newQuantitySum = 0;


    for (let val of state.pvIdsList.values()) {

        let quantity = val.value;
        if (typeof quantity === 'string')
            quantity = Number.parseInt(quantity);

        newQuantitySum = quantity + newQuantitySum;
    }

    state.pvCount = newQuantitySum;
}