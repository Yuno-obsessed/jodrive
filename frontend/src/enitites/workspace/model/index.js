import { create } from "zustand";
import { immer } from "zustand/middleware/immer";

export const useWorkspacesModel = create(
  immer((set) => ({
    userWorkspaces: [],
    activeWorkspace: null,

    setWorkspaces: (workspaces) =>
      set((state) => {
        state.userWorkspaces = workspaces;
      }),

    setActive: (workspace) =>
      set((state) => {
        state.activeWorkspace = workspace;
      }),

    addWorkspace: (workspace) => {
      set((state) => {
        if (!state.userWorkspaces.some((r) => r.id === workspace.id)) {
          state.userWorkspaces.push(workspace);
        }
      });
    },

    removeWorkspace: (workspace) => {
      set((state) => {
        state.userWorkspaces = state.userWorkspaces.filter(
          (r) => r.id !== workspace.id,
        );
      });
    },

    resetWorkspaces: () =>
      set((state) => {
        state.userWorkspaces = [];
      }),
  })),
);
