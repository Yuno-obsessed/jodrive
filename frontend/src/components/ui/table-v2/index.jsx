import {
  SortableContext,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { flexRender } from "@tanstack/react-table";

export const FileTreeTable = ({ table, dataIds, DraggableRow }) => (
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
      <SortableContext items={dataIds} strategy={verticalListSortingStrategy}>
        {table.getRowModel().rows.map((row) => (
          <DraggableRow key={row.id} row={row} />
        ))}
      </SortableContext>
    </tbody>
  </table>
);
