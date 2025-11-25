// src/components/doctor/AllReportsPage.jsx
import { useState, useEffect } from "react";
import DoctorNav from "./DoctorNav";
import { useNavigate } from "react-router-dom";


export default function AllReportsPage() {
  const [reports, setReports] = useState([]);
  const [filteredReports, setFilteredReports] = useState([]);
  const [filterDisease, setFilterDisease] = useState("all");
  const [sortOrder, setSortOrder] = useState("newest");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  // 🔄 Fetch all reports on mount (same style as RecentPatients & LowConfidence)
  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      console.error("No token found — forcing logout");
      window.location.href = "/";
      return;
    }

    setLoading(true);
    setError("");

    fetch("http://localhost:8080/api/doctor/reports", {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    })
      .then((res) => {
        if (res.status === 403) {
          throw new Error("Forbidden — doctor role required or bad token");
        }
        if (!res.ok) {
          throw new Error("Failed to fetch reports");
        }
        return res.json();
      })
      .then((data) => {
        // data: List<AllReportDto>
        // { id, patientName, patientId, disease, confidence, createdAt, doctor }

        const normalized = data.map((r) => ({
          id: r.id,
          patientName: r.patientName,
          disease: r.disease,
          // backend confidence is 0–1, show as %
          confidence: Math.round((r.confidence ?? 0) * 100),
          date: r.createdAt ? new Date(r.createdAt).toLocaleDateString() : "—",
          doctor: r.doctor || "—",
        }));

        setReports(normalized);
        setFilteredReports(sortReports(normalized, sortOrder));
      })
      .catch((err) => {
        console.error("Error fetching reports:", err);
        setError(err.message || "Unexpected error");
      })
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 🔁 Sort helper (newest/oldest) – works on normalized "date" field
  const sortReports = (data, order) => {
    return [...data].sort((a, b) => {
      const dateA = a.date === "—" ? 0 : new Date(a.date).getTime();
      const dateB = b.date === "—" ? 0 : new Date(b.date).getTime();
      return order === "newest" ? dateB - dateA : dateA - dateB;
    });
  };

  // 🧮 Filter + Sort Logic
  const handleFilterDisease = (value) => {
    setFilterDisease(value);
    const filtered =
      value === "all"
        ? reports
        : reports.filter((r) =>
            r.disease.toLowerCase().includes(value.toLowerCase())
          );
    setFilteredReports(sortReports(filtered, sortOrder));
  };

  const handleSortChange = (order) => {
    setSortOrder(order);
    setFilteredReports((current) => sortReports(current, order));
  };

  const handleViewDetails = (id) => {
    // Placeholder — in future can route to /doctor/report/:id
    alert(`Viewing details for report ID: ${id} (placeholder action)`);
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* 🧭 Doctor Navbar */}
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <DoctorNav activePage="All Reports" />
      </header>

      {/* 🩺 Page Content */}
      <div className="m-6">
        <h1 className="text-3xl font-bold mb-4">All Reports</h1>
        <p className="text-gray-700 mb-6">
          Browse and filter all diagnostic reports submitted across the CarePath
          system.
        </p>

        {loading && (
          <div className="mb-4 text-gray-600">Loading reports…</div>
        )}

        {error && (
          <div className="mb-4 text-red-600 font-medium">{error}</div>
        )}

        {/* 🔹 Filter + Sort Controls */}
        <div className="flex flex-wrap items-center justify-between mb-6 gap-4">
          <div className="flex items-center space-x-3">
            <label className="font-medium text-gray-700">
              Filter by Diagnosis:
            </label>
            <select
              value={filterDisease}
              onChange={(e) => handleFilterDisease(e.target.value)}
              className="border border-gray-300 rounded px-3 py-1 focus:ring focus:ring-[#b0372b]"
            >
              <option value="all">All</option>
              <option value="Influenza">Influenza (Flu)</option>
              <option value="Asthma">Asthma</option>
              <option value="Hypertension">Hypertension</option>
              <option value="Pneumonia">Pneumonia</option>
            </select>
          </div>

          <div className="flex items-center space-x-3">
            <label className="font-medium text-gray-700">Sort by Date:</label>
            <select
              value={sortOrder}
              onChange={(e) => handleSortChange(e.target.value)}
              className="border border-gray-300 rounded px-3 py-1 focus:ring focus:ring-[#b0372b]"
            >
              <option value="newest">Newest to Oldest</option>
              <option value="oldest">Oldest to Newest</option>
            </select>
          </div>
        </div>

        {/* 📋 Reports Table */}
        <div className="bg-white rounded-lg shadow-lg p-6">
          <h2 className="text-2xl font-semibold mb-4">Diagnostic Reports</h2>

          {filteredReports.length === 0 && !loading ? (
            <p className="text-gray-500">No reports found.</p>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-200 text-gray-700">
                  <th className="p-3">Date</th>
                  <th className="p-3">Patient</th>
                  <th className="p-3">Diagnosis</th>
                  <th className="p-3">Confidence</th>
                  <th className="p-3">Doctor</th>
                  <th className="p-3">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredReports.map((r) => (
                  <tr
                    key={r.id}
                    className="border-b hover:bg-gray-50 transition"
                  >
                    <td className="p-3">{r.date}</td>
                    <td className="p-3 font-medium text-gray-800">
                      {r.patientName}
                    </td>
                    <td className="p-3">{r.disease}</td>
                    <td
                      className={`p-3 font-semibold ${
                        r.confidence >= 85
                          ? "text-green-600"
                          : r.confidence >= 70
                          ? "text-yellow-600"
                          : "text-red-600"
                      }`}
                    >
                      {r.confidence}%
                    </td>
                    <td className="p-3">{r.doctor}</td>
                    <td className="p-3">
                      <button
                          onClick={() => navigate(`/doctor/report/${r.id}`)}
                          className="px-3 py-1 bg-[#b0372b] text-white rounded hover:bg-[#992c23] transition"
                        >
                          View Details
                        </button>

                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        
      </div>
    </div>
  );
}
