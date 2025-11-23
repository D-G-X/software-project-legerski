import React from "react";
import {downloadPdfOfficialDocument} from "../downloadPdf";

interface ModalDialogProps {
    open: boolean;
    showDownloadButton: boolean;
    onAccept: () => void;
    onCancel: () => void;
    acceptButtonLabel: string;
    cancelButtonLabel: string;
    downloadButtonLabel: string;
    downloadFileName?: string;
    title: string;
    text: string;
    t: (key: string) => string;
}

export default function ModalDialog({
                                        open,
                                        showDownloadButton,
                                        onAccept,
                                        onCancel,
                                        acceptButtonLabel,
                                        cancelButtonLabel,
                                        downloadButtonLabel,
                                        downloadFileName,
                                        title,
                                        text,
                                        t
}: ModalDialogProps) {
    if (!open) return null;

    const onDownload = () => {
        downloadPdfOfficialDocument({
            downloadFileName: downloadFileName || 'document',
            title: title,
            text: text,
            t: t
        }).then(() =>
        {
            console.log("download completed");
        });
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
            <div className="bg-white rounded-xl shadow-lg w-full max-w-400 p-6 animate-fadeIn">

                {/* Header */}
                <h2 className="text-xl font-semibold text-mallorca-purple mb-3">
                    {title}
                </h2>

                {/* Content */}
                <div
                    className="max-h-200 overflow-y-auto text-mallorca-purple/90 whitespace-pre-line border border-mallorca-purple/20 rounded p-3">
                    {text}
                </div>

                <div className="flex items-center justify-between w-full mt-5 gap-3">
                    {/* Left Buttons */}
                    <div className="flex items-center gap-3">
                        {showDownloadButton && (
                        <button
                            onClick={onDownload}
                            className="px-4 py-2 rounded-lg border border-mallorca-purple text-mallorca-purple hover:bg-mallorca-purple/10 transition"
                        >
                            {downloadButtonLabel}
                        </button>
                        )}
                    </div>

                    {/* Right Buttons */}
                    <div className="flex items-center gap-3">
                        <button
                            onClick={onCancel}
                            className="px-4 py-2 rounded-lg border border-mallorca-purple text-mallorca-purple hover:bg-mallorca-purple/10 transition"
                        >
                            {cancelButtonLabel}
                        </button>

                        <button
                            onClick={onAccept}
                            className="px-4 py-2 rounded-lg bg-mallorca-purple text-white hover:bg-mallorca-purple/90 transition"
                        >
                            {acceptButtonLabel}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}