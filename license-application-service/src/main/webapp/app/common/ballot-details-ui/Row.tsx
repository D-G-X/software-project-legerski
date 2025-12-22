import React from "react";

interface RowProps {
  label: string;
  children: React.ReactNode;
}

const Row: React.FC<RowProps> = ({ label, children }) => {
  return (
    <div className="flex items-center justify-between px-4 py-3">
      <span className="text-gray-600">{label}</span>
      <div className="flex items-center gap-2">{children}</div>
    </div>
  );
};

export default Row;
