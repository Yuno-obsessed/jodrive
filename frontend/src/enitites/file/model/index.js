import { create } from "zustand";
import { immer } from "zustand/middleware/immer";

export const useSearchModel = create(
  immer((set) => ({
    searchResults: [],

    setSearch: (results) =>
      set((state) => {
        state.searchResults = results;
      }),

    addSearchResult: (result) => {
      set((state) => {
        state.searchResults.elements.push(result);
      });
    },

    resetSearch: () =>
      set((state) => {
        state.searchResults = [];
      }),
  })),
);
