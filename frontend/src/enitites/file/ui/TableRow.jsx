import styles from "./FileRow.module.css";
import clsx from "clsx";

export const TableRow = ({
  entity,
  columns,
  columnRenderers,
  onClick,
  onMouseEnter,
  onMouseLeave,
  isSelected = false,
  buttons,
}) => {
  return (
    <tr
      className={clsx(isSelected && styles.isSelected)}
      onClick={onClick}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
    >
      {columns.map((column) => (
        <td key={column}>
          {columnRenderers[column]
            ? columnRenderers[column](entity)
            : entity[column]}
        </td>
      ))}
      <td>
        <div className={styles.fileIcons}>{buttons}</div>
      </td>
    </tr>
  );
};
