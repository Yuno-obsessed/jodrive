import React from "react";

export const FileActionButton = ({ src, alt, callback }) => {
  return (
    <button
      onClick={(e) => {
        e.stopPropagation();
        callback();
      }}
    >
      <img src={src} alt={alt} />
    </button>
  );
};
