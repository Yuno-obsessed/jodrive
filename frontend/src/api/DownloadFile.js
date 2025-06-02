import { BLOCK_URI } from "../consts/Constants.js";

export async function downloadFile(file, token, link) {
  if (!file || !file.id || !file.workspaceID) {
    return new Error("Invalid parameters");
  }
  try {
    const headers = token != null ? { Authorization: `Bearer ${token}` } : {};
    let linkParam = !link ? "" : `&link=${encodeURIComponent(link)}`;
    console.log(linkParam);
    const downloadRes = await fetch(
      `${BLOCK_URI}/download/${file.id}?wsID=${file.workspaceID}${linkParam}`,
      {
        method: "GET",
        headers: headers,
      },
    );
    const blob = await downloadRes.blob();
    const url = URL.createObjectURL(blob);

    const filename = file.name;
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
