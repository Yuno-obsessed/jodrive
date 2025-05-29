import { create } from "zustand";
import { immer } from "zustand/middleware/immer";

export const useWorkspaceUsersModel = create(
  immer((set) => ({
    workspaceUsers: [],

    setWorkspaceUsers: (workspaceUsers) =>
      set((state) => {
        state.workspaceUsers = workspaceUsers;
      }),

    addWorkspaceUser: (workspaceUser) => {
      set((state) => {
        if (!state.workspaceUsers.some((r) => r.id === workspaceUser.id)) {
          state.workspaceUsers.push(workspaceUser);
        }
      });
    },

    removeWorkspaceUsers: (workspaceUser) => {
      set((state) => {
        state.workspaceUsers = state.workspaceUsers.filter(
          (r) => r.id !== workspaceUser.id,
        );
      });
    },

    resetWorkspaceUsers: () =>
      set((state) => {
        state.workspaceUsers = [];
      }),
  })),
);
