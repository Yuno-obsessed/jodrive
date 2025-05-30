import { METADATA_URI } from "../consts/Constants.js";

export async function updateFile(file, newName, fileAction, token) {
  let newNameParam = "";
  let fileActionParam = "";
  if (file === null || (newName === null && fileAction === null)) {
    return new Error("Invalid parameters");
  }
  if (newName != null) {
    newNameParam = `&newName=${newName}`;
  }
  if (fileAction != null) {
    fileActionParam = `&fileAction=${fileAction}`;
  }
  const response = await fetch(
    `${METADATA_URI}/file/${file.id}?wsID=${file.workspaceID}${fileActionParam}${newNameParam}`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  );
  if (!response.ok) {
    throw new Error("Error updating file");
  }
  return response.text();
}
