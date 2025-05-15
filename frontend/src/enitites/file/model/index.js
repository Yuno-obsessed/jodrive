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
        if (
          !state.searchResults.elements.some(
            (r) => r.id === result.id && r.workspaceID === result.workspaceID,
          )
        ) {
          state.searchResults.elements.push(result);
        }
      });
    },

    resetSearch: () =>
      set((state) => {
        state.searchResults = [];
      }),
  })),
);
