import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { flexRender } from "@tanstack/react-table";
import { Menu, useContextMenu } from "react-contexify";
import "./context.css";

export const DraggableRow = ({ row, MENU_ID, contextMenuItems }) => {
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
    transform: CSS.Transform.toString(transform),
    transition: transition,
    opacity: isDragging ? 0.8 : 1,
    zIndex: isDragging ? 1 : 0,
    position: "relative",
  };

  const { show } = useContextMenu({
    id: MENU_ID,
  });

  const handleContext = (event) => {
    event.preventDefault();
    show({ event, id: MENU_ID, props: { row } });
  };

  return (
    <>
      <tr
        onContextMenu={handleContext}
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

      <Menu animation={"none"} id={MENU_ID}>
        {contextMenuItems && contextMenuItems}
      </Menu>
    </>
  );
};
