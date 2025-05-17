import { METADATA_URI } from "../consts/Constants.js";

export async function createDirectory(request, token) {
  if (request.workspaceID === null || request.path === null) {
    return new Error("Invalid parameters");
  }
  const response = await fetch(`${METADATA_URI}/directory`, {
    method: "POST",
    body: request,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });
  if (response.status !== 201) {
    throw new Error("Error creating directory");
  }
}

export async function getDirectories(wsID, path, token) {
  if (wsID === null) {
    return new Error("Invalid parameters");
  }
  let pathParam = "";
  if (path === null) {
    pathParam = `&path=${pathParam}`;
  }
  const response = await fetch(
    `${METADATA_URI}/directory?wsID=${wsID}${pathParam}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  );
  if (response.status !== 200) {
    throw new Error("Error listing directories");
  }
  return await response.json();
}
