import { useState, useEffect } from "react";
import AdminNav from "./AdminNav";
import { fetchAuditLogs } from "../../api";

// Convert eventType → readable text
function formatType(eventType) {
  if (eventType.startsWith("ADMIN_CREATE")) return "Account";
  if (eventType.startsWith("ADMIN_UPDATE")) return "Account";
  if (eventType.startsWith("ADMIN_DEACTIVATE")) return "Account";
  if (eventType.startsWith("ADMIN_REACTIVATE")) return "Account";

  if (eventType.includes("LOGIN") || eventType.includes("SIGNUP"))
    return "Access";

  return "System";
}

// Convert severity based on event
function getSeverity(eventType) {
  if (eventType.includes("DEACTIVATE")) return "Medium";
  if (eventType.includes("REACTIVATE")) return "Low";
  if (eventType.includes("UPDATE")) return "Low";
  if (eventType.includes("CREATE")) return "Low";

  return "Low"; // default
}

// Format timestamp nicely
function formatDate(iso) {
  const d = new Date(iso);
  return d.toLocaleString("en-US", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export default function ViewLogsPage() {
  const [logs, setLogs] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchAuditLogs()
      .then((data) => {
        // Convert backend logs → UI logs
        const formatted = data.map((log) => ({
          id: log.id,
          timestamp: formatDate(log.eventTime),
          user: log.actor,
          action: log.eventType.replace(/_/g, " "),
          type: formatType(log.eventType),
          severity: getSeverity(log.eventType),
          details: log.details,
        }));

        setLogs(formatted);
      })
      .catch((err) => {
        console.error("Error fetching logs:", err);
        setError("Failed to load logs");
      });
  }, []);

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* Navbar */}
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <AdminNav activePage="View Logs" />
      </header>

      <div className="m-6">
        <h1 className="text-3xl font-bold mb-4">View Logs</h1>

        <p className="text-gray-700 mb-6">
          Review recent activities, security events, and audit trails across the platform.
        </p>

        {error && (
          <div className="mb-4 p-3 bg-red-100 text-red-700 rounded">
            {error}
          </div>
        )}

        {/* Logs Table */}
        <div className="bg-white rounded-lg shadow-lg p-6">
          <h2 className="text-2xl font-semibold mb-4">Recent Activity Logs</h2>

          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-200 text-gray-700">
                <th className="p-3">Timestamp</th>
                <th className="p-3">User</th>
                <th className="p-3">Action</th>
                <th className="p-3">Type</th>
                <th className="p-3">Severity</th>
              </tr>
            </thead>

            <tbody>
              {logs.map((log) => (
                <tr key={log.id} className="border-b hover:bg-gray-50 transition">
                  <td className="p-3">{log.timestamp}</td>
                  <td className="p-3">{log.user}</td>
                  <td className="p-3">
                    {log.action}
                    <div className="text-gray-500 text-xs mt-1">{log.details}</div>
                  </td>
                  <td className="p-3">{log.type}</td>

                  <td
                    className={`p-3 font-semibold ${
                      log.severity === "High"
                        ? "text-red-600"
                        : log.severity === "Medium"
                        ? "text-yellow-600"
                        : "text-green-600"
                    }`}
                  >
                    {log.severity}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Placeholder removed since backend is now connected */}
      </div>
    </div>
  );
}
