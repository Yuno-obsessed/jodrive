import { METADATA_URI } from "../consts/Constants.js";

export async function getFileTree(params, token) {
  if (params == null || params.wsID == null) {
    throw new Error("Invalid parameters");
  }
  const response = await fetch(
    `${METADATA_URI}/file/tree?wsID=${params.wsID}&path=${params.path}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  );
  if (!response.ok) {
    throw new Error("Error searching files");
  }
  return await response.json();
}
