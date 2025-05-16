import useAuthStore from "../../util/authStore.js";
import { getFileTree } from "../../api/GetFileTree.js";
import { useTreeModel } from "../../enitites/file-tree/model/index.js";

export const FileTreePage = () => {
  const [token] = useAuthStore();
  const [treeNodes, setTree] = useTreeModel();

  const handleGetTree = () => {
    getFileTree({ wsID: 1 }, token)
      .then((res) => {
        console.log(res);
        setTree(res);
      })
      .catch(console.error);
  };

  return <div></div>;
};
