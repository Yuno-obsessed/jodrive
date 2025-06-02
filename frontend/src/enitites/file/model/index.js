import { create } from "zustand";
import { immer } from "zustand/middleware/immer";

export const useSearchModel = create(
  immer((set) => ({
    searchResults: [],

    setSearch: (results) =>
      set((state) => {
        console.log(results);
        state.searchResults = results;
      }),

    addSearchResult: (result) => {
      set((state) => {
        if (
          !state.searchResults.some(
            (r) => r.id === result.id && r.workspaceID === result.workspaceID,
          )
        ) {
          state.searchResults.push(result);
        }
      });
    },

    removeSearchResult: (result) => {
      set((state) => {
        state.searchResults = state.searchResults.filter(
          (r) => r.id !== result.id || r.workspaceID !== result.workspaceID,
        );
      });
    },

    resetSearch: () =>
      set((state) => {
        state.searchResults = [];
      }),

    renameSearchResult: (targetFile, newName) => {
      set((state) => {
        const file = state.searchResults.find(
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
