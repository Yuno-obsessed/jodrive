import styles from "./Header.module.css";
import useAuthStore from "../../util/authStore.js";
import { ProfileDropDownButton } from "../../features/profile-actions/index.jsx";
import MdiAccount from "~icons/mdi/account?width=42px&height=42px";
import { SearchBar } from "../../features/search-bar/SearchBar.jsx";

export const Header = () => {
  const { userInfo } = useAuthStore();
  return (
    <header className={styles.headerMain}>
      <SearchBar />
      <ProfileDropDownButton
        children={userInfo ? <MdiAccount /> : <div></div>}
      />
    </header>
  );
};
