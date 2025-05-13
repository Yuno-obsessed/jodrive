import styles from "./MainLayout.module.css";

export const MainLayout = ({ sidebar, header, children }) => {
  return (
    <div className={styles.layout}>
      {header}
      <div className={styles.content}>
        {sidebar}
        <main className={styles.main}>{children}</main>
      </div>
    </div>
  );
};
