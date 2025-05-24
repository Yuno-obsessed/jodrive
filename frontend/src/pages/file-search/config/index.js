import { getFilenameWithIcon } from "../../../util/filenameUtils.jsx";
import { formatByteSize } from "../../../util/fileUtils.js";

export const fileSearchColumns = (userInfo) => [
  {
    accessorFn: (row) => getFilenameWithIcon(row.name),
    cell: (info) => info.getValue(),
    header: "Name",
  },
  {
    accessorFn: (row) =>
      userInfo.workspaces.filter((f) => f.id == row.workspaceID)[0],
    cell: (info) => info.getValue().name,
    header: "Workspace",
  },
  { accessorKey: "workspaceID", header: "Workspace" },
  {
    accessorFn: (row) => formatByteSize(row.size),
    cell: (info) => info.getValue(),
    header: "Size",
  },
  { accessorKey: "uploaderName", header: "Uploader" },
  { accessorKey: "uploadedAt", header: "Uploaded At" },
];
