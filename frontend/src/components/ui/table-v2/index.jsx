import {
  SortableContext,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { flexRender } from "@tanstack/react-table";
import styles from "./Table.module.css";
import clsx from "clsx";
import "react-contexify/ReactContexify.css";
import { Item } from "react-contexify";

const MENU_ID = "table";

export const FileTreeTable = ({ table, dataIds, DraggableRow, className }) => {
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
            <DraggableRow
              MENU_ID={row.id}
              contextMenuItems={
                <id>
                  <Item>Create Directory</Item>
                  <Item>Download</Item>
                  <Item>Share</Item>
                  <Item>Rename</Item>
                  <Item>Delete</Item>
                </id>
              }
              key={row.id}
              row={row}
            />
          ))}
        </SortableContext>
      </tbody>
    </table>
  );
};
