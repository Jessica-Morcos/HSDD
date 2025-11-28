import { useState } from "react";
import { useNavigate } from "react-router-dom";
import carepathLogo from "../assets/carepath-logo.svg";

export default function ChangePassword() {
  const navigate = useNavigate();
  const userId = localStorage.getItem("userId");
  const [newPassword, setNewPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (newPassword !== confirm) {
      setMessage("❌ Passwords do not match");
      return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
      setMessage("❌ Not logged in.");
      return;
    }

    try {
      const res = await fetch(
        `http://localhost:8080/api/auth/reset-password/${userId}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + token,
          },
          body: JSON.stringify({ newPassword }),
        }
      );

      if (res.ok) {
        setMessage("✅ Password updated successfully.");

        // Log out the user after successful change
        setTimeout(() => {
          localStorage.removeItem("token");
          localStorage.removeItem("userId");
          window.location.href = "/";
        }, 1500);
      } else {
        setMessage("❌ Failed to update password.");
      }
    } catch (err) {
      setMessage("❌ Error connecting to server.");
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* Header */}
      <header className="flex items-center justify-between bg-[#b0372b] p-4">
        <img src={carepathLogo} alt="CarePath" className="w-28" />

        <button
          onClick={() => navigate(-1)}
          className="text-white border border-white px-4 py-1 rounded hover:bg-white hover:text-[#b0372b] transition"
        >
          Back
        </button>
      </header>

      <main className="flex-1 flex justify-center items-center px-4">
        <div className="bg-white shadow-lg rounded-xl p-8 w-[28rem]">
          <h2 className="text-2xl font-bold mb-4 text-center">Change Password</h2>

          {message && (
            <div className="text-center mb-4 text-sm font-medium">
              {message}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block font-medium mb-1">New Password</label>
              <input
                type="password"
                className="w-full border rounded p-2"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
              />
            </div>

            <div>
              <label className="block font-medium mb-1">Confirm Password</label>
              <input
                type="password"
                className="w-full border rounded p-2"
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                required
              />
            </div>

            <button
              type="submit"
              className="w-full bg-[#b0372b] text-white py-2 rounded hover:bg-[#922f24] transition"
            >
              Update Password
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}

