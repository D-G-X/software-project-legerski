import React from "react";

interface ValuePillProps {
  children: React.ReactNode;
}

const ValuePill: React.FC<ValuePillProps> = ({children}) => {
  return (
      <span className="border border-gray-300 rounded-md px-3 py-1 bg-white font-medium">
      {children}
    </span>
  );
};

export default ValuePill;
