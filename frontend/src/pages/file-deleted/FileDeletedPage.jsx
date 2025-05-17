import { useSearchModel } from "../../enitites/file/model/index.js";
import useAuthStore from "../../util/authStore.js";
import React, { useCallback, useEffect, useMemo, useState } from "react";
import { deleteFile } from "../../api/DeleteFile.js";
import Table from "../../components/table/index.jsx";
import styles from "../file-search/FileSearchPage.module.css";
import { DeletedFile } from "../../enitites/file/ui/DeletedFile.jsx";
import { updateFile } from "../../api/UpdateFile.js";

export const FileDeletedPage = () => {
  const { searchResults, removeSearchResult } = useSearchModel();
  const { token } = useAuthStore();

  const [hovered, setHovered] = useState(null);
  const [selected, setSelected] = useState(new Set());
  const [selectMode, setSelectMode] = useState(false);

  useEffect(() => {
    const handleKeyDown = (e) => e.ctrlKey && setSelectMode(true);
    const handleKeyUp = (e) => !e.ctrlKey && setSelectMode(false);

    window.addEventListener("keydown", handleKeyDown);
    window.addEventListener("keyup", handleKeyUp);
    return () => {
      window.removeEventListener("keydown", handleKeyDown);
      window.removeEventListener("keyup", handleKeyUp);
    };
  }, []);

  const toggleSelect = useCallback(
    (file) => {
      setSelected((prev) => {
        if (!selectMode) return new Set([file.id]);
        const next = new Set(prev);
        next.has(file.id) ? next.delete(file.id) : next.add(file.id);
        return next;
      });
    },
    [selectMode],
  );

  const handleDelete = (file) =>
    updateFile(file, null, "DELETE_FOREVER", token)
      .then(() => {
        removeSearchResult(file);
      })
      .catch(console.error);

  const handleRestore = (file) =>
    updateFile(file, null, "RESTORE", token)
      .then(() => {
        removeSearchResult(file);
      })
      .catch(console.error);

  const columns = useMemo(
    () => ["Name", "Deleted By", "Deleted At", "Size", "Workspace", "Path"],
    [],
  );

  const renderRow = (file) => (
    <DeletedFile
      key={`${file.id}_${file.workspaceID}`}
      file={file}
      isSelected={selected.has(file.id)}
      onDelete={() => handleDelete(file)}
      onRestore={() => handleRestore(file)}
      onClick={() => toggleSelect(file)}
      onMouseEnter={() => setHovered(file)}
      onMouseLeave={() => setHovered(null)}
    />
  );

  return (
    <>
      <Table
        columns={columns}
        data={searchResults?.elements || []}
        renderRow={renderRow}
        tableClassName={styles.filesList}
        headerRowClassName={styles.theader}
      />
    </>
  );
};
