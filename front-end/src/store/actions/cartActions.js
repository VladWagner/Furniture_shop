import {cartActionsTypes} from "../../infrastrucutre/actionsTypes";
import * as utils from "../../infrastrucutre/utils";
import store from "../createStore";
import {instance, instanceWithoutInterceptor} from "../../infrastrucutre/axiosInterceptor";
import {BasketDto} from "../../api/dto/request/basketDto";
import {userReducer} from "../reducers/userReducer";
import {Constants} from "../../infrastrucutre/constants";

// Состояние пользователя
let userState = store.getState().user;
const {syncServerBasket} = store.getState().cart;

// Задать начальное значение из локального хранилища, либо с сервера (если пользователь аутентифицирован)
export const setInitialBasketState = function (isAuth, isConfirmed) {
    return async (dispatch) => {

        let basketFromLs = getBasketFromLs();
        let syncBasket = localStorage.getItem(Constants.syncBasketLsKey);
        let sendBasketToServer = localStorage.getItem(Constants.sendBasketToServerLsKey);


        // Если пользователь зарегистрирован, тогда отправить запрос на получение корзины
        if (isAuth && isConfirmed) {
            // Получить козину для пользователя по access token
            instance.get(`/api/baskets/get_for_user`)
                .then(resp => {
                    console.log("Received response with basket")
                    console.dir(resp);

                    let basketDto = new BasketDto(resp.data);

                    if (!basketDto) {
                        console.log("Прочитать корзину из ответа не удалось!");
                        return Promise.reject();
                    }
                    let action;

                    if (syncBasket === 'false'){

                        console.log('Корзина из localStore:')
                        console.dir(basketFromLs)
                        if (basketFromLs) {
                            let action = {
                                type: cartActionsTypes.SET_BASKET_STATE,
                                payload: {pvIdsList: basketFromLs.pvCartItems, pvCount: basketFromLs.generalCount}
                            }
                            dispatch(action)
                        }

                        return;
                    }

                    // Чтобы задать корзину, полученную с сервера в state, на клиенте не должно быть корзины в LS или она
                    // должна быть пуста, или должен стоять флаг синхронизации
                    if (((!basketFromLs || basketFromLs.pvCartItems?.size === 0) && syncBasket === 'false') || syncBasket === 'true') {

                        action = {
                            type: cartActionsTypes.SET_BASKET_STATE,
                            payload: {
                                //pvDtoList: basketDto.productVariantsAndCount,
                                pvIdsList: basketDto.productVariantsAndCount.reduce((map, pvAndCount) =>
                                    map.set(pvAndCount.productVariantDto.id, {value: pvAndCount.count, timeStamp: 0}), new Map()
                                ),
                                sum: basketDto.sum,
                                pvCount: basketDto.productVariantsAndCount.reduce((accum, pv) => accum + pv.count, 0),
                                syncBasket: true
                            }
                        }
                    } else {
                        action = {
                            type: cartActionsTypes.SET_BASKET_STATE,
                            payload: {
                                dto: basketDto,
                                syncBasket: false
                            }
                        }
                    }

                    dispatch(action)

                })
                .catch(err => {
                    console.log(`Error while getting basket: ${err}`)
                    console.dir(err);

                    // Если корзины для пользователя нет на сервере
                    if (err?.response.status === 500 && err.response?.data?.message.toLowerCase().includes("не найдена")) {
                        console.log("У пользователя нет корзины на сервере");

                        if (basketFromLs && sendBasketToServer === "true")
                            createBasketOnServer(basketFromLs);

                        return;
                    }

                    let basket = getBasketFromLs();

                    // Если корзину найти не удалось
                    if (basket === undefined) {
                        dispatch({type: cartActionsTypes.ERROR, payload: "Корзины нет!"});
                        return;
                    }

                    // Изменение состояния
                    let action = {
                        type: cartActionsTypes.SET_BASKET_STATE,
                        payload: {pvIdsList: basket.pvCartItems, pvCount: basket.generalCount}
                    }
                    dispatch(action)

                })
        } else {

            // Если корзину найти не удалось
            if (!basketFromLs) {
                console.log('Не вышло получить корзину из локального хранилища')
                dispatch({type: cartActionsTypes.ERROR, payload: "Корзины нет!"})
                return;
            }

            // Изменение состояния
            let action = {
                type: cartActionsTypes.SET_BASKET_STATE,
                payload: {pvIdsList: basketFromLs.pvCartItems, pvCount: basketFromLs.generalCount}
            }
            dispatch(action)
        }
    }
};

