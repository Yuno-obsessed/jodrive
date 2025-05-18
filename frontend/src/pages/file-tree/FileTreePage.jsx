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
  const { files, setFiles } = useTreeModel();

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

  // handleGetFiles();
  console.log(files);

  const RowDragHandleCell = ({ rowId }) => {
    const { attributes, listeners } = useSortable({
      id: rowId,
    });
    return (
      // Alternatively, you could set these attributes on the rows themselves
      <button {...attributes} {...listeners}>
        🟰
      </button>
    );
  };

  // Row Component
  const DraggableRow = ({ row }) => {
    const {
      transform,
      transition,
      setNodeRef,
      isDragging,
      listeners,
      attributes,
    } = useSortable({
      id: row.original.id,
    });

    const style = {
      transform: CSS.Transform.toString(transform), //let dnd-kit do its thing
      transition: transition,
      opacity: isDragging ? 0.8 : 1,
      zIndex: isDragging ? 1 : 0,
      position: "relative",
    };
    return (
      // connect row ref to dnd-kit, apply important styles
      <tr ref={setNodeRef} style={style}>
        {row.getVisibleCells().map((cell) => (
          <td key={cell.id} style={{ width: cell.column.getSize() }}>
            {flexRender(cell.column.columnDef.cell, cell.getContext())}
          </td>
        ))}
        {/*{...listeners}*/}
        {/*{...attributes}*/}
      </tr>
    );
  };

  const columns = useMemo(
    () => [
      // Create a dedicated drag handle column. Alternatively, you could just set up dnd events on the rows themselves.
      {
        id: "drag-handle",
        header: "Move",
        cell: ({ row }) => <RowDragHandleCell rowId={row.id} />,
        size: 60,
      },
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

  const table = useReactTable({
    data: files,
    columns: columns,
    getCoreRowModel: getCoreRowModel(),
    getRowId: (row) => {
      return row.id;
    }, //required because row indexes will change
  });

  function handleDragEnd(event) {
    const { active, over } = event;
    if (active && over && active.id !== over.id) {
      setFiles((data) => {
        const oldIndex = dataIds.indexOf(active.id);
        const newIndex = dataIds.indexOf(over.id);
        return arrayMove(data, oldIndex, newIndex); //this is just a splice util
      });
    }
  }

  const sensors = useSensors(
    useSensor(MouseSensor, {}),
    useSensor(TouchSensor, {}),
    useSensor(KeyboardSensor, {}),
  );

  return (
    // <div className={styles.workSpace}>
    //   <Workspaces />
    // </div>
    <DndContext
      collisionDetection={closestCenter}
      modifiers={[restrictToVerticalAxis]}
      onDragEnd={() => handleDragEnd()}
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
