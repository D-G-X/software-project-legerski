import React, {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
} from "react";
import { createPortal } from "react-dom";

type GlobalLoaderContextType = {
  show: (msg?: string) => void;
  hide: () => void;
  visible: boolean;
  message?: string | null;
};

const GlobalLoaderContext = createContext<GlobalLoaderContextType | undefined>(
  undefined
);

export const GlobalLoaderProvider = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const [visible, setVisible] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const show = useCallback((msg?: string) => {
    setMessage(msg ?? null);
    setVisible(true);
  }, []);

  const hide = useCallback(() => {
    setVisible(false);
    setMessage(null);
  }, []);

  const value = useMemo(
    () => ({ show, hide, visible, message }),
    [show, hide, visible, message]
  );

  return (
    <GlobalLoaderContext.Provider value={value}>
      {children}
      <GlobalLoader visible={visible} message={message} />
    </GlobalLoaderContext.Provider>
  );
};

export const useGlobalLoader = () => {
  const ctx = useContext(GlobalLoaderContext);
  if (!ctx)
    throw new Error("useGlobalLoader must be used within GlobalLoaderProvider");
  return ctx;
};

function GlobalLoader({
  visible,
  message,
}: {
  visible: boolean;
  message?: string | null;
}) {
  if (typeof document === "undefined") return null;
  if (!visible) return null;

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
      <div className="bg-white/90 bg-clip-padding p-8 rounded-2xl shadow-2xl flex flex-col items-center gap-5">
        <div className="relative flex items-center justify-center">
          <div className="w-34 h-34 rounded-full border-8 border-gray-200 border-t-mallorca-purple animate-spin" />
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="w-34 h-34 rounded-full bg-mallorca-purple/10 flex items-center justify-center">
              <img
                src="/images/logo.webp"
                alt="Portal logo"
                className="w-24 h-24 object-contain"
              />
            </div>
          </div>
        </div>

        {message ? (
          <div className="text-center">
            <div className="text-lg font-semibold text-mallorca-purple">
              {message}
            </div>
            <div className="text-xs text-gray-500 mt-1">Please wait…</div>
          </div>
        ) : (
          <div className="text-sm text-mallorca-purple">Loading…</div>
        )}
      </div>
    </div>,
    document.body
  );
}
