import { USER_URI } from "../consts/Constants.js";

export async function changeUserAvatar(userID, filename, token) {
  if (userID === null || filename === null) {
    return new Error("Invalid parameters");
  }
  const response = await fetch(`${USER_URI}/${userID}?photo=${filename}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });
  if (response.status !== 200) {
    throw new Error("Error changing avatar");
  }
}

export async function uploadAvatar(file, token) {
  if ((!file) instanceof Blob) {
    return new Error("Invalid parameters");
  }
  const formData = new FormData();
  formData.set("photo", file);
  const response = await fetch(`${USER_URI}`, {
    method: "POST",
    body: formData,
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  if (response.status !== 200) {
    throw new Error("Failed uploading avatar");
  }
  return await response.json();
}
