import VscodeIconsFileTypePdf2 from "~icons/vscode-icons/file-type-pdf2";
import FluentColorImage16 from "~icons/fluent-color/image-16";
import TablerFileUnknown from "~icons/tabler/file-unknown";
import MaterialIconThemeVideo from "~icons/material-icon-theme/video";
import VscodeIconsFileTypeWord from "~icons/vscode-icons/file-type-word";
import VscodeIconsFileTypeExcel from "~icons/vscode-icons/file-type-excel";
import FlatColorIconsMusic from "~icons/flat-color-icons/music";
import TablerFolder from "~icons/tabler/folder";
import styles from "./filenameUtils.module.css";

export const getFilenameWithIcon = (filename) => {
  const getIcon = () => {
    if (filename[filename.length - 1] === "/") {
      return <TablerFolder />;
    }
    switch (filename.substring(filename.lastIndexOf(".") + 1)) {
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
      default:
        return <TablerFileUnknown />;
    }
  };
  return (
    <span className={styles.filename}>
      {getIcon()}
      <a>{filename}</a>
    </span>
  );
};
