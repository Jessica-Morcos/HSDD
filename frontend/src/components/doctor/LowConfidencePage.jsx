// src/components/doctor/LowConfidencePage.jsx
import { useState, useEffect } from "react";
import DoctorNav from "./DoctorNav";

export default function LowConfidencePage() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // 🔄 Fetch low-confidence reports on mount
  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      console.error("No token found — forcing logout");
      window.location.href = "/";
      return;
    }

    setLoading(true);
    setError("");

    fetch("http://localhost:8080/api/doctor/low-confidence", {
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
          throw new Error("Failed to fetch low-confidence reports");
        }
        return res.json();
      })
      .then((data) => {
        // data is List<LowConfidenceDto>
        // { id, patientName, patientId, predictedDisease, confidence, submittedOn, status }
        setReports(data);
      })
      .catch((err) => {
        console.error("Error fetching low-confidence reports:", err);
        setError(err.message || "Unexpected error");
      })
      .finally(() => setLoading(false));
  }, []);

  // ✅ Mark a report as reviewed (PUT backend + update UI)
  const handleMarkReviewed = async (id) => {
    const token = localStorage.getItem("token");

    if (!token) {
      window.location.href = "/";
      return;
    }

    try {
      const res = await fetch(
        `http://localhost:8080/api/doctor/low-confidence/${id}/review`,
        {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!res.ok) {
        throw new Error("Failed to mark report as reviewed");
      }

      // Update local state
      setReports((prev) =>
        prev.map((r) =>
          r.id === id ? { ...r, status: "Reviewed" } : r
        )
      );
    } catch (err) {
      console.error("Error marking report reviewed:", err);
      alert("Could not mark as reviewed. Please try again.");
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* 🩺 Doctor Navbar */}
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <DoctorNav activePage="Low Confidence" />
      </header>

      <div className="m-6">
        <h1 className="text-3xl font-bold mb-4">Low Confidence Reports</h1>
        <p className="text-gray-700 mb-6">
          Review reports automatically flagged by the AI engine for low
          confidence. Confirm or correct predictions to improve model accuracy.
        </p>

        {loading && (
          <div className="mb-4 text-gray-600">Loading flagged reports…</div>
        )}

        {error && (
          <div className="mb-4 text-red-600 font-medium">
            {error}
          </div>
        )}

        {/* 🧾 Reports Table */}
        <div className="bg-white rounded-lg shadow-lg p-6">
          <h2 className="text-2xl font-semibold mb-4">Flagged Predictions</h2>

          {reports.length === 0 && !loading ? (
            <p className="text-gray-500">No low-confidence reports found.</p>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-200 text-gray-700">
                  <th className="p-3">Patient</th>
                  <th className="p-3">Predicted Disease</th>
                  <th className="p-3">Confidence</th>
                  <th className="p-3">Submitted On</th>
                  <th className="p-3">Status</th>
                  <th className="p-3">Actions</th>
                </tr>
              </thead>
              <tbody>
                {reports.map((report) => {
                  // backend sends confidence as 0–1 double; show as %
                  const confidencePct = Math.round(report.confidence * 100);

                  return (
                    <tr
                      key={report.id}
                      className="border-b hover:bg-gray-50 transition"
                    >
                      <td className="p-3">{report.patientName}</td>
                      <td className="p-3">{report.predictedDisease}</td>
                      <td
                        className={`p-3 font-semibold ${
                          report.confidence < 0.5
                            ? "text-red-600"
                            : "text-yellow-600"
                        }`}
                      >
                        {confidencePct}%
                      </td>
                      <td className="p-3">
                        {report.submittedOn
                          ? new Date(report.submittedOn).toLocaleString()
                          : "—"}
                      </td>
                      <td
                        className={`p-3 font-medium ${
                          report.status === "Reviewed"
                            ? "text-green-600"
                            : "text-blue-600"
                        }`}
                      >
                        {report.status}
                      </td>
                      <td className="p-3">
                        {report.status === "Pending Review" ? (
                          <button
                            onClick={() => handleMarkReviewed(report.id)}
                            className="px-3 py-1 bg-[#b0372b] text-white rounded hover:bg-[#992c23] transition"
                          >
                            Mark Reviewed
                          </button>
                        ) : (
                          <span className="text-gray-500 text-sm italic">
                            ✓ Done
                          </span>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>

        
      </div>
    </div>
  );
}
