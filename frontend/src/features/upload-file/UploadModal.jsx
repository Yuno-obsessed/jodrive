import { useState } from "react";
import styles from "./UploadModal.module.css";
import {
  getFileChunksToUpload,
  checkChunkExistence,
} from "../../api/UploadFile.js";
import useAuthStore from "../../util/authStore.js";
import {
  uploadChunksWithRetry,
  UploadObserver,
} from "../../util/chunkUpload.js";
import { Modal } from "../../components/modal/index.jsx";
import { Button } from "../../components/ui/button/index.jsx";

export const UploadModal = ({ onClose }) => {
  const [progress, setProgress] = useState(0);
  const { token } = useAuthStore();
  const [file, setFile] = useState(null);

  const handleUpload = async () => {
    if (!file) return alert("Select a file first");
    const chunkList = await getFileChunksToUpload(file, token);
    const chunks = await checkChunkExistence(
      chunkList.chunks,
      file.name,
      chunkList.lastChunkSize,
      token,
    );

    const total = chunks.length;
    console.log(`Got ${total} to upload`);
    const observer = new UploadObserver();
    let uploaded = 0;

    observer.subscribe(({ type, count }) => {
      if (type === "batchUploaded") {
        uploaded += count;
        setProgress((uploaded / total) * 100);
      }
    });

    await uploadChunksWithRetry({
      chunks,
      token,
      batchSize: 5,
      poolSize: 8,
      maxRetries: 3,
      observer,
    });

    await checkChunkExistence(
      chunks,
      file.name,
      chunkList.lastChunkSize,
      token,
    );
    onClose();
  };

  return (
    <Modal className={styles.modal}>
      <Button className={styles.closeButton} onClick={onClose}>
        &times;
      </Button>
      <h2>Upload File</h2>
      <div className={styles.uploadBtn}>
        <input type="file" onChange={(e) => setFile(e.target.files[0])} />
      </div>
      <Button variant={"submit"} onClick={onClose}>
        Upload
      </Button>
      {file && (
        <div className={styles.uploadBar}>
          <div
            className={styles.uploadBarProgress}
            style={{ width: `${progress}%` }}
          />
        </div>
      )}
    </Modal>
  );
};
