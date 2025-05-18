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
          !state.files.elements.some(
            (r) => r.id === file.id && r.workspaceID === file.workspaceID,
          )
        ) {
          state.files.elements.push(file);
        }
      });
    },

    removeFile: (file) => {
      set((state) => {
        state.files.elements = state.files.elements.filter(
          (r) => r.id !== file.id && r.workspaceID !== file.workspaceID,
        );
      });
    },
  })),
);
