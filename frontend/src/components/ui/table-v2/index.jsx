import {
  SortableContext,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { flexRender } from "@tanstack/react-table";
import styles from "./Table.module.css";
import clsx from "clsx";
import "react-contexify/ReactContexify.css";
import { Item } from "react-contexify";
import { DraggableRow } from "../draggable-item/index.jsx";
import TablerShare from "~icons/tabler/share";
import LucideEdit3 from "~icons/lucide/edit-3";
import TablerDownload from "~icons/tabler/download";
import MynauiTrash from "~icons/mynaui/trash";

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
            <DraggableRow
              MENU_ID={"table"}
              contextMenuItems={
                <>
                  <Item
                    id="download"
                    data={row.original}
                    onClick={eventHandler}
                  >
                    <>
                      <TablerDownload />
                      <a>Download</a>
                    </>
                  </Item>
                  <Item id="share" data={row.original} onClick={eventHandler}>
                    <>
                      <TablerShare />
                      <a>Share</a>
                    </>
                  </Item>
                  <Item id="rename" data={row.original} onClick={eventHandler}>
                    <>
                      <LucideEdit3 />
                      <a>Rename</a>
                    </>
                  </Item>
                  <Item id="delete" data={row.original} onClick={eventHandler}>
                    <>
                      <MynauiTrash />
                      <a>Delete</a>
                    </>
                  </Item>
                </>
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
