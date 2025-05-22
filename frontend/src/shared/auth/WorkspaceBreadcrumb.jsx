import { useFilesystemStore } from "../fs-dir/index.js";
import { Link } from "react-router-dom";

export const WorkspaceBreadcrumb = ({ params }) => {
  const { workspaceNames } = useFilesystemStore();
  // const { userWorkspaces } = useWorkspacesModel();
  // TODO: add workspace name here
  const name = workspaceNames?.[params.id] || `Workspace ${params.id}`;
  return <Link to={`/workspace/${params.id}`}>{name}</Link>;
};
