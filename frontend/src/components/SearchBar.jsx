import React, { useState } from "react";
import styles from "./SearchBar.module.css";

export const SearchBar = () => {
  const [searchText, setText] = useState("");

  const clear = () => {
    setText("");
  };

  // TODO: both this and pressing enter should perform a search
  const search = () => {
    alert("Search triggered");
  };

  return (
    <div className={styles.searchBar}>
      <button className={styles.btnSearch} onClick={search}>
        <img src="./search.svg" alt="Search" className={styles.iconSearch} />
      </button>
      <input
        type="text"
        placeholder="Seach in Jodrive"
        className={styles.searchInput}
        value={searchText}
        onChange={(e) => setText(e.target.value)}
      />
      <button className={styles.btnClear} onClick={clear}>
        <img src="./close.svg" alt="Clear" className={styles.iconClose} />
      </button>
    </div>
  );
};
