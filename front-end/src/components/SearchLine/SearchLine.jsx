import './SearchLine.css'

const React = require('react')

function SearchLine({onSubmitHandler, onInputClick}) {
    return <form className="search" >

        <div className="search-form-row">
            <input name="q" placeholder="Поиск..." type="search" onClick={onInputClick}/>
            <button className="search-button">
                S
            </button>
        </div>
    </form>
}

export default SearchLine