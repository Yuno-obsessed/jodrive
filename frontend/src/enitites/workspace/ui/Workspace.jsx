import { Button } from "../../../components/ui/button/index.jsx";

export const Workspace = ({ workspace, onClick }) => {
  return (
    <Button onClick={onClick} variant={"ghost"}>
      <img alt="Workspace" src="workspaces.svg"></img>
      <a>{workspace.name}</a>
    </Button>
  );
};
