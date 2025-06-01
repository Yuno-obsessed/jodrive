import { Modal } from "../components/ui/modal/index.jsx";
import { downloadFile } from "../api/DownloadFile.js";
import { getFileInfo } from "../api/GetFileInfo.js";
import { useState, forwardRef, useImperativeHandle } from "react";
import { Button } from "../components/ui/button/index.jsx";
import styles from "./FileDownloader.module.css";

export const FileDownloader = forwardRef(({ token }, ref) => {
  const [showModal, setShowModal] = useState(false);
  const [info, setInfo] = useState(null);
  const [selectedVersion, setSelectedVersion] = useState(1);

  useImperativeHandle(ref, () => ({
    async download(fileToDownload) {
      const infoRes = await getFileInfo(
        {
          wsID: fileToDownload.workspaceID,
          id: fileToDownload.id,
          listVersions: true,
        },
        token,
      );
      console.log(infoRes);
      if (infoRes.versions && infoRes.versions.length > 1) {
        console.log("VERSIONS");
        setInfo(infoRes);
        setShowModal(true);
      } else {
        await downloadFile(infoRes, token);
      }
    },
  }));

  const handleDownloadVersion = async () => {
    console.log(selectedVersion);
    console.log(info);
    info.id = selectedVersion.fileID;
    console.log(info);
    await downloadFile(info, token);
    setShowModal(false);
  };

  return (
    <>
      {showModal && (
        <Modal
          className={styles.downloadModal}
          title="Choose Version"
          onClose={() => setShowModal(false)}
        >
          <p>This file has multiple versions. </p>
          <p>Choose which one to download</p>
          <div className={styles.radioUnits}>
            {info.versions.map((version) => (
              <label key={version.version}>
                <input
                  type="radio"
                  name="versionSelection"
                  value={version.version}
                  checked={selectedVersion === version}
                  onChange={() => setSelectedVersion(version)}
                />
                <a>{version.version}</a>
              </label>
            ))}
          </div>
          <div className={styles.btns}>
            <Button variant="ghost" onClick={handleDownloadVersion}>
              Download
            </Button>
            <Button variant="destruction" onClick={() => setShowModal(false)}>
              Cancel
            </Button>
          </div>
        </Modal>
      )}
    </>
  );
});
