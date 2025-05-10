import React from "react";
import styles from "./FileEntry.module.css";

export const FileEntry = ({
  file,
  onClick,
  onShare,
  onDownload,
  onDelete,
  onMouseEnter,
  onMouseLeave,
}) => {
  const FileEntryButton = ({ src, alt, callback }) => {
    return (
      <button
        onClick={(e) => {
          e.stopPropagation();
          callback();
        }}
      >
        <img src={src} alt={alt} />
      </button>
    );
  };

  const formatByteSize = (size) => {
    if (size >= 1_000_000_000) {
      return (size / 1_000_000_000).toFixed(2) + " GB";
    } else if (size >= 1_000_000) {
      return (size / 1_000_000).toFixed(2) + " MB";
    } else if (size >= 1_000) {
      return (size / 1_000).toFixed(2) + " KB";
    } else {
      return size + " B";
    }
  };

  return (
    <tr
      className={styles.fileEntry}
      onClick={onClick}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
    >
      <td>{file.filename}</td>
      <td>{file.uploadedAt}</td>
      <td>{formatByteSize(file.size)}</td>
      <td>{file.uploader}</td>
      <td>{file.workspaceID}</td>
      <td>
        <div className={styles.fileIcons}>
          <FileEntryButton src="share.svg" alt="Share" callback={onShare} />
          <FileEntryButton src="edit.svg" alt="Edit" callback={onShare} />
          <FileEntryButton
            src="download.svg"
            alt="Download"
            callback={onDownload}
          />
          <FileEntryButton src="delete.svg" alt="Delete" callback={onDelete} />
        </div>
      </td>
    </tr>
  );
};
