import React from "react";
import {SearchBar} from "../components/SearchBar.jsx";
import styles from './Header.module.css'
import Profile from "../components/Profile.jsx";

export const Header = () => {
    return (
        <header className={styles.headerMain}>
            <div className={styles.logo}>
                <img src="jodrive-logo.png" alt="logo"/>
            </div>
            <SearchBar/>
            <Profile/>
        </header>
    );
}