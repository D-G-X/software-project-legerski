import React from "react";

type FormHeaderProps = {
  heading: string;
  subHeading?: string;       // optional
  className?: string;        // optional
};

export function FormHeader({ heading, subHeading, className = "" }: FormHeaderProps) {
  return (
      <div className={className}>
        <div className="text-4xl font-bold text-mallorca-purple">
          {heading}
        </div>

        {subHeading && (
            <div className="text-md my-2 text-mallorca-purple/50">
              {subHeading}
            </div>
        )}
      </div>
  );
}