// Получить корзину из локального хранилища
export const getBasketFromLs = () => {

    // Получить JSON из корзины
    const lcBasket = localStorage.getItem("basket");

    if (!lcBasket)
        return undefined;

    let basketModel = JSON.parse(lcBasket);
    basketModel.pvCartItems = new Map(basketModel.pvCartItems);

    return basketModel;
}

// Создать корзину на сервере из корзины пользователя на клиенте
export const createBasketOnServer = (basketModel) => {


    if (!store.getState().user.isAuth || !basketModel || !basketModel.pvCartItems)
        return null;

    let productVariantIdAndCount = {};

    console.log("pvCartItems: ")

    for (let [key, val] of basketModel.pvCartItems) {
        productVariantIdAndCount[key] = val?.value;
    }

    // Запрос на создание новой/пе
    instance.post("/api/baskets", {
            productVariantIdAndCount: productVariantIdAndCount/*objectFromMap*/
        })
        .then(resp => {

            let basketDto = resp?.data ? new BasketDto(resp.data) : null;

            if (!basketDto) {
                console.log("Прочитать корзину из ответа не удалось!");
                return;
            }

            let action = {
                type: cartActionsTypes.SET_BASKET_STATE,
                payload: {
                    pvDtoList: basketDto.productVariantsAndCount, sum: basketDto.sum,
                    pvCount: basketDto.productVariantsAndCount.reduce((accum, pv) => accum + pv.count, 0),
                    syncBasket: true
                }
            };

            store.dispatch(action);
        })
}

// Добавить вариант товара в корзину
export const addPvToBasket = (productVariantId, instantlySave = false) => {
    return async (dispatch) => {

        let sendBasketToServer = localStorage.getItem(Constants.sendBasketToServerLsKey);
        if (instantlySave && sendBasketToServer === "true"){
            userState = store.getState().user;

            // Если пользователь зарегестрирован, тогда отправить запрос на добавление товара в корзину/создание корзины
            if (userState.isAuth && userState.isConfirmed && store.getState().cart.syncServerBasket) {
                // Добавить товар в корзину пользователя
                instance.put(`/api/baskets/add_product_variants`, {
                    productVariantIdAndCount: {[productVariantId]: 1}
                })
                    /*.then(resp => {
                        getBasketFromServer(resp, dispatch)
                    })*/
                    .catch(err => {
                        console.log(`Error while getting basket: ${err}`)

                        // Изменение состояния
                        let action = {
                            type: cartActionsTypes.ERROR,
                            payload: "Не удалось добавить вариант товара в корзину пользователя"
                        }
                        dispatch(action)

                    })
            }
        }


        // Именение состояния
        let action = {
            type: cartActionsTypes.ADD_PV_TO_CART,
            payload: {pvId: productVariantId}
        }
        dispatch(action)
    }
}

// Убрать вариант товара из корзины
export const removePvFormBasket = (productVariantId, instantlySave) => {
    return async (dispatch) => {

        // При установленном флаге мгновенной синхронизации произвести запрос
        let sendBasketToServer = localStorage.getItem(Constants.sendBasketToServerLsKey);
        if (instantlySave && sendBasketToServer === "true"){
            userState = store.getState().user;

            // Если пользователь зарегестрирован, тогда отправить запрос на добавление товара в корзину/создание корзины
            if (userState.isAuth && userState.isConfirmed && store.getState().cart.syncServerBasket) {
                // Добавить товар в корзину пользователя
                instance.delete(`/api/baskets/delete_pv?product_variant_id=${productVariantId}`)
                    /*.then(resp => {
                        getBasketFromServer(resp, dispatch)
                    })*/
                    .catch(err => {
                        console.log(`Error while getting basket: ${err}`)

                        // Изменение состояния
                        let action = {
                            type: cartActionsTypes.ERROR,
                            payload: "Не удалось удалить вариант товара в корзину пользователя"
                        }
                        dispatch(action)

                    })
            }
        }

        // Именение состояния
        let action = {
            type: cartActionsTypes.REMOVE_PV_FROM_CART,
            payload: {pvId: productVariantId}
        }
        dispatch(action)
    }
}

