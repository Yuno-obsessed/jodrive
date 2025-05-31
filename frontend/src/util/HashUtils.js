export async function parallelHashChunks(chunks, workerCount = 4) {
  const workers = Array.from(
    { length: workerCount },
    () =>
      new Worker(new URL("./hashWorker.js", import.meta.url), {
        type: "module",
      }),
  );
  const result = new Array(chunks.length);
  let current = 0;

  return new Promise((resolve, reject) => {
    let completed = 0;

    function assignWork(worker) {
      if (current >= chunks.length) return;

      const index = current++;
      worker.postMessage({ chunk: chunks[index].file, index });

      worker.onmessage = (e) => {
        result[e.data.index] = {
          ...chunks[e.data.index],
          hash: e.data.hash,
        };

        completed++;
        if (completed === chunks.length) {
          workers.forEach((w) => w.terminate());
          resolve(result);
        } else {
          assignWork(worker);
        }
      };

      worker.onerror = reject;
    }

    workers.forEach(assignWork);
  });
}
