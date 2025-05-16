import { create } from "zustand";
import { immer } from "zustand/middleware/immer";

export const useTreeModel = create(
  immer((set) => ({
    treeNodes: [],

    setTree: (nodes) =>
      set((state) => {
        state.treeNodes = nodes;
      }),
  })),
);
