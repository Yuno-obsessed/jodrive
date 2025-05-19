import { getFilenameWithIcon } from "../../../util/filenameUtils.jsx";

export const fileTreeColumns = [
  {
    accessorFn: (row) => getFilenameWithIcon(row.name),
    cell: (info) => info.getValue(),
    header: "Name",
  },
  { accessorKey: "workspaceID", header: "Workspace" },
  { accessorKey: "isDirectory", header: "Type" },
  { accessorKey: "uploaderName", header: "Uploader" },
];
