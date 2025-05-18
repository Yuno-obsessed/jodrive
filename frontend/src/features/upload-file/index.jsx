import { Button } from "../../components/ui/button/index.jsx";
import { UploadModal } from "./UploadModal.jsx";
import { useState } from "react";
import styles from "./UploadModal.module.css";

export const UploadModalButton = () => {
  const [showUploadModal, setShowUploadModal] = useState(false);
  return (
    <div className={styles.modalButton}>
      <Button onClick={() => setShowUploadModal(true)} variant={"ghost"}>
        Upload File
      </Button>
      {showUploadModal && (
        <UploadModal onClose={() => setShowUploadModal(!showUploadModal)} />
      )}
    </div>
  );
};
