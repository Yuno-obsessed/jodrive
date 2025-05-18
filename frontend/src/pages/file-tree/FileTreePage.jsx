import useAuthStore from "../../util/authStore.js";
import { getFileTree } from "../../api/GetFileTree.js";
import { useTreeModel } from "../../enitites/file-tree/model/index.js";
import { useParams } from "react-router-dom";
import styles from "./FileTreePage.module.css";
import { Workspaces } from "../../widgets/workspaces/Workspaces.jsx";
import { listDirectory } from "../../api/DirectoryAPI.js";
import React, { useEffect, useMemo } from "react";
import {
  flexRender,
  getCoreRowModel,
  useReactTable,
} from "@tanstack/react-table";
import {
  arrayMove,
  SortableContext,
  useSortable,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import {
  closestCenter,
  DndContext,
  KeyboardSensor,
  MouseSensor,
  TouchSensor,
  useDraggable,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import { restrictToVerticalAxis } from "@dnd-kit/modifiers";
import { CSS } from "@dnd-kit/utilities";

export const FileTreePage = () => {
  const { id } = useParams();
  const { token } = useAuthStore();
  const { files, setFiles, removeFile } = useTreeModel();

  console.log(files);

  const handleGetFiles = () => {
    listDirectory({ workspaceID: id, directory: "/" }, token)
      .then((res) => {
        console.log(res);
        setFiles(res.elements);
      })
      .catch(console.error);
  };

  useEffect(() => {
    handleGetFiles();
  }, [id]);

  console.log(files);

  const DraggableRow = ({ row }) => {
    const {
      transform,
      transition,
      setNodeRef,
      isDragging,
      listeners,
      attributes,
    } = useSortable({
      id: row.id,
    });

    const style = {
      transform: CSS.Transform.toString(transform), //let dnd-kit do its thing
      transition: transition,
      opacity: isDragging ? 0.8 : 1,
      zIndex: isDragging ? 1 : 0,
      position: "relative",
    };
    return (
      <tr {...attributes} {...listeners} ref={setNodeRef} style={style}>
        {row.getVisibleCells().map((cell) => (
          <td key={cell.id} style={{ width: cell.column.getSize() }}>
            {flexRender(cell.column.columnDef.cell, cell.getContext())}
          </td>
        ))}
      </tr>
    );
  };

  const columns = useMemo(
    () => [
      {
        accessorKey: "id",
        cell: (info) => info.getValue(),
      },
      {
        accessorKey: "workspaceID",
        header: "Workspace",
      },
      {
        accessorKey: "isDirectory",
        header: "Type",
      },
      {
        accessorKey: "uploaderName",
        header: "Uploader",
      },
    ],
    [],
  );

  const dataIds = useMemo(() => {
    if (!Array.isArray(files)) return [];
    return files.map(({ id }) => id);
  }, [files]);
  console.log(dataIds, files);
  const table = useReactTable({
    data: files,
    columns: columns,
    getCoreRowModel: getCoreRowModel(),
    getRowId: (row) => {
      return row.id;
    },
  });

  function handleDragEnd(event) {
    const { active, over } = event;
    // TODO: ezz;
    console.log(active, over);
    removeFile(active.id);
  }

  const sensors = useSensors(
    useSensor(MouseSensor, {}),
    useSensor(TouchSensor, {}),
    useSensor(KeyboardSensor, {}),
  );

  return (
    <DndContext
      collisionDetection={closestCenter}
      modifiers={[restrictToVerticalAxis]}
      onDragEnd={(e) => handleDragEnd(e)}
      onDragStart={(e) => {
        console.log(e);
      }}
      sensors={sensors}
    >
      <div style={{ padding: "2px" }}>
        <div style={{ height: "4px" }} />
        <div style={{ height: "4px" }} />
        <table>
          <thead>
            {table.getHeaderGroups().map((headerGroup) => (
              <tr key={headerGroup.id}>
                {headerGroup.headers.map((header) => (
                  <th key={header.id} colSpan={header.colSpan}>
                    {header.isPlaceholder
                      ? null
                      : flexRender(
                          header.column.columnDef.header,
                          header.getContext(),
                        )}
                  </th>
                ))}
              </tr>
            ))}
          </thead>
          <tbody>
            <SortableContext
              items={dataIds}
              strategy={verticalListSortingStrategy}
            >
              {table.getRowModel().rows.map((row) => (
                <DraggableRow key={row.id} row={row} />
              ))}
            </SortableContext>
          </tbody>
        </table>
      </div>
    </DndContext>
  );
};
