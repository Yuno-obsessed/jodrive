import React from "react";
import { formatByteSize } from "../../../util/fileUtils.js";
import { FileRow } from "./FileRow.jsx";
import { FileActionButton } from "./FileActionButton.jsx";

export const FileEntry = ({
  file,
  onClick,
  onShare,
  onRename,
  onDownload,
  onDelete,
  onMouseEnter,
  onMouseLeave,
  isSelected = false,
}) => {
  const columnRenderers = {
    name: (file) => file.name,
    uploadedAt: (file) => file.uploadedAt,
    size: (file) => formatByteSize(file.size),
    uploader: (file) => file.uploader,
    workspaceID: (file) => file.workspaceID,
  };

  return (
    <FileRow
      file={file}
      columns={["name", "uploadedAt", "size", "uploader", "workspaceID"]}
      columnRenderers={columnRenderers}
      onClick={onClick}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      isSelected={isSelected}
      buttons={
        <>
          <FileActionButton src="share.svg" alt="Share" callback={onShare} />
          <FileActionButton src="edit.svg" alt="Rename" callback={onRename} />
          <FileActionButton
            src="download.svg"
            alt="Download"
            callback={onDownload}
          />
          <FileActionButton src="delete.svg" alt="Delete" callback={onDelete} />
        </>
      }
    />
  );
};
