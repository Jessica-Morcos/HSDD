import { useState, useEffect } from "react";

import carepathLogo from "../assets/carepath-logo.svg";
import { Link } from "react-router-dom";
import DashboardCard from "./DashboardCard";
import userIcon from "../assets/user.svg";
import systemHealthIcon from "../assets/SystemHealth.svg";
import logsIcon from "../assets/Logs.svg";
import settingsIcon from "../assets/Settings.svg";

export default function DashboardAdmin() {
  const [language, setLanguage] = useState("en");
  const [open, setOpen] = useState(false);
  const t = (en, fr) => (language === "en" ? en : fr);

  const [activeUsers, setActiveUsers] = useState(null);
  const [systemStatus, setSystemStatus] = useState("Loading...");
  const [logCount, setLogCount] = useState(null);
  const [recentLogs, setRecentLogs] = useState([]);

  // ======================================================
  //                     LOGOUT FIX
  // ======================================================
  const handleLogout = () => {
   // 👈 resets login state
  window.location.href = "/";
};


  // ======================================================
  //                FETCH ALL DASHBOARD DATA
  // ======================================================
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      // If no token, force logout
      handleLogout();
      return;
    }

    // Fetch recent logs
    fetch("http://localhost:8080/api/admin/audit-logs/recent", {
      headers: { Authorization: "Bearer " + token },
    })
      .then((res) => res.json())
      .then((data) => {
        const formatted = data.map((log) => {
          const time = new Date(log.eventTime).toLocaleString("en-US");
          return `${time} — ${log.eventType.replace(/_/g, " ")} — ${log.details}`;
        });
        setRecentLogs(formatted);
      })
      .catch((err) => console.error("Failed to load recent logs:", err));

    // Fetch active users
    fetch("http://localhost:8080/api/admin/system-health", {
      headers: { Authorization: "Bearer " + token },
    })
      .then((res) => res.json())
      .then((data) => {
        setActiveUsers(data.activeUsers);
        setSystemStatus("No Issues Detected");
      });

    // Fetch total audit log count
    fetch("http://localhost:8080/api/admin/audit-logs?limit=1000", {
      headers: { Authorization: "Bearer " + token },
    })
      .then((res) => res.json())
      .then((logs) => {
        setLogCount(logs.length);
      });
  }, []);

  // ======================================================
  //                          RENDER
  // ======================================================
  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* Header */}
      <header className="flex items-center justify-between bg-[#b0372b] p-4 relative">
        <img src={carepathLogo} alt="CarePath" className="w-28" />

        {/* Language Toggle */}
        <button
          onClick={() => setLanguage(language === "en" ? "fr" : "en")}
          className="absolute right-[13rem] top-6 text-xs text-white border border-white rounded-full px-3 py-1 hover:bg-white hover:text-[#b0372b] transition"
        >
          {language === "en" ? "Français" : "English"}
        </button>

        {/* Account Dropdown */}
        <div className="relative">
          <button
            onClick={() => setOpen(!open)}
            className="text-white font-medium focus:outline-none"
          >
            {t("Account Settings ▾", "Paramètres du compte ▾")}
          </button>

          {open && (
            <div className="absolute right-0 mt-2 w-48 bg-white text-black rounded-md shadow-lg border border-gray-200 z-10">
              <Link
                to="/profile"
                className="block px-4 py-2 hover:bg-gray-100"
                onClick={() => setOpen(false)}
              >
                {t("Profile", "Profil")}
              </Link>

              <Link
                to="/change-password"
                className="block px-4 py-2 hover:bg-gray-100"
                onClick={() => setOpen(false)}
              >
                {t("Change Password", "Changer le mot de passe")}
              </Link>

              <hr className="border-gray-200" />

              {/* LOGOUT */}
              <button
                onClick={() => {
                  setOpen(false);
                  handleLogout();
                }}
                className="block w-full text-left px-4 py-2 text-red-600 hover:bg-gray-100"
              >
                {t("Logout", "Se déconnecter")}
              </button>
            </div>
          )}
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 px-10 py-8">
        <h1 className="text-4xl font-bold mb-2">
          {t("Welcome Back, Admin Jess", "Bon retour, Admin Jess")}
        </h1>

        <p className="mb-[5rem] text-gray-700">
          {new Date().toLocaleDateString(language === "en" ? "en-US" : "fr-FR", {
            month: "long",
            day: "numeric",
            year: "numeric",
          })}
        </p>

        {/* Notifications Preview */}
        <div className="bg-yellow-50 border border-yellow-300 rounded-xl p-5 mb-10 mx-[2rem] shadow-sm w-[40rem]">
          <h2 className="font-semibold mb-3 text-yellow-900 flex items-center gap-2 text-lg">
            <span>🔔</span> Recent Logs
          </h2>

          <ul className="space-y-2">
            {recentLogs.length > 0 ? (
              recentLogs.map((log, idx) => (
                <li
                  key={idx}
                  className="bg-white border border-yellow-200 rounded-lg p-3 text-sm text-gray-800 shadow-sm hover:bg-gray-50 transition"
                >
                  {log}
                </li>
              ))
            ) : (
              <li className="text-gray-600 text-sm">Loading recent logs...</li>
            )}
          </ul>

          <p className="text-right text-xs mt-3">
            <Link
              to="/notifications"
              className="text-blue-600 hover:underline hover:text-blue-800 transition"
            >
              see all..
            </Link>
          </p>
        </div>

        {/* Dashboard Cards */}
        <div className="grid grid-cols-2 max-w-5xl mx-[3rem]">
          <DashboardCard
            to="/admin/manage-users"
            title={t("Manage Users", "Gérer les utilisateurs")}
            subtitle={
              activeUsers !== null ? `${activeUsers} Active Users` : "Loading..."
            }
            img={userIcon}
          />

          <DashboardCard
            to="/admin/system-health"
            title={t("System Health", "Santé du système")}
            subtitle={systemStatus}
            img={systemHealthIcon}
          />

          <DashboardCard
            to="/admin/view-logs"
            title={t("View Logs", "Voir les journaux")}
            subtitle={logCount !== null ? `${logCount} Recent Events` : "Loading..."}
            img={logsIcon}
          />

          <DashboardCard
            to="/admin/settings"
            title={t("Settings", "Paramètres")}
            subtitle={t("Configure Application", "Configurer l'application")}
            img={settingsIcon}
          />
        </div>
      </main>
    </div>
  );
}
