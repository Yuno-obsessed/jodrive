import { useEffect, useState } from "react";
import useAuthStore from "../../util/authStore.js";
import { getWorkspaces } from "../../api/WorkspaceAPI.js";
import { useWorkspacesModel } from "../../enitites/workspace/model/index.js";
import styles from "./SelectWorkspacePage.module.css";
import { Workspace } from "../../enitites/workspace/ui/Workspace.jsx";

export const SelectWorkspacePage = () => {
  const { token } = useAuthStore();
  const { userWorkspaces, setWorkspaces, setActiveWorkspace } =
    useWorkspacesModel();

  useEffect(() => {
    getWorkspaces(token)
      .then((res) => setWorkspaces(res))
      .catch(console.log);
  }, []);

  return (
    <div className={styles.workspacesHolder}>
      <div className={styles.workspaces}>
        {userWorkspaces.map((workspace, index) => (
          <Workspace
            workspace={workspace}
            onClick={() => setActiveWorkspace(workspace)}
          ></Workspace>
        ))}
      </div>
    </div>
  );
};
