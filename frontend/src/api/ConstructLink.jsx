import { METADATA_URI } from "../consts/Constants.js";

export async function ConstructLink(file, timeUnit, expiration, token) {
  if (file === null || file.id === null || file.workspaceID === null) {
    return new Error("Invalid parameters");
  }
  let unit = timeUnit != null ? timeUnit : "MINUTE";
  let expiresIn = expiration != null ? expiration : 60;
  const response = await fetch(
    `${METADATA_URI}/file/${file.id}/share?wsID=${file.workspaceID}&timeUnit=${unit}&expiresIn=${expiresIn}`,
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
