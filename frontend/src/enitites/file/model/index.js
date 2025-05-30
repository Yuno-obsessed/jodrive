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

    removeSearchResult: (result) => {
      set((state) => {
        state.searchResults.elements = state.searchResults.elements.filter(
          (r) => r.id !== result.id && r.workspaceID !== result.workspaceID,
        );
      });
    },

    resetSearch: () =>
      set((state) => {
        state.searchResults = [];
      }),

    renameSearchResult: (targetFile, newName) => {
      set((state) => {
        const file = state.searchResults.elements.find(
          (f) =>
            f.id === targetFile.id && f.workspaceID === targetFile.workspaceID,
        );
        if (file) {
          file.name = newName;
        }
      });
    },
  })),
);
