import {useState} from 'react'
import styles from './ShareModal.module.css'

export const ShareModal = ({link, onClose}) => {
    const [copied, setCopied] = useState(false);

    const handleCopy = () => {
        navigator.clipboard.writeText(link).then(() => {
            setCopied(true);
            setTimeout(() => setCopied(false), 3000);
        });
    };

    return (
        <div className={styles.overlay}>
            <div className={styles.modal}>
                <button className={styles.closeButton} onClick={onClose}>
                    &times;
                </button>
                <h2>Share Link</h2>
                <p className={styles.link}>{link}</p>
                <button className={styles.copyButton} onClick={handleCopy}>
                    Copy Link
                </button>
                {copied && (
                    <p className={styles.copiedMessage}>
                        ✅ Link copied to clipboard
                    </p>
                )}
            </div>
        </div>
    );
}