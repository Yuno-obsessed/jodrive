import { BLOCK_URI, METADATA_URI } from "../consts/Constants.js";

export async function downloadFile(file, token) {
  try {
    const headers = {
      Authorization: `Bearer ${token}`,
    };
    const infoRes = await fetch(
      `${METADATA_URI}/file/${file.id}?wsID=${file.workspaceID}`,
      {
        method: "GET",
        headers: headers,
      },
    );
    const info = await infoRes.json();

    const downloadRes = await fetch(
      `${BLOCK_URI}/download/${file.id}?wsID=${file.workspaceID}`,
      {
        method: "GET",
        headers: headers,
      },
    );
    const blob = await downloadRes.blob();
    const url = URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = `${info.filename}`;
    a.click();
    URL.revokeObjectURL(url);
  } catch (e) {
    console.error("Download failed", e);
  }
}
