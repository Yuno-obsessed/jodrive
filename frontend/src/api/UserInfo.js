import { USER_URI } from "../consts/Constants.js";

export async function getUserInfo(userID, token) {
  const res = await fetch(`${USER_URI}/${userID}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return await res.json();
}
