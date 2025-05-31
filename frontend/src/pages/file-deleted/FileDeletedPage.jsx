import { useSearchModel } from "../../enitites/file/model/index.js";
import useAuthStore from "../../util/authStore.js";
import { useCallback, useEffect, useMemo, useState } from "react";
import Table from "../../components/ui/table/index.jsx";
import styles from "./FileDeletedPage.module.css";
import { updateFile } from "../../api/UpdateFile.js";
import { formatByteSize } from "../../util/fileUtils.js";
import { TableRow } from "../../enitites/file/ui/TableRow.jsx";
import { Button } from "../../components/ui/button/index.jsx";
import MaterialSymbolsDeleteForever from "~icons/material-symbols/delete-forever";
import MdiRestore from "~icons/mdi/restore";
import { getFilenameWithIcon } from "../../util/filenameUtils.jsx";

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

  const columnRenderers = {
    name: (file) => getFilenameWithIcon(file.name, 30),
    deletedBy: (file) => file.deletedBy?.username,
    deletedAt: (file) => file.deletedAt,
    size: (file) => formatByteSize(file.size),
    workspace: (file) => file.workspaceID,
    path: (file) => file.path,
  };

  const columns = useMemo(
    () => ["Name", "Deleted By", "Deleted At", "Size", "Workspace", "Path"],
    [],
  );

  const renderRow = (file) => (
    <TableRow
      key={`${file.id}_${file.workspaceID}`}
      entity={file}
      columns={["name", "deletedBy", "deletedAt", "size", "workspace", "path"]}
      columnRenderers={columnRenderers}
      onClick={() => toggleSelect(file)}
      onMouseEnter={() => setHovered(file)}
      isSelected={selected.has(file.id)}
      buttons={
        <>
          <Button variant="icon" onClick={() => handleDelete(file)}>
            <MaterialSymbolsDeleteForever className={styles.icons} />
          </Button>
          <Button variant="icon" onClick={() => handleRestore(file)}>
            <MdiRestore className={styles.icons} />
          </Button>
        </>
      }
    />
  );

  return (
    <>
      <div className={styles.deleteNotice}>
        Items in trash will be deleted after 30 days
      </div>
      <Table
        columns={columns}
        data={searchResults?.elements || []}
        renderRow={renderRow}
        tableClassName={styles.table}
      />
    </>
  );
};
