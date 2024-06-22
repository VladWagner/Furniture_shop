import './SyncBasketModal.css'
import {useStoreStateSelector} from "../../../hooks/useStoreStateSelector";
import {useContext, useEffect} from "react";
import {ModalContext} from "../../ModalConfiguration/ModalConfig";
import "../../../styles/styles.css"
import * as utils from "../../../infrastrucutre/utils";
import {setSynchronizeBasketFlag} from "../../../store/actions/cartActions";
import {useActions} from "../../../hooks/useActions";

const React = require('react')

function SyncBasketModal() {

    const basketState = useStoreStateSelector(state => state.cart);
    const {openModal, closeModal} = useContext(ModalContext);
    const {setSynchronizeBasketFlag} = useActions();

    // Обработчик клика на кнопку синхронизации корзины
    function onBtnClick(flag) {
        setSynchronizeBasketFlag(flag);
        closeModal();
    }

    const productsVariantsList = !basketState.basketDto || basketState.syncServerBasket === true ? null : <div className="modal-basket-preview">
        <div className="pv-list-basket-preview in-modal">
            {
                basketState.basketDto.productVariantsAndCount && basketState.basketDto.productVariantsAndCount.map(pvAndCountDto => {

                    let pvDto = pvAndCountDto.productVariantDto;

                    if (!pvDto)
                        return null;

                    let id = pvDto.id + "_modal";

                    let quantity = pvAndCountDto.count ? pvAndCountDto.count  : 1;

                    quantity = !quantity ? 1 : quantity;

                    return <div className="product-variant-basket-preview" key={id}>
                        <div className="product-variant-mini-basket-img"><img height="100%" width="100%"
                                                                              src={utils.getCorrectStaticFilePath(pvDto.previewImgLink)}
                                                                              alt={pvDto.productName}/></div>
                        <div className="product-variant-mini-content">
                            <span
                                className="pv-mini-basket-preview-title">{utils.uniteProductAndVariantNames(pvDto.productName, pvDto.title)}</span>
                            <div className="pv-mini-basket-preview-info">

                                <div className="pv-basket-price">
                                    {utils.formatStringFromNum(pvDto.discountPrice ?
                                        pvDto.discountPrice * quantity :
                                        pvDto.price * quantity)} Р.
                                </div>
                            </div>
                            <div>
                                Количество: {quantity}
                            </div>
                        </div>
                    </div>
                })
            }
        </div>
        <div className="basket-preview-info-block footer">
            <span>Сумма:</span>
            <span>{
                utils.formatStringFromNum(basketState.basketDto.sum)
            } Р.</span>
        </div>
    </div>;

    const modalContent = <div>
        <div className="modal-title">На сервере обнаружена корзина!</div>
        {productsVariantsList}
        <div className="modal-message">Хотите синхронизировать корзину с вашим аккаунтом?</div>
        <div className="basket-modal-buttons">
            <button className="btn btn-local-modal" onClick={() => onBtnClick(true)}>Да</button>
            <button className="btn btn-local-modal reject" onClick={() => onBtnClick(false)}>Нет</button>
        </div>
    </div>;

    // Хук должен отработать и вывести модальное окно только в первый рендеринг компонента
    useEffect(() => {

        if (basketState?.syncServerBasket === false && basketState.basketDto) {

            openModal(modalContent, false);
        }

    }, [basketState.basketDto])

    return null;
}

export default SyncBasketModal