import { useEffect } from "react";
import "./App.css";
import useAuthStore from "./util/authStore.js";
import { Route, Routes } from "react-router-dom";
import { MainLayout } from "./components/layouts/MainLayout.jsx";
import { Header } from "./widgets/header/Header.jsx";
import Sidebar from "./widgets/sidebar/Sidebar.jsx";
import { FileSearchPage } from "./pages/file-search/FileSearchPage.jsx";

function App() {
  const { authenticated, initKeycloak } = useAuthStore();

  useEffect(() => {
    if (!authenticated) {
      initKeycloak();
    }
  }, []);

  return (
    <>
      {authenticated ? (
        <Routes>
          <Route
            element={<MainLayout header={<Header />} sidebar={<Sidebar />} />}
          >
            <Route index element={<FileSearchPage />} />
          </Route>
        </Routes>
      ) : (
        <div>Loading...</div>
      )}
    </>
  );
}

export default App;
