import { useEffect, useState } from "react";
import DoctorNav from "./DoctorNav";
import { ChevronLeft, ChevronRight } from "lucide-react";

export default function FlaggedReportPage() {
  const [flaggedReports, setFlaggedReports] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);

  const [annotations, setAnnotations] = useState([]);
  const [notes, setNotes] = useState("");
  const [correctedLabel, setCorrectedLabel] = useState("");
  const [reviewStatus, setReviewStatus] = useState("Pending Review");

  const [loading, setLoading] = useState(true);
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [error, setError] = useState("");

  const token = localStorage.getItem("token");

  // --------------------------------------------------
  // 1. Load all low-confidence (flagged) reports
  // --------------------------------------------------
  useEffect(() => {
    if (!token) {
      window.location.href = "/";
      return;
    }

    setLoading(true);
    setError("");

    fetch("http://localhost:8080/api/doctor/flagged", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then((res) => {
        if (res.status === 403) {
          throw new Error("Forbidden – doctor role required or bad token.");
        }
        if (!res.ok) throw new Error("Failed to load flagged reports.");
        return res.json();
      })
      .then((data) => {
        const normalized = data.map((r) => ({
          id: r.id,
          patientName: r.patientName,
          patientId: r.patientId,
          predictedDisease: r.predictedDisease,
          confidence: Math.round((r.confidence ?? 0) * 100),
          submittedOn: r.submittedOn
            ? new Date(r.submittedOn).toLocaleString()
            : "—",

          symptomDescription: r.symptomDescription ?? "—", // << ADDED

          status: r.status || "Pending Review",
        }));

        setFlaggedReports(normalized);
        if (normalized.length > 0) {
          setReviewStatus(normalized[0].status || "Pending Review");
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [token]);

  const currentReport =
    flaggedReports.length > 0 ? flaggedReports[currentIndex] : null;

  // --------------------------------------------------
  // 2. Load annotations whenever report changes
  // --------------------------------------------------
  useEffect(() => {
    if (!currentReport || !token) {
      setAnnotations([]);
      return;
    }

    setLoadingDetails(true);

    fetch(
      `http://localhost:8080/api/doctor/predictions/${currentReport.id}/annotations`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    )
      .then((res) => {
        if (!res.ok) throw new Error("Failed to load annotations.");
        return res.json();
      })
      .then((data) => setAnnotations(data))
      .catch((err) => console.error("Error loading annotations:", err))
      .finally(() => setLoadingDetails(false));
  }, [currentReport, token]);

  // --------------------------------------------------
  // Helpers
  // --------------------------------------------------
  const resetForm = () => {
    setNotes("");
    setCorrectedLabel("");
    setReviewStatus(currentReport?.status || "Pending Review");
  };

  const handleNext = () => {
    if (currentIndex < flaggedReports.length - 1) {
      setCurrentIndex((idx) => idx + 1);
      resetForm();
    } else {
      alert("All flagged reports have been reviewed.");
    }
  };

  const handlePrev = () => {
    if (currentIndex > 0) {
      setCurrentIndex((idx) => idx - 1);
      resetForm();
    }
  };

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    if (!currentReport || !token) return;

    const predictionId = currentReport.id;

    try {
      // (a) Save annotation if present
      if (notes.trim() || correctedLabel.trim()) {
        const resAnno = await fetch(
          "http://localhost:8080/api/doctor/annotations",
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
              predictionId,
              notes: notes.trim(),
              correctedLabel: correctedLabel.trim() || null,
            }),
          }
        );

        if (!resAnno.ok) throw new Error("Failed to save annotation");

        const newAnno = await resAnno.json();
        setAnnotations((prev) => [...prev, newAnno]);
      }

      // (b) Update backend status
      const isReviewed = reviewStatus === "Reviewed";

      const endpoint = isReviewed
        ? `/api/doctor/low-confidence/${predictionId}/review`
        : `/api/doctor/low-confidence/${predictionId}/pending`;

      const resStatus = await fetch(`http://localhost:8080${endpoint}`, {
        method: "PUT",
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!resStatus.ok) throw new Error("Failed to update review status");

      // (c) UI updates
      if (isReviewed) {
        const updated = flaggedReports.filter((r) => r.id !== predictionId);
        setFlaggedReports(updated);

        if (currentIndex < updated.length) {
          setCurrentIndex(currentIndex);
        } else {
          setCurrentIndex(updated.length - 1);
        }

        alert("Report marked as Reviewed and removed from flagged list.");
      } else {
        setFlaggedReports((prev) =>
          prev.map((r, idx) =>
            idx === currentIndex ? { ...r, status: reviewStatus } : r
          )
        );

        alert("Review saved. Report remains flagged.");
        handleNext();
      }

      setNotes("");
      setCorrectedLabel("");
    } catch (err) {
      alert(err.message || "There was a problem saving your review.");
    }
  };

  // --------------------------------------------------
  // Render
  // --------------------------------------------------
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100">
        <header className="bg-[#b0372b] p-4">
          <DoctorNav activePage="Flagged Report" />
        </header>
        <div className="m-6 text-gray-700">Loading flagged reports…</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-100">
        <header className="bg-[#b0372b] p-4">
          <DoctorNav activePage="Flagged Report" />
        </header>
        <div className="m-6 text-red-600">{error}</div>
      </div>
    );
  }

  if (!currentReport) {
    return (
      <div className="min-h-screen bg-gray-100">
        <header className="bg-[#b0372b] p-4">
          <DoctorNav activePage="Flagged Report" />
        </header>
        <div className="m-6 text-gray-700">No flagged reports found.</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <DoctorNav activePage="Flagged Report" />
      </header>

      <div className="m-6">
        <h1 className="text-3xl font-bold mb-4">Flagged Reports</h1>
        <p className="text-gray-700 mb-6">
          Review and verify all AI-flagged reports for low confidence or
          inconsistencies. Use the arrows to move between flagged cases.
        </p>

        {/* 🩺 Report Navigation Header */}
        <div className="flex items-center justify-between mb-4">
          <button
            onClick={handlePrev}
            disabled={currentIndex === 0}
            className={`flex items-center gap-1 px-3 py-2 rounded-md ${
              currentIndex === 0
                ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                : "bg-[#b0372b] text-white hover:bg-[#992c23]"
            }`}
          >
            <ChevronLeft size={18} /> Previous
          </button>

          <p className="text-gray-700 font-medium">
            Report {currentIndex + 1} of {flaggedReports.length}
          </p>

          <button
            onClick={handleNext}
            disabled={currentIndex === flaggedReports.length - 1}
            className={`flex items-center gap-1 px-3 py-2 rounded-md ${
              currentIndex === flaggedReports.length - 1
                ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                : "bg-[#b0372b] text-white hover:bg-[#992c23]"
            }`}
          >
            Next <ChevronRight size={18} />
          </button>
        </div>

        {/* 🧾 Current Flagged Report */}
        <div className="bg-white rounded-lg shadow-lg p-6 max-w-4xl border border-gray-200">
          <h2 className="text-2xl font-semibold mb-4">
            {currentReport.patientName}'s Flagged Report
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
            <p>
              <strong>Predicted Disease:</strong>{" "}
              {currentReport.predictedDisease}
            </p>

            <p>
              <strong>Confidence:</strong>{" "}
              <span
                className={`font-semibold ${
                  currentReport.confidence < 50
                    ? "text-red-600"
                    : "text-yellow-600"
                }`}
              >
                {currentReport.confidence}%
              </span>
            </p>

            <p>
              <strong>Submitted On:</strong> {currentReport.submittedOn}
            </p>

            <p>
              <strong>Status:</strong> {reviewStatus}
            </p>

            {/* 🆕 Symptoms added EXACTLY as requested */}
            <p className="col-span-2">
              <strong>Symptoms:</strong> {currentReport.symptomDescription}
            </p>
          </div>

          {/* Existing annotations */}
          <div className="mb-6">
            <p className="font-medium mb-2">Existing Doctor Annotations:</p>
            {loadingDetails && (
              <p className="text-gray-500 text-sm">Loading annotations…</p>
            )}
            {!loadingDetails && annotations.length === 0 && (
              <p className="text-gray-500 text-sm">No annotations yet.</p>
            )}
            {!loadingDetails &&
              annotations.map((a) => (
                <div
                  key={a.id}
                  className="border border-gray-200 rounded p-3 mb-2 text-sm"
                >
                  <p>
                    <strong>Doctor:</strong> {a.doctorUsername ?? a.doctor}
                  </p>
                  <p>
                    <strong>Notes:</strong> {a.notes}
                  </p>
                  <p>
                    <strong>Corrected Label:</strong>{" "}
                    {a.correctedLabel || "—"}
                  </p>
                </div>
              ))}
          </div>

          {/* ✏️ Review / new annotation */}
          <form onSubmit={handleSubmitReview} className="space-y-4">
            <div>
              <label className="font-medium text-gray-800">
                Doctor Comments:
              </label>
              <textarea
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Write your observations or corrections..."
                className="w-full border border-gray-300 rounded-lg p-3 mt-2 focus:ring focus:ring-[#b0372b]"
                rows={4}
              />
            </div>

            <div>
              <label className="font-medium text-gray-800">
                Corrected Diagnosis (optional):
              </label>
              <input
                value={correctedLabel}
                onChange={(e) => setCorrectedLabel(e.target.value)}
                placeholder="e.g., Mild Flu"
                className="w-full border border-gray-300 rounded-lg p-2 mt-2 focus:ring focus:ring-[#b0372b]"
              />
            </div>

            <div className="flex flex-wrap items-center gap-3 mt-2">
              <label className="font-medium text-gray-800">Mark as:</label>
              <select
                value={reviewStatus}
                onChange={(e) => setReviewStatus(e.target.value)}
                className="border border-gray-300 rounded px-3 py-2 focus:ring focus:ring-[#b0372b]"
              >
                <option>Pending Review</option>
                <option>Reviewed</option>
              </select>
            </div>

            <button
              type="submit"
              className="bg-[#b0372b] text-white px-6 py-2 rounded hover:bg-[#992c23] transition"
            >
              Submit &amp; Next
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
