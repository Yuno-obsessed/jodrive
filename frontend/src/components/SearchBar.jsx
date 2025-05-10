import React, { useState } from "react";
import styles from "./SearchBar.module.css";
import { useNavigate } from "react-router-dom";

export const SearchBar = () => {
  const [searchText, setText] = useState("");
  const navigate = useNavigate();

  const clear = () => {
    setText("");
  };

  const search = () => {
    navigate(`/files?name=${searchText}`);
  };

  const searchOnKey = (e) => {
    if (e.key === "Enter") {
      navigate(`/files?name=${searchText}`);
    }
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
        onKeyDown={searchOnKey}
      />
      <button className={styles.btnClear} onClick={clear}>
        <img src="./close.svg" alt="Clear" className={styles.iconClose} />
      </button>
    </div>
  );
};
