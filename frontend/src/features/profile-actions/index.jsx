import { useEffect, useRef, useState } from "react";
import { Button } from "../../components/ui/button/index.jsx";
import styles from "./ProfileDropDown.module.css";
import { ProfileDropDown } from "./ProfileDropDown.jsx";

export const ProfileDropDownButton = ({ children }) => {
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
      <Button ref={btnRef} onClick={() => setShowProfile((prev) => !prev)}>
        {children}
      </Button>

      {showProfile && (
        <ProfileDropDown ref={modalRef} onClose={() => setShowProfile(false)} />
      )}
    </div>
  );
};
