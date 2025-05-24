import { getFilenameWithIcon } from "../../../util/filenameUtils.jsx";
import { formatByteSize } from "../../../util/fileUtils.js";

export const fileTreeColumns = [
  {
    accessorFn: (row) => getFilenameWithIcon(row.name),
    cell: (info) => info.getValue(),
    header: "Name",
  },
  {
    accessorFn: (row) => formatByteSize(row.size),
    cell: (info) => info.getValue(),
    header: "Size",
  },
  { accessorKey: "uploaderName", header: "Uploader" },
  { accessorKey: "uploadedAt", header: "Uploaded At" },
];
