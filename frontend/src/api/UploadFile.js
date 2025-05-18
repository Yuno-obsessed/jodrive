import { BLOCK_URI, METADATA_URI } from "../consts/Constants.js";
import { parallelHashChunks } from "../util/HashUtils.js";

const CHUNK_SIZE = 4_000_000; // 4 MB
const MAX_BODY_SIZE = 8_000_000; // 8 MB
const HASH_SIZE = 64;
const MAX_HASHES_PER_REQUEST = Math.floor(MAX_BODY_SIZE / HASH_SIZE);

export async function getFileChunksToUpload(file) {
  const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
  const chunkList = {
    chunks: [],
    lastChunkSize: file.size - CHUNK_SIZE * (totalChunks - 1),
  };

  for (let i = 0; i < totalChunks; i++) {
    const start = i * CHUNK_SIZE;
    const end = Math.min(start + CHUNK_SIZE, file.size);
    const fileChunk = file.slice(start, end);
    chunkList.chunks.push({ file: fileChunk, position: i });
  }

  chunkList.chunks = await parallelHashChunks(chunkList.chunks, 8);
  return chunkList;
}

export async function uploadBatchOfChunks(batch, token) {
  const formData = new FormData();
  batch.forEach((chunk) =>
    formData.append("blocks", new Blob([chunk.file]), chunk.hash),
  );
  formData.set(
    "body",
    JSON.stringify({ correlationID: "dfd040c1-283c-426c-8603-57065bd51553" }),
  );

  const res = await fetch(`${BLOCK_URI}/block`, {
    method: "POST",
    body: formData,
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error("Failed uploading batch");
}

export async function checkChunkExistence(
  workspaceID,
  chunks,
  filename,
  lastChunkSize,
  token,
) {
  console.log(chunks);
  const hashes = chunks.map(({ hash, position }) => ({ hash, position }));
  let missingBlocks = [];
  let index = 0;
  let response;
  while (index < hashes.length) {
    const batch = hashes.slice(index, index + MAX_HASHES_PER_REQUEST);
    index += MAX_HASHES_PER_REQUEST;

    const body = JSON.stringify({
      correlationID: "dfd040c1-283c-426c-8603-57065bd51553", //TODO: random UUID
      workspaceID: workspaceID,
      path: filename,
      blocks: batch,
      lastBlockSize: lastChunkSize,
    });

    const res = await fetch(METADATA_URI, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body,
    });

    if (!res.ok) throw new Error("Metadata check failed");
    response = await res.json();
    if (response.missingBlocks) {
      missingBlocks.push(...response.missingBlocks);
    }
  }
  return {
    chunks: chunks.filter((c) => missingBlocks.includes(c.hash)),
    fileInfo: response.fileInfo,
  };
}
