import { useState } from "react";
import carepathLogo from "../assets/carepath-logo.svg";
import { Link } from "react-router-dom";
import DashboardCard from "./DashboardCard";

// ✅ FIXED — IMPORT ALL ICONS PROPERLY
import enterIcon from "../assets/All reports.svg";
import viewIcon from "../assets/lowconfidenceReport.svg";
import historyIcon from "../assets/MedicalHistory.svg";
import messageIcon from "../assets/Message.svg";

export default function DashboardPatient() {
  const [language, setLanguage] = useState("en");
  const [open, setOpen] = useState(false);
  const username = localStorage.getItem("username") || "User";
  const t = (en, fr) => (language === "en" ? en : fr);
  const handleLogout = () => {
  const token = localStorage.getItem("token");

  // Call backend to record logout in audit logs
  fetch("http://localhost:8080/api/auth/logout", {
    method: "POST",
    headers: {
      Authorization: "Bearer " + token,
    },
  }).catch(() => {});

  // Clear frontend session
  localStorage.removeItem("token");
  localStorage.removeItem("userId");
  localStorage.removeItem("patientId");
  localStorage.removeItem("role");

  // Redirect to login page
  window.location.href = "/";
};



  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      
      {/* Header */}
      <header className="flex items-center justify-between bg-[#b0372b] p-4 relative">
        <img src={carepathLogo} alt="CarePath" className="w-28" />

        {/* 🌐 Language Toggle */}
        <button
          onClick={() => setLanguage(language === "en" ? "fr" : "en")}
          className="absolute right-[13rem] top-6 text-xs text-white border border-white rounded-full px-3 py-1 hover:bg-white hover:text-[#b0372b] transition"
        >
          {language === "en" ? "Français" : "English"}
        </button>

        {/* ⚙️ Account Settings Dropdown */}
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
            { month: "long", day: "numeric", year: "numeric" }
          )}
        </p>


        {/* Patient Dashboard Cards */}
        <div className="grid grid-cols-2 max-w-5xl mx-[3rem]">
          <DashboardCard
            to="/patient/enter-symptoms"
            title={t("Enter Symptoms", "Entrer les symptômes")}
            subtitle={t("Add new symptoms", "Ajouter de nouveaux symptômes")}
            img={enterIcon}   //  FIXED
          />

          <DashboardCard
            to="/patient/view-predictions"
            title={t("View Predictions", "Voir les prédictions")}
            subtitle={t("View past and recent predictions", "Voir les prédictions récentes et passées")}
            img={viewIcon}   // FIXED
          />

          <DashboardCard
            to="/patient/medical-history"
            title={t("Medical History", "Historique médical")}
            subtitle={t("Access your records", "Accéder à vos dossiers")}
            img={historyIcon}   // FIXED
          />

        
        </div>
      </main>

      {/* Footer */}
      <footer className="text-center text-xs py-4 text-gray-600">
        {t(
          "privacy policy · about · contact · CarePath Inc.",
          "politique de confidentialité · à propos · contact · CarePath Inc."
        )}
      </footer>
    </div>
  );
}
