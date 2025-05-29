import { CreateWorkspaceModal } from "./CreateWorkspaceModal.jsx";
import { useState } from "react";
import styles from "./CreateWorkspaceModal.module.css";
import LucidePlus from "~icons/lucide/plus";

export const CreateWorkspaceModalButton = () => {
  const [showCreateWsModal, setShowCreateWsModal] = useState(false);
  return (
    <>
      <span
        className={styles.innerBtn}
        onClick={(e) => {
          e.stopPropagation();
          setShowCreateWsModal(true);
        }}
      >
        <LucidePlus className={styles.innerBtn} />
      </span>

      {showCreateWsModal && (
        <CreateWorkspaceModal
          onClose={() => setShowCreateWsModal(!showCreateWsModal)}
        />
      )}
    </>
  );
};
