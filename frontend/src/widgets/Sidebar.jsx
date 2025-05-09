import React, { useState } from "react";
import styles from "./Sidebar.module.css";
import { UploadModal } from "../features/UploadModal.jsx";

export const Sidebar = () => {
  const [showUploadModal, setShowUploadModal] = useState(false);

  return (
    <>
      <aside className={styles.sidebar}>
        <button
          className={styles.btnNew}
          onClick={() => setShowUploadModal(true)}
        >
          <span>
            <img src="plus.svg" alt="Plus-New" className={styles.btnNewImg} />
          </span>
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
              <div className={styles.storageBarProgress} />
              <a className={styles.storageInfo}>X GB of y GB used</a>
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
