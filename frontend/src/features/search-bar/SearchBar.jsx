import { useCallback, useEffect, useState } from "react";
import styles from "./SearchBar.module.css";
import useAuthStore from "../../util/authStore.js";
import { useDebouncedCallback } from "use-debounce";
import { Input } from "../../components/ui/input/index.jsx";
import { Button } from "../../components/ui/button/index.jsx";
import { useSearchModel } from "../../enitites/file/model/index.js";
import { searchFile } from "../../api/SearchFile.js";
import { useLocation } from "react-router-dom";
import { UploadModalButton } from "../upload-file/index.jsx";
import TablerSearch from "~icons/tabler/search";
import { useWorkspacesModel } from "../../enitites/workspace/model/index.js";
import { getWorkspaces } from "../../api/WorkspaceAPI.js";

export const SearchBar = () => {
  const { token, userInfo } = useAuthStore();
  const { setSearch } = useSearchModel();
  const [searchText, setSearchText] = useState("");
  const { activeWorkspace, setWorkspaces, setActive } = useWorkspacesModel();
  const location = useLocation();

  const doSearch = useCallback(
    async (query) => {
      let deleted = false;
      if (location.pathname === "/deleted") {
        deleted = true;
      }
      const files = await searchFile(
        {
          name: query,
          wsID: activeWorkspace.id,
          userID: userInfo.id,
          deleted: deleted,
        },
        token,
      );
      setSearch(files);
    },
    [token, activeWorkspace, userInfo.id],
  );

  const debouncedSearch = useDebouncedCallback(doSearch, 500);

  const handleChange = (e) => {
    const value = e.target.value;
    setSearchText(value);
    debouncedSearch(value);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    debouncedSearch.flush();
  };

  useEffect(() => {
    if (!activeWorkspace) {
      getWorkspaces(token)
        .then((res) => {
          setWorkspaces(res);
          setActive(res[0]);
        })
        .catch(console.log);
    }
    doSearch("");
  }, [doSearch]);

  return (
    <form onSubmit={handleSubmit} className={styles.searchBar}>
      <Input
        className={styles.input}
        placeholder="Search in Jodrive"
        value={searchText}
        onChange={handleChange}
        action={
          <Button type="submit">
            <TablerSearch />
          </Button>
        }
      />
      <UploadModalButton />
    </form>
  );
};
