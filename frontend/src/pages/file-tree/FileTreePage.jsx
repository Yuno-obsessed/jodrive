import useAuthStore from "../../util/authStore.js";
import { useTreeModel } from "../../enitites/file-tree/model/index.js";
import { useNavigate, useParams } from "react-router-dom";
import { useMemo, useState } from "react";
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
import { downloadFile } from "../../api/DownloadFile.js";
import { RenameModal } from "../../features/rename-file/RenameModal.jsx";
import { ShareModal } from "../../features/share-file/ShareModal.jsx";

export const FileTreePage = () => {
  const { id } = useParams();
  const { token } = useAuthStore();
  const { files, setFiles, removeFile } = useTreeModel();
  const { setActive, userWorkspaces } = useWorkspacesModel();
  const navigate = useNavigate();
  useSyncFilesystemPath(); // sync filesystem vars
  const { currentPath, basePath } = useFilesystemStore();
  const [fileToRename, setFileToRename] = useState(null);
  const [fileToShare, setFileToShare] = useState(null);

  console.log(currentPath, basePath);
  // const folderSegments = currentPath.split("/").filter(Boolean);

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
        downloadFile(eventRow, token).then().catch(console.log);
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
            <Button variant="default" className={styles.secondaryActions}>
              Add user
            </Button>
            <CreateDirectoryModalButton path={currentPath} wsID={id} />
          </div>
          <FileTreeTable
            table={table}
            dataIds={dataIds}
            actions={<FileTreeMenuActions handleEvents={handleEvents} />}
          />
        </div>
      </DndContext>

      {fileToRename && (
        <RenameModal
          file={fileToRename}
          onClose={() => setFileToRename(null)}
        />
      )}
      {fileToShare && (
        <ShareModal file={fileToShare} onClose={() => setFileToShare(null)} />
      )}
    </div>
  );
};
