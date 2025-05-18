import React from "react";
import { SearchBar } from "../../features/search-bar/SearchBar.jsx";
import styles from "./Header.module.css";
import MaterialSymbolsLogoutSharp from "~icons/material-symbols/logout-sharp";
import MdiUser from "~icons/mdi/user";
import useAuthStore from "../../util/authStore.js";
import { Button } from "../../components/ui/button/index.jsx";
import { ProfileViewModalButton } from "../../features/profile-actions/index.jsx";

export const Header = () => {
  const { userInfo, logout } = useAuthStore();

  return (
    <header className={styles.headerMain}>
      <SearchBar />
      <div className={styles.user}>
        <ProfileViewModalButton
          children={
            userInfo.avatarURL ? (
              <img
                alt="profile"
                src={userInfo.avatarURL}
                className={styles.userImg}
              />
            ) : (
              <MdiUser className={styles.userImg} />
            )
          }
        ></ProfileViewModalButton>
        <Button variant="icon" onClick={logout}>
          <MaterialSymbolsLogoutSharp className={styles.userImg} />
        </Button>
      </div>
    </header>
  );
};
