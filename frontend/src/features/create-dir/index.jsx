import { Button } from "../../components/ui/button/index.jsx";
import { CreateDirectoryModal } from "./CreateDirectoryModal.jsx";
import { useState } from "react";

export const CreateDirectoryModalButton = ({ path, wsID }) => {
  const [showCreateDirModal, setShowCreateDirModal] = useState(false);
  return (
    <div>
      <Button onClick={() => setShowCreateDirModal(true)} variant={"ghost"}>
        Create Directory
      </Button>
      {showCreateDirModal && (
        <CreateDirectoryModal
          wsID={wsID}
          path={path}
          onClose={() => setShowCreateDirModal(!showCreateDirModal)}
        />
      )}
    </div>
  );
};
