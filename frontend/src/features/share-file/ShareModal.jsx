import { useState } from "react";
import { Modal } from "../../components/ui/modal/index.jsx";
import { Button } from "../../components/ui/button/index.jsx";
import styles from "./ShareModal.module.css";
import useAuthStore from "../../util/authStore.js";
import DatePicker from "react-multi-date-picker";
import TimePicker from "react-multi-date-picker/plugins/time_picker";
import dayjs from "dayjs";
import { constructLink } from "../../api/ConstructLink.js";

export const ShareModal = ({ file, onClose }) => {
  const [copied, setCopied] = useState(false);
  const [date, setDate] = useState(new Date());
  const { token } = useAuthStore();

  const createEntireLink = (link) => {
    return `${window.location.origin}/file?link=${link}`;
  };

  const handleShare = async () => {
    let expiresAt = dayjs(date.toISOString()).unix();
    let link = await constructLink(file, expiresAt, token);
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
        <p>Select a date of link expiration</p>
        <DatePicker
          value={date}
          format="YYYY-MM-DD HH:mm"
          minDate={new Date()}
          plugins={[<TimePicker hideSeconds={true} />]}
        />
        <Button
          className={styles.link}
          variant="ghost"
          type="submit"
          onClick={() => handleShare()}
        >
          Create link
        </Button>
      </form>
      {copied && (
        <p className={styles.copiedMessage}>✅ Link copied to clipboard</p>
      )}
    </Modal>
  );
};
