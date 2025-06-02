import styles from "./MainLayout.module.css";
import useAuthStore from "../../util/authStore.js";
import { Outlet } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import { useEffect } from "react";

export const MainLayout = ({ sidebar, header }) => {
  const { authenticated, initKeycloak } = useAuthStore();

  useEffect(() => {
    if (!authenticated) {
      void initKeycloak();
    }
  }, []);

  if (!authenticated) {
    return <div>Loading...</div>;
  }

  return (
    <div className={styles.layout}>
      <div className={styles.content}>
        {authenticated && sidebar}
        <main className={styles.main}>
          {authenticated && header}
          <Outlet />
        </main>
      </div>
      <Toaster position="top-right" reverseOrder={false} />
    </div>
  );
};
