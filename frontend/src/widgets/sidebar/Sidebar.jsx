import styles from "./Sidebar.module.css";
import useAuthStore from "../../util/authStore.js";
import { Button } from "../../components/ui/button/index.jsx";
import { useLocation, useNavigate } from "react-router-dom";

import clsx from "clsx";
import { navigation } from "./navigation/index.jsx";

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

  let usedStorage = userInfo?.statistics
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
            onClick={() => {
              console.log(item.children);
              if (!item.children) {
                navigate(item.link);
              }
            }}
          >
            {item.icon}
            {item.name}
            {item.children ? item.children : <></>}
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
