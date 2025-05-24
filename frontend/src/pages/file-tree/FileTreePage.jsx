import useAuthStore from "../../util/authStore.js";
import { useTreeModel } from "../../enitites/file-tree/model/index.js";
import { useNavigate, useParams } from "react-router-dom";
import { useMemo } from "react";
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

export const FileTreePage = () => {
  const { id } = useParams();
  const { token } = useAuthStore();
  const { files, setFiles, removeFile } = useTreeModel();
  const navigate = useNavigate();
  useSyncFilesystemPath(); // sync filesystem vars
  const { currentPath, basePath } = useFilesystemStore();

  console.log(currentPath, basePath);
  const folderSegments = currentPath.split("/").filter(Boolean);
  console.log(currentPath);
  console.log(folderSegments);

  useFileTree(id, currentPath, token, setFiles);

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
        console.log("share");
        break;
      case "download":
        console.log("share");
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
          <CreateDirectoryModalButton path={currentPath} wsID={id} />
          <FileTreeTable
            table={table}
            dataIds={dataIds}
            actions={<FileTreeMenuActions handleEvents={handleEvents} />}
          />
        </div>
      </DndContext>
    </div>
  );
};
