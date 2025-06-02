import { Modal } from "../../components/ui/modal/index.jsx";
import { Button } from "../../components/ui/button/index.jsx";
import authStore from "../../util/authStore.js";
import styles from "./CreateWorkspaceModal.module.css";
import { Input } from "../../components/ui/input/index.jsx";
import { useRef } from "react";
import { createWorkspace } from "../../api/WorkspaceAPI.js";
import { useWorkspacesModel } from "../../enitites/workspace/model/index.js";

export const CreateWorkspaceModal = ({ onClose }) => {
  const formRef = useRef();
  const { token } = authStore();
  const { addWorkspace } = useWorkspacesModel();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData(formRef.current);

    const name = formData.get("workspaceName");
    const description = formData.get("workspaceDescription");
    console.log(name, description);
    try {
      await createWorkspace({ name, description }, token)
        .then((res) => addWorkspace(res))
        .catch(console.log);
      onClose();
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <Modal
      title={"Create Workspace"}
      description="Enter needed data to create workspace"
      onClose={onClose}
      className={styles.modal}
    >
      <form ref={formRef} onSubmit={handleSubmit}>
        <Input
          className={styles.inputs}
          name="workspaceName"
          type="text"
          placeholder="Enter a new workspace name"
          required
        />
        <Input
          className={styles.inputs}
          name="workspaceDescription"
          type="text"
          placeholder="Enter a new workspace description"
        />
        <Button variant="ghost" type="submit">
          <p>Create</p>
        </Button>
      </form>
    </Modal>
  );
};
