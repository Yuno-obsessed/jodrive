import styles from "../Sidebar.module.css";
import MingcuteHome4Line from "~icons/mingcute/home-4-line";
import MaterialSymbolsGroups from "~icons/material-symbols/groups";
import MynauiTrash from "~icons/mynaui/trash";
import { Workspaces } from "../../workspaces/Workspaces.jsx";

export const navigation = [
  {
    name: "Home",
    icon: <MingcuteHome4Line className={styles.sidebarImages} />,
    link: "/",
  },
  {
    name: "Workspaces",
    icon: <MaterialSymbolsGroups className={styles.sidebarImages} />,
    link: "/workspaces",
    children: (
      <div>
        <Workspaces />
      </div>
    ),
  },
  {
    name: "Trash",
    icon: <MynauiTrash className={styles.sidebarImages} />,
    link: "/deleted",
  },
];
