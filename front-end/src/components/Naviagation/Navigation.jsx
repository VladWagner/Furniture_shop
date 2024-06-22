import {Link} from "react-router-dom";
import './Navigation.css'
import logo from '../../assets/img/M.logo_wb.svg'

import 'react-bootstrap';
import {useEffect, useLayoutEffect, useState} from "react";
import SearchLine from "../SearchLine/SearchLine";
import cn from "classnames";
import {FaCartShopping, FaUserLarge} from "react-icons/fa6";
import {IoIosArrowBack, IoIosArrowForward} from "react-icons/io";
import {useActions} from "../../hooks/useActions";
import {useStoreStateSelector} from "../../hooks/useStoreStateSelector";
import BasketPreview from "../Basket/basket_preview/BasketPreview";
import AuthModal from "../AuthModal/AuthModal";
import * as utils from "../../infrastrucutre/utils";

const React = require('react')

function NavBar() {

    let categories = useStoreStateSelector(state => state.categories).categoriesTree;
    let {loadCategoriesTree, getUserDetailedInfo} = useActions();
    let {id, roles, isAuth, userName, profilePhoto} = useStoreStateSelector(state => state.user)

    let maxColSize = 7;

    const [isPointHovered, setIsHovered] = useState(false);
    const [toggle, setToggle] = useState({});
    const [sideBarActive, setSideBarActive] = useState(false);
    const [rootCategoryOpened, setRootCategoryOpened] = useState(false);
    const [scrollPosition, setScrollPosition] = useState({posX: 0, posY: 0});
    const [openAuthModal, setOpenAuthModal] = useState(false);

    const {basketState, pvCount} = useStoreStateSelector(state => state.cart);

    function pointHover() {
        if (!sideBarActive)
            setIsHovered(true);
    }

    function pointLeaveHover() {

        if (!sideBarActive)
            setIsHovered(false);
    }

    function toggleOpenClick(e) {
        // Получить идентификатор раскрывающегося пункта меню
        let id = e.currentTarget.id;

        setToggle(prevState => {
            const updatedToggle = {
                ...prevState, [id]: {
                    isOpened: prevState[id] ? !prevState[id].isOpened : true,
                    isClosed: prevState[id] ? !prevState[id].isClosed : false,
                    className: prevState[id] && prevState[id].isOpened ? "close-toggle" : "open-toggle"
                }
            };

            if (id.toString().toLowerCase().includes("l1")) {
                setRootCategoryOpened(updatedToggle[id].isOpened);

                if (updatedToggle[id].isClosed)
                    closeChildDropDowns();

            }

            return updatedToggle;
        });
    }

    function onCategoriesDropdownLeave() {

        if (sideBarActive)
            return;

        closeChildDropDowns();
    }

    // Закрыть все дочерние выпадающие списки
    function closeChildDropDowns() {
        // Пройти по всем ключам объекта и перевести их в состояние закрытого toggle
        setToggle(prevState => (
            Object.keys(prevState).reduce((newState, id) => {

                // Создать новую копию состояния для конкретного выпадающего списка
                newState[id] = {
                    ...prevState[id],
                    isOpened: false,
                    isClosed: true,
                    className: ""
                };
                return newState;
            }, {})
        ));
    }

    function handleBurgerIconClick() {
        setSideBarActive(prevState => {
            const newActivityState = !prevState;

            // Закрыть все открытые dropdown и принудительня снять флаг открытия выпадающего списка корневой категории
            if (!newActivityState) {
                setRootCategoryOpened(false);
                closeChildDropDowns();
            }

            return newActivityState;
        });
    }

    useEffect(() => {
        loadCategoriesTree();

        /* basketState.pvIdsList = new Map([
             [43, 2],
             [51, 1],
             [65, 1],
             [4, 1]
         ]);

         let count = 0;

         for (const val of basketState.pvIdsList.values()) {
             count = val + count;
         }

         basketState.pvCount = count;*/

    }, [])

    useLayoutEffect(() => {
        if (sideBarActive) {
            document.body.style.overflow = 'hidden';
            setScrollPosition({posX: window.scrollX, posY: window.scrollY});
            window.scrollTo(0, 0);

        } else {
            document.body.style.overflow = 'auto';
            window.scrollTo(scrollPosition.posX, scrollPosition.posY);
        }
    }, [sideBarActive])

    return <div>

        <div className="header-top">
            <form className="search-top">

                <div className="search-form-row-top">
                    <input className="input-top" name="q" placeholder="Search..." type="search"/>
                    <button className="search-button-top">
                        S
                    </button>
                </div>
            </form>
        </div>
        <div className="header">
            <div id="menu-icon" className={sideBarActive ? "active" : ""} onClick={handleBurgerIconClick}>
                <div className="spans-container">
                    <span className="first"></span>
                    <span className="second"></span>
                    <span className="third"></span>
                </div>
            </div>

            <div className="logo">
                <Link to='/'><img src={logo} alt=""/></Link>
            </div>

            <SearchLine/>

            <div className="header-icons">
                {/*Данная кнопка должна служить для входа/навигации по возможностям пользователя, если он аутентифицирован (из userReducer)
                Если isAuth: true, то сюда выводится автарка пользователя и появляется список возможностей*/}
                <div className="header-icon-container" onClick={() => !isAuth ? setOpenAuthModal(true) : null}>
                    {isAuth ? <Link to="/personal"
                                    style={{padding: profilePhoto && profilePhoto.length > 0 ? "" : "7.6px 0 3px 0"}}
                                    className={cn("header-icons-item", profilePhoto && profilePhoto.length > 0 ? "profile-photo" : "")}>
                            {profilePhoto && profilePhoto.length > 0 ? <img src={profilePhoto} alt=""/> :
                                <span><FaUserLarge/></span>}
                        </Link> :
                        <div style={{padding: profilePhoto && profilePhoto.length > 0 ? "" : "7.6px 0 3px 0", cursor: "pointer"}}
                             className={cn("header-icons-item", profilePhoto && profilePhoto.length > 0 ? "profile-photo" : "")}>
                            {profilePhoto && profilePhoto.length > 0 ? <img src={profilePhoto} alt=""/> :
                                <span><FaUserLarge/></span>}
                        </div>}
                    <span className="under-icon-caption">{userName && userName.length > 0 ? userName : "Войти"}</span>
                </div>
                <div className="header-icon-container side" style={{marginLeft: "1rem"}}>
                    <div className="basket-container">
                        <Link to="/basket" className="header-icons-item"
                              style={{padding: profilePhoto && profilePhoto.length > 0 || !profilePhoto ? "9px 0 0px 0" : ""}}>
                            <span><FaCartShopping style={{marginTop: "0.5px"}}/></span>
                            <span className="badge">{pvCount}</span>
                        </Link>
                        <span className="under-icon-caption">Корзина</span>
                        <BasketPreview/>
                    </div>
                </div>
            </div>
        </div>

        <div className="nav-container">
            <nav style={{display: sideBarActive ? "flex" : "", justifyContent: sideBarActive ? "space-between" : ""}}>
                <ul id="nav-items-list"
                    className={cn("nav-list", sideBarActive && rootCategoryOpened ? "side-root-category-opened" : "")}
                    style={{
                        overflowX: isPointHovered ? "hidden" : !sideBarActive ? "auto" : "hidden",
                        marginBottom: isPointHovered && !sideBarActive ? "8px" : ""
                    }}>
                    {
                        categories.map((category, index) => {

                            // Данный id будет использоваться только при открытом боковом меню навигации
                            let rootToggleId = `${category.id}_l1_toggle`;
                            let rootToggleOpened = sideBarActive && toggle[rootToggleId] && toggle[rootToggleId].isOpened;

                            return category.childCategories && category.childCategories.length > 0 ?
                                <li className={cn("nav-list-item", "dropdown-exists", rootToggleOpened ? "root-dropdown-opened" : "")}
                                    key={category.id} onMouseEnter={pointHover}
                                    onMouseLeave={pointLeaveHover}>

                                    {!sideBarActive ? <Link className="nav-link"
                                                            to={`products-by-category/${category.id}`}>{category.categoryName}</Link> :
                                        <div className="subcategories-dropdown-toggle"
                                             style={{justifyContent: toggle[rootToggleId] && toggle[rootToggleId].isOpened && sideBarActive ? "left" : ""}}>
                                            <div
                                                style={{display: toggle[rootToggleId] && toggle[rootToggleId].isOpened && sideBarActive ? "flex" : "none"}}
                                                id={`${category.id}_l1_toggle`} onClick={toggleOpenClick}
                                                className="subcategories-dropdown-backward">
                                                <span className="arrow-back-toggle"
                                                ><IoIosArrowBack/></span>
                                                <span>назад</span>
                                            </div>
                                            <Link
                                                to={`products-by-category/${category.id}`}>{category.categoryName}</Link>
                                            <span id={`${category.id}_l1_toggle`}
                                                  style={{display: toggle[rootToggleId] && toggle[rootToggleId].isOpened ? "none" : "inline"}}
                                                  onClick={toggleOpenClick}><IoIosArrowForward/></span>
                                        </div>
                                    }

                                    {
                                        createSubcategoriesLists(category.childCategories, maxColSize, {
                                            onToggleOpenClick: toggleOpenClick,
                                            categoriesDropdownListLeave: onCategoriesDropdownLeave
                                        }, toggle, rootToggleId)
                                    }
                                </li>
                                :
                                <li className="nav-list-item">
                                    <Link className="nav-link"
                                          to={`products-by-category/${category.id}`}>{category.categoryName}</Link>
                                </li>

                        })
                    }


                    <li className="nav-list-item dropdown-exists">
                        <a className="nav-link">Tree</a>
                        <div className="categories-dropdown" onMouseLeave={onCategoriesDropdownLeave}>

                            {/*При клике на кнопку раскрытия нужно будет добавлять данный стиль сюда*/}
                            <div className="col">
                                <div className="dropdown-list-item">

                                    <div className="subcategories-dropdown-toggle">
                                        <a href="/subcateg">Category </a>

                                        <span id="subcategories_L_2" onClick={toggleOpenClick}><IoIosArrowForward
                                            className={toggle["subcategories_L_2"] && toggle["subcategories_L_2"].className}/></span>
                                    </div>
                                    <div className={cn("subcategories-dropdown",
                                        toggle["subcategories_L_2"] && toggle["subcategories_L_2"].isOpened ? "active" : "")}>

                                        <div className="subcategory-dropdown-item">
                                            <a href="#">Subcategory level 2</a>
                                        </div>
                                        <div className="subcategory-dropdown-item">
                                            <a href="#">Subcategory level 2</a>
                                        </div>
                                        <div className="subcategory-dropdown-item">
                                            <div className="subcategories-dropdown-toggle">
                                                <a href="#">Subcategory level 2</a>

                                                <span id={"subcategories_L_3"}
                                                      onClick={toggleOpenClick}><IoIosArrowForward
                                                    className={toggle["subcategories_L_3"] && toggle["subcategories_L_3"].className}/></span>
                                            </div>
                                            <div className={cn("subcategories-dropdown",
                                                toggle["subcategories_L_3"] && toggle["subcategories_L_3"].isOpened ? "active" : "")}>

                                                <div className="subcategory-dropdown-item">
                                                    <a href="#">Subcategory level 2</a>
                                                </div>
                                                <div className="subcategory-dropdown-item">
                                                    <a href="#">Subcategory level long long long long</a>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="subcategory-dropdown-item">
                                            <a href="#">Subcategory level long long long long</a>
                                        </div>
                                    </div>
                                </div>

                                <div className="dropdown-list-item">
                                    <a href="#">Category 2</a>
                                </div>
                                <div className="dropdown-list-item">
                                    <a href="#">Category 3</a>
                                </div>
                            </div>

                            <div className="col">
                                <li className="dropdown-list-item">
                                    <a href="#">Category 4</a>
                                </li>
                                <li className="dropdown-list-item">
                                    <a href="#">Category 4</a>
                                </li>
                                <li className="dropdown-list-item">
                                    <a href="#">Category 4</a>
                                </li>
                                <li className="dropdown-list-item">
                                    <a href="#">Category 5</a>
                                </li>
                            </div>
                        </div>
                    </li>

                    {
                        isAuth === true && utils.isUserAdmin(roles) ?
                            <li className="nav-list-item-admin-panel">
                                <Link className="nav-link-admin" to="/admin">Панель управления</Link>
                            </li> : ""
                    }

                </ul>
            </nav>
        </div>

        {openAuthModal ? <AuthModal onCloseOuterFunction={() => setOpenAuthModal(false)}/> : null}
    </div>

}

