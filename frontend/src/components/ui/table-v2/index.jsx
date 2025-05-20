import {
  SortableContext,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { flexRender } from "@tanstack/react-table";
import styles from "./Table.module.css";
import clsx from "clsx";
import "react-contexify/ReactContexify.css";
import { DraggableRow } from "../draggable-item/index.jsx";
import { RowContextMenu } from "../../../pages/file-search/context/index.jsx";

export const FileTreeTable = ({ table, dataIds, className, eventHandler }) => {
  return (
    <table className={clsx(styles.table, className)}>
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
        <SortableContext items={dataIds} strategy={verticalListSortingStrategy}>
          {table.getRowModel().rows.map((row) => (
            <DraggableRow key={row.id} row={row} />
          ))}
        </SortableContext>
        <RowContextMenu handleEvents={eventHandler} />
      </tbody>
    </table>
  );
};
