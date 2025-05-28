import { create } from "zustand";
import { immer } from "zustand/middleware/immer";

export const useTreeModel = create(
  immer((set) => ({
    files: [],

    setFiles: (files) =>
      set((state) => {
        state.files = files;
      }),

    addFile: (file) => {
      set((state) => {
        if (
          !state.files.some(
            (r) => r.id === file.id && r.workspaceID === file.workspaceID,
          )
        ) {
          state.files.push(file);
        }
      });
    },

    removeFile: (id) => {
      set((state) => {
        state.files = state.files.filter((r) => r.id !== id);
      });
    },
  })),
);
