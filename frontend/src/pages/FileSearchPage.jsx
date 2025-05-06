import styles from './FileSearchPage.module.css'
import React from "react";
import {FileEntry} from "../components/FileEntry.jsx";

export const FileSearchPage = () => {
    return (
        <>
            <table className={styles.filesList}>
                <thead>
                    <tr className={styles.columns}>
                        <th>Name</th>
                        <th>Updated at</th>
                        <th>Uploader</th>
                    </tr>
                </thead>
                <tbody>
                    <FileEntry file={{
                        name: "testfile.png",
                        uploadedAt: "05/05/2025",
                        size: 200,
                        uploader: "Some UUID"
                    }}/>

                    <FileEntry file={{
                        name: "testfile2.png",
                        uploadedAt: "05/06/2025",
                        size: 400,
                        uploader: "Some UUID@12123"
                    }}/>
                </tbody>
            </table>
        </>
    );
}