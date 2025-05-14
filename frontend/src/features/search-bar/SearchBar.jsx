import styles from "./SearchBar.module.css";
import { Input } from "../../components/ui/input/index.jsx";
import { useState } from "react";
import { Button } from "../../components/ui/button/index.jsx";
import { useNavigate } from "react-router-dom";

export const SearchBar = () => {
  const [searchText, setSearchText] = useState("");
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    navigate(`/?name=${searchText}}`);
  };

  return (
    <form onSubmit={handleSubmit} className={styles.searchBar}>
      <Input
        className={styles.input}
        type="text"
        action={
          <Button type="submit" onClick={handleSubmit}>
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
