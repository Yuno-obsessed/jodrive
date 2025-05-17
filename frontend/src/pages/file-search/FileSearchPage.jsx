import styles from "./FileSearchPage.module.css";
import { useCallback, useEffect, useMemo, useState } from "react";
import { ConstructLink } from "../../api/ConstructLink.js";
import useAuthStore from "../../util/authStore.js";
import { downloadFile } from "../../api/DownloadFile.js";
import { deleteFile } from "../../api/DeleteFile.js";
import { METADATA_URI } from "../../consts/Constants.js";
import { RenameModal } from "../../features/rename-file/RenameModal.jsx";
import { FileEntry } from "../../enitites/file/ui/FileEntry.jsx";
import { ShareModal } from "../../features/share-file/ShareModal.jsx";
import { useSearchModel } from "../../enitites/file/model/index.js";
import Table from "../../components/table";
import MajesticonsFolder from "~icons/majesticons/folder?width=24px&height=24px";
import { Button } from "../../components/ui/button/index.jsx";
import { Workspace } from "../../enitites/workspace/ui/Workspace.jsx";
import { Workspaces } from "../../widgets/workspaces/SelectWorkspacePage.jsx";

export const FileSearchPage = () => {
  const { searchResults, removeSearchResult } = useSearchModel();
  const { token } = useAuthStore();

  const [hovered, setHovered] = useState(null);
  const [selected, setSelected] = useState(new Set());
  const [selectMode, setSelectMode] = useState(false);

  const [showShareModal, setShowShareModal] = useState(false);
  const [showRenameModal, setShowRenameModal] = useState(false);

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

  const handleShare = (file) =>
    ConstructLink(file, "MINUTE", 60, token)
      .then(() => setShowShareModal(true))
      .catch(console.error);

  const handleDownload = (file) =>
    downloadFile(file, token).catch(console.error);

  const handleDelete = (file) =>
    deleteFile(file, token)
      .then(() => {
        removeSearchResult(file);
      })
      .catch(console.error);

  const columns = useMemo(
    () => ["Name", "Updated at", "Size", "Uploader", "Workspace"],
    [],
  );

  const renderRow = (file) => (
    <FileEntry
      key={`${file.id}_${file.workspaceID}`}
      file={file}
      isSelected={selected.has(file.id)}
      onShare={() => handleShare(file)}
      onRename={() => setShowRenameModal(true)}
      onDownload={() => handleDownload(file)}
      onDelete={() => handleDelete(file)}
      onClick={() => toggleSelect(file)}
      onMouseEnter={() => setHovered(file)}
      onMouseLeave={() => setHovered(null)}
    />
  );

  return (
    <div className={styles.page}>
      <div className={styles.workSpace}>
        <Workspaces />
      </div>
      <Table
        columns={columns}
        data={searchResults?.elements || []}
        renderRow={renderRow}
        tableClassName={styles.filesList}
        headerRowClassName={styles.theader}
      />

      {showShareModal && (
        <ShareModal
          link={`${METADATA_URI}/file/${[...selected][0]}?link={shareLink}`}
          onClose={() => setShowShareModal(false)}
        />
      )}

      {showRenameModal && (
        <RenameModal file={hovered} onClose={() => setShowRenameModal(false)} />
      )}
    </div>
  );
};
