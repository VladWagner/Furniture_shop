import {categoriesActionsTypes} from "../../infrastrucutre/actionsTypes";

const initialState = {
    categoriesTree: [],
    categoriesTileList: [],
    loadingTree: false,
    loadingTileList: false,
    errorTree: null,
    errorTile: null
}

// Изменение состояния хранилища в зависимости от заданного действия
export const categoriesReducer = function (state = initialState,action) {

    if (action === undefined || action.type === undefined)
        return state;

    switch (action.type) {
        // Получить категории с дочерними категориями для вывода в навигационной панели
        case categoriesActionsTypes.GET_CATEGORIES_TREE:

            if (action.payload === undefined)
                return state;

            return {...state, categoriesTree: action.payload, loadingTree: false};

        // Получить категории дл вывода плитки (в)
        case categoriesActionsTypes.GET_CATEGORIES_TILE:

            if (action.payload === undefined)
                return state;

            return {...state, categoriesTileList: action.payload, loadingTileList: false};

        // Флаг загрузки категорий в navbar
        case categoriesActionsTypes.SET_LOADING_TREE:

            if (action.payload === undefined)
                return state;

            return {...state, loadingTree: action.payload};

        // Флаг загрузки плитки категорий
        case categoriesActionsTypes.SET_LOADING_TILES:

            if (action.payload === undefined)
                return state;

            return {...state, loadingTileList: action.payload};

        // Ошибка загрузки категорий для navbar
        case categoriesActionsTypes.ERROR_CATEGORIES_TREE:

            if (action.payload === undefined)
                return state;

            return {...state, errorTree: action.payload};

        // Ошибка загрузки категорий для navbar
        case categoriesActionsTypes.ERROR_CATEGORIES_TILE:

            if (action.payload === undefined)
                return state;

            return {...state, errorTile: action.payload};

        default:
            return state;
    }
}