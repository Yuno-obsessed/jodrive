import { METADATA_URI } from "../consts/Constants.js";

export async function renameFile(file, newName, token) {
  if (file === null || newName === null) {
    return new Error("Invalid parameters");
  }
  const response = await fetch(
    `${METADATA_URI}/file/${file.id}?wsID=${file.workspaceID}&newName=${newName}`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  );
  if (!response.ok) {
    throw new Error("Error renaming file");
  }
}