export default NavBar

function createSubcategoriesLists(subcategories, maxColElementsCount, handlers, toggleState, rootToggleId) {

    // Рассчитать количество колонок горизонтального меню
    let columnsAmount = subcategories.length > maxColElementsCount ? Math.ceil(subcategories.length / maxColElementsCount) : 1;

    // В колонке желательно должно быть 4 элемента, иначе столбец слишком длинныый и много пустого места
    if (columnsAmount === 1 && subcategories.length >= 4) {
        columnsAmount = 2;
        maxColElementsCount = 4;
    } else if (columnsAmount === 2 && subcategories.length < maxColElementsCount * 2) {
        columnsAmount = Math.ceil(maxColElementsCount / 4);
        maxColElementsCount = 4;
    }


    // Сформировать пустой массив размером == кол-ву колонок -> получить часть массива подкатегорий так,
    // чтобы смещение индекса соответствовало <= требуемому количеству элементов в одной колонке
    // если элементов со смещением < кол-ва колонок, то slice просто вернёт все элементы до конца массива
    let cols = Array(columnsAmount)
        .fill(0)
        .map((_, i) =>
            subcategories.slice(i * maxColElementsCount, (i + 1) * maxColElementsCount)
        );

    return (
        <div
            className={cn("categories-dropdown", toggleState[rootToggleId] && toggleState[rootToggleId].isOpened ? "sb-opened" : "")}
            onMouseLeave={handlers.categoriesDropdownListLeave}>
            {
                // Проходим по массиву колонок (массив массивов)
                cols.map((col, i) => (
                    <div className="col" key={i + 1000}>
                        {
                            // Массив уже конкретных элементов в каждой колонке
                            col.map(sub => (
                                <div className="dropdown-list-item" key={sub.id}>
                                    {sub.childCategories && sub.childCategories.length > 0 ? createSubcategoriesToggles(sub, handlers, toggleState) :
                                        <Link to={`products-by-category/${sub.id}`}>{sub.categoryName}</Link>}
                                </div>
                            ))
                        }
                    </div>
                ))
            }
        </div>
    );
}// createSubcategoriesLists

// Рекурсивная функция вывода категорий >= 2 уровня вложенности
function createSubcategoriesToggles(category, handlers, toggleState) {

    if (!category)
        return "";
    let toggleId = `${category.id}_toggle`;
    return <>

        <div className="subcategories-dropdown-toggle">
            <Link to={`products-by-category/${category.id}`}>{category.categoryName}</Link>

            <span id={toggleId} onClick={handlers.onToggleOpenClick}><IoIosArrowForward
                className={toggleState[toggleId] && toggleState[toggleId].className}/></span>
        </div>
        <div className={cn("subcategories-dropdown",
            toggleState[toggleId] && toggleState[toggleId].isOpened ? "active" : "")}>
            {/*Либо вывод ссылки, либо формирование выпадающего списка при помощи рекурсивного вызова функии*/}
            {category.childCategories.map(s => (
                !s.childCategories || s.childCategories.length === 0 ? <div className="subcategory-dropdown-item">
                    <Link to={`products-by-category/${s.id}`}>{s.categoryName}</Link>
                </div> : createSubcategoriesToggles(s, handlers, toggleState)
            ))}
        </div>
    </>;
}