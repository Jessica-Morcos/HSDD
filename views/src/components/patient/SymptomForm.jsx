import { useState, useEffect } from "react";
import PatientNav from "./PatientNav";
import { submitSymptoms } from "../../api";

export default function SymptomForm() {
  const [text, setText] = useState("");
  const [tags, setTags] = useState([]);
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState("");

  const token = localStorage.getItem("token");
  const patientId = localStorage.getItem("patientId");

  useEffect(() => {
    console.log("🔥 SymptomForm loaded with:");
    console.log("token:", token);
    console.log("patientId:", patientId, "type:", typeof patientId);
  }, []);

  const TAG_OPTIONS = [
    "fever",
    "cough",
    "fatigue",
    "headache",
    "nausea",
    "rash",
    "shortness of breath",
  ];

  const toggleTag = (tag) => {
    setTags((prev) =>
      prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMsg("");

    if (!token || !patientId) {
      setMsg("❌ Missing authentication. Please log in again.");
      setLoading(false);
      return;
    }

    try {
      console.log("📡 Sending payload:", {
        patientId,
        text,
        tags,
      });

      const res = await submitSymptoms(token, patientId, text, tags);

      console.log("🔥 FINAL BACKEND RESPONSE OBJECT:", res);

      setMsg("✅ Symptoms submitted successfully!");
      setText("");
      setTags([]);
    } catch (err) {
      console.error("🔥 ERROR IN SUBMISSION:", err);
      setMsg("❌ Could not submit symptoms.");
    }

    setLoading(false);
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <PatientNav activePage="Symptom Form" />
      </header>

      <div className="m-6">
        <h1 className="text-3xl font-bold mb-4">Symptom Form</h1>

        <p className="text-gray-700">
          Log new or recurring symptoms to help your doctor track your health progress.
        </p>

        <div className="mt-6 bg-white border border-gray-300 rounded-lg p-6 shadow-sm max-w-xl">
          <form className="flex flex-col gap-4" onSubmit={handleSubmit}>

            <label className="font-medium">Describe your symptoms</label>
            <textarea
              placeholder="E.g., fever, cough, headache..."
              className="border border-gray-300 rounded-md p-3 resize-none focus:ring-2 focus:ring-[#b0372b]"
              rows="4"
              value={text}
              onChange={(e) => setText(e.target.value)}
              required
            />

            <div>
              <label className="font-medium">Select matching symptoms</label>
              <div className="grid grid-cols-2 gap-2 mt-2">
                {TAG_OPTIONS.map((tag) => (
                  <label key={tag} className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={tags.includes(tag)}
                      onChange={() => toggleTag(tag)}
                      className="w-4 h-4 text-[#b0372b]"
                    />
                    <span className="text-gray-700 capitalize">{tag}</span>
                  </label>
                ))}
              </div>
            </div>

            <button
              type="submit"
              className="bg-[#b0372b] text-white rounded-md py-2 hover:bg-[#962b21] transition"
              disabled={loading}
            >
              {loading ? "Submitting..." : "Submit Symptoms"}
            </button>

            {msg && (
              <p
                className={`mt-2 text-sm ${
                  msg.startsWith("❌") ? "text-red-600" : "text-green-700"
                }`}
              >
                {msg}
              </p>
            )}
          </form>
        </div>
      </div>
    </div>
  );
}
