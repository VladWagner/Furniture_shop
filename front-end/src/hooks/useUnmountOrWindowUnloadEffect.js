import {useEffect, useRef} from "react";

// Хук для задания обработчика на закрытие или обновление окна (сохранение или что-либо ещё)
export const useUnmountOrWindowUnloadEffect = (unmountHandler, callOnEffectCleanUp) => {

    const handlerRef = useRef();

    handlerRef.current = unmountHandler

    useEffect(() => {
        const handler = handlerRef.current;

        window.addEventListener('beforeunload', handler);
        //window.addEventListener('unload', handler);

        return () => {

            console.log('Unmount в отдельном хуке сработал!');

            if (callOnEffectCleanUp)
                handler();

            window.removeEventListener('beforeunload', handler);
        }

    }, [])
}