import { create } from "zustand";
import Keycloak from "keycloak-js";
import { getUserInfo } from "../api/UserInfo.js";
import {
  KEYCLOAK_CLIENT,
  KEYCLOAK_REALM,
  KEYCLOAK_URI,
} from "../consts/Constants.js";

const keycloak = new Keycloak({
  url: KEYCLOAK_URI,
  realm: KEYCLOAK_REALM,
  clientId: KEYCLOAK_CLIENT,
});

const useAuthStore = create((set) => ({
  keycloak,
  token: null,
  authenticated: false,
  userInfo: null,

  initKeycloak: async () => {
    const authenticated = await keycloak.init({ onLoad: "login-required" });

    if (authenticated) {
      const userInfo = await keycloak.loadUserInfo();
      const user = await getUserInfo(userInfo.sub, keycloak.token);
      set({
        token: keycloak.token,
        authenticated: true,
        userInfo: user,
      });
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
