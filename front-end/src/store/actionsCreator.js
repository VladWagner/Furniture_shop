import * as ProductsActionsCreator from './actions/productsActions'
import * as UserActionsCreator from './actions/userActions'
import * as VisitorActionsCreator from './actions/visitorActions'
import * as CategoriesActionsCreator from './actions/categoriesActions'
import * as BasketActionsCreator from './actions/cartActions'
import * as toastActionsCreator from './actions/toastNotificationsActions'

// Получить все функции действий из всех файлов
const creator = {
    ...ProductsActionsCreator,
    ...UserActionsCreator,
    ...VisitorActionsCreator,
    ...CategoriesActionsCreator,
    ...BasketActionsCreator,
    ...toastActionsCreator
}

export default creator;