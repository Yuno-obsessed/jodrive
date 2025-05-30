import { Modal } from "../../components/ui/modal/index.jsx";
import { Button } from "../../components/ui/button/index.jsx";
import { useState } from "react";
import { createDirectory } from "../../api/DirectoryAPI.js";
import authStore from "../../util/authStore.js";
import styles from "./CreateDirectoryModal.module.css";
import { Input } from "../../components/ui/input/index.jsx";
import { useTreeModel } from "../../enitites/file-tree/model/index.js";

export const CreateDirectoryModal = ({ path, wsID, onClose }) => {
  const [newDir, setNewDir] = useState(null);
  const { addFile } = useTreeModel();
  const { token } = authStore();

  return (
    <Modal
      title={"Create Directory"}
      onClose={onClose}
      className={styles.modal}
    >
      <Input
        type="text"
        placeholder="Enter new directory name"
        onChange={(e) => setNewDir(e.target.value)}
      />
      <Button
        variant={"ghost"}
        onClick={() => {
          createDirectory(
            { workspaceID: wsID, path: path, name: newDir },
            token,
          )
            .then((res) => {
              addFile(res);
              onClose();
            })
            .catch(console.log);
        }}
      >
        <p>Create</p>
      </Button>
    </Modal>
  );
};
