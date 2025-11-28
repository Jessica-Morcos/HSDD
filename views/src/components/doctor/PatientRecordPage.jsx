import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import DoctorNav from "./DoctorNav";

export default function PatientRecordPage() {
  const { patientId } = useParams();
  const navigate = useNavigate();

  const [record, setRecord] = useState(null);
  const [loading, setLoading] = useState(true);

  const [history, setHistory] = useState([]);
  const [newTitle, setNewTitle] = useState("");
  const [newDetails, setNewDetails] = useState("");

  const token = localStorage.getItem("token");

  // -----------------------------------------
  // Load predictions + trends
  // -----------------------------------------
  useEffect(() => {
    if (!token) {
      navigate("/");
      return;
    }

    async function loadRecord() {
      try {
        const res = await fetch(
          `http://localhost:8080/api/doctor/patient/${patientId}/full-record`,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (!res.ok) throw new Error("Failed to load patient record");

        const data = await res.json();
        setRecord(data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }

    loadRecord();
  }, [patientId, token, navigate]);

  // -----------------------------------------
  // Load Medical History
  // -----------------------------------------
  useEffect(() => {
    if (!token) return;

    fetch(`http://localhost:8080/api/history?patientId=${patientId}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then((data) => setHistory(data))
      .catch((err) => console.error("Error loading history:", err));
  }, [patientId, token]);

  // -----------------------------------------
  // Add medical-history entry
  // -----------------------------------------
  async function handleAddHistory() {
    if (!newTitle.trim() || !newDetails.trim()) {
      alert("Please fill in all fields");
      return;
    }

    const formData = new FormData();
    formData.append("patientId", patientId);
    formData.append("title", newTitle.trim());
    formData.append("details", newDetails.trim());

    const res = await fetch("http://localhost:8080/api/history/add", {
      method: "POST",
      headers: { Authorization: `Bearer ${token}` },
      body: formData,
    });

    if (!res.ok) {
      alert("Failed to add medical history entry");
      return;
    }

    const added = await res.json();
    setHistory((prev) => [added, ...prev]);

    setNewTitle("");
    setNewDetails("");
  }

  // -----------------------------------------
  // Rendering
  // -----------------------------------------
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center text-lg">
        Loading patient record…
      </div>
    );
  }

  if (!record) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center text-lg">
        Failed to load patient record.
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* NAVBAR */}
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <DoctorNav activePage="Patient Record" />
      </header>

      <div className="m-6">
        <button
          onClick={() => navigate("/doctor/patients")}
          className="px-4 py-2 mb-4 bg-gray-300 text-gray-800 rounded hover:bg-gray-400"
        >
          ← Back to Patients
        </button>

        <h1 className="text-3xl font-bold mb-2">Patient Record</h1>
        <p className="text-gray-600 mb-6">Patient ID: {patientId}</p>

        {/* Predictions */}
        <div className="bg-white shadow-lg rounded-lg p-6 mb-8">
          <h2 className="text-2xl font-semibold mb-4">Predictions History</h2>

          {record.predictions.length === 0 ? (
            <p className="text-gray-500">No prediction history available.</p>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-200 text-gray-700">
                  <th className="p-3">ID</th>
                  <th className="p-3">Symptom ID</th>
                  <th className="p-3">Label</th>
                  <th className="p-3">Confidence</th>
                  <th className="p-3">Date</th>
                </tr>
              </thead>
              <tbody>
                {record.predictions.map((p) => (
                  <tr key={p.id} className="border-b hover:bg-gray-50 transition">
                    <td className="p-3">{p.id}</td>
                    <td className="p-3">{p.symptomId}</td>
                    <td className="p-3">{p.label}</td>
                    <td className="p-3">{(p.confidence * 100).toFixed(1)}%</td>
                    <td className="p-3">
                      {new Date(p.createdAt).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Medical History */}
        <div className="bg-white shadow-lg rounded-lg p-6 mb-6">
          <h2 className="text-2xl font-semibold mb-4">Medical History</h2>

          {history.length === 0 ? (
            <p className="text-gray-500">No medical history available.</p>
          ) : (
            <ul className="space-y-3">
              {history.map((h) => (
                <li key={h.id} className="border border-gray-200 rounded p-3">
                  <p className="font-semibold">{h.title}</p>
                  <p className="text-gray-700">{h.details}</p>
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Add new history */}
        <div className="bg-white shadow-lg rounded-lg p-6 mb-8">
          <h3 className="text-xl font-semibold mb-2">
            Add New Medical History Entry
          </h3>

          <input
            type="text"
            placeholder="Title (e.g., Allergy, Surgery)"
            className="w-full border p-2 rounded mb-2"
            value={newTitle}
            onChange={(e) => setNewTitle(e.target.value)}
          />

          <textarea
            rows="3"
            placeholder="Details…"
            className="w-full border p-2 rounded mb-2"
            value={newDetails}
            onChange={(e) => setNewDetails(e.target.value)}
          />

          <button
            onClick={handleAddHistory}
            className="bg-[#b0372b] text-white px-4 py-2 rounded hover:bg-[#992c23]"
          >
            Add Entry
          </button>
        </div>

        {/* Trends */}
        <div className="bg-white shadow-lg rounded-lg p-6">
          <h2 className="text-2xl font-semibold mb-4">Diagnosis Trends</h2>

          {record.trends.length === 0 ? (
            <p className="text-gray-500">No trend data available.</p>
          ) : (
            <ul className="list-disc ml-6">
              {record.trends.map((t, index) => (
                <li key={index} className="text-gray-700 mb-1">
                  <strong>{t.label}</strong>: {t.count}{" "}
                  {t.count === 1 ? "occurrence" : "occurrences"}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
