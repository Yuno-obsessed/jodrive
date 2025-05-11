import { uploadBatchOfChunks } from "../api/UploadFile.js";

class UploadObserver {
  constructor() {
    this.subscribers = [];
  }

  subscribe(callback) {
    this.subscribers.push(callback);
  }

  notify(event) {
    this.subscribers.forEach((cb) => cb(event));
  }
}

export async function uploadChunksWithRetry({
  chunks,
  token,
  batchSize = 2,
  poolSize = 10,
  maxRetries = 3,
  observer = new UploadObserver(),
}) {
  const batches = [];
  for (let i = 0; i < chunks.length; i += batchSize) {
    batches.push(chunks.slice(i, i + batchSize));
  }

  let index = 0;
  const failedChunks = [];

  async function uploadWithRetry(batch, retries = maxRetries) {
    for (let attempt = 1; attempt <= retries; attempt++) {
      try {
        await uploadBatchOfChunks(batch, token);
        observer.notify({ type: "batchUploaded", count: batch.length, batch });
        return;
      } catch (e) {
        console.warn(`Upload failed (attempt ${attempt})`, e);
        if (attempt === retries) {
          failedChunks.push(...batch);
        } else {
          await new Promise((r) => setTimeout(r, 500 * attempt));
        }
      }
    }
  }

  async function worker() {
    while (index < batches.length) {
      const currentIndex = index++;
      const batch = batches[currentIndex];
      await uploadWithRetry(batch);
    }
  }

  await Promise.all(
    Array(poolSize)
      .fill()
      .map(() => worker()),
  );

  if (failedChunks.length > 0) {
    console.warn(`Retrying ${failedChunks.length} failed chunks...`);

    const retryBatches = [];
    for (let i = 0; i < failedChunks.length; i += batchSize) {
      retryBatches.push(failedChunks.slice(i, i + batchSize));
    }

    let retryIndex = 0;

    async function retryWorker() {
      while (retryIndex < retryBatches.length) {
        const currentIndex = retryIndex++;
        const batch = retryBatches[currentIndex];
        await uploadWithRetry(batch, 1);
      }
    }

    await Promise.all(
      Array(poolSize)
        .fill()
        .map(() => retryWorker()),
    );
  }
}

export { UploadObserver };
