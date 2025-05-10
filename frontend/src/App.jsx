import React, { useEffect } from "react";
import "./App.css";
import { MainLayout } from "./layouts/MainLayout.jsx";
import useAuthStore from "./util/authStore.js";
import { Route, Routes } from "react-router-dom";

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
        <MainLayout>
          <Routes>
            <Route path="/files" element=<FileSearchPage /> />
            <Route path="/" element=<HomePage /> />
          </Routes>
        </MainLayout>
      ) : (
        <div>Loading...</div>
      )}
    </>
  );
}

export default App;
