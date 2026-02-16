import React from "react";

interface SectionProps {
  title: string;
  children: React.ReactNode;
}

export const Section: React.FC<SectionProps> = ({title, children}) => {
  return (
      <section className="mt-5">
        <h2 className="text-xl font-semibold text-mallorca-purple">{title}</h2>
        <div className="pl-6 mt-2">{children}</div>
      </section>
  );
};

export const SubSection: React.FC<SectionProps> = ({title, children}) => {
  return (
      <section className="mt-5">
        <h3 className="text-lg font-semibold text-mallorca-purple">{title}</h3>
        <div className="pl-9 mt-2">{children}</div>
      </section>
  );
};
