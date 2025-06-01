import styles from "../Sidebar.module.css";
import MingcuteHome4Line from "~icons/mingcute/home-4-line";
import MaterialSymbolsGroups from "~icons/material-symbols/groups";
import MynauiTrash from "~icons/mynaui/trash";
import { CreateWorkspaceModalButton } from "../../../features/create-ws/index.jsx";
import { JoinWorkspaceModalButton } from "../../../features/join-workspace/index.jsx";

export const navigationElements = (token, workspaceItems = []) => {
  const workspacesLink = "/workspace";

  return [
    {
      name: "Home",
      icon: <MingcuteHome4Line className={styles.sidebarImages} />,
      link: "/",
    },
    {
      name: "Workspaces",
      icon: <MaterialSymbolsGroups className={styles.sidebarImages} />,
      link: workspacesLink,
      children: workspaceItems.map((workspace) => ({
        name: workspace.name,
        link: `${workspacesLink}/${workspace.id}`,
      })),
      buttons: (
        <div className={styles.sideBtns}>
          <CreateWorkspaceModalButton />
          <JoinWorkspaceModalButton />
        </div>
      ),
    },
    {
      name: "Trash",
      icon: <MynauiTrash className={styles.sidebarImages} />,
      link: "/deleted",
    },
  ];
};