// Изменить значение счётчика кол-ва вариантов товаров в корзине (id варианта товара + изменённое кол-во)
export const updatePvCounterInBasket = (productVariantId, count, instantlySave = false) => {
    return async (dispatch) => {

        if (count < 0)
            return;

        // При установленном флаге мгновенной синхронизации произвести запрос
        let sendBasketToServer = localStorage.getItem(Constants.sendBasketToServerLsKey);
        if (instantlySave && sendBasketToServer === "true"){
            userState = store.getState().user;

            // Если пользователь зарегестрирован, тогда отправить запрос на добавление товара в корзину/создание корзины
            if (userState.isAuth && userState.isConfirmed && store.getState().cart.syncServerBasket) {
                // Изменить счётчик
                instance.put(`/api/baskets/change_counter?product_variant_id=${productVariantId}&count=${count}`)
                    /*.then(resp => {
                        getBasketFromServer(resp, dispatch)
                    })*/
                    .catch(err => {
                        console.log(`Error while getting basket: ${err}`)

                        // Изменение состояния
                        let action = {
                            type: cartActionsTypes.ERROR,
                            payload: "Не удалось увеличить кол-во едениц вариантов товара в корзине пользователя"
                        }
                        dispatch(action)

                    })
            }
        }

        // Именение состояния
        let action = {
            type: cartActionsTypes.CHANGE_PV_COUNTER,
            payload: {pvId: productVariantId, count: count}
        }
        dispatch(action)
    }
}

// Задать флаг синхронизации корзины с сервером
export const setSynchronizeBasketFlag = (value) => {
    return async (dispatch) => {

        localStorage.setItem(Constants.syncBasketLsKey, value);

        // Убрать корзину, чтобы после выхода из записи у пользователя не было вопроса, откуда в корзине другие товары
        if (value)
            localStorage.removeItem("basket");

        // Изменение состояния
        let action = {
            type: cartActionsTypes.SYNCHRONIZE_BASKET_WITH_SERVER,
            payload: value !== undefined ? value : false
        }
        dispatch(action);

        // Если корзину синхронизиовать не будем, тогда попытаться получить её из LS
        /*if (value === false)
            setInitialBasketState(false, false);*/
    }
}

// Синхронизация состояния корзины с сервером
export const synchronizeBasketWithServer = (changeStateFlag = false) => {
    return async (dispatch) => {

        // Если пользователь при авторизации согласился отправлять корзину на сервер
        let sendBasketToServer = localStorage.getItem(Constants.sendBasketToServerLsKey);
        if (!sendBasketToServer || sendBasketToServer === "false")
            return;

        userState = store.getState().user;
        let basketState = store.getState().cart;

        // Если пользователь зарегистрирован, тогда отправить запрос на увеличение счётчика товаров
        if (!userState.isAuth || !userState.isConfirmed || basketState.syncServerBasket === false || basketState.basketToSynchronize === false)
            return null;

        let objectFromMap = Object.fromEntries([...basketState.pvIdsList.entries().map((entry) => [entry[0], entry[1].value])]);

        console.log('Map in JSON')
        console.dir(objectFromMap)

        await instance.put("/api/baskets/update?return_basket=false", {
                productVariantIdAndCount: objectFromMap
        }).catch(error => {
                console.log(`Error while synchronizing basket: ${error.response?.data?.message}`)
        })

        // Если сохраняем состояние не при закрытии/обновлении страницы
        if (changeStateFlag){

            // Убрать флаг необходимости производить сохранение корзины на сервер
            let action = {
                type: cartActionsTypes.SET_BASKET_TO_SYNC_FLAG,
                payload: false
            };
            dispatch(action);
        }
    }
}

// Очистка соостояния при выходе из корзины
export const cleanStateOnLogout = () => {
    return (dispatch) => {

        // Убрать флаг синхронизации
        localStorage.removeItem(Constants.syncBasketLsKey);

        let action = {
            type: cartActionsTypes.CLEAN_ON_LOGOUT
        }
        dispatch(action);
    }
}

// Получение корзины из ответа с сервера (метод будет определён после тестирования работоспособности решения в проимисах)
const getBasketFromServer = (resp, dispatch) => {
    console.log("Received response with basket")
    console.dir(resp);

    // Задать DTO изменённой корзины
    let basketDto = new BasketDto(resp.data);

    if (!basketDto)
        return Promise.reject("Прочитать корзину из ответа не удалось!");

    // Изменение общего состояния
    let action = {
        type: cartActionsTypes.SET_BASKET_STATE,
        payload: {
            pvDtoList: basketDto.productVariantsAndCount,
            sum: basketDto.sum,
            pvCount: basketDto.productVariantsAndCount.reduce((accum, pv) => accum + pv.count, 0)
        }
    };
    dispatch(action);
}