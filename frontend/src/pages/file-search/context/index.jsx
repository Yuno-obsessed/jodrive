import { Menu, Item } from "react-contexify";
import "react-contexify/ReactContexify.css";
import { useContextMenuStore } from "../model/context.js";
import TablerShare from "~icons/tabler/share";
import LucideEdit3 from "~icons/lucide/edit-3";
import TablerDownload from "~icons/tabler/download";
import MynauiTrash from "~icons/mynaui/trash";

export const RowContextMenu = ({ id = "table", handleEvents }) => {
  const row = useContextMenuStore((s) => s.row);

  return (
    <Menu id={id}>
      <Item id="download" onClick={handleEvents.download}>
        <>
          <TablerDownload />
          <a>Download</a>
        </>
      </Item>
      <Item id="share" onClick={handleEvents.share}>
        <>
          <TablerShare />
          <a>Share</a>
        </>
      </Item>
      <Item id="rename" onClick={handleEvents.rename}>
        <>
          <LucideEdit3 />
          <a>Rename</a>
        </>
      </Item>
      <Item id="delete" onClick={handleEvents.delete}>
        <>
          <MynauiTrash />
          <a>Delete</a>
        </>
      </Item>
    </Menu>
  );
};
