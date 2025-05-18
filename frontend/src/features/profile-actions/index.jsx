import { useEffect, useRef, useState } from "react";
import { Button } from "../../components/ui/button/index.jsx";
import styles from "./ProfileViewModal.module.css";
import { ProfileViewModal } from "./ProfileViewModal.jsx";

export const ProfileViewModalButton = ({ children }) => {
  const [showProfile, setShowProfile] = useState(false);
  const modalRef = useRef(null);
  const btnRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (
        btnRef.current?.contains(e.target) ||
        modalRef.current?.contains(e.target)
      ) {
        return;
      }
      setShowProfile(false);
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <div className={styles.modalButton}>
      <Button
        variant="icon"
        ref={btnRef}
        onClick={() => setShowProfile((prev) => !prev)}
      >
        {children}
      </Button>

      {showProfile && (
        <ProfileViewModal
          ref={modalRef}
          onClose={() => setShowProfile(false)}
        />
      )}
    </div>
  );
};
