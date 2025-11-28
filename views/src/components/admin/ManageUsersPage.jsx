import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import AdminNav from "./AdminNav";

import {
  fetchUsers,
  deactivateUser,
  reactivateUser,
} from "../../api/";

export default function ManageUsersPage() {
  const [users, setUsers] = useState([]);
  const [search, setSearch] = useState("");
  const navigate = useNavigate();

  // Load users from backend
  useEffect(() => {
    loadUsers();
  }, []);

  async function loadUsers() {
    const data = await fetchUsers();
    setUsers(data);
  }

  async function handleDeactivate(id) {
    await deactivateUser(id);
    loadUsers();
  }

  async function handleReactivate(id) {
    await reactivateUser(id);
    loadUsers();
  }

  const filtered = users.filter((u) => {
    const q = search.toLowerCase();
    return (
      u.username.toLowerCase().includes(q) ||
      (u.firstName && u.firstName.toLowerCase().includes(q)) ||
      (u.lastName && u.lastName.toLowerCase().includes(q)) ||
      u.email.toLowerCase().includes(q) ||
      String(u.userId).includes(q)
    );
  });

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* Header */}
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <AdminNav activePage="Manage Users" />
      </header>

      <div className="m-6">
        {/* Title */}
        <h1 className="text-3xl font-bold mb-2">Manage Users</h1>
        <p className="text-gray-700 mb-6">
          The Admin can view, search, edit, deactivate, or reactivate users,
          assign roles, and monitor account activity.
        </p>

        {/* Top Bar */}
        <div className="flex justify-between items-center mb-6">
          {/* 🔍 Search */}
          <input
            type="text"
            placeholder="Search by name, email, or ID..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="border border-gray-300 rounded px-3 py-2 w-1/3"
          />

          {/* ➕ Add User */}
          <button
            onClick={() => navigate("/admin/users/add")}
            className="bg-[#b0372b] text-white px-4 py-2 rounded hover:bg-[#992c23] transition"
          >
            + Add User
          </button>
        </div>

        {/* User Table */}
        <table className="w-full bg-white rounded-lg shadow">
          <thead>
            <tr className="bg-gray-200 text-left text-gray-700">
              <th className="p-3">ID</th>
              <th className="p-3">Name</th>
              <th className="p-3">Role</th>
              <th className="p-3">Email</th>
              <th className="p-3">Status</th>
              <th className="p-3 text-right">Actions</th>
            </tr>
          </thead>

          <tbody>
            {filtered.map((user) => (
              <tr key={user.userId} className="border-b hover:bg-gray-50">
                <td className="p-3">{user.userId}</td>

                {/* Name */}
                <td className="p-3">
                  {user.firstName ? (
                    <span>
                      {user.firstName} {user.lastName}
                    </span>
                  ) : (
                    <span>{user.username}</span>
                  )}
                </td>

                {/* Role */}
                <td className="p-3 capitalize">{user.role}</td>

                {/* Email */}
                <td className="p-3">{user.email}</td>

                {/* Status */}
                <td className="p-3">
                  {user.active ? (
                    <span className="text-green-700 font-medium">Active</span>
                  ) : (
                    <span className="text-red-600 font-medium">Inactive</span>
                  )}
                </td>

                {/* Actions */}
                <td className="p-3 text-right space-x-3">
                  {/* ✏️ Edit */}
                  <button
                    onClick={() =>
                      navigate(`/admin/user/${user.userId}`)
                    }
                    className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700"
                  >
                    Edit
                  </button>

                  {/* 🔻 Deactivate / 🔺 Reactivate */}
                  {user.active ? (
                    <button
                      onClick={() => handleDeactivate(user.userId)}
                      className="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700"
                    >
                      Deactivate
                    </button>
                  ) : (
                    <button
                      onClick={() => handleReactivate(user.userId)}
                      className="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700"
                    >
                      Reactivate
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        
      </div>
    </div>
  );
}
