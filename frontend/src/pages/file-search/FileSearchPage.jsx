import { useSearchModel } from "../../enitites/file/model/index.js";
import { getCoreRowModel, useReactTable } from "@tanstack/react-table";
import { fileTreeColumns } from "../file-tree/config/index.js";
import { useMemo } from "react";
import { FileTreeTable } from "../../components/ui/table-v2/index.jsx";
import { DraggableRow } from "../../components/ui/draggable-item/index.jsx";
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

export const FileSearchPage = () => {
  const { searchResults } = useSearchModel();

  const getRowID = (row) => {
    return row.id + "_" + row.workspaceID;
  };

  const elements = useMemo(
    () => searchResults?.elements ?? [],
    [searchResults],
  );

  const dataIds = useMemo(() => elements.map((f) => getRowID(f)), [elements]);

  const table = useReactTable({
    data: elements,
    columns: fileTreeColumns,
    getCoreRowModel: getCoreRowModel(),
    getRowId: (row) => getRowID(row),
  });

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
      onDragEnd={(e) => {
        console.log(e);
      }}
    >
      <FileTreeTable
        table={table}
        dataIds={dataIds}
        DraggableRow={DraggableRow}
      />
    </DndContext>
  );
};
