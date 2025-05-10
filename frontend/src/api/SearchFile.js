import { METADATA_URI } from "../consts/Constants.js";

export async function searchFile(params, token) {
  if (params == null) {
    throw new Error("Invalid parameters");
  }
  let page = params.page == null ? 0 : params.page;
  let size = params.size == null ? 10 : params.size;
  let deleted = params.deleted == null ? false : params.deleted;
  let name = params.name == null ? "" : params.name;
  const response = await fetch(
    `${METADATA_URI}/file/search?wsID=${params.wsID}&userID=${params.userID}&name=${name}&deleted=${deleted}&page=${page}&size=${size}`,
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
