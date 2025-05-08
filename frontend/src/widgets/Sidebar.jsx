import React from "react";
import styles from "./Sidebar.module.css";

export const Sidebar = () => {
  return (
    <aside className={styles.sidebar}>
      {/*<aside className="w-50 bg-white border-r p-4">*/}
      {/*<div className="mb-5">*/}
      <button className={styles.btnNew}>
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
          <img src="cloud.svg" alt="Storage" className={styles.sidebarImages} />
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
  );
};

export default Sidebar;
