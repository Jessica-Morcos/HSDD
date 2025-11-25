import { useState, useEffect } from "react";
import DoctorNav from "./DoctorNav";

export default function UserManagementPage() {
  // 🧑‍⚕️ Dummy data representing medical staff and roles
  const [users, setUsers] = useState([
    {
      id: 1,
      name: "Dr. Emily Carter",
      role: "Doctor",
      department: "Pulmonology",
      accessLevel: "Full",
      status: "Active",
    },
    {
      id: 2,
      name: "Nurse Michael Brown",
      role: "Nurse",
      department: "Emergency",
      accessLevel: "Limited",
      status: "Active",
    },
    {
      id: 3,
      name: "Dr. Sarah Patel",
      role: "Doctor",
      department: "Cardiology",
      accessLevel: "Full",
      status: "Inactive",
    },
    {
      id: 4,
      name: "Technician John Lee",
      role: "Lab Technician",
      department: "Diagnostics",
      accessLevel: "Limited",
      status: "Active",
    },
  ]);

  // 📡 Placeholder for backend integration (Spring Boot endpoint)
  // useEffect(() => {
  //   fetch("http://localhost:8080/api/doctor/staff")
  //     .then((res) => res.json())
  //     .then((data) => setUsers(data))
  //     .catch((err) => console.error("Error fetching staff data:", err));
  // }, []);

  const handleToggleStatus = (id) => {
    setUsers(
      users.map((u) =>
        u.id === id
          ? { ...u, status: u.status === "Active" ? "Inactive" : "Active" }
          : u
      )
    );
  };

  const handleChangeAccess = (id, newAccess) => {
    setUsers(
      users.map((u) =>
        u.id === id ? { ...u, accessLevel: newAccess } : u
      )
    );
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* 🩺 Doctor Navbar */}
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <DoctorNav activePage="User Management" />
      </header>

      {/* 📋 Page Header */}
      <div className="m-6">
        <h1 className="text-3xl font-bold mb-4">User Management</h1>
        <p className="text-gray-700 mb-6">
          Manage medical staff and adjust access levels for the CarePath platform.
        </p>

        {/* 👩‍⚕️ Staff Management Table */}
        <div className="bg-white rounded-lg shadow-lg p-6">
          <h2 className="text-2xl font-semibold mb-4">Medical Staff Overview</h2>
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-200 text-gray-700">
                <th className="p-3">Name</th>
                <th className="p-3">Role</th>
                <th className="p-3">Department</th>
                <th className="p-3">Access Level</th>
                <th className="p-3">Status</th>
                <th className="p-3">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id} className="border-b hover:bg-gray-50 transition">
                  <td className="p-3 font-medium text-gray-800">{u.name}</td>
                  <td className="p-3">{u.role}</td>
                  <td className="p-3">{u.department}</td>
                  <td className="p-3">
                    <select
                      value={u.accessLevel}
                      onChange={(e) => handleChangeAccess(u.id, e.target.value)}
                      className="border border-gray-300 rounded px-2 py-1 focus:ring focus:ring-[#b0372b]"
                    >
                      <option>Full</option>
                      <option>Limited</option>
                    </select>
                  </td>
                  <td
                    className={`p-3 font-semibold ${
                      u.status === "Active" ? "text-green-600" : "text-red-600"
                    }`}
                  >
                    {u.status}
                  </td>
                  <td className="p-3 space-x-2">
                    <button
                      onClick={() => handleToggleStatus(u.id)}
                      className="px-3 py-1 bg-[#b0372b] text-white rounded hover:bg-[#992c23] transition"
                    >
                      Toggle Status
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* ⚙️ Backend Integration Placeholder */}
        <div className="mt-10 p-4 bg-yellow-100 border border-yellow-300 rounded">
          <p className="text-sm text-gray-800">
            ⚙️ <strong>Backend Integration Placeholder:</strong>  
            Replace dummy staff data with Spring Boot API endpoints once available.
          </p>
          <ul className="list-disc ml-6 text-sm text-gray-700 mt-2">
            <li><code>GET /api/doctor/staff</code> → Fetch medical staff</li>
            <li><code>PUT /api/doctor/staff/:id/access</code> → Update access level</li>
            <li><code>PUT /api/doctor/staff/:id/status</code> → Activate/deactivate staff</li>
            <li><code>POST /api/doctor/staff</code> → Add new staff member (future)</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
