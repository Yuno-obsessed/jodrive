import styles from "./SearchBar.module.css";
import { Input } from "../../components/ui/input/index.jsx";
import { useState } from "react";
import { Button } from "../../components/ui/button/index.jsx";

export const SearchBar = () => {
  const [searchText, setSearchText] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
  };

  return (
    <form onSubmit={handleSubmit} className={styles.searchBar}>
      <Input
        className={styles.input}
        type="text"
        action={
          <Button type="submit">
            <img src="./search.svg" alt="Search" style={{ width: "1.2rem" }} />
          </Button>
        }
        placeholder="Search in Jodrive"
        value={searchText}
        onChange={(e) => setSearchText(e.target.value)}
        onKeyDown={(e) => e.key === "Enter" && handleSubmit(e)}
      />
    </form>
  );
};
