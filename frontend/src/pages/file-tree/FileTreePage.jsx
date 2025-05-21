import useAuthStore from "../../util/authStore.js";
import { useTreeModel } from "../../enitites/file-tree/model/index.js";
import { useLocation, useNavigate, useParams } from "react-router-dom";
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

export const FileTreePage = () => {
  const { id } = useParams();
  const { token } = useAuthStore();
  const { files, setFiles } = useTreeModel();
  const navigate = useNavigate();

  const location = useLocation();

  // Get the dynamic subpath (e.g. "/home/user/photos")
  const basePath = `/workspace/${id}`;
  const subPath = location.pathname.replace(basePath, "") || "/";
  const folderSegments = subPath.split("/").filter(Boolean);
  console.log(subPath);
  console.log(folderSegments);

  useFileTree(id, subPath, token, setFiles);

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
          console.log("opening " + basePath + subPath + eventRow.name);
          navigate(basePath + subPath + eventRow.name);
          // useFileTree(id, subPath + eventRow.name);
          break;
        }
      // useFileTree(id, "/" + data);
      case "share":
        console.log("share");
        break;
      case "download":
        console.log("share");
        break;
      case "delete":
        console.log("share");
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
          <CreateDirectoryModalButton path={subPath} wsID={id} />
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
