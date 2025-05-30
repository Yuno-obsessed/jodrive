import { Item } from "react-contexify";
import "react-contexify/ReactContexify.css";
import TablerShare from "~icons/tabler/share";
import LucideEdit3 from "~icons/lucide/edit-3";
import TablerDownload from "~icons/tabler/download";
import MynauiTrash from "~icons/mynaui/trash";
import TablerFolder from "~icons/tabler/folder";
import { useContextMenuStore } from "../../../components/ui/table-v2/config/store/index.js";

export const FileTreeMenuActions = ({ handleEvents }) => {
  const { row } = useContextMenuStore();

  return (
    <>
      {row.isDirectory && (
        <Item id="open" onClick={handleEvents}>
          <span>
            <TablerFolder />
            <a>Open</a>
          </span>
        </Item>
      )}
      {!row.isDirectory && (
        <>
          <Item id="download" onClick={handleEvents}>
            <span>
              <TablerDownload />
              <a>Download</a>
            </span>
          </Item>
          <Item id="share" onClick={handleEvents}>
            <span>
              <TablerShare />
              <a>Share</a>
            </span>
          </Item>
        </>
      )}
      <Item id="rename" onClick={handleEvents}>
        <span>
          <LucideEdit3 />
          <a>Rename</a>
        </span>
      </Item>
      {!row.isDirectory && (
        <Item id="delete" onClick={handleEvents}>
          <span>
            <MynauiTrash />
            <a>Delete</a>
          </span>
        </Item>
      )}
    </>
  );
};
