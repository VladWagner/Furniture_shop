
// Действия для пользователей
export const userActionsTypes = {
    LOGIN: 'login',
    AUTHENTICATE: 'auth',
    LOGOUT: 'logout',
    CONFIRM: 'confirm',
    GET_USER_DETAILED_INFO: 'get_user_img',
    GET_ACCESS_TOKEN: 'get_access_token',
    SET_ACCESS_TOKEN: 'set_access_token',
    SET_IS_AUTHENTICATING: 'set_is_authenticating',
    SET_EMAIL_FOR_CONFIRMATION: 'set_email_for_confirmation',
    USER_ERROR: 'user_error'
}
// Действия для посетителей
export const visitorActionsTypes = {
    COUNT_VISIT: 'count_visit',
    VISITOR_ERROR: 'visitor_error'
}
// Действия c категориями
export const categoriesActionsTypes = {
    GET_CATEGORIES_TREE: 'get_categories_tree',
    GET_CATEGORIES_TILE: 'get_categories_tile',
    SET_LOADING_TREE: "set_loading_tree",
    SET_LOADING_TILES: "set_loading_tiles",
    ERROR_CATEGORIES_TREE: 'error_categories_tree',
    ERROR_CATEGORIES_TILE: 'error_categories_tile'
}
// Действия для корзины
export const cartActionsTypes = {
    // Пытаться получить значения из корзины cartActions, а reducer отправлять обработанные значения
    SET_BASKET_STATE: 'set_basket_state',
    SET_BASKET_TO_SYNC_FLAG: 'set_basket_to_sync_flag',

    ADD_PV_TO_CART: 'get_pv_id_list',
    REMOVE_PV_FROM_CART: 'remove_pv_from_cart',

    CHANGE_PV_COUNTER: 'change_pv_counter',
    SYNCHRONIZE_BASKET_WITH_SERVER: 'synchronize_basket_with_server',
    CLEAN_ON_LOGOUT: 'clean_on_logout',

    ERROR: 'error_cart'
}

// Действия для вывода уведомлений
export const toastActionsTypes = {
    CREATE_TOAST_NOTIFICATION: 'create_toast_notification',
    REMOVE_TOAST_NOTIFICATION: 'remove_toast_notification',
    SET_CONTAINER_POSITION: 'set_container_position'
}