import { useState } from "react";
import styles from "./Sidebar.module.css";
import useAuthStore from "../../util/authStore.js";
import { UploadModal } from "../../features/upload-file/UploadModal.jsx";
import { Button } from "../../components/ui/button/index.jsx";

const navigation = [
  {
    name: "Home",
    icon: "home",
    link: "/frontend/public",
  },
  {
    name: "My Drive",
    icon: "drive",
    link: "/my-drive",
  },
];

export const Sidebar = () => {
  const [showUploadModal, setShowUploadModal] = useState(false);
  const { userInfo } = useAuthStore();

  const mapToGB = (bytes) => {
    const gb = bytes / 1_000_000_000;
    return parseFloat(gb.toFixed(2));
  };

  console.log(userInfo);
  let usedStorage = userInfo.statistics
    .filter((s) => s.quota === "USER_STORAGE_USED")
    .map((s) => mapToGB(s.value));
  let storageLimit = mapToGB(userInfo.subscription.storageLimit);

  const calculateStorageUsagePercentage = () => {
    return (storageLimit / 100) * usedStorage;
  };

  return (
    <>
      <aside className={styles.sidebar}>
        <Button
          className={styles.btnNew}
          onClick={() => setShowUploadModal(true)}
        >
          <img src="plus.svg" alt="Plus-New" className={styles.btnNewImg} />
          <span className={styles.btnNewText}>
            <a>New</a>
          </span>
        </Button>
        <ul className={styles.sidebarList}>
          {navigation.map((item) => (
            <Button className={styles.sidebarEl}>
              <img
                src={item.icon + ".svg"}
                alt="Home"
                className={styles.sidebarImages}
              />
              <p>{item.name}</p>
            </Button>
          ))}

          <li>
            <div className={styles.storageBar}>
              <div
                className={styles.storageBarProgress}
                style={{ width: `${calculateStorageUsagePercentage()}%` }}
              />
              <a className={styles.storageInfo}>
                {usedStorage} GB of {storageLimit} GB used
              </a>
            </div>
          </li>
        </ul>
      </aside>
      {showUploadModal && (
        <UploadModal onClose={() => setShowUploadModal(false)} />
      )}
    </>
  );
};

export default Sidebar;
