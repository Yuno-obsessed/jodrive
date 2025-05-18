import { useEffect, useState } from "react";
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
import { useSearchModel } from "../../enitites/file/model/index.js";
import { getDirectories } from "../../api/DirectoryAPI.js";
import clsx from "clsx";
import { Workspaces } from "../../widgets/workspaces/SelectWorkspacePage.jsx";

export const UploadModal = ({ onClose }) => {
  const [progress, setProgress] = useState(0);

  const { token } = useAuthStore();
  const { addSearchResult } = useSearchModel();
  const [file, setFile] = useState(null);

  const handleUpload = async () => {
    const chunkList = await getFileChunksToUpload(file, token);
    let filename = "/" + file.name;
    const metadata = await checkChunkExistence(
      chunkList.chunks,
      filename,
      chunkList.lastChunkSize,
      token,
    );

    console.log(metadata);
    if (metadata.chunks === null || metadata.chunks.length === 0) {
      console.log("File already exists");
      addSearchResult(metadata.fileInfo);
      onClose();
    }

    const total = metadata.chunks.length;
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
      chunks: metadata.chunks,
      token,
      batchSize: 2,
      poolSize: 8,
      maxRetries: 3,
      observer,
    });

    const committedMetadata = await checkChunkExistence(
      chunkList.chunks,
      filename,
      chunkList.lastChunkSize,
      token,
    );

    if (!committedMetadata.chunks || committedMetadata.chunks.length === 0) {
      console.log("File was uploaded");
      addSearchResult(metadata.fileInfo);
      onClose();
    }
  };

  return (
    <Modal title={"Upload File"} onClose={onClose} className={styles.modal}>
      <div className={styles.workspaces}>
        <Workspaces />
      </div>
      <div className={styles.uploadBtn}>
        <input type="file" onChange={(e) => setFile(e.target.files[0])} />
      </div>
      <Button variant={"ghost"} onClick={() => handleUpload()}>
        <p> Upload</p>
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
