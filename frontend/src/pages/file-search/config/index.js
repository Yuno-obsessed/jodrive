import { getFilenameWithIcon } from "../../../util/filenameUtils.jsx";
import { formatByteSize } from "../../../util/fileUtils.js";
import { getUserAvatar } from "../../../util/userAvatarUtils.jsx";

export const fileSearchColumns = (userInfo) => [
  {
    accessorFn: (row) => getFilenameWithIcon(row.name, 20),
    cell: (info) => info.getValue(),
    header: "Name",
  },
  {
    accessorFn: (row) =>
      userInfo.workspaces.filter((f) => f.id == row.workspaceID)[0],
    cell: (info) => info.getValue().name,
    header: "Workspace",
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
