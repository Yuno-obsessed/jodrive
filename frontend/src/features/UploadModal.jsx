import React, { useState } from "react";
import styles from "./UploadModal.module.css";
import {
  getFileChunksToUpload,
  uploadBatchOfChunks,
  checkChunkExistence,
} from "../api/UploadFile.js";
import useAuthStore from "../util/authStore.js";
import { uploadChunksWithRetry, UploadObserver } from "../util/chunkUpload.js";

export const UploadModal = ({ onClose }) => {
  const [progress, setProgress] = useState(0);
  const { token } = useAuthStore();
  const [file, setFile] = useState(null);

  const handleUpload = async () => {
    if (!file) return alert("Select a file first");
    // divide file on chunks, calculate last chunk size
    const chunkList = await getFileChunksToUpload(file, token);
    // ask which chunks are missing and need to be uploaded
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

    // commit uploaded blocks
    await checkChunkExistence(
      chunks,
      file.name,
      chunkList.lastChunkSize,
      token,
    );
    onClose();
  };

  return (
    <div className={styles.overlay}>
      <div className={styles.modal}>
        <button className={styles.closeButton} onClick={onClose}>
          &times;
        </button>
        <h2>Upload File</h2>
        <input type="file" onChange={(e) => setFile(e.target.files[0])} />
        <button className={styles.uploadBtn} onClick={handleUpload}>
          Upload
        </button>
        {file && (
          <div className={styles.uploadBar}>
            <div
              className={styles.uploadBarProgress}
              style={{ width: `${progress}%` }}
            />
          </div>
        )}
      </div>
    </div>
  );
};
