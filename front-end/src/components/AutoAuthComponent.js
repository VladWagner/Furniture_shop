import React, {useEffect, useMemo, useState} from 'react';
import {useActions} from "../hooks/useActions";
import {useStoreStateSelector} from "../hooks/useStoreStateSelector";

// Компонент автоматической аутентификации
export const AutoAuthComponent = () => {
    let {authenticate, getUserDetailedInfo, setInitialBasketState} = useActions();
    const {isAuth, isConfirmed} = useStoreStateSelector(state => state.user);


    /*useEffect( async () => {
        authenticate();

    }, [])*/

    useMemo(() => {
        if (isAuth) {
            getUserDetailedInfo();

            if (isConfirmed) {
                console.log(`Is confirmed after details getting: ${isConfirmed}`)
                setInitialBasketState(isAuth, isConfirmed)
            }
        }
    }, [isAuth]);

    return null;
};