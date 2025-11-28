import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import DoctorNav from "./DoctorNav";

export default function ReportDetailsPage() {
  const { reportId } = useParams();

  const [report, setReport] = useState(null);
  const [annotations, setAnnotations] = useState([]);
  const [issues, setIssues] = useState([]);

  const [notes, setNotes] = useState("");
  const [correctedLabel, setCorrectedLabel] = useState("");

  // ⭐️ NEW
  const [reviewStatus, setReviewStatus] = useState("Pending Review");

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // -------------------------------------------------------
  // FETCH REPORT + ITS ANNOTATIONS + ISSUES
  // -------------------------------------------------------
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) return window.location.href = "/";

    setLoading(true);

    Promise.all([
      fetch(`http://localhost:8080/api/doctor/reports`, {
        headers: { Authorization: `Bearer ${token}` }
      }).then(res => res.json()),

      fetch(`http://localhost:8080/api/doctor/predictions/${reportId}/annotations`, {
        headers: { Authorization: `Bearer ${token}` }
      }).then(res => res.json()),

      fetch(`http://localhost:8080/api/doctor/predictions/${reportId}/issues`, {
        headers: { Authorization: `Bearer ${token}` }
      }).then(res => res.json()),
    ])
      .then(([allReports, annos, issues]) => {
        const r = allReports.find(r => r.id == reportId);
        if (!r) throw new Error("Report not found");

        setReport(r);
        setAnnotations(annos);
        setIssues(issues);

        // ⭐️ Load correct current status
        setReviewStatus(r.status || "Pending Review");
      })
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));

  }, [reportId]);

  // -------------------------------------------------------
  // CREATE ANNOTATION
  // -------------------------------------------------------
  const handleCreateAnnotation = () => {
    const token = localStorage.getItem("token");

    fetch("http://localhost:8080/api/doctor/annotations", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify({
        predictionId: Number(reportId),
        notes,
        correctedLabel
      })
    })
      .then(res => res.json())
      .then(newAnno => {
        setAnnotations(prev => [...prev, newAnno]);
        setNotes("");
        setCorrectedLabel("");
      })
      .catch(err => alert("Failed to add annotation: " + err.message));
  };

  // -------------------------------------------------------
  // ⭐️ NEW — UPDATE REVIEW STATUS
  // -------------------------------------------------------
  const handleUpdateStatus = async () => {
    const token = localStorage.getItem("token");
    if (!token) return;

    const endpoint =
      reviewStatus === "Reviewed"
        ? `/api/doctor/low-confidence/${reportId}/review`
        : `/api/doctor/low-confidence/${reportId}/pending`;

    const res = await fetch(`http://localhost:8080${endpoint}`, {
      method: "PUT",
      headers: { Authorization: `Bearer ${token}` }
    });

    if (!res.ok) {
      alert("Failed to update review status");
      return;
    }

    alert(
      reviewStatus === "Reviewed"
        ? "Marked as Reviewed and removed from Flagged Reports."
        : "Marked as Pending Review and added back to Flagged Reports."
    );
  };

  // -------------------------------------------------------
  // RENDER
  // -------------------------------------------------------
  if (loading) return <div className="p-8">Loading…</div>;
  if (error) return <div className="p-8 text-red-600">{error}</div>;

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <header className="bg-[#b0372b] p-4">
        <DoctorNav activePage="All Reports" />
      </header>

      <div className="m-6">
        <h1 className="text-3xl font-bold mb-4">Report Details</h1>

        {/* ------------------ SUMMARY CARD ------------------- */}
        <div className="bg-white shadow p-6 rounded mb-6">
          <h2 className="text-xl font-semibold mb-4">Summary</h2>

          <p><strong>Patient:</strong> {report.patientName}</p>
          <p><strong>Diagnosis:</strong> {report.disease}</p>
          <p><strong>Confidence:</strong> {Math.round(report.confidence * 100)}%</p>
          <p><strong>Date:</strong> {report.createdAt}</p>

          {/* ⭐️ NEW: STATUS TOGGLE */}
          <div className="mt-4">
            <label className="font-medium text-gray-800 mr-3">Review Status:</label>
            <select
              value={reviewStatus}
              onChange={(e) => setReviewStatus(e.target.value)}
              className="border border-gray-300 rounded px-3 py-2 focus:ring focus:ring-[#b0372b]"
            >
              <option>Pending Review</option>
              <option>Reviewed</option>
            </select>

            <button
              onClick={handleUpdateStatus}
              className="ml-4 px-4 py-2 bg-[#b0372b] text-white rounded hover:bg-[#992c23]"
            >
              Save Status
            </button>
          </div>
        </div>

        {/* ------------------ ANNOTATIONS ------------------- */}
        <div className="bg-white shadow p-6 rounded mb-6">
          <h2 className="text-xl font-semibold mb-4">Doctor Annotations</h2>

          {annotations.length === 0 && (
            <p className="text-gray-500 mb-4">No annotations yet.</p>
          )}

          {annotations.map(a => (
            <div key={a.id} className="border p-3 rounded mb-3">
              <p><strong>Doctor:</strong> {a.doctor}</p>
              <p><strong>Notes:</strong> {a.notes}</p>
              <p><strong>Corrected Label:</strong> {a.correctedLabel ?? "—"}</p>
            </div>
          ))}

          <h3 className="font-semibold mt-6 mb-2">Add New Annotation</h3>
          <textarea
            className="border p-2 rounded w-full mb-2"
            placeholder="Notes"
            value={notes}
            onChange={e => setNotes(e.target.value)}
          />
          <input
            className="border p-2 rounded w-full mb-2"
            placeholder="Corrected Label"
            value={correctedLabel}
            onChange={e => setCorrectedLabel(e.target.value)}
          />

          <button
            onClick={handleCreateAnnotation}
            className="px-4 py-2 bg-[#b0372b] text-white rounded hover:bg-[#992c23]"
          >
            Add Annotation
          </button>
        </div>

        {/* ------------------ ISSUES ------------------- */}
        <div className="bg-white shadow p-6 rounded">
          <h2 className="text-xl font-semibold mb-4">Issue Reports</h2>

          {issues.length === 0 ? (
            <p className="text-gray-500">No issues reported.</p>
          ) : (
            issues.map(i => (
              <div key={i.id} className="border p-3 rounded mb-3">
                <p><strong>Description:</strong> {i.issueDescription}</p>
                <p><strong>Correct Label:</strong> {i.correctLabel}</p>
                <p><strong>Submitted By:</strong> {i.doctor}</p>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
