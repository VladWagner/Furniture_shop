import './EmailConfirmationForm.css'
import '../FormMutualStyles.css'
import {useEffect, useRef, useState} from "react";
import cn from "classnames";
import {Link} from "react-router-dom";
import {Constants as constant, toastsContainerPositions} from "../../../infrastrucutre/constants";
import {RxCross1} from "react-icons/rx";
import {GrValidate} from "react-icons/gr";
import {VscEye, VscEyeClosed} from "react-icons/vsc";
import {useActions} from "../../../hooks/useActions";
import {Constants} from "../../../infrastrucutre/constants";
import {errorFieldCssClass} from "../FormConstants";
import {instanceWithoutInterceptor} from "../../../infrastrucutre/axiosInterceptor";
import {useStoreStateSelector} from "../../../hooks/useStoreStateSelector";
import {useToastActions} from "../../../hooks/useToastActions";

const React = require('react')

function EmailConfirmationForm({onSubmitAdditionalHandler}) {

    const {email} = useStoreStateSelector(state => state.user);

    const {confirmCodeFieldId} = {
        confirmCodeFieldId: "confirmation-code-field"
    };

    const [fieldsValues, setFieldsValues] = useState({});
    const [isFieldFilled, setIsFieldFilled] = useState({});
    const [formGeneralError, setFormGeneralError] = useState();
    const [formGeneralMessage, setFormGeneralMessage] = useState();
    const [showAwaitAnimation, setShowAwaitAnimation] = useState(false);

    const [inputsMessages, setInputsMessages] = useState({});
    const [openAuthForm, setOpenAuthForm] = useState(false);

    const [showSendAgainBtn, setShowSendAgainBtn] = useState(false);

    // Количество времени до повторной отправки
    const timeBeforeNextSend = 30;
    const [sendAgainTimeCount, setSendAgainTimeCount] = useState(timeBeforeNextSend);
    const sendAgainTimeCountRef = useRef(timeBeforeNextSend);
    const sendAgainInterval = useRef(null);

    const setErrorFieldMessage = (message) => ({
        ["type"]: constant.errorFormField,
        ["text"]: message,
        ["style"]: errorFieldCssClass
    });

    useEffect(() => {
        startCountingBackTimer();

        return () => {
            if (sendAgainInterval.current)
                clearInterval(sendAgainInterval.current);
        }
    }, []);

    // Обработка снятия фокуса с элемента для смещения label + задание значений в состояние для отправки формы
    function onInputChange(e) {

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

    function onInputBlur(e) {

        if (e?.target && fieldsValues[e.target.id] === e.target.value && inputsMessages[e.target.id])
            return;

        onInputChange(e);
    }

    function onFormFieldInput(e) {
        const id = e.target.id;

        if (!id || !inputsMessages[id])
            return;

        setInputsMessages(prevState => ({...prevState, [id]: null}));
    }

    function sendAgainHandler() {

        if (!email) {
            setFormGeneralError("Почта указана некорретно!");
            return;
        }

        instanceWithoutInterceptor.get(`/api/users/resend_confirmation?email=${email}`).then(result => {

            setShowAwaitAnimation(false);
            setFormGeneralMessage("Код успешно отправлен, проверяйте почту!")

        }).catch(error => {
            console.log('Ошибка при повторной отправке:')
            console.dir(error);

            const message = error?.response?.data?.message;

            if (!message)
                return

            setFormGeneralError(message);
            setShowAwaitAnimation(false);
        });

        startCountingBackTimer();
    }

    // Общая валидация всех полей перед отправкой запроса
    function validateForm() {

        const confirmationCode = fieldsValues[confirmCodeFieldId];

        if (!confirmationCode || confirmationCode === "") {
            setInputsMessages(prevState => ({
                ...prevState, [confirmCodeFieldId]: setErrorFieldMessage("поле должно быть заполнено")
            }))

        }

        return (confirmationCode && confirmationCode.length > 0);
    }

    async function onSubmit(e) {
        e.preventDefault();

        if (!validateForm())
            return;


        instanceWithoutInterceptor.get(`/api/users/confirm?token=${fieldsValues[confirmCodeFieldId]}`).then(result => {

            setShowAwaitAnimation(false);
            setFormGeneralMessage("Почто подтверждена. Теперь выможете войти в аккаунт!");

            setTimeout(() => {
                if (onSubmitAdditionalHandler && typeof onSubmitAdditionalHandler === 'function')
                    onSubmitAdditionalHandler();
            }, 800)

        }).catch(error => {
            const message = error?.response?.data?.message;

            if (!message)
                return

            setFormGeneralError(message);
            setShowAwaitAnimation(false);
        });
        setShowAwaitAnimation(true);

        /*setTimeout(() => {
            if (onSubmitAdditionalHandler && typeof onSubmitAdditionalHandler === 'function')
                onSubmitAdditionalHandler();
        }, 1200)*/
    }

    function startCountingBackTimer() {

        // Отключить вывод кнопки повторной отправки
        setShowSendAgainBtn(false);

        sendAgainInterval.current = setInterval(() => {

            // По окончанию времени сбросить timer и задать базовое значение в кол-во оставшегося времени
            if (sendAgainTimeCountRef.current <= 1){
                setShowSendAgainBtn(true);

                clearInterval(sendAgainInterval.current);

                setSendAgainTimeCount(timeBeforeNextSend);
                sendAgainTimeCountRef.current = timeBeforeNextSend;
                return;
            }

            // Изменить кол-во оставшегося времени
            setSendAgainTimeCount(prevState => {
                const newCount = prevState - 1;

                sendAgainTimeCountRef.current = newCount;
                return newCount;
            });
        }, 1000);
    }
    
    return <div className="confirm-form-modal-wrapper">
        <div className="modal-header">
            <p>Подтверждение email</p>
        </div>
        <form onSubmit={onSubmit}>
            <div className="confirm-form-field">
                <div className={cn("field-container", "confirm-form-field-container",
                    isFieldFilled[confirmCodeFieldId] ? isFieldFilled[confirmCodeFieldId] : "", inputsMessages[confirmCodeFieldId]?.style)}>
                    <input className="form-input" id={confirmCodeFieldId} type="text" onChange={onInputChange} onBlur={onInputBlur} onInput={onFormFieldInput}/>
                    <label className="form-label" htmlFor={confirmCodeFieldId}>Введите код подтверждения</label>

                    {inputsMessages[confirmCodeFieldId] && inputsMessages[confirmCodeFieldId]?.text?.length > 0 ?
                        <span className="form-under-input-msg">{inputsMessages[confirmCodeFieldId].text}</span> : null}
                </div>

                <div className="btn-confirm-container" >
                    <button className="btn-submit-auth-form">подтвердить</button>
                </div>
                <div className="confirm-process-descr">
                    <p>Код для подтверждения был отправлен на указанную почту: {email ? email : "---"}</p>
                </div>
                <div className="send-again-btn" style={{textAlign: !showSendAgainBtn ? "center" : ""}}>
                    {showSendAgainBtn ? <span className="send-again-btn-text" onClick={sendAgainHandler}>Отправить повторно?</span> :
                        <span className="send-again-timer">Отправить повторно можно будет через {sendAgainTimeCount} сек.</span>}
                </div>
            </div>


            {formGeneralError ? <div className="form-error">{formGeneralError}</div> : null}
            {formGeneralMessage ? <div className="form-message">{formGeneralMessage}</div> : null}
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
    </div>
}

export default EmailConfirmationForm