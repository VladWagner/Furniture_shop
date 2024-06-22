import './AuthModal.css'
import {useCallback, useContext, useEffect, useRef, useState} from "react";
import {ModalContext} from "../ModalConfiguration/ModalConfig";
import "../../styles/styles.css"
import {useActions} from "../../hooks/useActions";
import cn from "classnames";
import AuthForm from "../Forms/AuthForm/AuthForm";
import store from "../../store/createStore";
import {useNavigate} from "react-router-dom";
import RegistrationForm from "../Forms/RegistrationForm/RegistrationForm";
import EmailConfirmationForm from "../Forms/EmailConfirmationForm/EmailConfirmationForm";
import {toastsContainerPositions} from "../../infrastrucutre/constants";

const React = require('react')

// Флаг способа открытия окна - если true, то значит, что окно открыто самим приложением при переходе на защищенный маршрут
function AuthModal({isOpenedBySystem = false, onCloseOuterFunction = null, openConfirmationFormProp = false}) {

    const {openModal, closeModal} = useContext(ModalContext);

    const {setToastsContainerPosition} = useActions();
    const navigate = useNavigate();

    const [activeTabWidth, setActiveTabWidth] = useState(0);
    const [activeTabTranslate, setActiveTabTranslate] = useState(0);


    const registerTabId = "register_btn";
    const loginTabId = "login_btn";
    const [openedTab, setOpenedTab] = useState({
        [loginTabId]: {
            isOpened: true,
            className: "active"
        },
        [registerTabId]: {
            isOpened: false,
            className: ""
        }
    });
    const [openedConfirmForm, setOpenedConfirmForm] = useState(false);

    // Функция будет вызвана сразу после рендеринга компонента, что уберёт задержку в появлении выделения вкладки
    const basicTabCallback = useCallback(e => {
        if (e) {
            console.log(`Current tab width: ${e.offsetWidth}`);
            setActiveTabWidth(e.offsetWidth);
        }
    }, []);
    const basicTabRef = useRef();


    useEffect(() => {

        console.log(`openConfirmationFormProp: ${openConfirmationFormProp}`)
        if (openConfirmationFormProp)
            setOpenedConfirmForm(openConfirmationFormProp);

        window.addEventListener("resize", onResize);
        onResize();

        setToastsContainerPosition(toastsContainerPositions.BOTTOM);

        return () => {
            console.log("Убираем обработчик resize");
            window.removeEventListener("resize", onResize);

        };

    }, [])

    // Изменить ширину выделения вкладки
    function onResize() {
        if (basicTabRef.current) {

            const newWidth = basicTabRef.current.offsetWidth;

            if (activeTabTranslate > 0) {
                let sizeChangePercentage = activeTabWidth > 0 && newWidth > 0 ? (activeTabWidth <= newWidth ? activeTabWidth / newWidth : newWidth / activeTabWidth)
                    : 0;

                //console.log(`New size: ${sizeChangePercentage}. new width: ${activeTabWidth}`)
                setActiveTabTranslate(prevState => sizeChangePercentage > 10^-8 ? prevState * sizeChangePercentage : prevState)

            }

            setActiveTabWidth(newWidth);
        }
    }

    function onTabShifting(e) {
        if (!e)
            return;

        const id = e.target.id;

        if (id === registerTabId) {
            setActiveTabTranslate(basicTabRef.current ? basicTabRef.current.offsetWidth : activeTabWidth);

            // Открыть вкладку регистрации и закрыть вход
            setOpenedTab({
                [id]: {
                    isOpened: true,
                    className: "active"
                },
                [loginTabId]: {
                    isOpened: false,
                    className: ""
                }
            });
        } else if (id === loginTabId) {
            setActiveTabTranslate(0);

            // Открыть вкладку входа и закрыть регистрацию
            setOpenedTab({
                [id]: {
                    isOpened: true,
                    className: "active"
                },
                [registerTabId]: {
                    isOpened: false,
                    className: ""
                }
            });
        }
    }

    const modalContent = !openedConfirmForm ? <div>
        <div className="auth-modal-tabs-list">
            <div id={loginTabId} ref={basicTabCallback}
                 className={cn("auth-modal-tab", openedTab && openedTab[loginTabId].className)}
                 onClick={onTabShifting}>вход
            </div>

            <div id={registerTabId} ref={basicTabRef}
                 className={cn("auth-modal-tab", openedTab && openedTab[registerTabId].className)}
                 onClick={onTabShifting}>регистрация
            </div>

            <div className="tab-active"
                 style={{width: `${activeTabWidth}px`, transform: `translateX(${activeTabTranslate}px)`}}></div>
        </div>

        <div className="form-container">
            {<div style={{display: openedTab ? openedTab[loginTabId].isOpened === true ? "inline" : "none" : ""}}><AuthForm onSubmitAdditionalHandler={() => closeModal()}/></div>}
            {<div style={{display: openedTab ? openedTab[registerTabId].isOpened === true ? "inline" : "none" : ""}}><RegistrationForm  onSubmitAdditionalHandler={() => setOpenedConfirmForm(true)}/></div>}
        </div>

    </div> :
        <EmailConfirmationForm onSubmitAdditionalHandler={() => {/*closeModal()*/
            setOpenedConfirmForm(false);

            setActiveTabTranslate(0);
            // Открыть вкладку входа и закрыть регистрацию
            setOpenedTab({
                [loginTabId]: {
                    isOpened: true,
                    className: "active"
                },
                [registerTabId]: {
                    isOpened: false,
                    className: ""
                }
            });

        }}/>;

    // Поведение при закрытии окна, открытого при переходе на защищенный маршрут
    const onOpenedBySystemAuthModalClose = () => {

        const isAuth = store.getState().user.isAuth;

        // Если просто закрытие и пользователь не аутентифицирован, тогда перейти на главный маршрут,
        // в противном случае обновить текущий маршрут
        if (!isAuth)
            navigate('/');
        else if (isAuth === true) {
            window.location.reload();
        }
    }

    const onAuthModalClose = () => {

        // Если произошло принудительное открытие формы приложением
        if (isOpenedBySystem)
            onOpenedBySystemAuthModalClose();


        if (onCloseOuterFunction && typeof onCloseOuterFunction === 'function')
            onCloseOuterFunction();

    }

    // Хук должен отработать и вывести модальное окно только в первый рендеринг компонента
    useEffect(() => {

        if (!store.getState().user.isAuth) {
            console.log(`isOpenedBySystem: ${isOpenedBySystem}`)
        }

        openModal(modalContent, true, onAuthModalClose);

    }, [activeTabWidth, activeTabTranslate, openedConfirmForm]);

    return null;
}

export default AuthModal