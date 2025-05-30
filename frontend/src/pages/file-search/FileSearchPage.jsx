import { useSearchModel } from "../../enitites/file/model/index.js";
import { getCoreRowModel, useReactTable } from "@tanstack/react-table";

import { useMemo, useState } from "react";
import { FileTreeTable } from "../../components/ui/table-v2/index.jsx";
import {
  closestCenter,
  DndContext,
  KeyboardSensor,
  MouseSensor,
  TouchSensor,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import { restrictToVerticalAxis } from "@dnd-kit/modifiers";
import { fileSearchColumns } from "./config/index.js";
import { RenameModal } from "../../features/rename-file/RenameModal.jsx";
import { ShareModal } from "../../features/share-file/ShareModal.jsx";
import useAuthStore from "../../util/authStore.js";
import { FileSearchMenuActions } from "./context/index.jsx";
import { useContextMenuStore } from "../../components/ui/table-v2/config/store/index.js";
import { downloadFile } from "../../api/DownloadFile.js";
import { deleteFile } from "../../api/DeleteFile.js";

export const FileSearchPage = () => {
  const { searchResults, removeSearchResult, renameSearchResult } =
    useSearchModel();
  const { token, userInfo } = useAuthStore();

  const [fileToRename, setFileToRename] = useState(null);
  const [fileToShare, setFileToShare] = useState(null);

  const elements = useMemo(
    () => searchResults?.elements ?? [],
    [searchResults],
  );

  const getRowID = (row) => {
    return row.id + "_" + row.workspaceID;
  };

  const dataIds = useMemo(() => elements.map((f) => getRowID(f)), [elements]);

  const table = useReactTable({
    data: elements,
    columns: fileSearchColumns(userInfo),
    getCoreRowModel: getCoreRowModel(),
    getRowId: (row) => getRowID(row),
  });

  const sensors = useSensors(
    useSensor(MouseSensor),
    useSensor(TouchSensor),
    useSensor(KeyboardSensor),
  );

  const row = useContextMenuStore();
  const handleEvents = ({ id }) => {
    let eventRow = row.row;
    switch (id) {
      case "share":
        setFileToShare(eventRow);
        break;
      case "download":
        downloadFile(eventRow, token).then().catch(console.log);
        break;
      case "delete":
        deleteFile(
          { id: eventRow.id, workspaceID: eventRow.workspaceID },
          token,
        )
          .then(() => removeSearchResult(eventRow))
          .catch(console.log);
        break;
      case "rename":
        setFileToRename(eventRow);
        console.log("rename");
        break;
    }
  };

  return (
    <div>
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        modifiers={[restrictToVerticalAxis]}
      >
        <FileTreeTable
          table={table}
          dataIds={dataIds}
          actions={<FileSearchMenuActions handleEvents={handleEvents} />}
        />
      </DndContext>

      {fileToRename && (
        <RenameModal
          file={fileToRename}
          renameFile={renameSearchResult}
          onClose={() => setFileToRename(null)}
        />
      )}
      {fileToShare && (
        <ShareModal file={fileToShare} onClose={() => setFileToShare(null)} />
      )}
    </div>
  );
};
