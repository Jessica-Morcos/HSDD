import { useState, useEffect } from "react";

import carepathLogo from "../assets/carepath-logo.svg";
import { Link } from "react-router-dom";
import DashboardCard from "./DashboardCard";

import lowconfidence from "../assets/lowconfidenceReport.svg";
import Asset from "../assets/Asset 1.svg";
import reports from "../assets/All reports.svg";
import flagged from "../assets/Stock Image.svg";

export default function DashboardDoctor() {
  const [language, setLanguage] = useState("en");
  const [open, setOpen] = useState(false);

  const username = localStorage.getItem("username") || "User";
  const t = (en, fr) => (language === "en" ? en : fr);

  // ================================
  // STATE FOR DASHBOARD STATS
  // ================================
  const [recentNotifications, setRecentNotifications] = useState([]);
  const [flaggedCount, setFlaggedCount] = useState(0);
  const [latestFlagged, setLatestFlagged] = useState(null);

  // ================================
  // LOGOUT
  // ================================
  const handleLogout = () => {
    const token = localStorage.getItem("token");

    fetch("http://localhost:8080/api/auth/logout", {
      method: "POST",
      headers: { Authorization: "Bearer " + token },
    }).catch(() => {});

    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("patientId");
    localStorage.removeItem("role");

    window.location.href = "/";
  };

  // ================================
  // FETCH NOTIFICATIONS
  // ================================
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) return;

    fetch("http://localhost:8080/api/doctor/notifications", {
      headers: { Authorization: "Bearer " + token },
    })
      .then((res) => res.json())
      .then((data) => {
        setRecentNotifications(Array.isArray(data) ? data : []);
      })
      .catch(() => setRecentNotifications([]));
  }, []);

  // ================================
  // FETCH FLAGGED REPORTS (LOW CONFIDENCE)
  // ================================
  useEffect(() => {
  const token = localStorage.getItem("token");
  if (!token) return;

  fetch("http://localhost:8080/api/doctor/low-confidence", {
    headers: { Authorization: "Bearer " + token },
  })
    .then((res) => res.json())
    .then((data) => {
      setFlaggedCount(data.length);

      if (data.length > 0) {
        // sort by submittedOn (from LowConfidenceDto)
        const sorted = [...data].sort(
          (a, b) =>
            new Date(b.submittedOn ?? 0) - new Date(a.submittedOn ?? 0)
        );
        setLatestFlagged(sorted[0]);
      }
    })
    .catch(() => {
      setFlaggedCount(0);
      setLatestFlagged(null);
    });
}, []);


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
                to="/change-password"
                className="block px-4 py-2 hover:bg-gray-100"
                onClick={() => setOpen(false)}
              >
                {t("Change Password", "Changer le mot de passe")}
              </Link>

              <hr className="border-gray-200" />

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
          {t(`Welcome Back, ${username}`, `Bon retour, ${username}`)}
        </h1>
        <p className="mb-[5rem] text-gray-700">
          {new Date().toLocaleDateString(
            language === "en" ? "en-US" : "fr-FR",
            {
              month: "long",
              day: "numeric",
              year: "numeric",
            }
          )}
        </p>

        {/* Notifications */}
        <div className="bg-yellow-50 border border-yellow-300 rounded-xl p-5 mb-8 mx-[4rem] w-[35rem] shadow-sm">
          <h2 className="font-semibold mb-3 text-yellow-900 flex items-center gap-2 text-lg">
            <span>🔔</span> {t("Recent Notifications", "Notifications récentes")}
          </h2>

          <ul className="space-y-2">
            {recentNotifications.length > 0 ? (
              recentNotifications.slice(0, 2).map((n, i) => (
                <li
                  key={i}
                  className="bg-white border border-yellow-200 rounded-lg p-3 text-sm text-gray-800 shadow-sm hover:bg-gray-50 transition"
                >
                  {n.message}
                  <div className="text-[10px] text-gray-500 mt-1">
                    {new Date(n.createdAt).toLocaleString()}
                  </div>
                </li>
              ))
            ) : (
              <li className="text-gray-700 text-sm italic bg-white border border-yellow-200 rounded-lg p-3 shadow-sm">
                {t(
                  "No new notifications — you're all caught up 🎉",
                  "Aucune nouvelle notification — vous êtes à jour 🎉"
                )}
              </li>
            )}
          </ul>

          <p className="text-right text-xs mt-3">
            <Link
              to="/notifications"
              className="text-orange-950 hover:underline text-orange-950 font-semibold transition"
            >
              {t("See all >", "Voir tout >")}
            </Link>
          </p>
        </div>

        {/* Dashboard Cards */}
        <div className="grid grid-cols-2 max-w-5xl mx-[3rem]">
          <DashboardCard
            to="/doctor/low-confidence"
            title={t("Low Confidence Reports", "Rapports à faible confiance")}
            subtitle={
              flaggedCount > 0
                ? `${flaggedCount} ${t("Reports Flagged", "rapports signalés")}`
                : t("No flagged reports", "Aucun rapport signalé")
            }
            img={lowconfidence}
          />

          <DashboardCard
            to="/doctor/patients"
            title={t("Patients", "Patients")}
            subtitle={t("Review recent reports", "Examiner les rapports récents")}
            img={Asset}
          />

          <DashboardCard
            to="/doctor/all-reports"
            title={t("All Reports", "Tous les rapports")}
            subtitle={t("Review patient cases", "Examiner les cas des patients")}
            img={reports}
          />

          <DashboardCard
              to="/doctor/flagged-report"
              title={t("Flagged Report", "Rapport signalé")}
              subtitle={
                latestFlagged
                  ? `${latestFlagged.patientName} — ${
                      latestFlagged.submittedOn
                        ? new Date(latestFlagged.submittedOn).toLocaleDateString()
                        : t("Recently flagged", "Récemment signalé")
                    }`
                  : t("No flagged reports", "Aucun rapport signalé")
              }
              img={flagged}
            />

        </div>
      </main>
    </div>
  );
}
