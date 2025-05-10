import { METADATA_URI } from "../consts/Constants.js";

export async function deleteFile(file, token) {
  if (file === null || file.id === null || file.workspaceID === null) {
    return new Error("Invalid parameters");
  }
  const response = await fetch(
    `${METADATA_URI}/file/${file.id}?wsID=${file.workspaceID}`,
    {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  );
  if (response.status !== 204) {
    throw new Error("Error deleting file");
  }
}
