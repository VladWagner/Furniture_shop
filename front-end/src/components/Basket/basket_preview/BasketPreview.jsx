import './BasketPreview.css'
import {useEffect, useLayoutEffect, useMemo, useReducer, useRef, useState} from "react";
import {useStoreStateSelector} from "../../../hooks/useStoreStateSelector";
import {useActions} from "../../../hooks/useActions";
import {IoIosAdd, IoIosArrowBack, IoIosArrowForward, IoIosRemove} from "react-icons/io";
import * as utils from "../../../infrastrucutre/utils"
import {MdDeleteOutline} from "react-icons/md";
import {ProductsVariantsApi} from "../../../api/endpoints/productsVariantsApi";
import SyncBasketModal from "../sync_basket_modal/SyncBasketModal";
import {useDispatch} from "react-redux";
import {useUnmountOrWindowUnloadEffect} from "../../../hooks/useUnmountOrWindowUnloadEffect";
import {instance} from "../../../infrastrucutre/axiosInterceptor";
import {Link} from "react-router-dom";

const React = require('react')

function BasketPreview() {

    const [pvDtoLocalList, setPvDtoLocalList] = useState(null);

    const basketState = useStoreStateSelector(state => state.cart);
    const userState = useStoreStateSelector(state => state.user);
    let {pvIdsList, pvDtoList, idsMapUpdated, sum,} = useStoreStateSelector(state => state.cart);
    const {
        setInitialBasketState,
        updatePvCounterInBasket,
        removePvFormBasket,
        synchronizeBasketWithServer
    } = useActions();
    const [basketHovered, setBasketHovered] = useState(false);
    const [timer, setTimer] = useState(null);
    const [inputError, setInputError] = useState({})
    const counterRef = useRef(null);
    const [ignore, forceUpdate] = useReducer(x => x + 1, 0, x => 0);

    // Получить состояние корзины
    useEffect(() => {
        setInitialBasketState(userState.isAuth, userState.isConfirmed);

        return () => {
            clearTimeout(timer);
        }
    }, []);

    // Получение вариантов с сервера
    useEffect(() => {

        if ((pvIdsList && pvIdsList.size > 0) && (!pvDtoList || pvDtoList.length === 0)) {
            ProductsVariantsApi().getByIdsList([...pvIdsList.keys()])
                .then(resp => {
                    setPvDtoLocalList(resp ? resp.sort((pv1, pv2) => pvIdsList.get(pv1.id).timeStamp - pvIdsList.get(pv2.id).timeStamp) : resp);
                    basketState.idsMapUpdated = false;
                });

        } else if (pvDtoList && pvDtoList.length > 0)
            setPvDtoLocalList(null)

    }, [pvIdsList.size, idsMapUpdated, pvDtoList])

    // Отключение scrollbar на body, чтобы прокручивание было только в окне корзины
    useLayoutEffect(() => {
        let basketPanel = document.getElementsByClassName("basket-preview-container")[0];

        if (basketHovered) {
            // Получить ширину scrollbar'a
            const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;
            document.body.style.cssText = `overflow: hidden; margin-right: ${scrollbarWidth + 6.3}px`;
            if (basketPanel)
                basketPanel.style.marginRight = `${scrollbarWidth}px`;

        } else {
            document.body.style.overflow = "auto";
            document.body.style.cssText = '';

            if (basketPanel)
                basketPanel.style.marginRight = ``;
        }

    }, [basketHovered]);

    // Уменьшение/увеличение кол-ва вариантов
    function onQuantityBtnClick(e) {

        let btnType = e.currentTarget.getAttribute("data-btn-type");
        let pvId = e.currentTarget.getAttribute("data-pv-id")

        if (!pvId)
            return;

        if (typeof pvId === 'string')
            pvId = Number.parseInt(pvId);

        let inputElement = document.getElementById(`basket-preview-quantity-pv-${pvId}`);
        let inputVal = inputElement && inputElement.value ? Number.parseInt(inputElement.value) : null;

        console.log(`PV id: ${pvId}`)

        // Определить тип кнопки
        if (btnType && btnType.toLowerCase().includes('incr')) {

            if ((inputVal && inputVal) < 50 || inputVal === 0) {
                inputElement.value = inputVal + 1;

                console.log(`Increment amount. InputVal ${inputVal}. Curr_val: ${inputElement.value}`)

                // Снять ошибки с поля ввода, если они есть, поскольку установленные значения корректны
                if (inputError[inputElement.id] && inputError[inputElement.id] === true)
                    setInputError(prevState => ({...prevState, [inputElement.id]: false}));

                updatePvCounterInBasket(pvId, inputVal + 1);
            }

        } else if (btnType && btnType.toLowerCase().includes('decr')) {

            if (inputVal && inputVal > 1) {
                inputElement.value = inputVal - 1;

                // Снять ошибки с поля ввода
                if (inputError[inputElement.id] && inputError[inputElement.id] === true) {
                    setInputError(prevState => ({...prevState, [inputElement.id]: false}));
                }
                updatePvCounterInBasket(pvId, inputVal - 1);
            }


        }
    }

    // Изменение кол-ва вариантов через ввод кол-ва
    function handleInputBlur(e) {

        // Если input будет отрабатывать по выходу с элемента ввода, а не по timer
        if (timer)
            clearTimeout(timer);

        let pvId = e.target.getAttribute("data-pv-id");

        if (!pvId)
            return;

        if (typeof pvId === 'string')
            pvId = Number.parseInt(pvId);

        let quantity = e.target.value;
        //let quantity = counterRef.current.value;

        if (typeof quantity === 'string')
            quantity = Number.parseInt(quantity);

        // Задать ошибку на определённый input
        if (quantity > 50 || quantity < 1) {
            setInputError(prevState => ({...prevState, [e.target.id]: true}));
            return;
        } else if (inputError[e.target.id] === true)
            setInputError(prevState => ({...prevState, [e.target.id]: false}))

        updatePvCounterInBasket(pvId, quantity);

    }

    // Обработка отпуска клавиши при вводе кол-ва значений
    function onKeyUp(e) {

        if (timer) {
            clearTimeout(timer);
        }

        setTimer(setTimeout(() => handleInputBlur(e), 1000)); // Задержка в 1 секунды
    }

    // Обработка удаления товара из корзины
    function onDeletePvClick(e) {
        let pvId = e.currentTarget.getAttribute("data-pv-id");

        if (!pvId)
            return;

        pvId = Number.parseInt(pvId);

        removePvFormBasket(pvId);

        // Временное удаление элемента из списка
        let indexToRemove = pvDtoLocalList.findIndex(pv => pv.id === pvId);
        console.log(`Id: ${pvId}, idx_to_remove: ${indexToRemove}. pvDtoList: ↓`);
        console.dir(pvDtoLocalList);

        if (indexToRemove < 0 || indexToRemove > pvDtoLocalList.length)
            return;

        setPvDtoLocalList(prevList => {

            let newList = [...prevList];

            newList.splice(indexToRemove, 1);
            return newList;
        });


    }

    // Сохранение при уходе с компонента 
    function syncBasketOnBlur(e) {

        // Доп.проверка, чтобы не вызывать получение состояний в методе синхронизации корзины
        if (basketState.basketToSynchronize === true || basketState.syncServerBasket)
            synchronizeBasketWithServer(true);
    }

    // Рендер карточки варианта
    function renderPvCard(pvDto, count) {

        return <div className="product-variant-basket-preview" key={pvDto.id}>
            <Link to={`/product-info/${pvDto.productId}`} className="product-variant-mini-link"></Link>
            <div className="product-variant-mini-basket-img"><img height="100%" width="100%"
                                                                  src={utils.getCorrectStaticFilePath(pvDto.previewImgLink)}
                                                                  alt={pvDto.productName}/></div>
            <div className="product-variant-mini-content">
                            <span
                                className="pv-mini-basket-preview-title">{utils.uniteProductAndVariantNames(pvDto.productName, pvDto.title)}</span>
                <div className="pv-mini-basket-preview-info">

                    <div className="basket-pv-quantity-buttons">
                        <button className="btn counter" onClick={onQuantityBtnClick} data-pv-id={pvDto.id}
                                data-btn-type="decr" disabled={count <= 1}><IoIosRemove/></button>
                        <input className="basket-pv-quantity-input" onKeyUp={onKeyUp} onBlur={handleInputBlur}
                               type="number" id={`basket-preview-quantity-pv-${pvDto.id}`}
                               data-pv-id={pvDto.id}
                               defaultValue={count}
                               ref={counterRef}
                               style={{border: inputError[`basket-preview-quantity-pv-${pvDto.id}`] === true ? "1px solid #ff4400" : ""}}/>
                        <button className="btn counter" onClick={onQuantityBtnClick} data-pv-id={pvDto.id}
                                data-btn-type="incr" disabled={count >= 50}><IoIosAdd/></button>
                    </div>

                    <div className="pv-basket-price">
                        {utils.formatStringFromNum(pvDto.discountPrice ? pvDto.discountPrice * count : pvDto.price * count)} Р.
                    </div>
                </div>

            </div>
            <span className="pv-remove-from-basket" data-pv-id={pvDto.id} onClick={onDeletePvClick}>
                            {/*<MdDeleteOutline/>*/}
                <svg stroke="currentColor" fill="currentColor" stroke-width="0" viewBox="0 0 24 24"
                     height="1.3rem"
                     width="1.3rem" xmlns="http://www.w3.org/2000/svg"><path fill="none"
                                                                             d="M0 0h24v24H0V0z"></path><path
                    d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4z"></path></svg>
                        </span>
        </div>
    }


    return <div className="basket-preview-container" onMouseEnter={(e) => setBasketHovered(true)}
                onMouseLeave={(e) => {
                    setBasketHovered(false);
                    syncBasketOnBlur(e);
                }}>
        {basketState?.syncServerBasket === false && basketState.basketDto ? <SyncBasketModal/> : null}

        <div className="basket-preview-info-block header">
            <span>В корзине:</span>
            <span>{basketState.pvCount} {utils.getRightNumDeclension(basketState.pvCount, "товар", "товара", "товаров")}</span>
        </div>
        {/*Вывод списка вариантов товаров в корзине*/}
        <div className="pv-list-basket-preview">
            <div className="product-variant-basket-preview">
                <a href="#" className="product-variant-mini-link"></a>
                <div className="product-variant-mini-basket-img"><img width="100%" height="100%"
                                                                      src="http://192.168.0.105:3001/drive/Graduation%20project/back-end/uploads/thumbnails/8/12/SHT-1-3yuVe9HZ-thumb.jpeg"
                                                                      alt="variant"/></div>
                <div className="product-variant-mini-content">
                    <span className="pv-mini-basket-preview-title">Product_variant_1</span>
                    <div className="pv-mini-basket-preview-info">

                        <div className="basket-pv-quantity-buttons">
                            <button className="btn counter" onClick={onQuantityBtnClick} data-pv-id={0}
                                    data-btn-type="decr" disabled={1 < 1}><IoIosRemove/></button>
                            <input className="basket-pv-quantity-input" onKeyUp={onKeyUp} onBlur={handleInputBlur}
                                   type="number" id={`basket-preview-quantity-pv-${0}`}
                                   data-pv-id={0}
                                   defaultValue={1}
                                   style={{border: inputError[`basket-preview-quantity-pv-${0}`] === true ? "1px solid #ff4400" : ""}}/>
                            <button className="btn counter" onClick={onQuantityBtnClick} data-pv-id={0}
                                    data-btn-type="incr" disabled={50 > 50}><IoIosAdd/></button>
                        </div>

                        <div className="pv-basket-price">

                        </div>
                    </div>

                </div>
                <span className="pv-remove-from-basket" data-pv-id={0}>
                    {/*<MdDeleteOutline/>*/}
                    <svg stroke="currentColor" fill="currentColor" stroke-width="0" viewBox="0 0 24 24"
                         height="1.6rem"
                         width="1.6rem" xmlns="http://www.w3.org/2000/svg"><path fill="none" d="M0 0h24v24H0V0z"></path><path
                        d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4z"></path></svg>
                </span>
            </div>

            {
                pvDtoLocalList ? pvDtoLocalList.map(pvDto => {

                        let id = pvDto.id;

                        let quantity = pvIdsList ? pvIdsList.get(id)?.value : 1;

                        quantity = !quantity ? 1 : quantity;

                        return renderPvCard(pvDto, quantity);
                    }) :
                    pvDtoList && pvDtoList.map(pvAnCount => renderPvCard(pvAnCount.productVariantDto, pvAnCount.count))
            }
        </div>
        <div className="basket-preview-info-block footer">
            <span>Сумма:</span>
            <span>{
                utils.formatStringFromNum(sum ? sum : countSum(pvDtoLocalList, pvIdsList))
            } Р.</span>
        </div>
        <div className="basket-preview-info-block go-to-basket-button">
            <span style={{alignSelf: "center"}}>Перейти на страницу корзины:</span>
            <span className="arrow-forward-basket"><IoIosArrowForward/></span>
        </div>
    </div>
}

export default BasketPreview

const countSum = (pvDtoList, idAndQuantityList) => {

    if ((!pvDtoList || !idAndQuantityList) || (pvDtoList.length === 0 || idAndQuantityList.length === 0))
        return 0;

    // Расчитать сумму в корзине
    return pvDtoList.reduce((sum, pv) => {

        if (idAndQuantityList.hasOwnProperty(pv.id))
            return sum;

        let pvPrice = pv.discountPrice ? pv.discountPrice : pv.price;
        return sum + pvPrice * idAndQuantityList.get(pv.id)?.value;
    }, 0)
}