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

  const findByID = (rowID) => {
    console.log(rowID.substring(0, rowID.indexOf("_")));
    console.log(rowID.substring(rowID.indexOf("_") + 1, rowID.length));
    console.log(elements);
    return elements.filter(
      (f) =>
        f.id == rowID.substring(0, rowID.indexOf("_")) &&
        f.workspaceID == rowID.substring(rowID.indexOf("_") + 1, rowID.length),
    )[0];
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

  // TODO: This logic will be only in filetree page (per workspace)
  const moveFileInDirectory = (event) => {
    const { active, over } = event;
    const activeFile = findByID(active.id);
    const overFile = findByID(over.id);
    console.log(activeFile, overFile);
    if (activeFile && overFile && active.id !== over.id) {
      if (!activeFile.isDirectory && overFile.isDirectory) {
        console.log(
          `Putting file ${activeFile.id} to directory ${overFile.id}`,
        );
      }
    }
  };

  return (
    <div>
      <Breadcrumb />
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        modifiers={[restrictToVerticalAxis]}
        onDragEnd={(e) => {
          moveFileInDirectory(e);
        }}
      >
        <FileTreeTable
          table={table}
          dataIds={dataIds}
          eventHandler={handleEvents}
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
