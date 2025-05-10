import React, { useState } from "react";
import styles from "./Sidebar.module.css";
import { UploadModal } from "../features/UploadModal.jsx";
import useAuthStore from "../util/authStore.js";

export const Sidebar = () => {
  const [showUploadModal, setShowUploadModal] = useState(false);
  const { userInfo } = useAuthStore();

  const mapToGB = (bytes) => {
    const gb = bytes / 1_000_000_000;
    return parseFloat(gb.toFixed(2));
  };

  console.log(userInfo);
  let usedStorage = userInfo.statistics
    .filter((s) => s.quota === "USER_STORAGE_USED")
    .map((s) => mapToGB(s.value));
  let storageLimit = mapToGB(userInfo.subscription.storageLimit);

  const calculateStorageUsagePercentage = () => {
    return (storageLimit / 100) * usedStorage;
  };

  return (
    <>
      <aside className={styles.sidebar}>
        <button
          className={styles.btnNew}
          onClick={() => setShowUploadModal(true)}
        >
          <img src="plus.svg" alt="Plus-New" className={styles.btnNewImg} />
          <span className={styles.btnNewText}>
            <a>New</a>
          </span>
        </button>
        <ul className={styles.sidebarList}>
          <li className={styles.sidebarEl}>
            <img src="home.svg" alt="Home" className={styles.sidebarImages} />
            <a href="/" className={styles.sidebarLinks}>
              Home
            </a>
          </li>
          <li className={styles.sidebarEl}>
            <img src="drive.svg" alt="Drive" className={styles.sidebarImages} />
            <a href="/my-drive" className={styles.sidebarLinks}>
              My Drive
            </a>
          </li>
          <li className={styles.sidebarEl}>
            <img
              src="workspaces.svg"
              alt="Workspaces"
              className={styles.sidebarImages}
            />
            <a href="/workspaces" className={styles.sidebarLinks}>
              Workspaces
            </a>
          </li>
          <li className={styles.sidebarEl}>
            <img
              src="cloud.svg"
              alt="Storage"
              className={styles.sidebarImages}
            />
            <a href="/storage" className={styles.sidebarLinks}>
              Storage
            </a>
          </li>
          <li className={styles.sidebarEl}>
            <div className={styles.storageBar}>
              <div
                className={styles.storageBarProgress}
                style={{ width: `${calculateStorageUsagePercentage()}%` }}
              />
              <a className={styles.storageInfo}>
                {usedStorage} GB of {storageLimit} GB used
              </a>
            </div>
          </li>
        </ul>
      </aside>
      {showUploadModal && (
        <UploadModal onClose={() => setShowUploadModal(false)} />
      )}
    </>
  );
};

export default Sidebar;
