import styles from "./ProfileDropDown.module.css";
import useAuthStore from "../../util/authStore.js";

export const ProfileDropDown = ({ ref }) => {
  const { userInfo } = useAuthStore();
  console.log(userInfo);
  return (
    <div className={styles.overlay}>
      <div ref={ref} className={styles.dropdown}>
        <h3>{userInfo.username}</h3>
      </div>
    </div>
  );
};
