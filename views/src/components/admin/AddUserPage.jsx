import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AdminNav from "./AdminNav";
import { createUser } from "../../api";

export default function AddUserPage() {
  const navigate = useNavigate();

  const [role, setRole] = useState("patient");

  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    dateOfBirth: "",
    phone: "",
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();

    try {
      setError("");
      const payload = { ...form, role };

      const result = await createUser(payload);

      setSuccess("User created successfully!");

      setTimeout(() => {
        navigate("/admin/manage-users");
      }, 1200);
    } catch (err) {
      setError("Failed to create user. Check fields and try again.");
    }
  }

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* Header */}
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <AdminNav activePage="Manage Users" />
      </header>

      <div className="max-w-3xl w-xl mx-auto bg-white shadow rounded-lg p-10 mt-4 mb-10">
        <button
          onClick={() => navigate(-1)}
          className="text-blue-600 hover:underline mb-4"
        >
          ← Back to Manage Users
        </button>

        <h1 className="text-3xl font-bold mb-6">Add New User</h1>

        {/* ERROR / SUCCESS */}
        {error && (
          <p className="bg-red-100 text-red-700 p-3 rounded mb-4">{error}</p>
        )}
        {success && (
          <p className="bg-green-100 text-green-700 p-3 rounded mb-4">
            {success}
          </p>
        )}

        {/* ROLE TOGGLE */}
        <div className="mb-6">
          <label className="block text-gray-700 font-medium mb-2">
            Select User Role
          </label>

          <div className="flex gap-4">
            <button
              className={`px-4 py-2 rounded border ${
                role === "patient"
                  ? "bg-[#b0372b] text-white"
                  : "bg-white border-gray-300"
              }`}
              onClick={() => setRole("patient")}
            >
              Patient
            </button>

            <button
              className={`px-4 py-2 rounded border ${
                role === "doctor"
                  ? "bg-[#b0372b] text-white"
                  : "bg-white border-gray-300"
              }`}
              onClick={() => setRole("doctor")}
            >
              Doctor
            </button>
          </div>
        </div>

        {/* FORM */}
        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Username */}
          <div>
            <label className="block text-gray-700 mb-1">Username</label>
            <input
              name="username"
              value={form.username}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
            />
          </div>

          {/* Email */}
          <div>
            <label className="block text-gray-700 mb-1">Email</label>
            <input
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
            />
          </div>

          {/* Password */}
          <div>
            <label className="block text-gray-700 mb-1">Password</label>
            <input
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
            />
          </div>

          {/* First Name */}
          <div>
            <label className="block text-gray-700 mb-1">First Name</label>
            <input
              name="firstName"
              value={form.firstName}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
            />
          </div>

          {/* Last Name */}
          <div>
            <label className="block text-gray-700 mb-1">Last Name</label>
            <input
              name="lastName"
              value={form.lastName}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
            />
          </div>

          {/* DOB */}
          <div>
            <label className="block text-gray-700 mb-1">Date of Birth</label>
            <input
              type="date"
              name="dateOfBirth"
              value={form.dateOfBirth}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
            />
          </div>

          {/* Phone */}
          <div>
            <label className="block text-gray-700 mb-1">Phone Number</label>
            <input
              name="phone"
              value={form.phone}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
            />
          </div>

          {/* SUBMIT BUTTON */}
          <div className="mt-6">
            <button
              type="submit"
              className="px-6 py-2 bg-[#b0372b] text-white rounded hover:bg-[#992c23]"
            >
              Create User
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
