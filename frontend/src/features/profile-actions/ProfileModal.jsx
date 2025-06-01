import styles from "./ProfileModal.module.css";
import useAuthStore from "../../util/authStore.js";
import { Modal } from "../../components/ui/modal/index.jsx";
import MdiAccount from "~icons/mdi/account";
import { Button } from "../../components/ui/button/index.jsx";
import { changeUserAvatar, uploadPhoto } from "../../api/UserAPI.js";
import { useEffect, useState } from "react";
import { getUserInfo } from "../../api/UserInfo.js";

export const ProfileModal = ({ ref, onClose, targetUser }) => {
  const { userInfo, token } = useAuthStore();
  const [avatar, setAvatar] = useState(userInfo.avatarURL);
  const [resolvedTargetUser, setResolvedTargetUser] = useState(targetUser);

  const handleAvatarChange = async (file) => {
    const photoURL = await uploadPhoto(file, token)
      .then((res) => changeUserAvatar(userInfo.id, res, token))
      .catch(console.log);
    if (photoURL != null) {
      userInfo.avatarURL = photoURL;
      setAvatar(photoURL);
    }
  };

  useEffect(() => {
    if (targetUser.id !== userInfo.id) {
      console.log("ASDHJDS");
      getUserInfo(targetUser.id, token).then((res) => {
        setResolvedTargetUser(res);
      });
    } else {
      setResolvedTargetUser(targetUser);
    }
  }, [targetUser, userInfo, token]);

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
          <div className={styles.changeAvatar}></div>
        )}
        <div className={styles.userInfoItem}>
          <label>Username:</label>
          <a>{resolvedTargetUser.username}</a>
        </div>
        <div className={styles.userInfoItem}>
          <label>Email:</label>
          <a>{resolvedTargetUser.email}</a>
        </div>
        <div className={styles.userInfoItem}>
          <label>Subscription:</label>
          <a>{resolvedTargetUser?.subscription?.description}</a>
        </div>
      </div>
    </Modal>
  );
};
