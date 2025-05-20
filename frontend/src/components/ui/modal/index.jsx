import styles from "./modal.module.css";
import clsx from "clsx";

export const Modal = ({ children, className, onClose, title, ref }) => {
  return (
    <div className={styles.overlay} ref={ref}>
      <div className={clsx(styles.modal, className)}>
        <div className={styles.name}>
          <h2>{title}</h2>
          <button className={styles.closeButton} onClick={onClose}>
            &times;
          </button>
        </div>
        {children}
      </div>
    </div>
  );
};
