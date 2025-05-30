import { METADATA_URI } from "../consts/Constants.js";
import dayjs from "dayjs";

export async function constructLink(file, expiresAt, token) {
  if (file === null || file.id === null || file.workspaceID === null) {
    return new Error("Invalid parameters");
  }
  let expiresAtUnix =
    expiresAt != null ? expiresAt : dayjs(new Date()).add(2, "hour").unix();
  const response = await fetch(
    `${METADATA_URI}/file/${file.id}/share?wsID=${file.workspaceID}&expiresAt=${expiresAtUnix}`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  );
  let textResponse;
  try {
    textResponse = await response.text();
  } catch (error) {
    console.error("Failed to parse text response:", error);
    throw new Error("Failed to parse text response");
  }

  if (!response.ok) {
    console.error("Response error:", textResponse.message);
    throw new Error(textResponse.message || "Error in response");
  }
  return textResponse;
}
