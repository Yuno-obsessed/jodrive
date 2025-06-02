import { useState } from "react";
import { Modal } from "../../components/ui/modal/index.jsx";
import { Button } from "../../components/ui/button/index.jsx";
import styles from "./ShareModal.module.css";
import useAuthStore from "../../util/authStore.js";
import DatePicker from "react-multi-date-picker";
import TimePicker from "react-multi-date-picker/plugins/time_picker";
import { constructLink } from "../../api/ConstructLink.js";
import dayjs from "dayjs";

export const ShareModal = ({ file, onClose }) => {
  const [copied, setCopied] = useState(false);
  const [date] = useState(new Date(Date.now() + 3_600_000)); // add one hour by defaukt
  const { token } = useAuthStore();

  const createEntireLink = (link) => {
    return `${window.location.origin}/file?link=${encodeURIComponent(link)}`;
  };

  const handleShare = async () => {
    let expiresAt = dayjs(date.toISOString()).unix();
    let link = await constructLink(file, expiresAt, token);
    link = createEntireLink(link);
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
          calendarPosition={"left"}
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
