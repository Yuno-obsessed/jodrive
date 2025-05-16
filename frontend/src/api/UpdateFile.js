import { METADATA_URI } from "../consts/Constants.js";

export async function updateFile(file, newName, stateAction, token) {
  if (file === null && (newName === null || stateAction === null)) {
    return new Error("Invalid parameters");
  }
  const response = await fetch(
    `${METADATA_URI}/file/${file.id}?wsID=${file.workspaceID}&newName=${newName}&stateAction=${stateAction}`,
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
