import styles from "./FileSearchPage.module.css";
import { useEffect, useState } from "react";
import { ConstructLink } from "../../api/ConstructLink.js";
import useAuthStore from "../../util/authStore.js";
import { downloadFile } from "../../api/DownloadFile.js";
import { deleteFile } from "../../api/DeleteFile.js";
import { METADATA_URI } from "../../consts/Constants.js";
import { RenameModal } from "../../features/rename-file/RenameModal.jsx";
import { FileEntry } from "../../enitites/file/ui/FileEntry.jsx";
import { ShareModal } from "../../features/share-file/ShareModal.jsx";
import { useSearchModel } from "../../enitites/file/model/index.js";

export const FileSearchPage = () => {
  const [hovered, setHovered] = useState(null);
  const { searchResults } = useSearchModel();
  console.log(searchResults);
  const [selected, setSelected] = useState(new Set());

  const [selectMode, setSelectMode] = useState(false);
  const { token, userInfo } = useAuthStore();

  const [showShareModal, setShowShareModal] = useState(false);
  const [showRenameModal, setShowRenameModal] = useState(false);

  const handleKeyDown = (event) => {
    if (event.ctrlKey) {
      setSelectMode(true);
      console.log(`Ctrl key is down`);
    }
  };

  const handleKeyUp = (event) => {
    if (!event.ctrlKey) {
      setSelectMode(false);
      console.log(`Ctrl key released`);
    }
  };

  window.addEventListener("keydown", handleKeyDown);
  window.addEventListener("keyup", handleKeyUp);

  const handleShare = (file) => {
    ConstructLink(file, "MINUTE", 60, token).then(
      (link) => {
        console.log(`Success: ${link}`);
        setShowShareModal(true);
      },
      (err) => console.error("error", err),
    );
  };

  const handleDownload = (file) => {
    downloadFile(file, token).then(
      () => {
        console.log("File downloaded");
      },
      (err) => console.log("error", err),
    );
  };

  const handleDelete = (file) => {
    deleteFile(file, token).then(
      () => {
        console.log("File deleted");
      },
      (err) => console.log("error", err),
    );
  };

  return (
    <>
      <table className={styles.filesList}>
        <thead>
          <tr className={styles.theader}>
            <th>Name</th>
            <th>Updated at</th>
            <th>Size</th>
            <th>Uploader</th>
            <th>Workspace</th>
          </tr>
        </thead>
        <tbody>
          {searchResults?.elements?.map((file) => (
            <FileEntry
              file={file}
              key={`${file.id}_${file.workspaceID}`}
              isSelected={selected.has(file.id)}
              onShare={() => handleShare(file)}
              onRename={() => setShowRenameModal(true)}
              onDownload={() => handleDownload(file)}
              onDelete={() => handleDelete(file)}
              onClick={() => {
                console.log(`selected ${selectMode}`);
                console.log(`before ${selected.size}`);
                if (!selectMode) {
                  setSelected(new Set([file.id]));
                }
                if (!selected.has(file.id)) {
                  if (selected.size < 1 || selectMode) {
                    setSelected(new Set([...selected, file.id]));
                  }
                }
                console.log(`after ${selected.size}`);
              }}
              onMouseEnter={() => {
                console.log(`hovered ${hovered}`);
                setHovered(file);
              }}
              onMouseLeave={() => {
                console.log(`hovered leave ${hovered}`);
                setHovered(null);
              }}
            />
          ))}
        </tbody>
      </table>

      {showShareModal && (
        <ShareModal
          // TODO: construct a link to a file page but with link query param appended
          link={`${METADATA_URI}/file/${selected.id}?link={shareLink}`}
          onClose={() => setShowShareModal(false)}
        />
      )}
      {showRenameModal && (
        <RenameModal file={hovered} onClose={() => setShowRenameModal(false)} />
      )}
    </>
  );
};
