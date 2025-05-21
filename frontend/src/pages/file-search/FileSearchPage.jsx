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
import { METADATA_URI } from "../../consts/Constants.js";
import useAuthStore from "../../util/authStore.js";
import { Breadcrumb } from "../../components/ui/breadcrumb/index.jsx";
import { FileSearchMenuActions } from "./context/index.jsx";

export const FileSearchPage = () => {
  const { searchResults } = useSearchModel();
  const { token } = useAuthStore();

  const [fileToRename, setFileToRename] = useState(null);
  const [fileToShare, setFileToShare] = useState(false);

  const handleShare = (file) => {
    console.log(`HANDLE SHARE FOR ${getRowID(file)}`);
    // constructLink(file, "MINUTE", 60, token)
    //     .then(() => setShowShareModal(true))
    //     .catch(console.error);
  };

  const handleDownload = (file) => {
    console.log(`HANDLE Download FOR ${getRowID(file)}`);
    // downloadFile(file, token).catch(console.error);
  };

  const handleDelete = (file) => {
    console.log(`HANDLE Delete FOR ${getRowID(file)}`);
    // deleteFile(file, token)
    //     .then(() => {
    //       removeSearchResult(file);
    //     })
    //     .catch(console.error);
  };

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
    columns: fileSearchColumns,
    getCoreRowModel: getCoreRowModel(),
    getRowId: (row) => getRowID(row),
  });

  const sensors = useSensors(
    useSensor(MouseSensor),
    useSensor(TouchSensor),
    useSensor(KeyboardSensor),
  );

  const handleEvents = ({ id, event, data }) => {
    console.log(id, event, data);
    switch (id) {
      case "share":
        handleShare(data);
        break;
      case "download":
        handleDownload(data);
        break;
      case "delete":
        handleDelete(data);
        break;
      case "rename":
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

      {fileToShare && (
        <ShareModal
          // TODO: call share api in modal?
          link={`${METADATA_URI}/file/${fileToShare.id + "_" + fileToShare.workspaceID}?link={shareLink}`}
          onClose={() => setFileToShare(null)}
        />
      )}

      {fileToRename && (
        <RenameModal
          file={fileToRename}
          onClose={() => setFileToRename(null)}
        />
      )}
    </div>
  );
};
