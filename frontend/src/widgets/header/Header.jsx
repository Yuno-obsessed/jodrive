import styles from "./Header.module.css";
import useAuthStore from "../../util/authStore.js";
import { ProfileDropDownButton } from "../../features/profile-actions/index.jsx";
import MdiAccount from "~icons/mdi/account";
import { SearchBar } from "../../features/search-bar/SearchBar.jsx";

export const Header = () => {
  const { userInfo } = useAuthStore();
  return (
    <header className={styles.headerMain}>
      <SearchBar />
      <div className={styles.user}>
        <ProfileDropDownButton
          children={
            userInfo.avatarURL ? (
              <img
                alt="profile"
                src={userInfo.avatarURL}
                className={styles.userImg}
              />
            ) : (
              <MdiAccount className={styles.userImg} />
            )
          }
        ></ProfileDropDownButton>
      </div>
    </header>
  );
};
