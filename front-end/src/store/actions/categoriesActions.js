import {categoriesActionsTypes} from "../../infrastrucutre/actionsTypes";
import {CategoryWithChildrenDto} from "../../api/dto/request/categories/categoryTreeDto";
import {instance} from "../../infrastrucutre/axiosInterceptor";
import {CategoryDto} from "../../api/dto/request/categories/categoryDto";

export const loadCategoriesTree = function () {
    return async (dispatch) => {

        let categories;

        let expirationDate = localStorage.getItem("categories_list_expiration_date");

        // Если есть значения в локальном хранилище
        if (expirationDate && new Date(expirationDate) > new Date()) {
            const localCategories = localStorage.getItem("categories");

            if (localCategories) {

                categories = JSON.parse(localCategories).map((c) => CategoryWithChildrenDto.readJsObject(c));

                // Изменение состояния
                let action = {
                    type: categoriesActionsTypes.GET_CATEGORIES_TREE,
                    payload: categories
                }
                dispatch(action)
            }
        } else {

            instance.get("api/categories/get_tree")
                .then(
                    resp => {

                        categories = resp.data.map(c => CategoryWithChildrenDto.readFromParsedObject(c));

                        console.log('Категории из запроса:')
                        console.dir(categories);

                        localStorage.setItem("categories", JSON.stringify(categories))

                        // Задать categories в ls
                        let currDate = new Date();

                        // Сохранить записи в lc для уменьшения кол-ва обращений к серверу
                        let expirationDate = new Date(currDate.getTime() + 1000 * 3600 * 24);
                        localStorage.setItem("categories_list_expiration_date", expirationDate.toString())

                        // Изменение состояния
                        let action = {
                            type: categoriesActionsTypes.GET_CATEGORIES_TREE,
                            payload: categories
                        }
                        dispatch(action)
                    }
                )
                .catch(error => {
                    console.error("Error with categories tree:", error);

                    // Попытаться прочитать категории из localStorage
                    const localCategories = localStorage.getItem("categories");

                    if (localCategories) {

                        let action = {
                            type: categoriesActionsTypes.GET_CATEGORIES_TREE,
                            payload: JSON.parse(localCategories).map((c) => new CategoryWithChildrenDto(c))
                        }

                        dispatch(action)
                    }
                })

            // Задать флаг загрузки
            let action = {
                type: categoriesActionsTypes.SET_LOADING_TREE,
                payload: true
            }
            dispatch(action)
        }//else
    }
};

// Получить категори для вовода плитки
export const loadCategoriesTile = function () {
    return async (dispatch) => {

        instance.get("api/categories/get_all_with_repeating")
            .then(
                resp => {

                    let categories = resp.data.map(c => CategoryDto.readFromParsedObject(c));

                    // Изменение состояния
                    let action = {
                        type: categoriesActionsTypes.GET_CATEGORIES_TILE,
                        payload: categories
                    }
                    dispatch(action)
                }
            )
            .catch(error => {
                console.error("Error with categories tree:", error);
            })

        // Задать флаг загрузки общих категорий
        let action = {
            type: categoriesActionsTypes.SET_LOADING_TILES,
            payload: true
        }
        dispatch(action)
    }
}


