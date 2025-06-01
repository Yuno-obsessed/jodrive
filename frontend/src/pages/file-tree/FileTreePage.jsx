import useAuthStore from "../../util/authStore.js";
import { useTreeModel } from "../../enitites/file-tree/model/index.js";
import { useNavigate, useParams } from "react-router-dom";
import { useMemo, useRef, useState } from "react";
import { getCoreRowModel, useReactTable } from "@tanstack/react-table";

import {
  closestCenter,
  DndContext,
  KeyboardSensor,
  MouseSensor,
  TouchSensor,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import { restrictToVerticalAxis } from "@dnd-kit/modifiers";
import { useFileTree } from "./model/index.js";
import { fileTreeColumns } from "./config/index.js";
import { FileTreeTable } from "../../components/ui/table-v2/index.jsx";
import { Breadcrumb } from "../../components/ui/breadcrumb/index.jsx";
import { FileTreeMenuActions } from "./context/index.jsx";
import { useContextMenuStore } from "../../components/ui/table-v2/config/store/index.js";
import styles from "./FileTreePage.module.css";
import { CreateDirectoryModalButton } from "../../features/create-dir/index.jsx";
import { useSyncFilesystemPath } from "../../shared/fs-dir/hook.js";
import { useFilesystemStore } from "../../shared/fs-dir/index.js";
import { deleteFile } from "../../api/DeleteFile.js";
import { Button } from "../../components/ui/button/index.jsx";
import { useWorkspacesModel } from "../../enitites/workspace/model/index.js";
import { RenameModal } from "../../features/rename-file/RenameModal.jsx";
import { ShareModal } from "../../features/share-file/ShareModal.jsx";
import { updateFile } from "../../api/UpdateFile.js";
import { FileDownloader } from "../../util/FileDownloader.jsx";
import { AddWorkspaceUserModalButton } from "../../features/add-user/index.jsx";

export const FileTreePage = () => {
  const { id } = useParams();
  const { token } = useAuthStore();
  const { files, setFiles, removeFile, renameFile } = useTreeModel();
  const { setActive, userWorkspaces } = useWorkspacesModel();
  const navigate = useNavigate();
  useSyncFilesystemPath(); // sync filesystem vars
  const { currentPath, basePath } = useFilesystemStore();
  const [fileToRename, setFileToRename] = useState(null);
  const [fileToShare, setFileToShare] = useState(null);
  const downloaderRef = useRef();

  console.log(currentPath, basePath);

  useFileTree(id, currentPath, token, setFiles);
  setActive(userWorkspaces.filter((e) => e.id == id)[0]);

  const table = useReactTable({
    data: files,
    columns: fileTreeColumns,
    getCoreRowModel: getCoreRowModel(),
    getRowId: (row) => row.id,
  });

  const dataIds = useMemo(() => files.map((f) => f.id), [files]);

  const sensors = useSensors(
    useSensor(MouseSensor),
    useSensor(TouchSensor),
    useSensor(KeyboardSensor),
  );

  const row = useContextMenuStore();
  const handleEvents = ({ id }) => {
    let eventRow = row.row;
    switch (id) {
      case "open":
        console.log("open");
        if (eventRow.isDirectory) {
          console.log("opening " + basePath + currentPath + eventRow.name);
          navigate(basePath + currentPath + eventRow.name);
        }
        break;
      case "share":
        setFileToShare(eventRow);
        console.log("share");
        break;
      case "download":
        downloaderRef.current.download(eventRow);
        console.log("download");
        break;
      case "delete":
        console.log("Deleting file");
        deleteFile(
          { id: eventRow.id, workspaceID: eventRow.workspaceID },
          token,
        )
          .then(() => removeFile(eventRow.id))
          .catch(console.log);
        break;
      case "rename":
        setFileToRename(eventRow);
        console.log("rename");
        break;
    }
  };

  const findByID = (rowID) => {
    console.log(files);
    return files.filter((f) => f.id === rowID)[0];
  };

  const moveFileInDirectory = (event) => {
    const { active, over } = event;
    const activeFile = findByID(active.id);
    const overFile = findByID(over.id);
    console.log(activeFile, overFile);
    if (activeFile && overFile && active.id !== over.id) {
      if (!activeFile.isDirectory && overFile.isDirectory) {
        console.log(
          `Putting file ${activeFile.id} to directory ${overFile.id}`,
        );
        let newName = overFile.name + activeFile.name;
        updateFile(activeFile, newName, null, token)
          .then(() => removeFile(activeFile.id))
          .catch(console.log);
      }
    }
  };

  return (
    <div>
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        modifiers={[restrictToVerticalAxis]}
        onDragEnd={(e) => {
          moveFileInDirectory(e);
        }}
      >
        <div className={styles.wrapper}>
          <Breadcrumb />
          <div className={styles.workspaceActions}>
            <Button
              variant="default"
              className={styles.secondaryActions}
              onClick={() => navigate(`${basePath}/users`)}
            >
              List users
            </Button>
            <AddWorkspaceUserModalButton
              className={styles.secondaryActions}
              wsID={id}
            />
            <CreateDirectoryModalButton path={currentPath} wsID={id} />
          </div>
          <FileTreeTable
            table={table}
            dataIds={dataIds}
            actions={<FileTreeMenuActions handleEvents={handleEvents} />}
          />
        </div>
      </DndContext>

      <FileDownloader ref={downloaderRef} token={token} />
      {fileToRename && (
        <RenameModal
          file={fileToRename}
          renameFile={renameFile}
          onClose={() => setFileToRename(null)}
        />
      )}
      {fileToShare && (
        <ShareModal file={fileToShare} onClose={() => setFileToShare(null)} />
      )}
    </div>
  );
};
