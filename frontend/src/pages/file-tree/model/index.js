import { listDirectory } from "../../../api/DirectoryAPI.js";
import { useEffect } from "react";

export const useFileTree = (workspaceID, token, setFiles) => {
  useEffect(() => {
    const fetchFiles = async () => {
      try {
        const res = await listDirectory({ workspaceID, directory: "/" }, token);
        setFiles(res.elements);
      } catch (err) {
        console.error("Failed to load files:", err);
      }
    };
    if (workspaceID && token) fetchFiles().then((r) => console.log(r));
  }, [workspaceID, token, setFiles]);
};
