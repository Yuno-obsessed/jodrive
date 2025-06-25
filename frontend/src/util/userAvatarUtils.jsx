import MdiAccount from "~icons/mdi/account";
import styles from "./userAvatarUtils.module.css";

export const getUserAvatar = (row) => {
  return (
    <span title={row.uploaderName}>
      {row.uploaderAvatar ? (
        <img
          alt="uploaderAvatar"
          src={row.uploaderAvatar}
          className={styles.userImg}
        />
      ) : (
        <MdiAccount className={styles.userImg} />
      )}
    </span>
  );
};
