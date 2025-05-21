import { create } from "zustand";

export const useContextMenuStore = create((set) => ({
  row: null,
  setRow: (row) => set({ row }),
  resetRow: () => set({ row: null }),
}));
