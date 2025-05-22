import { Link, useLocation, useMatches } from "react-router-dom";
import styles from "./index.module.css";
import { useFilesystemStore } from "../../../shared/fs-dir/index.js";

export const Breadcrumb = () => {
  const matches = useMatches();
  const location = useLocation();
  const { basePath } = useFilesystemStore(); // should be something like "/workspace/1"

  const crumbs = [];

  // Static route breadcrumbs (e.g., Home, Workspace)
  matches.forEach((match, index) => {
    const Crumb = match.handle?.crumb;
    if (Crumb) {
      const isLast =
        index === matches.length - 1 && location.pathname === match.pathname;
      crumbs.push(
        <span key={match.pathname}>
          {!isLast ? (
            <Link to={match.pathname}>
              <Crumb params={match.params} />
            </Link>
          ) : (
            <span className={styles.isLast}>
              <Crumb params={match.params} />
            </span>
          )}
          {" / "}
        </span>,
      );
    }
  });

  // Dynamic folder segments (e.g. /home/user/docs)
  if (basePath && location.pathname.startsWith(basePath)) {
    const subPath = location.pathname.replace(basePath, "").replace(/^\/+/, "");
    const segments = subPath.split("/").filter(Boolean);

    let cumulativePath = basePath;

    segments.forEach((segment, idx) => {
      cumulativePath += `/${segment}`;
      const isLast = idx === segments.length - 1;

      crumbs.push(
        <span key={cumulativePath}>
          {!isLast ? (
            <Link to={cumulativePath}>{segment}</Link>
          ) : (
            <span className={styles.isLast}>{segment}</span>
          )}
          {!isLast && " / "}
        </span>,
      );
    });
  }

  return <nav className={styles.crumb}>{crumbs}</nav>;
};
