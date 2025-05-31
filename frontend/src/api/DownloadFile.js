import { BLOCK_URI } from "../consts/Constants.js";
import { getFileInfo } from "./GetFileInfo.js";

export async function downloadFile(file, token) {
  if (file === null || file.id === null || file.workspaceID === null) {
    return new Error("Invalid parameters");
  }
  try {
    const headers = {
      Authorization: `Bearer ${token}`,
    };
    const info = await getFileInfo(
      { wsID: file.workspaceID, id: file.id, listVersions: true },
      token,
    );

    const downloadRes = await fetch(
      `${BLOCK_URI}/download/${file.id}?wsID=${file.workspaceID}`,
      {
        method: "GET",
        headers: headers,
      },
    );
    const blob = await downloadRes.blob();
    const url = URL.createObjectURL(blob);

    const filename = info.name.substring(info.name.lastIndexOf("/") + 1);
    console.log(filename);

    const a = document.createElement("a");
    a.href = url;
    a.download = `${filename}`;
    a.click();
    URL.revokeObjectURL(url);
  } catch (e) {
    console.error("Download failed", e);
  }
}
