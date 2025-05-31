import styles from "./Sidebar.module.css";
import useAuthStore from "../../util/authStore.js";
import { Button } from "../../components/ui/button/index.jsx";
import { useLocation, useNavigate } from "react-router-dom";
import MdiMenuRight from "~icons/mdi/menu-right";

import clsx from "clsx";
import { navigationElements } from "./navigation/index.jsx";
import { useEffect, useState } from "react";
import { useWorkspacesModel } from "../../enitites/workspace/model/index.js";
import { getWorkspaces } from "../../api/WorkspaceAPI.js";

export const Sidebar = () => {
  const { userInfo, token } = useAuthStore();
  const [showWorkspaces, setShowWorkspaces] = useState(false);
  const { userWorkspaces, setWorkspaces } = useWorkspacesModel();

  const navigate = useNavigate();
  const location = useLocation();

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

  useEffect(() => {
    if (!userWorkspaces || userWorkspaces.length === 0) {
      getWorkspaces(token)
        .then((res) => {
          setWorkspaces(res);
        })
        .catch(console.log);
    }
  }, [token]);

  const navItems = navigationElements(token, userWorkspaces);

  const isLinkActive = (link) => location.pathname === link;

  return (
    <aside className={styles.sidebar}>
      <ul className={styles.sidebarList}>
        {navItems.map((item) => {
          const active = isLinkActive(item.link);

          return (
            <div key={item.name}>
              <Button
                variant="ghost"
                className={clsx(
                  styles.sidebarEl,
                  active && styles.activeButton,
                )}
                onClick={(e) => {
                  e.stopPropagation();
                  if (item.children) {
                    setShowWorkspaces(!showWorkspaces);
                  } else {
                    navigate(item.link);
                  }
                }}
              >
                <div className={styles.item}>
                  <div className={styles.itemMain}>
                    {item.icon}
                    {item.name}
                  </div>
                  <div
                    className={styles.itemButtons}
                    onClick={(e) => e.stopPropagation()}
                  >
                    {item.buttons}
                  </div>
                </div>
              </Button>

              {item.children && showWorkspaces && (
                <div className={styles.sidebarChildren}>
                  {item.children.map((child) => (
                    <Button
                      key={child.link}
                      variant="ghost"
                      className={clsx(
                        styles.sidebarEl,
                        isLinkActive(child.link) && styles.activeButton,
                      )}
                      onClick={(e) => {
                        e.stopPropagation();
                        navigate(child.link);
                      }}
                    >
                      <div className={styles.wsItem}>
                        <MdiMenuRight />
                        {child.name}
                      </div>
                    </Button>
                  ))}
                </div>
              )}
            </div>
          );
        })}

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
