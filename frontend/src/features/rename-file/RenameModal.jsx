import { useState } from "react";
import styles from "./RenameModal.module.css";
import useAuthStore from "../../util/authStore.js";
import { updateFile } from "../../api/UpdateFile.js";
import { Modal } from "../../components/ui/modal/index.jsx";
import { Input } from "../../components/ui/input/index.jsx";
import { Button } from "../../components/ui/button/index.jsx";

export const RenameModal = ({ file, onClose, renameFile }) => {
  const getShortenedFileName = () => {
    return file.isDirectory
      ? file.name.substring(0, file.name.lastIndexOf("/"))
      : file.name.substring(0, file.name.lastIndexOf("."));
  };
  const [newName, setNewName] = useState(getShortenedFileName);
  const { token } = useAuthStore();

  const fileType = () => {
    return file.isDirectory ? "directory" : "file";
  };

  const handleRename = async () => {
    let changedName = await updateFile(file, newName, null, token);
    renameFile(file, changedName);
    onClose();
  };

  return (
    <Modal
      title={"Rename"}
      description={`Enter a new name for a ${fileType()}`}
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
