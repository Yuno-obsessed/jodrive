import { METADATA_URI } from "../consts/Constants.js";

export async function getFileInfo(request, token) {
  if (!request.wsID && (!request.path || !request.id)) {
    return new Error("Invalid parameters");
  }
  // TODO make it usable without token, with link only
  let pathParam = !request.path ? "" : `&path=${request.path}`;
  let idParam = !request.id ? "" : `&fileID=${request.id}`;
  let listVersionsParam = !request.listVersions
    ? ""
    : `&listVersions=${request.listVersions}`;
  let versionParam = !request.version ? "" : `&version=${request.version}`;
  const response = await fetch(
    `${METADATA_URI}/file?wsID=${request.wsID}${idParam}${pathParam}${listVersionsParam}${versionParam}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  );
  if (response.status !== 200) {
    throw new Error("Error getting file info");
  }
  return response.json();
}
