import React from 'react';


export default function LegalNoticeLayout({ children }: { children: React.ReactNode }) {


    return (
        <div className="w-full h-screen overflow-auto bg-white text-gray-800 flex flex-col">
            <main className="max-w-4xl mx-auto px-4 py-10" >
                {children}
            </main>
        </div>


    );
}