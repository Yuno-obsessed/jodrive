import { createBrowserRouter } from "react-router-dom";
import { MainLayout } from "./components/layouts/MainLayout.jsx";
import { Header } from "./widgets/header/Header.jsx";
import Sidebar from "./widgets/sidebar/Sidebar.jsx";
import { FileSearchPage } from "./pages/file-search/FileSearchPage.jsx";
import { FileTreePage } from "./pages/file-tree/FileTreePage.jsx";
import { FileDeletedPage } from "./pages/file-deleted/FileDeletedPage.jsx";
import { WorkspaceBreadcrumb } from "./shared/auth/WorkspaceBreadcrumb.jsx";
import { WorkspaceUsers } from "./pages/ws-users/WorkspaceUsers.jsx";

export const router = () =>
  createBrowserRouter([
    {
      element: <MainLayout header={<Header />} sidebar={<Sidebar />} />,
      handle: {
        crumb: () => <span>Home</span>,
      },
      children: [
        {
          path: "/",
          element: <FileSearchPage />,
        },
        {
          path: "workspace/:id/*",
          element: <FileTreePage />,
          handle: {
            crumb: WorkspaceBreadcrumb,
          },
        },
        {
          path: "deleted",
          element: <FileDeletedPage />,
        },
        {
          path: "workspace/:id/users",
          element: <WorkspaceUsers />,
        },
      ],
    },
    // {
    //   children: [
    //     {
    //       path: "workspace/:id/users",
    //       element: <SomePage />,
    //     },
    //   ],
    // },
  ]);
