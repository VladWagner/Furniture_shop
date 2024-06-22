import {useStoreStateSelector} from "./useStoreStateSelector";
import {Route, Routes} from "react-router-dom";
import {isUserAdmin} from "../infrastrucutre/utils";
import * as utils from "../infrastrucutre/utils";
import MainLayout from "../pages/MainLayout/MainLayout";
import AdminPanelMain from "../components/AdminPanel/AdminPanelMain/AdminPanelMain";
import MainPage from "../pages/MainPage/MainPage";
import React, {useEffect, useState} from "react";
import ErrorsPage from "../pages/ErrorsPage/ErrorsPage";
import {useActions} from "./useActions";
import LoadingScreenPage from "../pages/LoadingScreenPage/LoadingScreenPage";
import {EmailConfirmationComponent} from "../components/EmailConfirmationComponent.jsx";

const useRoutes = () => {
    const {isAuth, isConfirmed, roles} = useStoreStateSelector(state => state.user);
    const {authenticate} = useActions();

    const [isAuthState, setIsAuthState] = useState(null);


    useEffect(() => {
        authenticate().then(result => {
            setIsAuthState(result);
        })
            .catch(error => {
                console.log(`Ошибка аутентификации: `)
                console.dir(error);
                setIsAuthState(false);
            })
    }, []);

    /*return (
      <Routes>
          {
              // Маршруты для админ-панели. Если пользователь не аутентифицирован или не является админом, тогда отправить на главный экран и показать
              // модальное окно аутентификации
              isAuthState && isConfirmed && utils.isUserAdmin(roles) ? (
                  <Route path="/admin" element={<AdminPanelMain/>}>

                      <Route index element={<AdminPanelMain/>}></Route>
                      <Route path="*" element={<AdminPanelMain />} />

                      {/!*Статистика с дочерними маршрутами *!/}
                      <Route path="/statistics" element={<AdminPanelMain/>}>

                          {/!*Общая статистика по дням*!/}
                          <Route path="/daily" element={<AdminPanelMain/>}>
                              <Route path="/absolute" element={<AdminPanelMain/>}></Route>
                              <Route path="/conversions" element={<AdminPanelMain/>}></Route>
                          </Route>

                          {/!*Статистика по товарам*!/}
                          <Route path="/products" element={<AdminPanelMain/>}>
                          </Route>
                      </Route>

                      {/!*Работа с товарами*!/}
                      <Route path="/products"       element={<AdminPanelMain/>}>
                          <Route path="/all"        element={<AdminPanelMain/>}></Route>
                          <Route path="/add"        element={<AdminPanelMain/>}></Route>
                          <Route path="/update/:id" element={<AdminPanelMain/>}></Route>
                      </Route>

                      {/!*Работа с категориями*!/}
                      <Route path="/categories"     element={<AdminPanelMain/>}>
                          <Route path="/all"        element={<AdminPanelMain/>}></Route>
                          <Route path="/add"        element={<AdminPanelMain/>}></Route>
                          <Route path="/update/:id" element={<AdminPanelMain/>}></Route>
                      </Route>

                      {/!*Работа с производителями*!/}
                      <Route path="/categories"     element={<AdminPanelMain/>}>
                          <Route path="/all"        element={<AdminPanelMain/>}></Route>
                          <Route path="/all-deleted"        element={<AdminPanelMain/>}></Route>
                          <Route path="/add"        element={<AdminPanelMain/>}></Route>
                          <Route path="/update/:id" element={<AdminPanelMain/>}></Route>
                      </Route>

                      {/!*Работа со скидками*!/}
                      <Route path="/categories" element={<AdminPanelMain/>}>
                          <Route path="/all" element={<AdminPanelMain/>}></Route>
                          <Route path="/add" element={<AdminPanelMain/>}></Route>
                          <Route path="/update/:id" element={<AdminPanelMain/>}></Route>
                      </Route>

                      {/!*Работа с пользователями*!/}
                      <Route path="/users" element={<AdminPanelMain/>}>
                          <Route path="/all" element={<AdminPanelMain/>}></Route>
                          <Route path="/get_by_keyword" element={<AdminPanelMain/>}></Route>
                          <Route path="/add" element={<AdminPanelMain/>}></Route>
                          <Route path="/update/:id" element={<AdminPanelMain/>}></Route>
                      </Route>

                      {/!*Работа с отзывами*!/}
                      <Route path="/reviews" element={<AdminPanelMain/>}></Route>

                  </Route>
              ) : !isAuthState? <Route path="/admin/!*" element={<MainLayout openLoginModal={true}/>} /> :
                  <Route path="/" element={<MainLayout/>}>
                      <Route path="admin/!*" element={<LoadingScreenPage errorsList={[!utils.isUserAdmin(roles) ?
                          "Для перехода на админ панель нужно быть администратором!" : null]}/>} />
                  </Route>
          }

          {/!*<Route path="/admin/!*" render={(props) => <MainLayout {...props} openLoginModal={true}/>} />*!/}

          {/!*Основные маршруты*!/}
          <Route path="/" element={<MainLayout/>}>
              <Route index element={<MainPage/>}></Route>
              <Route path="*" element={<MainPage />} />
              <Route path="products-by-category/:category_id" element={<MainPage/>}></Route>
              <Route path="products-by-producer/:producer_id" element={<MainPage/>}></Route>
              <Route path="product-info/:product_id" element={<MainPage/>}></Route>
              <Route path="search-products" element={<MainPage/>}></Route>

              <Route path="basket" element={<MainPage/>}></Route>
              <Route path="order" element={<MainPage/>}></Route>
              <Route path="about" element={<MainPage/>}></Route>
              <Route path="errors" element={<LoadingScreenPage/>}></Route>

              <Route path="order-info-by-code/:code" element={<MainPage/>}></Route>

              {
                  isAuthState ?
                      <Route path="/personal" element={<MainLayout/>}>
                          <Route index></Route>
                      </Route>
                   :  null
              }
          </Route>

          {!isAuthState ?
              <Route path="/personal/!*" element={<MainLayout openLoginModal={true}/>}/>
              : null
          }

      </Routes>
    );*/
    return <>
        { isAuthState === null ? <Routes><Route path="*" element={<MainLayout/>}>
            <Route index element={<LoadingScreenPage/>}/>
        </Route>
        </Routes> :
            <Routes>
                {
                    // Маршруты для админ-панели. Если пользователь не аутентифицирован или не является админом, тогда отправить на главный экран и показать
                    // модальное окно аутентификации
                    isAuthState && isConfirmed && utils.isUserAdmin(roles) ? (
                        <Route path="/admin" element={<AdminPanelMain/>}>

                            <Route index element={<AdminPanelMain/>}></Route>
                            <Route path="*" element={<AdminPanelMain/>}/>

                            {/*Статистика с дочерними маршрутами */}
                            <Route path="statistics" element={<AdminPanelMain/>}>

                                {/*Общая статистика по дням*/}
                                <Route path="daily" element={<AdminPanelMain/>}>
                                    <Route path="absolute" element={<AdminPanelMain/>}></Route>
                                    <Route path="conversions" element={<AdminPanelMain/>}></Route>
                                </Route>

                                {/*Статистика по товарам*/}
                                <Route path="products" element={<AdminPanelMain/>}>
                                </Route>
                            </Route>

                            {/*Работа с товарами*/}
                            <Route path="products" element={<AdminPanelMain/>}>
                                <Route path="all" element={<AdminPanelMain/>}></Route>
                                <Route path="add" element={<AdminPanelMain/>}></Route>
                                <Route path="update/:id" element={<AdminPanelMain/>}></Route>
                            </Route>

                            {/*Работа с категориями*/}
                            <Route path="categories" element={<AdminPanelMain/>}>
                                <Route path="all" element={<AdminPanelMain/>}></Route>
                                <Route path="add" element={<AdminPanelMain/>}></Route>
                                <Route path="update/:id" element={<AdminPanelMain/>}></Route>
                            </Route>

                            {/*Работа с производителями*/}
                            <Route path="categories" element={<AdminPanelMain/>}>
                                <Route path="all" element={<AdminPanelMain/>}></Route>
                                <Route path="all-deleted" element={<AdminPanelMain/>}></Route>
                                <Route path="add" element={<AdminPanelMain/>}></Route>
                                <Route path="update/:id" element={<AdminPanelMain/>}></Route>
                            </Route>

                            {/*Работа со скидками*/}
                            <Route path="discounts" element={<AdminPanelMain/>}>
                                <Route path="all" element={<AdminPanelMain/>}></Route>
                                <Route path="add" element={<AdminPanelMain/>}></Route>
                                <Route path="update/:id" element={<AdminPanelMain/>}></Route>
                            </Route>

                            {/*Работа с пользователями*/}
                            <Route path="users" element={<AdminPanelMain/>}>
                                <Route path="all" element={<AdminPanelMain/>}></Route>
                                <Route path="get_by_keyword" element={<AdminPanelMain/>}></Route>
                                <Route path="add" element={<AdminPanelMain/>}></Route>
                                <Route path="update/:id" element={<AdminPanelMain/>}></Route>
                            </Route>

                            {/*Работа с отзывами*/}
                            <Route path="reviews" element={<AdminPanelMain/>}></Route>

                        </Route>
                    ) : !isAuthState ? <Route path="/admin/*" element={<MainLayout openLoginModal={true}/>}/> :
                        <Route path="/" element={<MainLayout/>}>
                            <Route path="admin/*" element={<ErrorsPage errorsList={[!utils.isUserAdmin(roles) ?
                                "Для перехода на админ панель нужно быть администратором!" : null]}/>}/>
                        </Route>
                }

                {/*<Route path="/admin/*" render={(props) => <MainLayout {...props} openLoginModal={true}/>} />*/}

                {/*Основные маршруты*/}
                <Route path="/" element={<MainLayout/>}>
                    <Route index element={<MainPage/>}></Route>
                    <Route path="*" element={<MainPage/>}/>
                    <Route path="products-by-category/:category_id" element={<MainPage/>}></Route>
                    <Route path="products-by-producer/:producer_id" element={<MainPage/>}></Route>
                    <Route path="product-info/:product_id" element={<MainPage/>}></Route>
                    <Route path="search-products" element={<MainPage/>}></Route>

                    <Route path="basket" element={<MainPage/>}></Route>
                    <Route path="order" element={<MainPage/>}></Route>
                    <Route path="about" element={<MainPage/>}></Route>
                    <Route path="errors" element={<ErrorsPage/>}></Route>

                    <Route path="order-info-by-code/:code" element={<MainPage/>}></Route>
                    <Route path="confirm" element={<EmailConfirmationComponent/>}></Route>

                    {
                        isAuthState ?
                            <Route path="/personal" element={<MainLayout/>}>
                                <Route index></Route>
                            </Route>
                            : null
                    }
                </Route>

                {!isAuthState ?
                    <Route path="/personal/*" element={<MainLayout openLoginModal={true}/>}/>
                    : null
                }

            </Routes>
        }</>

}

export default useRoutes;
