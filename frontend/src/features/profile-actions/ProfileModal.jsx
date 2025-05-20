import styles from "./ProfileModal.module.css";
import useAuthStore from "../../util/authStore.js";
import { Modal } from "../../components/ui/modal/index.jsx";
import MdiAccount from "~icons/mdi/account";
import { Button } from "../../components/ui/button/index.jsx";
import { changeUserAvatar, uploadPhoto } from "../../api/UserAPI.js";
import { useState } from "react";

export const ProfileModal = ({ ref, onClose, targetUser }) => {
  const { userInfo, token } = useAuthStore();
  const [avatar, setAvatar] = useState(userInfo.avatarURL);

  const handleAvatarChange = async (file) => {
    const photoURL = await uploadPhoto(file, token)
      .then((res) => changeUserAvatar(userInfo.id, res, token))
      .catch(console.log);
    if (photoURL != null) {
      userInfo.avatarURL = photoURL;
      setAvatar(photoURL);
    }
  };

  console.log(userInfo);
  console.log(avatar);
  return (
    <Modal title="Profile" ref={ref} onClose={onClose}>
      <div className={styles.userInfo}>
        {avatar ? <img alt="profile" src={avatar} /> : <MdiAccount />}
        {targetUser.id === userInfo.id ? (
          <Button variant="ghost" className={styles.changeAvatar}>
            <label htmlFor="avatar">Change avatar</label>
            <input
              id="avatar"
              type="file"
              onChange={(e) => handleAvatarChange(e.target.files[0])}
            />
          </Button>
        ) : (
          <></>
        )}
        <a className={styles.userInfoItem}>{targetUser.username}</a>
        <a className={styles.userInfoItem}>{targetUser.email}</a>
        <a className={styles.userInfoItem}>
          {targetUser.subscription.description}
        </a>
      </div>
    </Modal>
  );
};
