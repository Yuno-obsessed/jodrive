import { Button } from "../../components/ui/button/index.jsx";
import { UploadModal } from "./UploadModal.jsx";
import { useState } from "react";

export const UploadModalButton = () => {
  const [showUploadModal, setShowUploadModal] = useState(false);
  return (
    <div>
      <Button onClick={() => setShowUploadModal(true)} variant={"ghost"}>
        Upload File
      </Button>
      {showUploadModal && (
        <UploadModal onClose={() => setShowUploadModal(!showUploadModal)} />
      )}
    </div>
  );
};
