import { Link } from "react-router-dom";
import useAuthStore from "../../util/authStore.js";

export const WorkspaceBreadcrumb = ({ params }) => {
  const { userInfo } = useAuthStore();
  console.log(userInfo);
  const name =
    userInfo.workspaces.filter((w) => w.id == params.id).map((w) => w.name) ||
    `Workspace ${params.id}`;
  console.log(name);
  return <Link to={`/workspace/${params.id}`}>{name}</Link>;
};
