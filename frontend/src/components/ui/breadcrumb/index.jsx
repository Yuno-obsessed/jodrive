import { Link, useMatches } from "react-router-dom";
import styles from "./index.module.css";
import { useWorkspacesModel } from "../../../enitites/workspace/model/index.js";

export const Breadcrumb = () => {
  const matches = useMatches();
  // const { userWorkspaces } = useWorkspacesModel();
  console.log(matches);

  const crumbs = matches
    .filter((match) => match.handle?.crumb)
    .map((match, index, arr) => {
      const isLast = index === arr.length - 1;
      const Crumb = match.handle.crumb;

      return isLast ? (
        <span className={styles.isLast} key={match.pathname}>
          <Crumb params={match.params} />
        </span>
      ) : (
        <Link key={match.pathname} to={match.pathname}>
          <Crumb params={match.params} />
        </Link>
      );
    });

  return <nav className={styles.crumb}>{crumbs}</nav>;
};
