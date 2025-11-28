import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import AdminNav from "./AdminNav";

import {
  fetchUser,
  updateUser,
  deactivateUser,
  reactivateUser,
} from "../../api";

export default function UserDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);
  const [form, setForm] = useState({
    email: "",
    firstName: "",
    lastName: "",
    phone: "",
    dateOfBirth: "",
    active: true,
  });

  useEffect(() => {
    load();
  }, [id]);

  async function load() {
    setLoading(true);
    const data = await fetchUser(id);
    setUser(data);

    setForm({
      email: data.email || "",
      firstName: data.firstName || "",
      lastName: data.lastName || "",
      phone: data.phone || "",
      dateOfBirth: data.dateOfBirth || "",
      active: data.active,
    });

    setLoading(false);
  }

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSave() {
    await updateUser(id, form);
    alert("User updated successfully!");
    load();
  }

  async function handleDeactivate() {
    await deactivateUser(id);
    alert("User deactivated.");
    load();
  }

  async function handleReactivate() {
    await reactivateUser(id);
    alert("User reactivated.");
    load();
  }

  if (loading) return <div className="p-10 text-lg">Loading user...</div>;

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* Header */}
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <AdminNav activePage="Manage Users" />
      </header>

      <div className="max-w-3xl mx-auto w-xl bg-white shadow rounded-lg p-10 mt-4 mb-10">
        <button
          onClick={() => navigate(-1)}
          className="text-blue-600 hover:underline mb-4"
        >
          ← Back to Manage Users
        </button>

        <h1 className="text-3xl font-bold mb-4">
          Edit User #{user.userId}
        </h1>

        <p className="text-gray-700 mb-6">
          Role: <strong className="capitalize">{user.role}</strong>
        </p>

        {/* FORM */}
        <div className="space-y-5">
          {/* Email */}
          <div>
            <label className="block text-gray-700 mb-1">Email</label>
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded px-3 py-2"
            />
          </div>

          {/* First Name */}
          <div>
            <label className="block text-gray-700 mb-1">First Name</label>
            <input
              type="text"
              name="firstName"
              value={form.firstName}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded px-3 py-2"
            />
          </div>

          {/* Last Name */}
          <div>
            <label className="block text-gray-700 mb-1">Last Name</label>
            <input
              type="text"
              name="lastName"
              value={form.lastName}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded px-3 py-2"
            />
          </div>

          {/* Phone */}
          <div>
            <label className="block text-gray-700 mb-1">Phone</label>
            <input
              type="text"
              name="phone"
              value={form.phone}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded px-3 py-2"
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
            />
          </div>

          {/* Status */}
          <div>
            <label className="block text-gray-700 mb-1">Account Status</label>
            <p className="font-medium">
              {form.active ? (
                <span className="text-green-700">Active</span>
              ) : (
                <span className="text-red-600">Inactive</span>
              )}
            </p>
          </div>
        </div>

        {/* ACTION BUTTONS */}
        <div className="flex gap-4 mt-8">
          {/* Save */}
          <button
            onClick={handleSave}
            className="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Save Changes
          </button>

          {/* Deactivate / Reactivate */}
          {form.active ? (
            <button
              onClick={handleDeactivate}
              className="px-6 py-2 bg-red-600 text-white rounded hover:bg-red-700"
            >
              Deactivate
            </button>
          ) : (
            <button
              onClick={handleReactivate}
              className="px-6 py-2 bg-green-600 text-white rounded hover:bg-green-700"
            >
              Reactivate
            </button>
          )}
        </div>

        {/* Metadata */}
        <div className="mt-8 text-sm text-gray-600">
          <p>Created At: {new Date(user.createdAt).toLocaleString()}</p>
          {user.patientId && (
            <p className="mt-1">Patient ID: {user.patientId}</p>
          )}
        </div>
      </div>
    </div>
  );
}
