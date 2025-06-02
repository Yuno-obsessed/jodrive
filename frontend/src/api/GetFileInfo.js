import { METADATA_URI } from "../consts/Constants.js";

export async function getFileInfo(request, token) {
  if (!request.wsID && (!request.path || !request.id) && !request.link) {
    return new Error("Invalid parameters");
  }
  const params = new URLSearchParams();
  if (request.link) params.append("link", request.link);
  if (request.wsID) params.append("wsID", request.wsID);
  if (request.path) params.append("path", request.path);
  if (request.id) params.append("fileID", request.id);
  if (request.listVersions) params.append("listVersions", request.listVersions);
  if (request.version) params.append("version", request.version);
  const headers = token != null ? { Authorization: `Bearer ${token}` } : {};
  console.log(headers);
  const response = await fetch(`${METADATA_URI}/file?${params.toString()}`, {
    method: "GET",
    headers,
  });
  if (response.status !== 200) {
    throw new Error("Error getting file info");
  }
  return await response.json();
}
