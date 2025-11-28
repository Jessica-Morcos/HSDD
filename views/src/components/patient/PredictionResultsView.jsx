import { useState, useEffect } from "react";
import PatientNav from "./PatientNav";

export default function PredictionResultsView() {
  const [predictions, setPredictions] = useState([]);
  const [error, setError] = useState("");

  const token = localStorage.getItem("token");
  const patientId = localStorage.getItem("patientId");

  // ----------------------------
  // Fetch Predictions From Backend
  // ----------------------------
  const fetchPredictions = async () => {
    try {
      const res = await fetch(
        `http://localhost:8080/api/records/predictions?patientId=${patientId}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      if (!res.ok) {
        setError("Failed to load predictions.");
        return;
      }

      const data = await res.json();
      console.log("BACKEND PREDICTIONS:", data);

      // Map backend → UI
      const mapped = data.map((p) => ({
        id: p.id,
        disease: p.correctedLabel || p.label, // doctor override applied
        confidence: Math.round(p.confidence * 100),
        date: new Date(p.createdAt).toLocaleDateString(),
        doctor: p.doctorUsername || null,
        notes: p.doctorNotes || "—",
      }));

      setPredictions(mapped);
    } catch (err) {
      console.error(err);
      setError("Server connection error.");
    }
  };

  useEffect(() => {
    fetchPredictions();
  }, []);

  const latestPrediction =
    predictions[0] || {
      disease: "No predictions yet",
      confidence: 0,
      date: "-",
      notes: "-",
      doctor: null,
    };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* NAVBAR */}
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4">
        <PatientNav activePage="Prediction Results" />
      </header>

      <div className="m-6">
        <h1 className="text-3xl font-bold mb-4">Prediction Results</h1>
        <p className="text-gray-700 mb-6">
          Review your AI-generated health predictions based on your latest
          symptom entries.
        </p>

        {/* ERROR */}
        {error && <p className="text-red-600 mb-4">{error}</p>}

        {/* LATEST PREDICTION CARD */}
        <div className="bg-white border border-gray-300 rounded-lg p-6 max-w-xl shadow-sm mb-10">
          <h2 className="font-semibold text-lg mb-2 text-gray-800">
            Latest Prediction
          </h2>

          <p className="text-gray-900 text-xl font-bold mb-1">
            {latestPrediction.disease}
          </p>

          <p
            className={`text-sm font-semibold ${
              latestPrediction.confidence >= 85
                ? "text-green-600"
                : latestPrediction.confidence >= 70
                ? "text-yellow-600"
                : "text-red-600"
            }`}
          >
            Confidence: {latestPrediction.confidence}%
          </p>

          <p className="text-sm text-gray-500 mt-3">
            Last Updated: {latestPrediction.date}
          </p>

          {latestPrediction.doctor && (
            <p className="text-xs text-gray-500 mt-1">
              Reviewed by <span className="font-semibold">{latestPrediction.doctor}</span>
            </p>
          )}

          <p className="text-gray-700 mt-2">{latestPrediction.notes}</p>
        </div>

        {/* HISTORY TABLE */}
        <div className="bg-white border border-gray-300 rounded-lg p-6 max-w-3xl shadow-sm">
          <h2 className="text-xl font-semibold mb-4 text-gray-800">
            Previous Predictions
          </h2>

          {predictions.length <= 1 ? (
            <p className="text-gray-600">No previous predictions.</p>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-200 text-gray-700">
                  <th className="p-3">Date</th>
                  <th className="p-3">Prediction</th>
                  <th className="p-3">Confidence</th>
                  <th className="p-3">Doctor Notes</th>
                </tr>
              </thead>
              <tbody>
                {predictions.slice(1).map((p) => (
                  <tr key={p.id} className="border-b hover:bg-gray-50 transition">
                    <td className="p-3">{p.date}</td>

                    <td className="p-3 font-medium text-gray-800">
                      {p.disease}
                      {p.doctor && (
                        <span className="block text-xs text-gray-500">
                          Reviewed by <strong>{p.doctor}</strong>
                        </span>
                      )}
                    </td>

                    <td
                      className={`p-3 font-semibold ${
                        p.confidence >= 85
                          ? "text-green-600"
                          : p.confidence >= 70
                          ? "text-yellow-600"
                          : "text-red-600"
                      }`}
                    >
                      {p.confidence}%
                    </td>

                    <td className="p-3 text-sm text-gray-700">
                      {p.notes || "—"}
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
