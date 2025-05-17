import styles from "./MainLayout.module.css";

export const MainLayout = ({ sidebar, header, children }) => {
  return (
    <div className={styles.layout}>
      <div className={styles.content}>
        {sidebar}
        <main className={styles.main}>
          {header}
          {children}
        </main>
      </div>
    </div>
  );
};
