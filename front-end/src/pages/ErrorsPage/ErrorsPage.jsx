import {useStoreStateSelector} from "../../hooks/useStoreStateSelector";
import './ErrorsPage.css'
import {useEffect, useState} from "react";

const React = require('react')

function ErrorsPage({errorsList, singeError = null}) {

    const userState = useStoreStateSelector(state => state.user);
    const basketState = useStoreStateSelector(state => state.cart);

    const [errors, setErrors] = useState([
        /*useStoreStateSelector(state => state.user.error),
        useStoreStateSelector(state => state.cart.error),*/
        userState.error,
        basketState.error,
        ...errorsList,
        singeError
    ]);

    useEffect(() => {

        /*if (errorsList && errorsList.length > 0)
            errors = [...errors, ...errorsList];

        if (singeError)
            errors.push(singeError);*/

        setErrors([
            userState.error,
            basketState.error,
            ...errorsList,
            singeError
        ])

        console.log(`Errors: `);
        console.dir(errors);

    },[userState.error, basketState.error])

    return (
        <div className={"errors-container"}>

            {
                errors && errors.map(err => {
                    return err && err.length > 0 ? <div key={err} className="error-message-item error-a">{err}</div> : "";
                })
            }
        </div>
    );


}

export default ErrorsPage;