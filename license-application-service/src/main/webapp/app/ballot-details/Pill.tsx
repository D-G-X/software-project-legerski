import React from "react";

interface PillProps {
  children: React.ReactNode;
}

const Pill: React.FC<PillProps> = ({children}) => {
  return (
      <span className="border border-gray-300 rounded-full px-3 py-1 text-sm bg-white">
      {children}
    </span>
  );
};

export default Pill;
