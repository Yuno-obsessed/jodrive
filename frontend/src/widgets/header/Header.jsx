import styles from "./Header.module.css";
import useAuthStore from "../../util/authStore.js";
import { ProfileModalButton } from "../../features/profile-actions/index.jsx";
import MdiAccount from "~icons/mdi/account";
import MaterialSymbolsLogoutSharp from "~icons/material-symbols/logout-sharp";
import { SearchBar } from "../../features/search-bar/SearchBar.jsx";
import { Button } from "../../components/ui/button/index.jsx";

export const Header = () => {
  const { userInfo, logout } = useAuthStore();
  return (
    <header className={styles.headerMain}>
      <SearchBar />
      <div className={styles.user}>
        <ProfileModalButton
          currentUser={userInfo}
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
        ></ProfileModalButton>
        <Button variant="icon" onClick={logout}>
          <MaterialSymbolsLogoutSharp className={styles.userImg} />
        </Button>
      </div>
    </header>
  );
};
