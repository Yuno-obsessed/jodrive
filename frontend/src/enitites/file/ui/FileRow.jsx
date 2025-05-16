import styles from "./FileRow.module.css";
import clsx from "clsx";

export const FileRow = ({
  file,
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
            ? columnRenderers[column](file)
            : file[column]}
        </td>
      ))}
      <td>
        <div className={styles.fileIcons}>{buttons}</div>
      </td>
    </tr>
  );
};
