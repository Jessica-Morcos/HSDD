import { useState, useEffect } from "react";
import PatientNav from "./PatientNav";

export default function MedicalHistoryPage() {
  const [file, setFile] = useState(null);
  const [uploadStatus, setUploadStatus] = useState("");
  const [history, setHistory] = useState([]);

  const token = localStorage.getItem("token");
  const patientId = localStorage.getItem("patientId");

  const fetchHistory = async () => {
    try {
      const res = await fetch(
        `http://localhost:8080/api/history?patientId=${patientId}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      if (!res.ok) {
        setUploadStatus("Failed to load medical history.");
        return;
      }

      const data = await res.json();
      setHistory(data);
    } catch {
      setUploadStatus("Server error.");
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const uploadFile = async (e) => {
    e.preventDefault();

    if (!file) {
      setUploadStatus("Select a file first.");
      return;
    }

    const formData = new FormData();
    formData.append("patientId", patientId);
    formData.append("file", file);

    const res = await fetch("http://localhost:8080/api/history/upload", {
      method: "POST",
      body: formData,
      headers: { Authorization: `Bearer ${token}` },
    });

    if (res.ok) {
      setUploadStatus("Upload successful.");
      fetchHistory();
    } else {
      setUploadStatus("Upload failed.");
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-[#b0372b] p-4">
        <PatientNav activePage="Medical History" />
      </header>

      <div className="p-6 max-w-3xl mx-auto">
        <h1 className="text-3xl font-bold mb-4">Medical History</h1>

        {/* UPLOAD */}
        <form
          onSubmit={uploadFile}
          className="bg-white p-4 rounded border shadow mb-6"
        >
          <input
            type="file"
            onChange={(e) => setFile(e.target.files[0])}
            className="mb-4"
          />
          <button
            className="bg-[#b0372b] text-white px-4 py-2 rounded"
            type="submit"
          >
            Upload
          </button>
          {uploadStatus && <p className="mt-2 text-gray-700">{uploadStatus}</p>}
        </form>

        {/* HISTORY LIST */}
        <div className="bg-white p-6 rounded shadow border">
          <h2 className="text-xl font-bold mb-4">Past Records</h2>
          {history.length === 0 ? (
            <p>No records found.</p>
          ) : (
            <ul className="divide-y">
              {history.map((h) => (
                <li key={h.id} className="py-3">
                  <strong>{h.title}</strong>
                  <br />
                  <span className="text-gray-700">{h.details}</span>
                  <br />
                  <small className="text-gray-500">
                    {new Date(h.diagnosedAt).toLocaleDateString()}
                  </small>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
