import { WORKSPACE_URI } from "../consts/Constants.js";

export async function createWorkspace(request, token) {
  if (request.name === null || request.description === null) {
    return new Error("Invalid parameters");
  }
  const response = await fetch(`${WORKSPACE_URI}`, {
    method: "POST",
    body: request,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });
  if (response.status !== 201) {
    throw new Error("Error creating workspace");
  }
}

export async function getWorkspaces(token) {
  const response = await fetch(`${WORKSPACE_URI}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });
  if (response.status !== 200) {
    throw new Error("Error listing workspaces");
  }
  return await response.json();
}

export async function getWorkspaceUsers(request, token) {
  if (request.wsID === null) {
    return new Error("Invalid parameters");
  }
  let page = request.page ? request.page : 0;
  let size = request.size ? request.size : 10;
  const response = await fetch(
    `${WORKSPACE_URI}/${request.wsID}/users?page=${page}&size=${size}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  );
  if (response.status !== 200) {
    throw new Error("Error listing workspace users");
  }
  return await response.json();
}
