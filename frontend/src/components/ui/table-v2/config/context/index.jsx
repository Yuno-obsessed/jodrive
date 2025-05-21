import { Menu } from "react-contexify";
import "react-contexify/ReactContexify.css";
import styles from "./index.module.css";

export const RowContextMenu = ({ id = "table", actions }) => {
  return (
    <Menu id={id} className={styles.menu}>
      {actions}
    </Menu>
  );
};
