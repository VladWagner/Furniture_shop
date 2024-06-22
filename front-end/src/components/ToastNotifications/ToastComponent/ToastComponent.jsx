import './ToastComponent.css'
import {useActions} from "../../../hooks/useActions";
import {GrClose} from "react-icons/gr";
import {useEffect, useRef, useState} from "react";
import cn from "classnames";
import {FaCircleCheck, FaCircleXmark} from "react-icons/fa6";
import {IoIosWarning, IoMdInformationCircle} from "react-icons/io";
import {toastsTypes} from "../../../infrastrucutre/constants";

const React = require('react')

function ToastComponent({id, type, message, timeout = 0}) {

    const {removeToast} = useActions();
    const [isDeleted, setIsDeleted] = useState(false);

    // Таймер для проверки имени пользователя на уникальность при вводе
    const [timer, setTimer] = useState(null);

    // Сколько времени осталось до окончания timeout
    const [timeLeft, setTimeLeft] = useState(null);
    const [isAnimationActive, setIsAnimationActive] = useState(true);

    // % пройденной анимации
    const animAnimationPercentageRef = useRef(null);

    // Текущее время в момент запуска таймера
    let timerLaunchMomentRef = useRef(0);

    // Является ли текущее устройство мобильным
    let isMobileDeviceRef = useRef(false);
    const progressBarId = `pb_${id}`;

    const iconsToType = {
        [toastsTypes.SUCCESS]: <FaCircleCheck/>,
        [toastsTypes.ERROR]: <FaCircleXmark/>,
        [toastsTypes.MESSAGE]: <IoMdInformationCircle/>,
        [toastsTypes.WARNING]: <IoIosWarning/>
    }
    const stylesToType = {
        [toastsTypes.SUCCESS]: "success",
        [toastsTypes.ERROR]: "error",
        [toastsTypes.MESSAGE]: "message",
        [toastsTypes.WARNING]: "warning"
    }

    function onCloseToastClick() {
        setIsDeleted(true);

        removeToast(id);
        if (timer) {
            clearTimeout(timer);
        }
        //setTimeout(() => removeToast(id), 400)

    }

    useEffect(() => {

        if (timeout > 0) {

            // Если timeout задан в секундах, то перевести в миллисекунды
            if (timeout < 250)
                timeout = timeout * 1000;

            setTimer(setTimeout(() => {
                setIsDeleted(true);
                setTimeout(() => removeToast(id), 600)
            }, timeout));

            timerLaunchMomentRef.current = Date.now();

            isMobileDeviceRef.current = /Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent) || window.screen.width <= 480;
        }

        return () => {
            if (timer) {
                clearTimeout(timer);
            }
        }

    }, []);

    // Если кнопка мыши нажата, то остановить timer
    function onToastMouseEnter(e) {
        if (timer) {
            clearTimeout(timer);

            /*if (timerLaunchMomentRef.current > 0) {
                timeout = timeout < 250 ? timeout * 1000 : timeout;
                setTimeLeft(timeout - (Date.now() - timerLaunchMomentRef.current));

            }*/

            const bar = document.getElementById(progressBarId);

            if (!bar) {
                timeout = timeout < 250 ? timeout * 1000 : timeout;
                setTimeLeft(timeout - (Date.now() - timerLaunchMomentRef.current));
                return;
            }

            const barAnimation = bar.getAnimations()[0];

            barAnimation.pause();

            // Вычислить % пройденной анимации для понимания % пройденного времени из timeout
            const percentage = (barAnimation.currentTime / barAnimation.effect.getComputedTiming().duration);
            animAnimationPercentageRef.current = percentage;

            setTimeLeft((timeout - (timeout * percentage)) * 1000);

            //const toast = e.target.getBoundingClientRect();

        }
    }

    // Если кнопка мыши отпущена, то продолжить timer с того же места
    function onToastMouseLeave() {

        if (timerLaunchMomentRef.current <= 0)
            return;

        // Если timeout задан в секундах, то перевести в миллисекунды
        /*if (timeout < 250)
             timeout = timeout * 1000;

          // Сколько времени прошло с момента нажатия на toast и началом отсчёта
        const timeGone = Date.now() - timerLaunchMomentRef.current;

        if (timeGone < timeout) {

            setTimer(setTimeout(() => {
                setIsDeleted(true);
                setTimeout(() => removeToast(id), 600)
            }, timeout - timeGone));

        }
        else {
            setIsDeleted(true);
            setTimeLeft(50);
            setTimeout(() => removeToast(id), 600);
        }// Вновь запустить анимацию
        setIsAnimationActive(true);
    */

        setTimer(setTimeout(() => {
            setIsDeleted(true);
            setTimeout(() => removeToast(id), 600)
        }, timeLeft));


        const bar = document.getElementById(progressBarId);
        if (!bar) {
            return;
        }

        const barAnimation = bar.getAnimations()[0];

        barAnimation.play();

    }

    return <div className={cn("toast", isDeleted ? "hide-toast-fast" : "")} onMouseEnter={() => {
        if (!isMobileDeviceRef.current)
            onToastMouseEnter();
    }}
     onMouseLeave={() => {
             if (!isMobileDeviceRef.current)
                 onToastMouseLeave();
         }} onTouchStart={onToastMouseEnter} onTouchEnd={onToastMouseLeave}>
        <span className={cn("toast-icon", stylesToType[type])}>{iconsToType[type]}</span>
        <span className="toast-message">{message}</span>
        <span className="toast-close-btn-container"><GrClose className="toast-close-btn"
                                                             onClick={onCloseToastClick}></GrClose></span>
        <div className={cn("toast-progress", stylesToType[type])} id={progressBarId} style={{
            width: countProgressBarWidth(timeout, animAnimationPercentageRef.current) + '%',
            animation: `progress ${timeout + 's'} linear forwards`
        }}></div>
    </div>
}

export default ToastComponent

function countProgressBarWidth(timeout, animationPercent) {

    return animationPercent ? 100 - animationPercent.current : 100;
}
