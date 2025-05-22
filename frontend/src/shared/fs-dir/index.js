import { create } from "zustand";
import { immer } from "zustand/middleware/immer";

export const useFilesystemStore = create(
  immer((set) => ({
    currentPath: "/",
    basePath: "",

    setCurrPath: (path) =>
      set((state) => {
        state.currentPath = path || "/";
      }),

    setBasePath: (path) =>
      set((state) => {
        state.basePath = path || "/";
      }),
  })),
);
