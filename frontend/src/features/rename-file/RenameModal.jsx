import { useState } from "react";
import styles from "./RenameModal.module.css";
import useAuthStore from "../../util/authStore.js";
import { updateFile } from "../../api/UpdateFile.js";
import { Modal } from "../../components/ui/modal/index.jsx";
import { Input } from "../../components/ui/input/index.jsx";
import { Button } from "../../components/ui/button/index.jsx";

export const RenameModal = ({ file, onClose }) => {
  console.log(file);
  const [newName, setNewName] = useState(file?.name);
  const { token } = useAuthStore();

  const handleRename = async () => {
    await updateFile(file, newName, null, token);
    onClose();
  };

  return (
    <Modal title={"Rename"} onClose={onClose}>
      <Input
        type="text"
        placeholder="New name"
        value={newName}
        onChange={(e) => setNewName(e.target.value)}
      />
      <div className={styles.btnRename}>
        <Button variant={"destruction"} onClick={onClose}>
          Cancel
        </Button>
        <Button variant={"submit"} onClick={handleRename}>
          OK
        </Button>
      </div>
    </Modal>
  );
};
