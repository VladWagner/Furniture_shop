import {useStoreStateSelector} from "../../hooks/useStoreStateSelector";
import './LoadingScreenPage.css'
import {useEffect, useState} from "react";
import svg from '../../assets/img/M.logo_wb.svg'

const React = require('react')

function LoadingScreenPage() {


    useEffect(() => {
        /*let svg = document.getElementById("svg_animation");

        if (svg) {
            let allPaths = svg.querySelectorAll("path");
            console.log(`Found paths:`)
            console.dir(allPaths)

            if (allPaths && allPaths.length) {
                for (let i = 0; i < allPaths.length; i++) {
                    let pathLength = allPaths[i].getTotalLength();

                    console.log(`Path idx: ${i + 1}. Path length: ${pathLength}`)
                }
            }
        }*/



    }, [])

    return (
        <div className={"loading-svg-container"}>
            {/*<img className={"loading-svg"} id="svg_animation" src={svg}/>*/}
            <svg className={"loading-svg"} id="svg_animation" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2000 2000">
                <path d="M1662.5,400v812.9c-4.1-0.3-8.3-0.4-12.5-0.4c-117.4,0-212.5,95.1-212.5,212.5c0,72.6,36.4,136.7,91.9,175h-216.9v-562.5
      l-300,287.5l-300-300v575h-350V400h350l300,437.5l300-437.5H1662.5z"/>
                <circle cx="1650" cy="1425" r="155" fill="black" stroke="url(#gradientFade)" strokeWidth="4">
                </circle>

            </svg>

        </div>
    );


}
//<animate attributeName="r" begin="0s" dur="1s" repeatCount="indefinite" from="155" to="170"/>
/*<path stroke="black"   d="M1800,1425c0,79.2-61.4,144.1-139.3,149.6c-3.5,0.3-7.1,0.4-10.7,0.4c-82.8,0-150-67.2-150-150s67.2-150,150-150
      c3.6,0,7.2,0.1,10.7,0.4C1738.6,1280.9,1800,1345.8,1800,1425z"/>*/
export default LoadingScreenPage;