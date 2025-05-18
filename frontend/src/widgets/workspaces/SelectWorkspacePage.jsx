import { useEffect, useState } from "react";
import useAuthStore from "../../util/authStore.js";
import { getWorkspaces } from "../../api/WorkspaceAPI.js";
import { useWorkspacesModel } from "../../enitites/workspace/model/index.js";
import { Button } from "../../components/ui/button/index.jsx";
import MajesticonsFolder from "~icons/majesticons/folder";

export const Workspaces = () => {
  const { token } = useAuthStore();
  const { userWorkspaces, setWorkspaces, setActiveWorkspace } =
    useWorkspacesModel();

  useEffect(() => {
    getWorkspaces(token)
      .then((res) => setWorkspaces(res))
      .catch(console.log);
  }, []);

  return userWorkspaces.map((workspace, index) => (
    <Button key={workspace.name} variant="ghost">
      <MajesticonsFolder width="24px" height="24px" />
      {workspace.name}
    </Button>
  ));
};
