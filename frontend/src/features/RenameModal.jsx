import { useState } from "react";
import styles from "./RenameModal.module.css";
import useAuthStore from "../util/authStore.js";
import { renameFile } from "../api/RenameFile.js";

export const RenameModal = ({ file, onClose }) => {
  const [newName, setNewName] = useState(file.filename);
  const { token } = useAuthStore();

  const handleRename = async () => {
    await renameFile(file, newName, token);
    onClose();
  };

  return (
    <div className={styles.overlay}>
      <div className={styles.modal}>
        <div className={styles.rename}>
          <h2>Rename</h2>
          <button className={styles.closeButton} onClick={onClose}>
            &times;
          </button>
        </div>
        <input
          type="text"
          placeholder="New name"
          value={newName}
          onChange={(e) => setNewName(e.target.value)}
        ></input>
        <div className={styles.btnRename}>
          <button onClick={onClose}>Cancel</button>
          <button onClick={handleRename}>OK</button>
        </div>
      </div>
    </div>
  );
};
