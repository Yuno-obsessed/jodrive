import React, {use, useState} from 'react';
import './SearchBar.css';

export const SearchBar = () => {
    const [searchText, setText] = useState('');

    const clear = () => {
        setText('')
    }

    // TODO: both this and pressing enter should perform a search
    const search = () => {
        alert("Search triggered")
    }

    return (
        <div className="search-bar">
            <button className="btn-search" onClick={search}>
                <img src="./search.svg" alt="Search" className="icon-search"/>
            </button>
            <input type="text" placeholder="Seach in Jodrive" className="search-input"
                   value={searchText}
                   onChange={(e) => setText(e.target.value)}
            />
            <button className="btn-clear" onClick={clear}>
                <img src="./close.svg" alt="Clear" className="icon-close"/>
            </button>
        </div>
    );
};
