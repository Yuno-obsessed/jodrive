import { getFilenameWithIcon } from "../../../util/filenameUtils.jsx";
import { formatByteSize } from "../../../util/fileUtils.js";
import { getUserAvatar } from "../../../util/userAvatarUtils.jsx";

export const fileTreeColumns = [
  {
    accessorFn: (row) => getFilenameWithIcon(row.name, 35),
    cell: (filename) => filename.getValue(),
    header: "Name",
  },
  {
    accessorFn: (row) => formatByteSize(row.size),
    cell: (info) => info.getValue(),
    header: "Size",
  },
  {
    accessorFn: (row) => getUserAvatar(row),
    cell: (avatar) => avatar.getValue(),
    header: "Uploader",
  },
  { accessorKey: "uploadedAt", header: "Uploaded At" },
];
