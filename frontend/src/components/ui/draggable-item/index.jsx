import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { flexRender } from "@tanstack/react-table";
import { useContextMenu } from "react-contexify";
import { useContextMenuStore } from "../../../pages/file-search/model/context.js";

export const DraggableRow = ({ row, MENU_ID = "table" }) => {
  const {
    transform,
    transition,
    setNodeRef,
    isDragging,
    listeners,
    attributes,
  } = useSortable({ id: row.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.8 : 1,
    zIndex: isDragging ? 1 : 0,
    position: "relative",
  };

  const { show } = useContextMenu({ id: MENU_ID });
  const setRow = useContextMenuStore((s) => s.setRow);

  function handleContextMenu(e) {
    e.preventDefault();
    setRow(row.original);
    show({ event: e });
  }

  return (
    <tr
      onContextMenu={handleContextMenu}
      {...attributes}
      {...listeners}
      ref={setNodeRef}
      style={style}
    >
      {row.getVisibleCells().map((cell) => (
        <td key={cell.id} style={{ width: cell.column.getSize() }}>
          {flexRender(cell.column.columnDef.cell, cell.getContext())}
        </td>
      ))}
    </tr>
  );
};
