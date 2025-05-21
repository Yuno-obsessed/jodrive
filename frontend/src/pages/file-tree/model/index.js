import { listDirectory } from "../../../api/DirectoryAPI.js";
import { useEffect } from "react";

export const useFileTree = (workspaceID, directory, token, setFiles) => {
  useEffect(() => {
    const fetchFiles = async () => {
      try {
        const res = await listDirectory({ workspaceID, directory }, token);
        setFiles(res.elements);
      } catch (err) {
        console.error("Failed to load files:", err);
      }
    };
    if (workspaceID && token) void fetchFiles();
  }, [directory, workspaceID, token, setFiles]);
};
