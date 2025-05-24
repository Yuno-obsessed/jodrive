import { useEffect } from "react";
import "./App.css";
import useAuthStore from "./util/authStore.js";
import { RouterProvider } from "react-router-dom";
import { router } from "./routes.jsx";

function App() {
  const { authenticated, initKeycloak } = useAuthStore();

  useEffect(() => {
    if (!authenticated) {
      void initKeycloak();
    }
  }, []);

  if (!authenticated) {
    return <div>Loading...</div>;
  }

  return <RouterProvider router={router()} />;
}

export default App;
