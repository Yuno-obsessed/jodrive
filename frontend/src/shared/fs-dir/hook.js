import { useLocation, useParams } from "react-router-dom";
import { useFilesystemStore } from "./index.js";
import { useEffect } from "react";

export const useSyncFilesystemPath = () => {
  const location = useLocation();
  const { id } = useParams();
  const { currentPath, setCurrPath, setBasePath } = useFilesystemStore();

  useEffect(() => {
    console.log("CURR", location.pathname);
    if (!id && !location.pathname.startsWith("/workspace/")) return;

    const basePath = `/workspace/${id}`;
    const subPath = location.pathname.replace(basePath, "") || "/";
    setCurrPath(subPath);
    setBasePath(basePath);
  }, [location.pathname, id, currentPath]);
};
