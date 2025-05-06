import React from "react";
import styles from "./FileEntry.module.css";
import {METADATA_URI} from "../consts/Constants.js";

export const FileEntry = ({file}) => {
    async function share() {
        const response = await fetch(`${METADATA_URI}/file/${file.id}/share?wsID=1&timeUnit=SECONDS&expiresIn=30`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        let jsonResponse;
        try {
            jsonResponse = await response.json();
        } catch (error) {
            console.error("Failed to parse JSON response:", error);
            throw new Error("Failed to parse JSON response");
        }

        if (!response.ok) {
            console.error("Response error:", jsonResponse.message);
            throw new Error(jsonResponse.message || "Error in response");
        }

    }
    return (
        <tr className={styles.fileEntry}>
            <td>{file.name}</td>
            <td>{file.uploadedAt}</td>
            <td>{file.size}</td>
            <td>{file.uploader}</td>
            <td>
                <div className={styles.fileIcons}>
                    <button onClick={(e) => share()}>
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