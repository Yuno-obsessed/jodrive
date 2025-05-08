import { create } from "zustand";
import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
  url: "http://localhost:7080",
  realm: "jodrive-realm",
  clientId: "jodrive",
});

const useAuthStore = create((set) => ({
  keycloak,
  token: null,
  authenticated: false,
  userInfo: null,

  initKeycloak: async () => {
    const authenticated = await keycloak.init({ onLoad: "login-required" });

    if (authenticated) {
      set({
        token: keycloak.token,
        authenticated: true,
      });

      const userInfo = await keycloak.loadUserInfo();
      set({ userInfo });
    }
    setInterval(() => {
      keycloak.updateToken(5).then((refreshed) => {
        if (refreshed) {
          useAuthStore.setState({ token: keycloak.token });
        }
      });
    }, 300);
  },

  logout: () => {
    keycloak.logout();
    set({ authenticated: false, token: null, userInfo: null });
  },
}));

export default useAuthStore;
