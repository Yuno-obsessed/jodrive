import Sidebar from "../widgets/Sidebar.jsx";
import styles from "./MainLayout.module.css";
import React from "react";
import { Header } from "../widgets/Header.jsx";

export const MainLayout = ({ children }) => {
  return (
    <div className={styles.layout}>
      <Header />
      <div className={styles.content}>
        <Sidebar />
        <main className={styles.main}>{children}</main>
      </div>
    </div>
  );
};
