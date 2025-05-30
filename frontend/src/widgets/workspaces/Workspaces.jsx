import { useEffect } from "react";
import useAuthStore from "../../util/authStore.js";
import { getWorkspaces } from "../../api/WorkspaceAPI.js";
import { useWorkspacesModel } from "../../enitites/workspace/model/index.js";
import { Button } from "../../components/ui/button/index.jsx";
import MajesticonsFolder from "~icons/majesticons/folder";
import clsx from "clsx";
import styles from "./Workspaces.module.css";

export const Workspaces = () => {
  const { token } = useAuthStore();
  const { userWorkspaces, setWorkspaces, setActive, activeWorkspace } =
    useWorkspacesModel();

  useEffect(() => {
    if (!userWorkspaces || userWorkspaces.length === 0) {
      getWorkspaces(token)
        .then((res) => {
          setWorkspaces(res);
          if (!activeWorkspace) {
            setActive(res[0]);
          }
        })
        .catch(console.log);
    }
  }, []);

  return userWorkspaces.map((workspace) => (
    <Button
      key={workspace.name}
      className={clsx(
        workspace === activeWorkspace && styles.activeWorkspace,
        styles.btn,
      )}
      variant="default"
      onClick={() => {
        setActive(workspace);
      }}
    >
      <MajesticonsFolder width="24px" height="24px" />
      {workspace.name}
    </Button>
  ));
};
