import { useState } from "react";
import styles from "./JoinWorkspaceModal.module.css";
import MingcuteUserAdd2Line from "~icons/mingcute/user-add-2-line?width=20px&height=20px";
import { JoinWorkspaceModal } from "./JoinWorkspaceModal.jsx";

export const JoinWorkspaceModalButton = () => {
  const [showJoinWsModal, setShowJoinWsModal] = useState(false);
  return (
    <>
      <span
        className={styles.innerBtn}
        onClick={(e) => {
          e.stopPropagation();
          setShowJoinWsModal(true);
        }}
      >
        <MingcuteUserAdd2Line className={styles.innerBtn} />
      </span>

      {showJoinWsModal && (
        <JoinWorkspaceModal
          onClose={() => setShowJoinWsModal(!showJoinWsModal)}
        />
      )}
    </>
  );
};
