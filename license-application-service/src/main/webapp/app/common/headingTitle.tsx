import React from "react";

export function FormHeader(props: { heading: string; subHeading: string }) {
  return (
    <>
      <div className="text-4xl font-bold text-mallorca-purple">
        {props.heading}
      </div>
      <div className="text-md my-2 text-mallorca-purple/50">
        {props.subHeading}
      </div>
    </>
  );
}
