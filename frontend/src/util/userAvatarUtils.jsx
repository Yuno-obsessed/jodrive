import MdiAccount from "~icons/mdi/account";
import styles from "./userAvatarUtils.module.css";

export const getUserAvatar = (row) => {
  return (
    <span title={row.uploaderName}>
      {row.avatarURL ? (
        <img
          alt="uploaderAvatar"
          src={row.avatarURL}
          className={styles.userImg}
        />
      ) : (
        <MdiAccount className={styles.userImg} />
      )}
    </span>
  );
};
