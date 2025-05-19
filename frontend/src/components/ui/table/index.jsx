import React from "react";
import PropTypes from "prop-types";
import clsx from "clsx";

/**
 * Re‑usable table component.
 *
 * @example
 * const columns = [
 *   { Header: "Name", accessor: "name" },
 *   { Header: "Updated at", accessor: "updatedAt" },
 *   { Header: "Size", accessor: "size" },
 *   { Header: "Uploader", accessor: "uploader" },
 *   { Header: "Workspace", accessor: "workspace" },
 * ];
 *
 * <Table
 *   columns={columns}
 *   data={files}
 *   renderRow={(file) => (
 *     <FileEntry
 *       key={`${file.id}_${file.workspaceID}`}
 *       file={file}
 *       // …other props…
 *     />
 *   )}
 *   tableClassName={styles.filesList}
 *   headerRowClassName={styles.theader}
 * />
 */
const Table = React.forwardRef(
  (
    {
      columns = [],
      data = [],
      renderRow,
      tableClassName,
      headerRowClassName,
      headerCellClassName,
      bodyRowClassName,
      bodyCellClassName,
      emptyMessage = "No data",
      ...rest
    },
    ref,
  ) => {
    const defaultRenderRow = (row, rowIndex) => (
      <tr key={rowIndex} className={bodyRowClassName}>
        {columns.map((col, colIndex) => {
          const accessor = typeof col === "string" ? col : col.accessor;
          const CellComp = col.Cell;
          const value = accessor ? row[accessor] : undefined;
          return (
            <td
              key={colIndex}
              className={clsx(bodyCellClassName, col.tdClassName)}
            >
              {CellComp ? <CellComp value={value} row={row} /> : value}
            </td>
          );
        })}
      </tr>
    );

    return (
      <table ref={ref} className={tableClassName} {...rest}>
        <thead>
          <tr className={headerRowClassName}>
            {columns.map((col, i) => {
              const header = typeof col === "string" ? col : col.Header;
              return (
                <th
                  key={i}
                  className={clsx(headerCellClassName, col.thClassName)}
                >
                  {header}
                </th>
              );
            })}
          </tr>
        </thead>
        <tbody>
          {data && data.length > 0 ? (
            data.map((row, i) => (renderRow || defaultRenderRow)(row, i))
          ) : (
            <tr>
              <td colSpan={columns.length} className="text-center py-4">
                {emptyMessage}
              </td>
            </tr>
          )}
        </tbody>
      </table>
    );
  },
);

Table.displayName = "Table";

Table.propTypes = {
  columns: PropTypes.array.isRequired,
  data: PropTypes.array,
  renderRow: PropTypes.func,
  tableClassName: PropTypes.string,
  headerRowClassName: PropTypes.string,
  headerCellClassName: PropTypes.string,
  bodyRowClassName: PropTypes.string,
  bodyCellClassName: PropTypes.string,
  emptyMessage: PropTypes.string,
};

export default Table;
