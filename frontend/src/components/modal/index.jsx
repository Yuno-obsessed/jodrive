import styles from "./modal.module.css";
import clsx from "clsx";

export const Modal = ({ children, className }) => {
  return (
    <div className={styles.overlay}>
      <div className={clsx(styles.modal, className)}>{children}</div>
    </div>
  );
};
