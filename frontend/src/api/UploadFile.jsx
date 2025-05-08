import { getSHA256Hash } from "../util/HashUtils.js";
import { METADATA_URI, BLOCK_URI } from "../consts/Constants.js";

const CHUNK_SIZE = 4 * 1024 * 1024;
const MAX_BODY_SIZE = 8096 * 1024;
const HASH_SIZE = 64;
const MAX_HASHES_PER_REQUEST = Math.floor(MAX_BODY_SIZE / HASH_SIZE);

// TODO: maybe uncouple these functions to provide easier access to create a progress bar
export async function uploadFile(file, token) {
  const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
  const lastChunkSize = file.size - CHUNK_SIZE * (totalChunks - 1);
  const chunks = [];

  for (let i = 0; i < totalChunks; i++) {
    const start = i * CHUNK_SIZE;
    const end = Math.min(start + CHUNK_SIZE, file.size);
    const chunk = file.slice(start, end);
    const hash = await getSHA256Hash(chunk);
    chunks.push({ file: chunk, hash, position: i, toUpload: false });
  }

  await checkChunkExistence(chunks, file.name, lastChunkSize, token);
  let batch = [];
  console.log("Chunks to upload:", chunks.filter((c) => c.toUpload).length);

  for (let chunk of chunks) {
    if (!chunk.toUpload) continue;
    batch.push(chunk);
    if (batch.length === 2) {
      await uploadBatch(batch);
      batch = []; // clear batch after upload
    }
  }

  if (batch.length) await uploadBatch(batch, token);
  console.log("Upload complete!");
}

async function uploadBatch(batch, token) {
  const formData = new FormData();
  batch.forEach((chunk) =>
    formData.append("blocks", new Blob([chunk.file]), chunk.hash),
  );
  formData.set(
    "body",
    JSON.stringify({ correlationID: "dfd040c1-283c-426c-8603-57065bd51553" }),
  );

  const res = await fetch(BLOCK_URI, {
    method: "POST",
    body: formData,
    headers: {
      Authorization: `Bearer: ${token}`,
    },
  });
  if (!res.ok) throw new Error("Failed uploading batch");
}

async function checkChunkExistence(chunks, filename, lastChunkSize, token) {
  const hashes = chunks.map(({ hash, position }) => ({ hash, position }));
  let index = 0;
  while (index < hashes.length) {
    const batch = hashes.slice(index, index + MAX_HASHES_PER_REQUEST);
    index += MAX_HASHES_PER_REQUEST;

    const body = JSON.stringify({
      correlationID: "dfd040c1-283c-426c-8603-57065bd51553", //TODO: random UUID
      workspaceID: 1, // TODO: user has to select which workspace to upload a file to
      filename: filename,
      blocks: batch,
      lastBlockSize: lastChunkSize,
    });

    const res = await fetch(METADATA_URI, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer: ${token}`,
      },
      body,
    });

    if (!res.ok) throw new Error("Metadata check failed");
    const json = await res.json();
    json.missingBlocks.forEach((hash) => {
      const chunk = chunks.find((c) => c.hash === hash);
      if (chunk) chunk.toUpload = true;
    });
  }
}
