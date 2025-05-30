import { useState } from "react";
import styles from "./RenameModal.module.css";
import useAuthStore from "../../util/authStore.js";
import { updateFile } from "../../api/UpdateFile.js";
import { Modal } from "../../components/ui/modal/index.jsx";
import { Input } from "../../components/ui/input/index.jsx";
import { Button } from "../../components/ui/button/index.jsx";
import { useTreeModel } from "../../enitites/file-tree/model/index.js";

export const RenameModal = ({ file, onClose }) => {
  const getShortenedFileName = () => {
    return file.isDirectory
      ? file.name
      : file.name.substring(0, file.name.lastIndexOf("."));
  };
  const [newName, setNewName] = useState(getShortenedFileName);
  const { addFile, removeFile } = useTreeModel();
  const { token } = useAuthStore();

  const handleRename = async () => {
    let changedName = await updateFile(file, newName, null, token);
    let changedFile = { ...file, name: changedName };
    removeFile(file.id);
    addFile(changedFile);
    onClose();
  };

  return (
    <Modal
      title={"Rename"}
      description="Enter a new name for a file"
      onClose={onClose}
    >
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
        <Button variant={"ghost"} onClick={handleRename}>
          OK
        </Button>
      </div>
    </Modal>
  );
};
