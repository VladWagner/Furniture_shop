import './AuthForm.css'
import '../FormMutualStyles.css'
import {useState} from "react";
import cn from "classnames";
import {Link} from "react-router-dom";
import {Constants as constant} from "../../../infrastrucutre/constants";
import {RxCross1} from "react-icons/rx";
import {GrValidate} from "react-icons/gr";
import {VscEye, VscEyeClosed} from "react-icons/vsc";
import {useActions} from "../../../hooks/useActions";
import {Constants} from "../../../infrastrucutre/constants";
import {errorFieldCssClass} from "../FormConstants";
import {setSynchronizeBasketFlag} from "../../../store/actions/cartActions";

const React = require('react')

function AuthForm({onSubmitAdditionalHandler}) {

    const {login, setSynchronizeBasketFlag} = useActions();

    const {emailFieldId, passwordFieldId, syncBasketCbId} = {
        emailFieldId: "email-field-auth-form",
        passwordFieldId: "password-field",
        syncBasketCbId: "sync-basket-checkbox"
    };

    const [fieldsValues, setFieldsValues] = useState({[syncBasketCbId]: true});
    const [isFieldFilled, setIsFieldFilled] = useState({});
    const [formGeneralError, setFormGeneralError] = useState();

    const [inputsMessages, setInputsMessages] = useState({});

    // Флаг показа пароля
    const [showPassword, setShowPassword] = useState(false);
    const [showAwaitAnimation, setShowAwaitAnimation] = useState(false);

    // Обработка снятия фокуса с элемента для смещения label + задание значений в состояние для отправки формы
    function onInputBlur(e) {

        const id = e.target.id;

        if (!id)
            return;

        if (e.target.value) {
            setIsFieldFilled(prevState => {
                return {...prevState, [id]: "filled"}
            });


        } else {
            setIsFieldFilled(prevState => {
                return {...prevState, [id]: ""}
            });
        }

        // Если на поле есть ошибки валидации, тогда не изменять состояние
        if (inputsMessages[id] && inputsMessages[id].type === constant.errorFormField)
            return;

        setFieldsValues(prevState => ({...prevState, [id]: e.target?.value}))
    }

    function onInputBlurValidation(e) {

        const id = e.target.id;

        if (!id || !e.target.value)
            return;

        if (id === emailFieldId) {

            const emailTest = constant.regExpEmail.test(e.target.value);
            const userNameTest = constant.regExpUserName.test(e.target.value);

            console.log(`emailTest: ${emailTest}; userNameTest: ${userNameTest}`);

            if (!emailTest && !userNameTest) {
                setInputsMessages(prevState => ({
                    ...prevState, [emailFieldId]: {
                        ["type"]: constant.errorFormField,
                        ["text"]: "некорректное имя пользователя или email",
                        ["style"]: errorFieldCssClass
                    }
                }));
            } else {
                setInputsMessages(prevState => ({...prevState, [emailFieldId]: null}));
            }

        }

    }

    function onFormFieldInput(e) {
        const id = e.target.id;

        if (!id || !inputsMessages[id])
            return;

        setInputsMessages(prevState => ({...prevState, [id]: null}));
    }

    function onCheckBoxClick(e) {
        const id = e.target.id;

        if (!id)
            return;

        setFieldsValues(prevState => ({...prevState, [syncBasketCbId]: e.target.checked}))
    }

    function togglePasswordVisibility() {
        setShowPassword(prevState => !prevState);
    }

    // Общая валидация всех полей перед отправкой запроса
    function validateForm() {

        // Проверить наличие ошибок на полях
        if (inputsMessages[emailFieldId]?.type === constant.errorFormField ||
            inputsMessages[passwordFieldId]?.type === constant.errorFormField ){
            setFormGeneralError("В одном из полей есть ошибка!");

            return false;
        }

        const emailFieldValue = fieldsValues[emailFieldId];
        const passwordFieldValue = fieldsValues[passwordFieldId];

        if (!emailFieldValue || emailFieldValue === "") {
            setInputsMessages(prevState => ({
                ...prevState, [emailFieldId]: {
                    ["type"]: constant.errorFormField,
                    ["text"]: "имя пользователя или email должны быть заданы",
                    ["style"]: errorFieldCssClass
                }
            }))

        }
        if (!passwordFieldValue || passwordFieldValue === "") {
            setInputsMessages(prevState => ({
                ...prevState, [passwordFieldId]: {
                    ["type"]: constant.errorFormField,
                    ["text"]: "пароль должен быть задан",
                    ["style"]: errorFieldCssClass
                }
            }))
        }

        return (emailFieldValue && emailFieldValue.length > 0) && (passwordFieldValue && passwordFieldValue.length > 0);
    }

    async function onSubmit(e) {
        e.preventDefault();

        if (!validateForm())
            return;

        console.log("Data to send:");
        console.dir(fieldsValues);

        // Синхронизировать ли корзину с сервером
        setSynchronizeBasketFlag(fieldsValues[syncBasketCbId]);

        login(fieldsValues[emailFieldId], fieldsValues[passwordFieldId]).then(result => {

            if (onSubmitAdditionalHandler && typeof onSubmitAdditionalHandler === 'function')
                onSubmitAdditionalHandler();

            setShowAwaitAnimation(false);
        }).catch(error => {

            setShowAwaitAnimation(false);

            console.log(`В submit формы так же получили ошибку: ${error}`)
            if (!error || typeof error !== "string")
                return
            error = error.toLowerCase();

            if (error.includes("пользователь") || error.includes("username")) {
                setInputsMessages(prevState => ({
                    ...prevState, [emailFieldId]: {
                        ["type"]: constant.errorFormField,
                        ["text"]: "неверное имя пользователя или email",
                        ["style"]: errorFieldCssClass
                    }
                }))
                return;
            }

            if (error.includes("пароль")) {
                setInputsMessages(prevState => ({
                    ...prevState, [passwordFieldId]: {
                        ["type"]: constant.errorFormField,
                        ["text"]: "неверный пароль",
                        ["style"]: errorFieldCssClass
                    }
                }))
                return;
            }

            // Если ошибка не содержит информации о пользователе или пароле, тогда просто вывести её снизу
            setFormGeneralError(error);
        });

        setShowAwaitAnimation(true);

        // Убрать общее сообщение об ошибке, если оно имеется, поскольку поля валидны и запрос отправлен
        if (formGeneralError)
            setFormGeneralError(null);

    }

    return <form onSubmit={onSubmit}>
        <div
            className={cn("field-container", isFieldFilled[emailFieldId] ? isFieldFilled[emailFieldId] : "", inputsMessages[emailFieldId]?.style)}>
            <input className="form-input" id={emailFieldId} type="text" onBlur={(e) => {
                onInputBlur(e);
                onInputBlurValidation(e);
            }} onInput={onFormFieldInput}/>
            <label className="form-label" htmlFor={emailFieldId}>Введите email или имя пользователя</label>

            {inputsMessages[emailFieldId] && inputsMessages[emailFieldId]?.text?.length > 0 ?
                <span className="form-under-input-msg">{inputsMessages[emailFieldId].text}</span> : null}

            {inputsMessages[emailFieldId]?.type ?
                <span className={cn("form-field-validation-icon", inputsMessages[emailFieldId] &&
                    inputsMessages[emailFieldId].type !== constant.messageFormField ? "active" : "",
                    !inputsMessages[emailFieldId] || inputsMessages[emailFieldId]?.text?.length <= 0 ? "no-form-field-text-message" : "")}>
                {inputsMessages[emailFieldId].type === constant.errorFormField ? < RxCross1/> :
                    <GrValidate/>}</span> : null}
        </div>

        <div
            className={cn("field-container", isFieldFilled[passwordFieldId] ? isFieldFilled[passwordFieldId] : "", inputsMessages[passwordFieldId]?.style)}>
            <input className="form-input" id={passwordFieldId} type={showPassword ? "text" : "password"}
                   onBlur={onInputBlur}
                   onInput={onFormFieldInput}/>
            <label className="form-label" htmlFor={passwordFieldId}>Пароль</label>

            {/*Сообщение под полем ввода*/}
            {inputsMessages[passwordFieldId] && inputsMessages[passwordFieldId]?.text?.length > 0 ?
                <span className="form-under-input-msg">{inputsMessages[passwordFieldId].text}</span> : null}

            {/*Иконки с правого конца поля ввода для демонстрации состояния*/}
            {inputsMessages[passwordFieldId] ?
                <span className={cn("form-field-validation-icon", inputsMessages[passwordFieldId] &&
                    inputsMessages[passwordFieldId].type !== constant.messageFormField ? "active" : "",
                    !inputsMessages[passwordFieldId] || inputsMessages[passwordFieldId]?.text?.length <= 0 ? "no-form-field-text-message" : "")}>
                {inputsMessages[passwordFieldId]?.type === constant.errorFormField ? < RxCross1/> :
                    <GrValidate/>}</span> : null}

            {/*Кнопки для скрытия/показа пароля*/}
            <span
                className={cn("hide-password-icon", !inputsMessages[passwordFieldId] || inputsMessages[passwordFieldId]?.text?.length <= 0 ? "no-form-field-text-message" : "",
                    inputsMessages[passwordFieldId] && inputsMessages[passwordFieldId].type !== constant.messageFormField ? "has-info-icon" : "")}
                onClick={togglePasswordVisibility}>
                {showPassword ? <VscEyeClosed/> : <VscEye/>}</span>

        </div>

        {/*Чек-бокс*/}
        <div className="field-container">
            <label>
                <input className="form-checkbox-input" type="checkbox" id={syncBasketCbId}
                       checked={fieldsValues[syncBasketCbId]}
                       onClick={onCheckBoxClick}/>

                <span className="form-checkbox-select">
                <svg className="form-checkbox-tick" version="1.1" xmlns="http://www.w3.org/2000/svg"
                     x="0px" y="0px" viewBox="0 0 512.008 512.008">
                    <path
                        d="M502.795,60.572c-11.183-9.782-28.214-8.677-38.023,2.533L177.837,391.028L46.603,251.036 c-10.186-10.833-27.217-11.372-38.077-1.213c-10.86,10.159-11.426,27.244-1.24,38.104l151.579,161.684 c5.12,5.416,12.207,8.488,19.672,8.488h0.458c7.626-0.108,14.794-3.449,19.833-9.189L505.355,98.595 C515.137,87.385,514.005,70.381,502.795,60.572z"/>
                </svg>
            </span>
                <span className="form-checkbox-label">Сохранять корзину на сервере</span>
            </label>

        </div>

        <div className="field-container" style={{display: "flex"}}>
            <Link className="forgot-password" to={"/"}>Забыли пароль?</Link>
        </div>

        <div className="field-container">
            <button className="btn-submit-auth-form">Войти</button>
        </div>

        {formGeneralError ? <div className="form-error">{formGeneralError}</div> : null}
        {showAwaitAnimation ? <div className="form-response-await-animation">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 200">
                <circle fill="#FFB715" stroke="#FFB715" stroke-width="15" r="15" cx="70" cy="100">
                    <animate attributeName="opacity" calcMode="spline" dur="0.9" values="1;0;1;"
                             keySplines=".5 0 .5 1;.5 0 .5 1" repeatCount="indefinite" begin="-.4"></animate>
                </circle>
                <circle fill="#FFB715" stroke="#FFB715" stroke-width="15" r="15" cx="210" cy="100">
                    <animate attributeName="opacity" calcMode="spline" dur="0.9" values="1;0;1;"
                             keySplines=".5 0 .5 1;.5 0 .5 1" repeatCount="indefinite" begin="-.2"></animate>
                </circle>
                <circle fill="#FFB715" stroke="#FFB715" stroke-width="15" r="15" cx="340" cy="100">
                    <animate attributeName="opacity" calcMode="spline" dur="0.9" values="1;0;1;"
                             keySplines=".5 0 .5 1;.5 0 .5 1" repeatCount="indefinite" begin="0"></animate>
                </circle>
            </svg>
        </div> : null}
    </form>
}

export default AuthForm