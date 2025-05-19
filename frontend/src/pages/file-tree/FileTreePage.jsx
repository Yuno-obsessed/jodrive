import useAuthStore from "../../util/authStore.js";
import { useTreeModel } from "../../enitites/file-tree/model/index.js";
import { useParams } from "react-router-dom";
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
import { DraggableRow } from "../../components/ui/draggable-item/index.jsx";
import { useFileTree } from "./model/index.js";
import { fileTreeColumns } from "./config/index.js";
import { FileTreeTable } from "../../components/ui/table-v2/index.jsx";

export const FileTreePage = () => {
  const { id } = useParams();
  const { token } = useAuthStore();
  const { files, setFiles } = useTreeModel();

  useFileTree(id, token, setFiles);

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

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      modifiers={[restrictToVerticalAxis]}
    >
      <FileTreeTable
        table={table}
        dataIds={dataIds}
        DraggableRow={DraggableRow}
      />
    </DndContext>
  );
};
