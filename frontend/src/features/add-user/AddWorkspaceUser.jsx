import authStore from "../../util/authStore.js";
import { Modal } from "../../components/ui/modal/index.jsx";
import styles from "./AddWorkspaceUser.module.css";
import { Button } from "../../components/ui/button/index.jsx";
import { createLink } from "../../api/WorkspaceAPI.js";
import { useState } from "react";

export const AddWorkspaceUserModal = ({ wsID, onClose }) => {
  const { token } = authStore();
  const [copied, setCopied] = useState(false);

  const generateLink = () => {
    createLink(wsID, token)
      .then((res) => {
        console.log(res);
        navigator.clipboard.writeText(res).then(() => {
          setCopied(true);
          setTimeout(() => {
            setCopied(false);
            onClose();
          }, 1000);
        });
      })
      .catch(console.log);
  };
  return (
    <Modal
      title={"Add Workspace User"}
      description="Pass this link to user you want to add to workspace"
      onClose={onClose}
      className={styles.modal}
    >
      <Button variant={"ghost"} onClick={() => generateLink()}>
        <p>Generate link</p>
      </Button>
      {copied && (
        <p className={styles.copiedMessage}>✅ Link copied to clipboard</p>
      )}
    </Modal>
  );
};
