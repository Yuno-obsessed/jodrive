import React from "react";
import { FileActionButton } from "./FileActionButton.jsx";
import { formatByteSize } from "../../../util/fileUtils.js";
import { FileRow } from "./FileRow.jsx";
import { Filename } from "./Filename.jsx";

export const DeletedFile = ({
  file,
  onClick,
  onDelete,
  onRestore,
  onMouseEnter,
  onMouseLeave,
  isSelected = false,
}) => {
  const columnRenderers = {
    name: (file) => <Filename filename={file.name} />,
    deletedBy: (file) => file.deletedBy?.username,
    deletedAt: (file) => file.deletedAt,
    size: (file) => formatByteSize(file.size),
    workspace: (file) => file.workspaceID,
    path: (file) => file.path,
  };

  return (
    <FileRow
      file={file}
      columns={["name", "deletedBy", "deletedAt", "size", "workspace", "path"]}
      columnRenderers={columnRenderers}
      onClick={onClick}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      isSelected={isSelected}
      buttons={
        <>
          <FileActionButton
            src="delete-forever.svg"
            alt="DeleteForever"
            callback={onDelete}
          />
          <FileActionButton
            src="restore.svg"
            alt="Restore"
            callback={onRestore}
          />
        </>
      }
    />
  );
};
