import {useContext, useEffect, useState} from "react";
import {ModalContext} from "../ModalConfig";
import ReactDOM from "react-dom";
import "./Modal.css";
import { GrClose } from "react-icons/gr";
import cn from "classnames";

const React = require('react');

export const ModalPortal = () => {

    // Получить состояние
    const {isModalOpened, closeByOuterClick, modalContent, closeModal} = useContext(ModalContext);

    function handleEscPress(e) {
        e.stopPropagation();
        if (e.keyCode !== 27)
            return;

        closeModal();
    }

    // Закрытие по нажатию esc (в зависимостях функция, поскольку изначально она пуста)
    /*useEffect(() => {

        if (!closeByOuterClick)
            return;

        window.addEventListener("keydown", handleEscPress);

        console.log('Добавили обработчик нажатия на esc!');

        return () => {
            console.log('Убираем обработчик нажатия на esc!');
            window.removeEventListener("keydown", handleEscPress)
        }

    }, [closeModal]);*/

    useEffect(() => {

        if (isModalOpened){
            // Получить ширину scrollbar'a
            const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;
            document.body.style.cssText = `overflow: hidden; margin-right: ${scrollbarWidth + 6.3}px`;
        }else {

            document.body.style.overflow = "auto";
            document.body.style.cssText = '';

        }

        if (!closeByOuterClick || !isModalOpened)
            return;

        window.addEventListener("keydown", handleEscPress);


        return () => {
            window.removeEventListener("keydown", handleEscPress)
        }

    }, [isModalOpened]);

    function onOverlayMouseDown(e) {
        if (e.target === e.currentTarget && closeByOuterClick)
            closeModal();
    }

    /*return !isModalOpened ? null : ReactDOM.createPortal(
        <div onClick={closeByOuterClick ? closeModal : () => {}} className="modal-overlay">
            <div className="modal-container" onClick={e => {e.stopPropagation()}}>
                {closeByOuterClick ? <div className="modal-close-btn-container">
                    <GrClose className="modal-close-btn" onClick={closeModal}></GrClose>
                    <span>Esc</span>
                </div> : ""}
                <div className="modal-body">{modalContent}</div>
            </div>
        </div>,
        document.getElementById("modal-root"));*/

    return ReactDOM.createPortal(
        <div onMouseDown={onOverlayMouseDown} className={cn("modal-overlay", isModalOpened ? "active" : "")}>
            <div className={cn("modal-container", isModalOpened ? "active" : "")} onClick={e => {e.stopPropagation()}}>
                {closeByOuterClick ? <div className="modal-close-btn-container">
                    <GrClose className="modal-close-btn" onClick={closeModal}></GrClose>
                    <span>Esc</span>
                </div> : ""}
                <div className="modal-body" >{modalContent}</div>
            </div>
        </div>,
        document.getElementById("modal-root"));

}