import React from "react";
import { SearchBar } from "../../features/search-bar/SearchBar.jsx";
import styles from "./Header.module.css";

export const Header = () => {
  return (
    <header className={styles.headerMain}>
      <div className={styles.logo}>
        <img src="jodrive-logo.png" alt="logo" />
      </div>
      <SearchBar />
    </header>
  );
};
