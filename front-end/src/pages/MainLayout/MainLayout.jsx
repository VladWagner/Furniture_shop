import {useStoreStateSelector} from "../../hooks/useStoreStateSelector";
import {useActions} from "../../hooks/useActions";
import {instance} from "../../infrastrucutre/axiosInterceptor";
import {useContext, useEffect, useState} from "react";
import {getRandom} from "../../infrastrucutre/utils";
import CategoriesTile from "../../components/CategoriesTile/CategoriesTile";
import NavBar from "../../components/Naviagation/Navigation";
import Footer from "../../components/Footer/Footer";
import {Outlet} from "react-router-dom";
import AuthModal from "../../components/AuthModal/AuthModal";
import {ModalContext} from "../../components/ModalConfiguration/ModalConfig";

const React = require('react')

function MainLayout({openLoginModal = false}) {

    return (
        <div className="app-container">
            {openLoginModal ? <AuthModal isOpenedBySystem={true}/> : null}
            <NavBar/>
            <div className="container">
                <Outlet/>
            </div>
            <Footer/>
        </div>)


}

export default MainLayout;