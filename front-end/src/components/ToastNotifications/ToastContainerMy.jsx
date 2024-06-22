import '../../styles/styles.css'
import {useStoreStateSelector} from "../../hooks/useStoreStateSelector";
import ToastComponent from "./ToastComponent/ToastComponent";
import {toastsContainerPositions} from "../../infrastrucutre/constants";

const React = require('react')



function ToastContainerMy() {

    const toastsState = useStoreStateSelector(state => state.toasts);


    return <div className="toast-container"
                style={{top: toastsState.containerPosition === toastsContainerPositions.TOP ? "5rem" : "68%"}}>
        {[...toastsState.toastsPaged.values()].map(i => {
            const t = toastsState.toasts[i];

            return t ? <ToastComponent key={t.id} id={t.id} type={t.type} message={t.message} timeout={t.timeout}/> : null;
        })}
    </div>
}

export default ToastContainerMy