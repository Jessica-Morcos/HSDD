import { useState, useEffect } from "react";
import DoctorNav from "./DoctorNav";
import { useNavigate } from "react-router-dom";

export default function RecentPatientsPage() {
  const [patients, setPatients] = useState([]);
  const [sortOrder, setSortOrder] = useState("newest");
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      window.location.href = "/";
      return;
    }

    fetch("http://localhost:8080/api/doctor/all-patients", {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    })
      .then((res) => {
        if (res.status === 403) throw new Error("Forbidden");
        if (!res.ok) throw new Error("Failed to fetch patients");
        return res.json();
      })
      .then((data) => {
        const formatted = data.map((p) => {
          const raw =
            p.lastVisit !== null && p.lastVisit !== "No visits yet"
              ? new Date(p.lastVisit)
              : null;

          return {
            ...p,
            lastVisitRaw: raw,
            lastVisit: raw ? raw.toLocaleString() : "No visits yet",
            doctorNotes: p.doctorNotes || "—",
          };
        });

        setPatients(formatted);
        sortPatients(sortOrder, formatted);
      })
      .catch((err) => console.error("Error fetching patients:", err));
  }, []);

  const sortPatients = (order, list = patients) => {
    const sorted = [...list].sort((a, b) => {
      const aVal = a.lastVisitRaw;
      const bVal = b.lastVisitRaw;

      // If NO visits → always bottom
      if (!aVal && !bVal) return 0;
      if (!aVal) return 1;
      if (!bVal) return -1;

      return order === "newest" ? bVal - aVal : aVal - bVal;
    });

    setPatients(sorted);
  };

  const handleSortChange = (e) => {
    const order = e.target.value;
    setSortOrder(order);
    sortPatients(order);
  };

  const handleViewRecord = (patientId) => {
    navigate(`/doctor/patient/${patientId}/record`);
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <DoctorNav activePage="Patients" />
      </header>

      <div className="m-6">
        <h1 className="text-3xl font-bold mb-4">Patients</h1>

        <div className="flex justify-end mb-4">
          <label className="mr-3 font-medium text-gray-700">Sort by Date:</label>
          <select
            value={sortOrder}
            onChange={handleSortChange}
            className="border border-gray-300 rounded px-3 py-1 focus:ring focus:ring-[#b0372b]"
          >
            <option value="newest">Newest to Oldest</option>
            <option value="oldest">Oldest to Newest</option>
          </select>
        </div>

        <div className="bg-white rounded-lg shadow-lg p-6">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-200 text-gray-700">
                <th className="p-3">Patient Name</th>
                <th className="p-3">Age</th>
                <th className="p-3">Last Visit</th>
                <th className="p-3">Diagnosis</th>
                <th className="p-3">Doctor Notes</th>
                <th className="p-3">Actions</th>
              </tr>
            </thead>

            <tbody>
              {patients.map((p, index) => (
                <tr key={index} className="border-b hover:bg-gray-50 transition">
                  <td className="p-3 font-medium text-gray-800">{p.name}</td>
                  <td className="p-3">{p.age ?? "—"}</td>
                  <td className="p-3">{p.lastVisit}</td>
                  <td className="p-3">{p.lastDiagnosis}</td>
                  <td className="p-3 text-sm text-gray-600">{p.doctorNotes}</td>
                  <td className="p-3">
                    <button
                      onClick={() => handleViewRecord(p.patientId)}
                      className="px-3 py-1 bg-[#b0372b] text-white rounded hover:bg-[#992c23] transition"
                    >
                      View Record
                    </button>
                  </td>
                </tr>
              ))}

              {patients.length === 0 && (
                <tr>
                  <td
                    colSpan="6"
                    className="text-center p-6 text-gray-500 text-lg"
                  >
                    No patients found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
