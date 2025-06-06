import VscodeIconsFileTypePdf2 from "~icons/vscode-icons/file-type-pdf2";
import FluentColorImage16 from "~icons/fluent-color/image-16";
import TablerFileUnknown from "~icons/tabler/file-unknown";
import MaterialIconThemeVideo from "~icons/material-icon-theme/video";
import VscodeIconsFileTypeWord from "~icons/vscode-icons/file-type-word";
import VscodeIconsFileTypeExcel from "~icons/vscode-icons/file-type-excel";
import FlatColorIconsMusic from "~icons/flat-color-icons/music";
import VscodeIconsFileTypeJson from "~icons/vscode-icons/file-type-json";
import TablerFolder from "~icons/tabler/folder";
import styles from "./filenameUtils.module.css";

export const getFilenameWithIcon = (filename, maxText) => {
  const shortenText = (maxLength) => {
    if (!filename) return "";
    if (filename.length <= maxLength) return filename;
    return filename.slice(0, maxLength - 3) + "...";
  };

  const getIcon = () => {
    if (!filename) return;
    if (filename[filename.length - 1] === "/") {
      return <TablerFolder />;
    }
    switch (filename.substring(filename.lastIndexOf(".") + 1).toLowerCase()) {
      case "mp4":
        return <MaterialIconThemeVideo />;
      case "png":
      case "jpg":
      case "jpeg":
        return <FluentColorImage16 />;
      case "pdf":
        return <VscodeIconsFileTypePdf2 />;
      case "docx":
      case "doc":
        return <VscodeIconsFileTypeWord />;
      case "xlsx":
        return <VscodeIconsFileTypeExcel />;
      case "mp3":
      case "ogg":
      case "m4a":
        return <FlatColorIconsMusic />;
      case "json":
        return <VscodeIconsFileTypeJson />;
      default:
        return <TablerFileUnknown />;
    }
  };
  return (
    <span title={filename} className={styles.filename}>
      {getIcon()}
      <a>{shortenText(maxText)}</a>
    </span>
  );
};
