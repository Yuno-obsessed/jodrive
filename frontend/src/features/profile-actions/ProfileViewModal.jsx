import { Button } from "../../components/ui/button/index.jsx";
import useAuthStore from "../../util/authStore.js";
import styles from "./ProfileViewModal.module.css";

export const ProfileViewModal = () => {
  const { userInfo } = useAuthStore();
  return <div className={styles.profile}></div>;
};
