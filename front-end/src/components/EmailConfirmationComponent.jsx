import React, {useContext, useEffect, useMemo, useState} from 'react';
import {useActions} from "../hooks/useActions";
import {useStoreStateSelector} from "../hooks/useStoreStateSelector";
import {useNavigate, useSearchParams} from "react-router-dom";
import {instanceWithoutInterceptor} from "../infrastrucutre/axiosInterceptor";
import LoadingScreenPage from "../pages/LoadingScreenPage/LoadingScreenPage";
import AuthModal from "./AuthModal/AuthModal";
import {ModalContext} from "./ModalConfiguration/ModalConfig";
import {useToastActions} from "../hooks/useToastActions";


// Компонент для отправки кода подтверждения почты из параметров маршрута
export const EmailConfirmationComponent = () => {

    // Состояние для получения именованных параметров запроса
    const [searchParams, setSearchParams] = useSearchParams();
    const token = searchParams.get("token");
    const navigate = useNavigate();
    const [isTokenExpired, setIsTokenExpired] = useState();
    const {closeModal} = useContext(ModalContext);
    const {createSuccessToast, createWarningToast, createErrorToast} = useToastActions();
    const {setEmailForConfirmation} = useActions();

    function requestResendToken() {

        // Получить email на который отправлен token
        const parts = token.split('.')

        if (!parts || parts.length < 2) {
            // Добавить сообщение о том, что токен некорректен и перейти на главную
            createErrorToast("Повторная отправка невозможна, заданный токен не корректен! Вы были переведены на главную страницу", 10)

            navigate("/");
            return;
        }

        // Убрать символы
        const encodedEmail = parts[1].replace('_/g', '/').replace('-/g', '+');

        const decodedEmail = atob(encodedEmail);

        createWarningToast(`Срок действия токена истёк. Сообщение повторно отправлено на адрес: ${decodedEmail}`, 5);

        // Задать email в хранилище
        setEmailForConfirmation(decodedEmail);

        // Отправить запрос на повторную отправку письма
        instanceWithoutInterceptor.get(`/api/users/resend_confirmation?email=${decodedEmail}`).then(resp => {
            // Задать сообщение о том, что токен отправлен повторно
        }).catch(error => {
            const message = error?.response?.data?.message;

            // Задать сообщение о том, что письмо повторно отправить не получилось
            createErrorToast(`Ошибка повторной отправки: ${message?.length <= 120 ? message : message.substring(0,120) + "..."}`, 10)

            setIsTokenExpired(prevState => {

                // Перейти на главную страницу
                if (prevState !== undefined)
                    closeModal();
                return undefined;
            })
        })

    }


    useEffect(() => {

        console.log(`is verification token expired: ${isTokenExpired}`)

        if (!token || token.length === 0) {
            // Добавить сообщение о том, что токен не задан
            navigate("/");
            return;
        }

        console.log(`Из компонента EmailConfirmation, token=${token}`);

        instanceWithoutInterceptor.get(`/api/users/confirm?token=${token}`).then(result => {

            // Задать сообщение об успешном подтверждении
            createSuccessToast("Ваш аккаунт успешно подтвержден!", 7)

            setIsTokenExpired(false);

        }).catch(error => {
            const message = error?.response?.data?.message;

            console.log(`Confirmation component: ${message}`)

            if (message?.includes("подтверждения почты")) {
                setIsTokenExpired(true);
                requestResendToken();
            }
        });

    }, [])


    return <>
        <LoadingScreenPage/>
        {isTokenExpired !== undefined ?
            <AuthModal onCloseOuterFunction={() => navigate("/")} openConfirmationFormProp={isTokenExpired}/>
            : null}
    </>;
};