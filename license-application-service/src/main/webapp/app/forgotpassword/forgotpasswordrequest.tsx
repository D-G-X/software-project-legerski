import React, { useState } from "react";
import { redirect } from "react-router";

export default function ForgotPasswordRequest() {
  const [email, setEmail] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    redirect("/request-sent");

    // try {
    //   await axios.post("/api/account/reset-password/init", email, {
    //     headers: { "Content-Type": "text/plain" },
    //   });
    // } catch (error) {
    //   console.error("Failed to send reset email", error);
    //   alert("An error occurred. Please try again.");
    //   setIsLoading(false);
    // }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-50">
      <div className="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
        <div className="text-center">
          <h2 className="text-2xl font-bold">Forgot Password</h2>
          <p className="mt-2 text-sm text-gray-600">Enter your E-mail</p>
        </div>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label
              htmlFor="email"
              className="block text-sm font-medium text-left text-gray-700"
            >
              Email
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="john.doe@provider.com"
              required
              className="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
            />
          </div>
          <button
            type="submit"
            disabled={isLoading}
            className="w-full py-2 px-4 font-semibold text-white bg-purple-800 rounded-md hover:bg-purple-900 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 disabled:bg-gray-400"
          >
            {isLoading ? "Sending..." : "Reset password"}
          </button>
        </form>
      </div>
    </div>
  );
}
