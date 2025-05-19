import { getFilenameWithIcon } from "../../../util/filenameUtils.jsx";

export const fileTreeColumns = [
  {
    accessorFn: (row) => getFilenameWithIcon(row.name),
    cell: (info) => info.getValue(),
    header: "Name",
  },
  { accessorKey: "isDirectory", header: "Type" },
  { accessorKey: "uploaderName", header: "Uploader" },
];
