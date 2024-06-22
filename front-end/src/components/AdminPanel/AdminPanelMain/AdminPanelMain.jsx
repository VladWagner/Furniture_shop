import './AdminPanelMain.css'
import {getCorrectStaticFilePath, getRandom, getRightNumDeclension} from "../../../infrastrucutre/utils";
import cn from "classnames";
import {useStoreStateSelector} from "../../../hooks/useStoreStateSelector";
import {useActions} from "../../../hooks/useActions";
import {useEffect} from "react";
import {Outlet} from "react-router-dom";

const React = require('react')

function AdminPanelMain() {


    useEffect(() =>{
    }, [])


    return <div>
        <h2>Admin panel in development!</h2>
        <Outlet/>
    </div>
}

export default AdminPanelMain