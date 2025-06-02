import { useSearchParams } from "react-router-dom";
import styles from "./FileInfoPage.module.css";
import { useEffect, useState } from "react";
import { getFileInfo } from "../../api/GetFileInfo.js";
import { getFilenameWithIcon } from "../../util/filenameUtils.jsx";
import { formatByteSize } from "../../util/fileUtils.js";
import TablerDownload from "~icons/tabler/download";
import { Button } from "../../components/ui/button/index.jsx";
import { downloadFile } from "../../api/DownloadFile.js";

export const FileInfoPage = () => {
  const [searchParams] = useSearchParams();
  const [file, setFile] = useState(null);
  const [versions, setVersions] = useState([]);
  const [selectedVersion, setSelectedVersion] = useState(1);

  const link = searchParams.get("link");

  useEffect(() => {
    if (!file) {
      getFileInfo({ link: link, listVersions: true })
        .then((res) => {
          setFile(res);
          setVersions(res.versions);
        })
        .catch(console.log);
    } else if (selectedVersion) {
      getFileInfo({
        link: link,
        version: selectedVersion.version,
      })
        .then((res) => setFile(res))
        .catch(console.log);
    }
  }, [link, selectedVersion]);

  const handleDownload = () => {
    downloadFile(file, null, link).then(console.log).catch(console.log);
  };

  if (!file) {
    return <></>;
  }
  return (
    <div className={styles.file}>
      <div className={styles.filename}>
        {getFilenameWithIcon(file.name, 50)}
        <Button
          variant="icon"
          title="Download"
          onClick={() => handleDownload()}
        >
          <TablerDownload />
        </Button>
      </div>
      <div className={styles.fileInfo}>
        <div className={styles.fileInfoEntry}>
          <label>Uploader:</label>
          <a>{file.uploaderName}</a>
        </div>
        <div className={styles.fileInfoEntry}>
          <label>Size:</label>
          <a>{formatByteSize(file.size)}</a>
        </div>
        <div className={styles.fileInfoEntry}>
          <label>Uploaded at:</label>
          <a>{file.uploadedAt}</a>
        </div>
      </div>
      {versions && (
        <>
          <p className={styles.versions}>
            Select a version of file to display:
          </p>
          <div className={styles.radioUnits}>
            {versions.map((version) => (
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
        </>
      )}
    </div>
  );
};
