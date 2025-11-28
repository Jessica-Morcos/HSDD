import { useState, useEffect } from "react";
import DoctorNav from "./DoctorNav";
import { useNavigate } from "react-router-dom";

export default function AllReportsPage() {
  const [reports, setReports] = useState([]);
  const [filteredReports, setFilteredReports] = useState([]);
  const [filterDisease, setFilterDisease] = useState("all");
  const [sortOrder, setSortOrder] = useState("newest");
  const [searchQuery, setSearchQuery] = useState("");
  const [uniqueDiagnoses, setUniqueDiagnoses] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      window.location.href = "/";
      return;
    }

    fetch("http://localhost:8080/api/doctor/reports", {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    })
      .then((res) => {
        if (res.status === 403) throw new Error("Forbidden");
        if (!res.ok) throw new Error("Failed to fetch reports");
        return res.json();
      })
      .then((data) => {
        const normalized = data.map((r) => ({
          id: r.id,
          patientName: r.patientName,
          patientId: r.patientId,
          disease: r.disease,
          confidence: Math.round((r.confidence ?? 0) * 100),
          date: r.createdAt ? new Date(r.createdAt).toLocaleDateString() : "—",
          doctor: r.doctor || "—",
        }));

        setReports(normalized);
        setFilteredReports(sortReports(normalized, sortOrder));

        // ⬅️ Extract unique diagnoses dynamically
        const diagSet = new Set();
        normalized.forEach((r) => diagSet.add(r.disease));
        setUniqueDiagnoses(["all", ...Array.from(diagSet)]);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  // Sorting helper
  const sortReports = (data, order) => {
    return [...data].sort((a, b) => {
      const dateA = a.date === "—" ? 0 : new Date(a.date).getTime();
      const dateB = b.date === "—" ? 0 : new Date(b.date).getTime();
      return order === "newest" ? dateB - dateA : dateA - dateB;
    });
  };

  // Apply search + filter + sort
  useEffect(() => {
    let list = [...reports];

    // 🔍 SEARCH BAR FILTER
    if (searchQuery.trim() !== "") {
      const q = searchQuery.toLowerCase();
      list = list.filter(
        (r) =>
          r.patientName.toLowerCase().includes(q) ||
          r.disease.toLowerCase().includes(q) ||
          r.patientId.toLowerCase().includes(q)
      );
    }

    // 🎯 DIAGNOSIS FILTER
    if (filterDisease !== "all") {
      list = list.filter((r) => r.disease === filterDisease);
    }

    // 🗂 SORT
    list = sortReports(list, sortOrder);

    setFilteredReports(list);
  }, [searchQuery, filterDisease, sortOrder, reports]);

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <DoctorNav activePage="All Reports" />
      </header>

      <div className="m-6">
        <h1 className="text-3xl font-bold mb-4">All Reports</h1>
        <p className="text-gray-700 mb-6">
          Browse, search, and filter all diagnostic reports.
        </p>

        {loading && <div className="mb-4 text-gray-600">Loading reports…</div>}
        {error && <div className="mb-4 text-red-600 font-medium">{error}</div>}

        {/* 🔎 Search + Filter + Sort */}
        <div className="flex flex-wrap items-center justify-between mb-6 gap-4">

          {/* 🔍 Search bar */}
          <div>
            <input
              type="text"
              placeholder="Search by name, diagnosis, or patient ID…"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="border border-gray-300 rounded px-3 py-2 w-64 focus:ring focus:ring-[#b0372b]"
            />
          </div>

          {/* 🎯 Filter by diagnosis */}
          <div className="flex items-center space-x-3">
            <label className="font-medium text-gray-700">Diagnosis:</label>
            <select
              value={filterDisease}
              onChange={(e) => setFilterDisease(e.target.value)}
              className="border border-gray-300 rounded px-3 py-2 focus:ring focus:ring-[#b0372b]"
            >
              {uniqueDiagnoses.map((d) => (
                <option key={d} value={d}>
                  {d === "all" ? "All" : d}
                </option>
              ))}
            </select>
          </div>

          {/* 🗂 Sort */}
          <div className="flex items-center space-x-3">
            <label className="font-medium text-gray-700">Sort:</label>
            <select
              value={sortOrder}
              onChange={(e) => setSortOrder(e.target.value)}
              className="border border-gray-300 rounded px-3 py-2 focus:ring focus:ring-[#b0372b]"
            >
              <option value="newest">Newest → Oldest</option>
              <option value="oldest">Oldest → Newest</option>
            </select>
          </div>
        </div>

        {/* 📋 Reports table */}
        <div className="bg-white rounded-lg shadow-lg p-6">
          <h2 className="text-2xl font-semibold mb-4">Diagnostic Reports</h2>

          {filteredReports.length === 0 ? (
            <p className="text-gray-500">No reports found.</p>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-200 text-gray-700">
                  <th className="p-3">Date</th>
                  <th className="p-3">Patient</th>
                  <th className="p-3">Diagnosis</th>
                  <th className="p-3">Confidence</th>
                  <th className="p-3">Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredReports.map((r) => (
                  <tr key={r.id} className="border-b hover:bg-gray-50 transition">
                    <td className="p-3">{r.date}</td>
                    <td className="p-3 font-medium text-gray-800">{r.patientName}</td>
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

                    <td className="p-3">
                      <button
                        onClick={() => navigate(`/doctor/report/${r.id}`)}
                        className="px-3 py-1 bg-[#b0372b] text-white rounded hover:bg-[#992c23]"
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
