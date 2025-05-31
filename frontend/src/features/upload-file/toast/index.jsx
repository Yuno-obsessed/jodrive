import styles from "./toast.module.css";

export const UploadToast = ({ t, progress }) => {
  return (
    <div
      className={styles.toastContainer}
      style={{ opacity: t.visible ? 1 : 0 }}
    >
      <div className={styles.toastTitle}>Uploading file...</div>
      <div className={styles.progressBarWrapper}>
        <div className={styles.progressBar} style={{ width: `${progress}%` }} />
      </div>
      <div className={styles.progressText}>{progress.toFixed(0)}%</div>
    </div>
  );
};
