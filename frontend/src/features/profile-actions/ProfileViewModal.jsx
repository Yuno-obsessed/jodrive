import useAuthStore from "../../util/authStore.js";
import styles from "./ProfileViewModal.module.css";

export const ProfileViewModal = ({ ref }) => {
  const { userInfo } = useAuthStore();
  return (
    <div className={styles.overlay}>
      <div ref={ref} className={styles.dropdown}>
        TODO: Profile View Modal
      </div>
    </div>
  );
};
