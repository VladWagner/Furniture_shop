import React, {useEffect, useRef, useState} from 'react';

/*const React = require('react')*/

// Контекст для хранения состояния модального окна и его контента
export const ModalContext = React.createContext({
    // Открыто ли сейчас какое-либо модальное окно
    isModalOpened: false,

    // Разметка внутри модального окна
    modalContent: null,

    // Закрывать ли модальное коно при клике на за пределы окна или по кнопке закрытия
    closeByOuterClick: false,
    openModal: () => {},
    closeModal: () => {}
});

// Provider для модальных окон
export const ModalProvider = ({children}) => {

    const [modalContent, setModalModalContent] = useState(null);

    // Флаг открытия модального окна
    const [isModalOpen, setIsModalOpen] = useState(false);

    // Флаг закрытия модального окна при клике за пределы
    const [closeByOuterClick, setCloseByOuterClick] = useState(false);

    // Функция для определения поведения при закрытии окна
    //const [onCloseModalBehavior, setOnCloseModalBehavior] = useState(null);
    const onCloseModalBehaviorRef = useRef(null);

    const openModal = (content, closeByOuterClick = true, onCloseAdditionalHandler = null) => {

        // Задать содержимое модального окна
        setModalModalContent(content);
        setIsModalOpen(true);
        setCloseByOuterClick(closeByOuterClick);
        onCloseModalBehaviorRef.current = onCloseAdditionalHandler;
    };

    const closeModal = () => {
        setIsModalOpen(false);

        if (onCloseModalBehaviorRef.current && typeof onCloseModalBehaviorRef.current === 'function') {
            onCloseModalBehaviorRef.current();
        }
    };

    return (

      <ModalContext.Provider value={{ isModalOpened: isModalOpen, closeByOuterClick: closeByOuterClick, modalContent: modalContent,
          openModal: openModal, closeModal: closeModal }}>
          {children}
      </ModalContext.Provider>
    );
};