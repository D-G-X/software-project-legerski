import React, { useEffect, useState } from "react";

type AnimatedDotsProps = {
  speed?: number; // speed in ms
}

export function AnimatedDots({ speed = 400 }: AnimatedDotsProps) {
  const [dots, setDots] = useState("");

  useEffect(() => {
    const interval = setInterval(() => {
      setDots(prev => (prev.length === 3 ? "" : prev + "."));
    }, speed); // Geschwindigkeit nach Bedarf anpassen

    return () => clearInterval(interval);
  }, [speed]);

  return <span>{dots}</span>;
}