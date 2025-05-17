import { Button } from "../../../components/ui/button/index.jsx";
import styles from "./Workspace.module.css";

export const Workspace = ({ workspace, onClick }) => {
  return (
    <Button onClick={onClick} variant={"ghost"}>
      <img alt="Workspace" src="workspaces.svg"></img>
      <a>{workspace.name}</a>
    </Button>
  );
};
