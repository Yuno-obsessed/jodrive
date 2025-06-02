import { useState } from "react";
import authStore from "../../util/authStore.js";
import { useWorkspacesModel } from "../../enitites/workspace/model/index.js";
import { joinWorkspace } from "../../api/WorkspaceAPI.js";
import { Modal } from "../../components/ui/modal/index.jsx";
import styles from "../create-ws/CreateWorkspaceModal.module.css";
import { Input } from "../../components/ui/input/index.jsx";
import { Button } from "../../components/ui/button/index.jsx";

export const JoinWorkspaceModal = ({ onClose }) => {
  const { token } = authStore();
  const [link, setLink] = useState("");
  const [error, setError] = useState("");
  const { addWorkspace } = useWorkspacesModel();

  const handleJoin = async () => {
    try {
      await joinWorkspace(link, token)
        .then((res) => addWorkspace(res))
        .catch((err) => {
          console.log(err);
          setError(err);
        });
      onClose();
    } catch (error) {
      console.error(error);
      onClose();
    }
  };

  return (
    <>
      <Modal
        title={"Join Workspace"}
        onClose={onClose}
        className={styles.modal}
      >
        <Input
          className={styles.inputs}
          name="invitationLink"
          type="text"
          placeholder="Paste your invitation link"
          onChange={(e) => setLink(e.target.value)}
          required
        />
        <Button variant="ghost" type="submit" onClick={() => handleJoin()}>
          <p>Join</p>
        </Button>
      </Modal>
      {error !== "" && (
        <Modal title="Error" onClose={onClose} className={styles.modal}>
          <p>User is already present in workspace</p>
        </Modal>
      )}
    </>
  );
};
