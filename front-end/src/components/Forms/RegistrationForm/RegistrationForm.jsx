import './RegistrationForm.css'
import '../FormMutualStyles.css'
import {useEffect, useLayoutEffect, useState} from "react";
import cn from "classnames";
import {Link} from "react-router-dom";
import {Constants as constant} from "../../../infrastrucutre/constants";
import {RxCross1} from "react-icons/rx";
import {GrValidate} from "react-icons/gr";
import {VscEye, VscEyeClosed} from "react-icons/vsc";
import {useActions} from "../../../hooks/useActions";
import {Constants} from "../../../infrastrucutre/constants";
import {errorFieldCssClass, validFieldCssClass} from "../FormConstants";
import {instanceWithoutInterceptor} from "../../../infrastrucutre/axiosInterceptor";

const React = require('react')

function RegistrationForm({onSubmitAdditionalHandler}) {

    const {register} = useActions();

    const {emailFieldId, snpNameFiledId, passwordFieldId, userNameFiledId, passwordConfirmFieldId} = {
        emailFieldId: "email-field-register-form",
        snpNameFiledId: "snp-field",
        userNameFiledId: "user-name-field-register-form",
        passwordFieldId: "password-field-register-form",
        passwordConfirmFieldId: "password-confirm-field",
    };

    const [fieldsValues, setFieldsValues] = useState({});

    // Коллекция флагов заполненности поля для уменьшения надписи
    const [isFieldFilled, setIsFieldFilled] = useState({});
    const [formGeneralError, setFormGeneralError] = useState();
    const [showAwaitAnimation, setShowAwaitAnimation] = useState(false);

    const [inputsMessages, setInputsMessages] = useState({});

    // Флаг показа пароля
    const [showPassword, setShowPassword] = useState(false);

    // Таймер для проверки имени пользователя на уникальность при вводе
    const [timer, setTimer] = useState(null);

    useEffect(() => {
        return () => {
            if (timer) {
                clearTimeout(timer);
            }
        }
    }, []);

    const setErrorFieldMessage = (message) => ({
        ["type"]: constant.errorFormField,
        ["text"]: message,
        ["style"]: errorFieldCssClass
    });
    const setValidFieldMessage = (message) => ({
        ["type"]: constant.validFormField,
        ["text"]: message,
        ["style"]: validFieldCssClass
    });

    function checkUsernameUniqueness(userName) {

        if (!userName || userName.length === 0)
            return;

        if (!constant.regExpUserName.test(userName)) {
            setInputsMessages(prevState => ({
                ...prevState, [userNameFiledId]: setErrorFieldMessage("некорректное имя пользователя")
            }));
            return;
        }

        instanceWithoutInterceptor.get(`/api/users/check_login?val=${userName}`)
            .then(resp => {

                // Ответ положительный - email существует
                if (resp.data === true) {
                    setInputsMessages(prevState => ({
                        ...prevState, [userNameFiledId]: setErrorFieldMessage("данное имя пользователя уже занято!")
                    }));
                } else {
                    setInputsMessages(prevState => ({
                        ...prevState, [userNameFiledId]: setValidFieldMessage("")
                    }));
                }
            })
            .catch(error => {
                console.log("При проверке уникальности имени пользователя возникла ошибка: ")
                console.dir(error);
            })
    }

    function validatePassword(value) {
        const passwordLetters = constant.regExpPassword.letters.test(value);
        const passwordDigits = constant.regExpPassword.digits.test(value);

        if (value.length < 8) {
            setInputsMessages(prevState => ({
                ...prevState, [passwordFieldId]: setErrorFieldMessage("минимальная длина пароля 8 символов")
            }));
        } else if (!passwordLetters) {
            setInputsMessages(prevState => ({
                ...prevState, [passwordFieldId]: setErrorFieldMessage("пароль должен содержать буквы")
            }));
        } else if (!passwordDigits) {
            setInputsMessages(prevState => ({
                ...prevState, [passwordFieldId]: setErrorFieldMessage("пароль должен содержать цифры")
            }));
        } else {
            if (value.length === 0)
                setInputsMessages(prevState => ({...prevState, [passwordFieldId]: null}));
            else
                setInputsMessages(prevState => ({...prevState, [passwordFieldId]: setValidFieldMessage("")}));
        }
    }

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

        const value = e.target.value;

        switch (id) {

            /*Валидация введённого email*/
            case emailFieldId:
                if (fieldsValues[emailFieldId] === value && inputsMessages[emailFieldId])
                    return;

                const emailTest = constant.regExpEmail.test(value);

                if (!emailTest) {
                    setInputsMessages(prevState => ({
                        ...prevState, [emailFieldId]: setErrorFieldMessage("email введён некорректно")
                    }));
                } else {
                    setInputsMessages(prevState => ({...prevState, [emailFieldId]: null}));

                    /*Проверить наличие аккаунта с таким email*/
                    instanceWithoutInterceptor.get(`/api/users/check_email?val=${value}`)
                        .then(resp => {

                            const message = resp.data?.message;
                            if (resp.status === 400 || message?.includes("задан некорректно")) {
                                setFormGeneralError(message ? message : "Проверка почты на уникальность не удалась!")
                            }

                            // Ответ положительный - email существует
                            if (resp.data === true) {
                                setInputsMessages(prevState => ({
                                    ...prevState, [emailFieldId]: setErrorFieldMessage("данный email уже использован!")
                                }));
                            } else {
                                setInputsMessages(prevState => ({
                                    ...prevState, [emailFieldId]: setValidFieldMessage("")
                                }));
                            }
                        })
                        .catch(error => {
                            console.log("При проверке уникальности email возникла ошибка: ")
                            console.dir(error);
                        })
                }
                break;

            /*Валидация введённого userName*/
            case userNameFiledId:
                if (fieldsValues[userNameFiledId] === value && inputsMessages[userNameFiledId])
                    return;

                const userNameTest = constant.regExpUserName.test(value);

                console.log(`userNameTest: ${userNameTest}`);

                if (!userNameTest) {
                    setInputsMessages(prevState => ({
                        ...prevState, [userNameFiledId]: setErrorFieldMessage("некорректное имя пользователя")
                    }));
                } else {

                    // Если timer ещё не отработал, тогда провести проверку уникальности здеьсс
                    if (timer) {
                        setInputsMessages(prevState => ({...prevState, [userNameFiledId]: null}));

                        // Очистить timer ожидания после окончания ввода и проверить наличие такого пользователя на сервере

                        clearInterval(timer);

                        checkUsernameUniqueness(value);
                    }

                }
                break;

            /*Валидация введённого пароля*/
            case passwordFieldId:
                validatePassword(value);
                break;
            /*Валидация введённого подтверждения пароля*/
            case passwordConfirmFieldId:

                if (fieldsValues[passwordConfirmFieldId] === value && inputsMessages[passwordConfirmFieldId])
                    return;

                if (value !== fieldsValues[passwordFieldId]) {
                    setInputsMessages(prevState => ({
                        ...prevState,
                        [passwordConfirmFieldId]: setErrorFieldMessage("пароль отличается от введённого выше")
                    }));
                } else if (fieldsValues[passwordFieldId]) {
                    setInputsMessages(prevState => ({
                        ...prevState,
                        [passwordConfirmFieldId]: setValidFieldMessage("")
                    }));
                } else {

                    setInputsMessages(prevState => ({...prevState, [passwordConfirmFieldId]: null}));
                }

                break;
        }

    }

    // Обработка отпуска клавиши при вводе имени пользователя
    function onUsernameKeyUpHandler(e) {

        if (timer) {
            console.log('timer очищен ')
            clearTimeout(timer);
        }

        setTimer(setTimeout(() => {
            checkUsernameUniqueness(e.target.value);

            // обнулить объект таймера, чтобы было понятно, что проверка уже пройдена
            setTimer(null);
        }, 700)); // Задержка в 0.7 секунды
    }

    function onFormFieldInput(e) {
        const id = e.target.id;

        if (!id || !inputsMessages[id])
            return;

        setInputsMessages(prevState => ({...prevState, [id]: null}));
    }

    function togglePasswordVisibility() {
        setShowPassword(prevState => !prevState);
    }

    // Общая валидация всех полей перед отправкой запроса
    function validateForm() {

        if (inputsMessages[userNameFiledId]?.type === constant.errorFormField ||
            inputsMessages[emailFieldId]?.type === constant.errorFormField ||
            inputsMessages[passwordFieldId]?.type === constant.errorFormField ||
            inputsMessages[passwordConfirmFieldId]?.type === constant.errorFormField){
            setFormGeneralError("В одном из полей есть ошибка!");

            return false;
        }

        const userNameFieldValue = fieldsValues[userNameFiledId];
        const emailFieldValue = fieldsValues[emailFieldId];
        const passwordFieldValue = fieldsValues[passwordFieldId];
        const passwordConfirmFieldValue = fieldsValues[passwordConfirmFieldId];

        if (!userNameFieldValue || userNameFieldValue === "") {
            setInputsMessages(prevState => ({
                ...prevState, [userNameFiledId]: setErrorFieldMessage("имя пользователя должно быть задано")
            }))

        }
        if (!emailFieldValue || emailFieldValue === "") {
            setInputsMessages(prevState => ({
                ...prevState, [emailFieldId]: setErrorFieldMessage("email должен быть задан")
            }))

        }

        if (!passwordFieldValue || passwordFieldValue === "") {
            setInputsMessages(prevState => ({
                ...prevState, [passwordFieldId]: setErrorFieldMessage("пароль должен быть задан")
            }))
        }

        if (!passwordConfirmFieldValue || passwordConfirmFieldValue === "") {
            setInputsMessages(prevState => ({
                ...prevState, [passwordConfirmFieldId]: setErrorFieldMessage("поле должно быть заполнено")
            }))
        } else if ((passwordFieldValue && passwordFieldValue.length > 0) && passwordFieldValue !== passwordConfirmFieldValue) {
            setInputsMessages(prevState => ({
                ...prevState, [passwordConfirmFieldId]: setErrorFieldMessage("пароль отличается от введённого выше")
            }))
        }

        return (emailFieldValue && emailFieldValue.length > 0) && (userNameFieldValue && userNameFieldValue.length > 0) &&
            (passwordFieldValue && passwordFieldValue.length > 0) && (passwordConfirmFieldValue && passwordConfirmFieldValue.length > 0) && passwordFieldValue === passwordConfirmFieldValue;
    }

    async function onSubmit(e) {
        e.preventDefault();

        if (!validateForm())
            return;

        register(fieldsValues[snpNameFiledId], fieldsValues[userNameFiledId], fieldsValues[emailFieldId], fieldsValues[passwordFieldId]).then(result => {

            if (onSubmitAdditionalHandler && typeof onSubmitAdditionalHandler === 'function')
                onSubmitAdditionalHandler();

            setShowAwaitAnimation(false);
        }).catch(error => {

            console.log(`В submit формы так же получили ошибку: ${error}`)
            if (!error || typeof error !== "string")
                return

            setFormGeneralError(error);
            setShowAwaitAnimation(false);
        });

        setShowAwaitAnimation(true);

        if (formGeneralError)
            setFormGeneralError(null);

        /*setTimeout(() => {
            if (onSubmitAdditionalHandler && typeof onSubmitAdditionalHandler === 'function')
                onSubmitAdditionalHandler();

            setShowAwaitAnimation(false);
        }, 800);*/

    }

    return <form onSubmit={onSubmit}>

        {/*Поле ввода ФИО пользователя*/}
        <div
            className={cn("field-container", isFieldFilled[snpNameFiledId] ? isFieldFilled[snpNameFiledId] : "", inputsMessages[snpNameFiledId]?.style)}>
            <input className="form-input" id={snpNameFiledId} type="text" onBlur={(e) => {
                onInputBlur(e);
            }} onInput={onFormFieldInput}/>
            <label className="form-label" htmlFor={snpNameFiledId}>Введите имя и фамилию</label>

        </div>

        {/*Поле ввода имени пользователя*/}
        <div
            className={cn("field-container", isFieldFilled[userNameFiledId] ? isFieldFilled[userNameFiledId] : "", inputsMessages[userNameFiledId]?.style)}>
            <input className="form-input" id={userNameFiledId} type="text" onBlur={(e) => {
                onInputBlur(e);
                onInputBlurValidation(e);
            }} onInput={onFormFieldInput} onKeyUp={onUsernameKeyUpHandler}/>
            <label className="form-label" htmlFor={userNameFiledId}>Введите имя пользователя
                <span className="field-required">*</span></label>

            {inputsMessages[userNameFiledId] && inputsMessages[userNameFiledId]?.text?.length > 0 ?
                <span className="form-under-input-msg">{inputsMessages[userNameFiledId].text}</span> : null}

            {inputsMessages[userNameFiledId]?.type ?
                <span className={cn("form-field-validation-icon", inputsMessages[userNameFiledId] &&
                    inputsMessages[userNameFiledId].type !== constant.messageFormField ? "active" : "",
                    !inputsMessages[userNameFiledId] || inputsMessages[userNameFiledId]?.text?.length <= 0 ? "no-form-field-text-message" : "")}>
                {inputsMessages[userNameFiledId].type === constant.errorFormField ? < RxCross1/> :
                    <GrValidate/>}</span> : null}
        </div>

        {/*Поле ввода email*/}
        <div
            className={cn("field-container", isFieldFilled[emailFieldId] ? isFieldFilled[emailFieldId] : "", inputsMessages[emailFieldId]?.style)}>
            <input className="form-input" id={emailFieldId} type="text" onBlur={(e) => {
                onInputBlur(e);
                onInputBlurValidation(e);
            }} onInput={onFormFieldInput}/>
            <label className="form-label" htmlFor={emailFieldId}>Введите email
                <span className="field-required">*</span></label>

            {inputsMessages[emailFieldId] && inputsMessages[emailFieldId]?.text?.length > 0 ?
                <span className="form-under-input-msg">{inputsMessages[emailFieldId].text}</span> : null}

            {inputsMessages[emailFieldId]?.type ?
                <span className={cn("form-field-validation-icon", inputsMessages[emailFieldId] &&
                    inputsMessages[emailFieldId].type !== constant.messageFormField ? "active" : "",
                    !inputsMessages[emailFieldId] || inputsMessages[emailFieldId]?.text?.length <= 0 ? "no-form-field-text-message" : "")}>
                {inputsMessages[emailFieldId].type === constant.errorFormField ? < RxCross1/> :
                    <GrValidate/>}</span> : null}
        </div>

        {/*Первое поле ввода пароля*/}
        <div
            className={cn("field-container", isFieldFilled[passwordFieldId] ? isFieldFilled[passwordFieldId] : "", inputsMessages[passwordFieldId]?.style)}>
            <input className="form-input" id={passwordFieldId} type={showPassword ? "text" : "password"}
                   onBlur={(e) => {
                       onInputBlur(e);
                       onInputBlurValidation(e);
                   }}/>
            <label className="form-label" htmlFor={passwordFieldId}>Введите пароль
                <span className="field-required">*</span></label>

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

        <div
            className={cn("field-container", isFieldFilled[passwordConfirmFieldId] ? isFieldFilled[passwordConfirmFieldId] : "", inputsMessages[passwordConfirmFieldId]?.style)}>
            <input className="form-input" id={passwordConfirmFieldId} type={showPassword ? "text" : "password"}
                   onBlur={(e) => {
                       onInputBlur(e);
                       onInputBlurValidation(e);
                   }}
                   onInput={onFormFieldInput}/>
            <label className="form-label" htmlFor={passwordConfirmFieldId}>Подтвердите пароль
                <span className="field-required">*</span></label>

            {/*Сообщение под полем ввода*/}
            {inputsMessages[passwordConfirmFieldId] && inputsMessages[passwordConfirmFieldId]?.text?.length > 0 ?
                <span className="form-under-input-msg">{inputsMessages[passwordConfirmFieldId].text}</span> : null}

            {/*Иконки с правого конца поля ввода для демонстрации состояния*/}
            {inputsMessages[passwordConfirmFieldId] ?
                <span className={cn("form-field-validation-icon", inputsMessages[passwordConfirmFieldId] &&
                    inputsMessages[passwordConfirmFieldId].type !== constant.messageFormField ? "active" : "",
                    !inputsMessages[passwordConfirmFieldId] || inputsMessages[passwordConfirmFieldId]?.text?.length <= 0 ? "no-form-field-text-message" : "")}>
                {inputsMessages[passwordConfirmFieldId]?.type === constant.errorFormField ? < RxCross1/> :
                    <GrValidate/>}</span> : null}

            {/*Кнопки для скрытия/показа пароля*/}
            <span
                className={cn("hide-password-icon", !inputsMessages[passwordConfirmFieldId] || inputsMessages[passwordConfirmFieldId]?.text?.length <= 0 ? "no-form-field-text-message" : "",
                    inputsMessages[passwordConfirmFieldId] && inputsMessages[passwordConfirmFieldId].type !== constant.messageFormField ? "has-info-icon" : "")}
                onClick={togglePasswordVisibility}>
                {showPassword ? <VscEyeClosed/> : <VscEye/>}</span>

        </div>


        <div className="field-container">
            <button className="btn-submit-auth-form registration-form">Подтвердить email</button>
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

export default RegistrationForm