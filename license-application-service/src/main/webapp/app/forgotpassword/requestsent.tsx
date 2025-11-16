import React from "react";
import { Link } from "react-router";

export default function RequestSent() {
  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-50">
      <div className="w-full max-w-md p-8 space-y-6 text-center bg-white rounded-lg shadow-md">
        <h2 className="text-2xl font-bold">Sent!</h2>
        <p className="text-sm text-gray-600">
          The link has been sent to your email.
        </p>
        <Link
          to="/login"
          className="inline-block w-full py-2 px-4 font-semibold text-white bg-purple-800 rounded-md hover:bg-purple-900"
        >
          Go back to sign in
        </Link>
      </div>
    </div>
  );
}
