// src/components/NotificationView.jsx
import { Link } from "react-router-dom";
import { useState, useEffect } from "react";

export default function NotificationView({ userRole }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const title =
    userRole === "admin"
      ? "System Logs"
      : userRole === "doctor"
      ? "All Notifications"
      : "Your Notifications";

  useEffect(() => {
    const token = localStorage.getItem("token");
    setLoading(true);
    setError("");

    // -------------------------
    // ADMIN → audit logs
    // -------------------------
    if (userRole === "admin") {
      if (!token) {
        setError("Missing token");
        setLoading(false);
        return;
      }

      fetch("http://localhost:8080/api/admin/audit-logs/recent", {
        headers: { Authorization: `Bearer ${token}` },
      })
        .then((res) => {
          if (!res.ok) throw new Error("Failed to load logs");
          return res.json();
        })
        .then((data) => {
          const formatted = data.map((log) => ({
            id: log.id,
            text: `${new Date(log.eventTime).toLocaleString(
              "en-US"
            )} — ${log.eventType.replace(/_/g, " ")} — ${log.details}`,
          }));
          setItems(formatted);
        })
        .catch((err) => setError(err.message))
        .finally(() => setLoading(false));

      return;
    }

    // -------------------------
    // DOCTOR → real notifications
    // -------------------------
    if (userRole === "doctor") {
      if (!token) {
        setError("Missing token");
        setLoading(false);
        return;
      }

      fetch("http://localhost:8080/api/doctor/notifications", {
        headers: { Authorization: `Bearer ${token}` },
      })
        .then((res) => {
          if (res.status === 403) {
            throw new Error("Forbidden – doctor role required.");
          }
          if (!res.ok) throw new Error("Failed to load notifications");
          return res.json();
        })
        .then((data) => {
          const formatted = data.map((n) => ({
            id: n.id,
            predictionId: n.predictionId,
            text: n.message,
            createdAt: n.createdAt
              ? new Date(n.createdAt).toLocaleString("en-US")
              : "",
            read: n.readFlag,
          }));
          setItems(formatted);
        })
        .catch((err) => setError(err.message))
        .finally(() => setLoading(false));

      return;
    }

    // -------------------------
    // PATIENT / default
    // -------------------------
    const defaultNotifications = [
      { id: 1, text: "Doctor reviewed your last report" },
      { id: 2, text: "New pattern detected in your data" },
      { id: 3, text: "AI model updated for improved accuracy" },
    ];
    setItems(defaultNotifications);
    setLoading(false);
  }, [userRole]);

  // -------------------------
  // Doctor click → mark as read
  // -------------------------
  const handleClickNotification = (item) => {
    if (userRole !== "doctor") return;

    const token = localStorage.getItem("token");
    if (!token) return;

    fetch(`http://localhost:8080/api/doctor/notifications/${item.id}/read`, {
      method: "POST",
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => {
        if (!res.ok) throw new Error("Failed to mark notification as read");
        // remove from list locally
        setItems((prev) => prev.filter((n) => n.id !== item.id));
      })
      .catch((err) => console.error("Mark read failed:", err));
  };

  // -------------------------
  // Render
  // -------------------------
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 flex flex-col">
        <header className="flex justify-between items-center bg-[#b0372b] text-white p-4 shadow-md">
          <h1 className="text-lg font-semibold">{title}</h1>
          <Link
            to="/"
            className="text-white border border-white px-3 py-1 rounded-full hover:bg-white hover:text-[#b0372b] transition"
          >
            ← Back to Dashboard
          </Link>
        </header>
        <main className="flex-1 p-8">
          <p className="text-gray-700">Loading…</p>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* Navigation Bar */}
      <header className="flex justify-between items-center bg-[#b0372b] text-white p-4 shadow-md">
        <h1 className="text-lg font-semibold">{title}</h1>
        <Link
          to="/"
          className="text-white border border-white px-3 py-1 rounded-full hover:bg-white hover:text-[#b0372b] transition"
        >
          ← Back to Dashboard
        </Link>
      </header>

      <main className="flex-1 p-8">
        <h2 className="text-2xl font-bold mb-4">
          All Recent {userRole === "admin" ? "Logs" : "Notifications"}
        </h2>

        {error && (
          <p className="text-red-600 mb-4 text-sm">
            {error}
          </p>
        )}

        <ul className="space-y-3">
          {items.map((item) => (
            <li
              key={item.id}
              onClick={() => handleClickNotification(item)}
              className={`bg-white border border-gray-300 rounded-lg p-4 hover:shadow-md transition text-gray-700 ${
                userRole === "doctor"
                  ? "cursor-pointer hover:border-[#b0372b]"
                  : ""
              }`}
            >
              <div className="flex justify-between items-center">
                <span>{item.text}</span>
                {item.createdAt && (
                  <span className="text-xs text-gray-500 ml-3">
                    {item.createdAt}
                  </span>
                )}
              </div>
            </li>
          ))}

          {items.length === 0 && !error && (
            <li className="text-gray-500 text-sm">No notifications to show.</li>
          )}
        </ul>
      </main>
    </div>
  );
}
