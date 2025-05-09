import React from "react";
import styles from "./FileEntry.module.css";

export const FileEntry = ({
  file,
  onClick,
  onShare,
  onDownload,
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

  return (
    <tr
      className={styles.fileEntry}
      onClick={onClick}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
    >
      <td>{file.name}</td>
      <td>{file.uploadedAt}</td>
      <td>{file.size}</td>
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
          <FileEntryButton src="delete.svg" alt="Delete" callback={onShare} />
        </div>
      </td>
    </tr>
  );
};
