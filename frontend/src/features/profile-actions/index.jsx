import { useState } from "react";
import { Button } from "../../components/ui/button/index.jsx";
import { ProfileViewModal } from "./ProfileViewModal.jsx";

export const ProfileViewModalButton = ({ children }) => {
  const [showProfile, setShowProfile] = useState(false);

  return (
    <>
      <Button variant="icon" onClick={() => setShowProfile(true)}>
        {children}
      </Button>
      {showProfile && (
        <ProfileViewModal onClose={() => setShowProfile(false)} />
      )}
    </>
  );
};
