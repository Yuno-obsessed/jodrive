import { Button } from "../../../components/ui/button/index.jsx";
import styles from "./Workspace.module.css";

export const Workspace = ({ workspace, onClick }) => {
  // TODO: add select workspace for file tree page
  return (
    <Button onClick={onClick} className={styles.workspace}>
      <img alt="Workspace" src="workspaces.svg"></img>
      <a>{workspace.name}</a>
    </Button>
  );
};
