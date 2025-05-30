import { useLocation, useNavigate, useParams } from "react-router-dom";
import { getWorkspaceUsers } from "../../api/WorkspaceAPI.js";
import useAuthStore from "../../util/authStore.js";
import { useEffect, useMemo, useState } from "react";
import { TableRow } from "../../enitites/file/ui/TableRow.jsx";
import { Button } from "../../components/ui/button/index.jsx";
import CuidaUserRemoveOutline from "~icons/cuida/user-remove-outline";
import TablerInfoCircle from "~icons/tabler/info-circle";
import TablerArrowBackUp from "~icons/tabler/arrow-back-up";
import styles from "./WorkspaceUsers.module.css";
import Table from "../../components/ui/table/index.jsx";
import { useWorkspaceUsersModel } from "../../enitites/ws-users/model/index.js";
import { ProfileModalButton } from "../../features/profile-actions/index.jsx";

export const WorkspaceUsers = () => {
  const { token, userInfo } = useAuthStore();
  const { id } = useParams();
  const [selected, setSelected] = useState(null);
  const [hovered, setHovered] = useState();
  const location = useLocation();
  const navigate = useNavigate();
  const { workspaceUsers, setWorkspaceUsers } = useWorkspaceUsersModel();

  useEffect(() => {
    getWorkspaceUsers({ wsID: id }, token)
      .then((res) => setWorkspaceUsers(res.elements))
      .catch(console.log);
  }, [id]);

  const getWorkspaceUser = () => {
    return workspaceUsers.filter((u) => u.id === userInfo.id)[0];
  };

  const handleRemove = (user) => {};

  const columnRenderers = {
    name: (user) => user.username,
    role: (user) => user.role,
    joinedAt: (user) => user.joinedAt,
  };

  const columns = useMemo(
    () => ["Username", "Role", "Joined At", "Actions"],
    [],
  );

  const renderRow = (user) => (
    <TableRow
      key={`${user.id}`}
      entity={user}
      columns={["name", "role", "joinedAt"]}
      columnRenderers={columnRenderers}
      onClick={() => setSelected(user)}
      onMouseEnter={() => setHovered(user)}
      isSelected={selected === user}
      buttons={
        userInfo.id === user.id ? (
          <p className={styles.actions}>It's you</p>
        ) : (
          <>
            {getWorkspaceUser().role === "OWNER" && user.role !== "OWNER" && (
              <Button
                className={styles.btn}
                variant="icon"
                onClick={() => handleRemove(file)}
              >
                <CuidaUserRemoveOutline className={styles.icons} />
              </Button>
            )}

            <ProfileModalButton
              className={styles.btn}
              variant="icon"
              currentUser={userInfo}
              children={<TablerInfoCircle className={styles.icons} />}
            />
          </>
        )
      }
    />
  );

  return (
    <>
      <Button
        variant="icon"
        className={styles.backBtn}
        onClick={() => {
          const currPath = location.pathname;
          navigate(currPath.substring(0, currPath.lastIndexOf("/")));
        }}
      >
        <TablerArrowBackUp />
      </Button>
      <Table
        columns={columns}
        data={workspaceUsers || []}
        renderRow={renderRow}
        tableClassName={styles.table}
      />
    </>
  );
};
