import { useState } from "react";
import { Modal } from "../../components/ui/modal/index.jsx";
import { Button } from "../../components/ui/button/index.jsx";
import { Input } from "../../components/ui/input/index.jsx";
import styles from "./ShareModal.module.css";
import useAuthStore from "../../util/authStore.js";
import { constructLink } from "../../api/ConstructLink.js";

const Unit = {
  MINUTE: "MINUTE",
  HOUR: "HOUR",
  DAY: "DAY",
  MONTH: "MONTH",
};

export const ShareModal = ({ file, onClose }) => {
  const [copied, setCopied] = useState(false);
  const unitValues = Object.values(Unit);
  const [selectedUnit, setUnit] = useState(unitValues[0]);
  const [unitQuantity, setQuantity] = useState(1);
  const { token } = useAuthStore();

  const createEntireLink = (link) => {
    return `${window.location.origin}/file?link=${link}`;
  };

  const handleShare = async () => {
    console.log(selectedUnit, unitQuantity);
    let link = await constructLink(file, selectedUnit, unitQuantity, token);
    console.log(link);
    link = createEntireLink(link);
    console.log(link);
    navigator.clipboard.writeText(link).then(() => {
      setCopied(true);
      setTimeout(() => {
        setCopied(false);
        onClose();
      }, 1000);
    });
  };

  return (
    <Modal title="Share file" onClose={onClose}>
      <form className={styles.shareForm} onSubmit={(e) => e.preventDefault()}>
        <p>Select a period of link expiration</p>
        <div className={styles.radioUnits}>
          {unitValues.map((unit) => (
            <label key={unit}>
              <input
                type="radio"
                name="unitSelection"
                value={unit}
                checked={selectedUnit === unit}
                onChange={() => setUnit(unit)}
              />
              <a>{unit}</a>
            </label>
          ))}
        </div>
        <Input
          className={styles.quantity}
          type="text"
          placeholder={`Enter quantity of ${selectedUnit}`}
          onChange={(e) => setQuantity(e.target.value)}
        />
        <Button variant="ghost" type="submit" onClick={() => handleShare()}>
          Create link
        </Button>
      </form>
      {copied && (
        <p className={styles.copiedMessage}>✅ Link copied to clipboard</p>
      )}
    </Modal>
    // <div className={styles.overlay}>
    //   <div className={styles.modal}>
    //     <button className={styles.closeButton} onClick={onClose}>
    //       &times;
    //     </button>
    //     <h2>Share Link</h2>
    //     <p className={styles.link}>{link}</p>
    //     <button className={styles.copyButton} onClick={handleCopy}>
    //       Copy Link
    //     </button>
    //     {copied && (
    //       <p className={styles.copiedMessage}>✅ Link copied to clipboard</p>
    //     )}
    //   </div>
    // </div>
  );
};
