import React from "react";
import styles from "./FileEntry.module.css";

export const FileEntry = ({file, onClick, onShare, onMouseEnter, onMouseLeave}) => {

    return (
        <tr className={styles.fileEntry}
            onClick={onClick} onMouseEnter={onMouseEnter} onMouseLeave={onMouseLeave}>
            <td>{file.name}</td>
            <td>{file.uploadedAt}</td>
            <td>{file.size}</td>
            <td>{file.uploader}</td>
            <td>{file.workspaceID}</td>
            <td>
                <div className={styles.fileIcons}>
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            onShare()
                        }}
                    >
                        <img src="share.svg" className={styles.fileEntryIcon} alt="Share"/>
                    </button>
                    <img src="edit.svg" className={styles.fileEntryIcon} alt="Edit"/>
                    <img src="download.svg" className={styles.fileEntryIcon} alt="Download"/>
                    <img src="delete.svg" className={styles.fileEntryIcon} alt="Delete"/>
                </div>
            </td>
        </tr>
    )
}