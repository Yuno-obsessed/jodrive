import styles from "./Sidebar.module.css";
import useAuthStore from "../../util/authStore.js";
import { Button } from "../../components/ui/button/index.jsx";
import { useLocation, useNavigate } from "react-router-dom";
import MingcuteHome4Line from "~icons/mingcute/home-4-line";
import CarbonWorkspace from "~icons/carbon/workspace";
import MynauiTrash from "~icons/mynaui/trash";
import clsx from "clsx";

const navigation = [
  {
    name: "Home",
    icon: <MingcuteHome4Line className={styles.sidebarImages} />,
    link: "/",
  },
  {
    name: "Workspaces",
    icon: <CarbonWorkspace className={styles.sidebarImages} />,
    link: "/workspaces",
  },
  {
    name: "Trash",
    icon: <MynauiTrash className={styles.sidebarImages} />,
    link: "/deleted",
  },
];

export const Sidebar = () => {
  const { userInfo } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();

  const isActive = (link) => {
    return location.pathname === link;
  };

  const mapToGB = (bytes) => {
    const gb = bytes / 1_073_741_824;
    return parseFloat(gb.toFixed(2));
  };

  let usedStorage = userInfo.statistics
    .filter((s) => s.quota === "USER_STORAGE_USED")
    .map((s) => mapToGB(s.value));

  let storageLimit = mapToGB(userInfo.subscription.storageLimit);
  const calculateStorageUsagePercentage = () => {
    return (storageLimit / 100) * usedStorage;
  };

  return (
    <aside className={styles.sidebar}>
      <ul className={styles.sidebarList}>
        {navigation.map((item) => (
          <Button
            variant={"ghost"}
            key={item.name}
            className={clsx(
              styles.sidebarEl,
              isActive(item.link) && styles.activeButton,
            )}
            onClick={() => navigate(item.link)}
          >
            {item.icon}
            {item.name}
          </Button>
        ))}

        <div className={styles.storageBar}>
          <div
            className={styles.storageBarProgress}
            style={{ width: `${calculateStorageUsagePercentage()}%` }}
          />
          <a className={styles.storageInfo}>
            {usedStorage} GB of {storageLimit} GB used
          </a>
        </div>
      </ul>
    </aside>
  );
};

export default Sidebar;
