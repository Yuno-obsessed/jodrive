import { useEffect, useRef, useState } from "react";
import { Button } from "../../components/ui/button/index.jsx";
import { ProfileModal } from "./ProfileModal.jsx";

export const ProfileModalButton = ({
  variant,
  className,
  children,
  currentUser,
}) => {
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
    <>
      <Button
        className={className}
        variant={variant}
        ref={btnRef}
        onClick={() => setShowProfile((prev) => !prev)}
      >
        {children}
      </Button>

      {showProfile && (
        <ProfileModal
          ref={modalRef}
          targetUser={currentUser}
          onClose={() => setShowProfile(false)}
        />
      )}
    </>
  );
};
