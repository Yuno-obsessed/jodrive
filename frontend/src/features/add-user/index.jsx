import { useState } from "react";
import { Button } from "../../components/ui/button/index.jsx";
import { AddWorkspaceUserModal } from "./AddWorkspaceUser.jsx";

export const AddWorkspaceUserModalButton = ({ className, wsID }) => {
  const [showAddWsUserModal, setShowAddWsUserModal] = useState(false);
  return (
    <div>
      <Button
        className={className}
        onClick={() => setShowAddWsUserModal(true)}
        variant="default"
      >
        Add Workspace User
      </Button>
      {showAddWsUserModal && (
        <AddWorkspaceUserModal
          wsID={wsID}
          onClose={() => setShowAddWsUserModal(!showAddWsUserModal)}
        />
      )}
    </div>
  );
};
