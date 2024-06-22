import React, {useEffect, useMemo} from 'react';
import {useActions} from "./hooks/useActions";
import {useStoreStateSelector} from "./hooks/useStoreStateSelector";
import {ModalProvider} from "./components/ModalConfiguration/ModalConfig";
import {ModalPortal} from "./components/ModalConfiguration/Modal/ModalPortal";
import {useUnmountOrWindowUnloadEffect} from "./hooks/useUnmountOrWindowUnloadEffect";
import {AutoAuthComponent} from "./components/AutoAuthComponent";
import useRoutes from "./hooks/useRoutes";
import {BrowserRouter} from "react-router-dom";

function App() {
    let {
        countVisit,
        setInitialBasketState,
        synchronizeBasketWithServer
    } = useActions();
    const {isAuth, isConfirmed} = useStoreStateSelector(state => state.user);


    useEffect(() => {

        // Засчитать посещение если прошло >= 24h
        countVisit();
    }, []);

    useUnmountOrWindowUnloadEffect((e) => {

        synchronizeBasketWithServer();
    }, true);

    useMemo(() => {

        // Получить корзину с сервера
        if (isAuth && isConfirmed)
            setInitialBasketState(isAuth, isConfirmed);

    }, [isConfirmed]);

    // Маршруты
    let routes = useRoutes();

    return (
        <BrowserRouter>
            <AutoAuthComponent/>
            <ModalProvider>
                {routes}
                <ModalPortal/>
            </ModalProvider>
        </BrowserRouter>
    );
}

export default App;