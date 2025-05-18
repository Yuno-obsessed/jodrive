import styles from "./FileSearchPage.module.css";
import React, { useCallback, useEffect, useMemo, useState } from "react";
import { ConstructLink } from "../../api/ConstructLink.js";
import useAuthStore from "../../util/authStore.js";
import { downloadFile } from "../../api/DownloadFile.js";
import { deleteFile } from "../../api/DeleteFile.js";
import { METADATA_URI } from "../../consts/Constants.js";
import { RenameModal } from "../../features/rename-file/RenameModal.jsx";
import { ShareModal } from "../../features/share-file/ShareModal.jsx";
import { useSearchModel } from "../../enitites/file/model/index.js";
import Table from "../../components/table";
import { Workspace } from "../../enitites/workspace/ui/Workspace.jsx";
import { Workspaces } from "../../widgets/workspaces/SelectWorkspacePage.jsx";
import { formatByteSize } from "../../util/fileUtils.js";
import { FileRow } from "../../enitites/file/ui/FileRow.jsx";
import TablerShare from "~icons/tabler/share";
import LucideEdit3 from "~icons/lucide/edit-3";
import TablerDownload from "~icons/tabler/download";
import MingcuteDelete2Line from "~icons/mingcute/delete-2-line";
import { Button } from "../../components/ui/button/index.jsx";
import { getFilenameWithIcon } from "../../util/filenameUtils.jsx";

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

  const columnRenderers = {
    name: (file) => getFilenameWithIcon(file.name),
    uploadedAt: (file) => file.uploadedAt,
    size: (file) => formatByteSize(file.size),
    uploader: (file) => file.uploaderName,
    workspaceID: (file) => file.workspaceID,
  };

  const renderRow = (file) => (
    <FileRow
      key={`${file.id}_${file.workspaceID}`}
      file={file}
      columns={["name", "uploadedAt", "size", "uploader", "workspaceID"]}
      columnRenderers={columnRenderers}
      onClick={() => toggleSelect(file)}
      onMouseEnter={() => setHovered(file)}
      isSelected={selected.has(file.id)}
      buttons={
        <>
          <Button variant="icon" callback={() => handleShare(file)}>
            <TablerShare className={styles.icons} />
          </Button>
          <Button variant="icon" callback={() => setShowRenameModal(true)}>
            <LucideEdit3 className={styles.icons} />
          </Button>
          <Button variant="icon" callback={() => handleDownload(file)}>
            <TablerDownload className={styles.icons} />
          </Button>
          <Button variant="icon" callback={() => handleDelete(file)}>
            <MingcuteDelete2Line className={styles.icons} />
          </Button>
        </>
      }
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
