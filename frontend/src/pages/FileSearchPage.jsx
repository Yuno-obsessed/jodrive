import styles from "./FileSearchPage.module.css";
import React, { useEffect, useState } from "react";
import { FileEntry } from "../components/FileEntry.jsx";
import { ShareModal } from "../features/ShareModal.jsx";
import { ConstructLink } from "../api/ConstructLink.js";
import useAuthStore from "../util/authStore.js";
import { downloadFile } from "../api/DownloadFile.js";

export const FileSearchPage = (searchParams) => {
  const getFiles = (params) => {
    return [
      {
        id: 1,
        name: "testfile.png",
        uploadedAt: "05/05/2025",
        size: 200,
        uploader: "Some UUID",
        workspaceID: 1,
      },
      {
        id: 2,
        name: "testfile2.png",
        uploadedAt: "05/06/2025",
        size: 400,
        uploader: "Some UUID@12123",
        workspaceID: 1,
      },
    ];
  };

  const [hovered, setHovered] = useState(null);
  const [selected, setSelected] = useState(new Set());
  const [selectMode, setSelectMode] = useState(false);
  const [sharedFile, setSharedFile] = useState(null);
  const [shareLink, setShareLink] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const { token } = useAuthStore();

  const handleShare = (file) => {
    ConstructLink(file, "MINUTE", 60, token).then(
      (link) => {
        console.log(`Success: ${link}`);
        setSharedFile(file);
        setShareLink(link);
        setShowModal(true);
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

  useEffect(() => {
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
  }, []);

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
          {getFiles(searchParams).map((file) => (
            <FileEntry
              file={file}
              key={file.id}
              isSelected={selected.has(file.id)}
              onShare={() => handleShare(file)}
              onDownload={() => handleDownload(file)}
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
              onMouseEnter={(e) => {
                setHovered(file);
              }}
              onMouseLeave={(e) => {
                setHovered(null);
              }}
            />
          ))}
        </tbody>
      </table>

      {showModal && (
        <ShareModal link={shareLink} onClose={() => setShowModal(false)} />
      )}
    </>
  );
};
