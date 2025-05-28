import { useState } from "react";
import styles from "./UploadModal.module.css";
import {
  checkChunkExistence,
  getFileChunksToUpload,
} from "../../api/UploadFile.js";
import useAuthStore from "../../util/authStore.js";
import {
  uploadChunksWithRetry,
  UploadObserver,
} from "../../util/chunkUpload.js";
import { Modal } from "../../components/ui/modal/index.jsx";
import { Button } from "../../components/ui/button/index.jsx";
import { useSearchModel } from "../../enitites/file/model/index.js";
import { Workspaces } from "../../widgets/workspaces/Workspaces.jsx";
import { useWorkspacesModel } from "../../enitites/workspace/model/index.js";
import { useSyncFilesystemPath } from "../../shared/fs-dir/hook.js";
import { useFilesystemStore } from "../../shared/fs-dir/index.js";
import { useParams } from "react-router-dom";
import { useTreeModel } from "../../enitites/file-tree/model/index.js";

export const UploadModal = ({ onClose }) => {
  const [progress, setProgress] = useState(0);
  const { token } = useAuthStore();
  const { addSearchResult } = useSearchModel();
  const { addFile } = useTreeModel();
  const [file, setFile] = useState(null);
  const { activeWorkspace } = useWorkspacesModel();
  const { id } = useParams();
  useSyncFilesystemPath(); // sync filesystem vars
  const { currentPath, basePath } = useFilesystemStore();

  const handleUpload = async () => {
    let isTreePage = false;
    const chunkList = await getFileChunksToUpload(file, token);
    let filename = "/" + file.name;
    if (basePath.startsWith("/workspace") && id == activeWorkspace.id) {
      isTreePage = true;
      filename = currentPath + file.name;
    }
    let workspaceID = activeWorkspace.id;
    console.log(`Uploading file to a workspace ${workspaceID}`);
    const metadata = await checkChunkExistence(
      workspaceID,
      chunkList.chunks,
      filename,
      chunkList.lastChunkSize,
      token,
    );

    console.log(metadata);
    if (metadata.chunks === null || metadata.chunks.length === 0) {
      console.log("File already exists");
      if (isTreePage) {
        addFile(metadata.fileInfo);
      } else {
        addSearchResult(metadata.fileInfo);
      }
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
      workspaceID,
      chunkList.chunks,
      filename,
      chunkList.lastChunkSize,
      token,
    );

    if (!committedMetadata.chunks || committedMetadata.chunks.length === 0) {
      console.log(`File ${filename} was uploaded`);
      if (isTreePage) {
        addFile(metadata.fileInfo);
      } else {
        addSearchResult(metadata.fileInfo);
      }
      onClose();
    }
  };

  return (
    <Modal
      title={"Upload File"}
      onClose={onClose}
      className={styles.modal}
      description={"You need to select a workspace where to upload"}
    >
      <div className={styles.workspaces}>
        <Workspaces />
      </div>

      <Button variant="ghost" className={styles.uploadBtn}>
        <label htmlFor="upload">Choose a file to upload</label>
        <input
          id="upload"
          type="file"
          onChange={(e) => setFile(e.target.files[0])}
        />
      </Button>
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
