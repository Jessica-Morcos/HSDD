import { useState } from "react";
import AdminNav from "./AdminNav";

export default function SettingsPage() {
  const tabs = ["General", "Security", "Integration", "Display"];
  const [activeTab, setActiveTab] = useState("General");

  const [settings, setSettings] = useState({
    // General
    systemMode: "Production",
    autoBackup: true,
    notifications: true,

    // Security
    twoFactorAuth: false,
    passwordPolicy: "Strong",
    sessionTimeout: "30 min",
    encryptionLevel: "AES-256",

    // Integration
    aiEngine: "Active",
    apiAccess: "Restricted",
    loggingService: "Cloud (AWS)",

    // Display
    theme: "Light",
    language: "English",
    dateFormat: "MM/DD/YYYY",
  });

  const handleToggle = (key) =>
    setSettings({ ...settings, [key]: !settings[key] });

  const handleChange = (key, value) =>
    setSettings({ ...settings, [key]: value });

  const handleSave = () => {
    alert("Settings saved! (Frontend simulation only)");
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <AdminNav activePage="Settings" />
      </header>

      <div className="m-6">
        <h1 className="text-3xl font-bold mb-4">Settings</h1>
        <p className="text-gray-700 mb-6">
          Configure system, security, integration, and display preferences.
        </p>

        {/* 🔹 Tab Navigation */}
        <div className="flex flex-wrap gap-2 mb-6">
          {tabs.map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-5 py-2 rounded-md font-medium ${
                activeTab === tab
                  ? "bg-[#b0372b] text-white"
                  : "bg-white text-gray-700 border"
              } transition`}
            >
              {tab}
            </button>
          ))}
        </div>

        {/* 🔸 Active Tab Content */}
        <div className="bg-white rounded-lg shadow-lg p-6 max-w-3xl">
          {activeTab === "General" && (
            <Section title="General Settings">
              <Toggle
                label="Enable Notifications"
                checked={settings.notifications}
                onChange={() => handleToggle("notifications")}
              />
              <Toggle
                label="Enable Auto Backup"
                checked={settings.autoBackup}
                onChange={() => handleToggle("autoBackup")}
              />
              <Select
                label="System Mode"
                value={settings.systemMode}
                options={["Production", "Maintenance", "Development"]}
                onChange={(v) => handleChange("systemMode", v)}
              />
            </Section>
          )}

          {activeTab === "Security" && (
            <Section title="Security Settings">
              <Toggle
                label="Two-Factor Authentication (2FA)"
                checked={settings.twoFactorAuth}
                onChange={() => handleToggle("twoFactorAuth")}
              />
              <Select
                label="Password Policy"
                value={settings.passwordPolicy}
                options={["Basic", "Strong", "Strict"]}
                onChange={(v) => handleChange("passwordPolicy", v)}
              />
              <Select
                label="Session Timeout"
                value={settings.sessionTimeout}
                options={["15 min", "30 min", "60 min"]}
                onChange={(v) => handleChange("sessionTimeout", v)}
              />
              <Select
                label="Encryption Level"
                value={settings.encryptionLevel}
                options={["AES-128", "AES-256"]}
                onChange={(v) => handleChange("encryptionLevel", v)}
              />
            </Section>
          )}

          {activeTab === "Integration" && (
            <Section title="Integration Settings">
              <Select
                label="AI Engine"
                value={settings.aiEngine}
                options={["Active", "Disabled"]}
                onChange={(v) => handleChange("aiEngine", v)}
              />
              <Select
                label="API Access Mode"
                value={settings.apiAccess}
                options={["Internal", "Public", "Restricted"]}
                onChange={(v) => handleChange("apiAccess", v)}
              />
              <Select
                label="Logging Service"
                value={settings.loggingService}
                options={["Local", "Cloud (AWS)"]}
                onChange={(v) => handleChange("loggingService", v)}
              />
            </Section>
          )}

          {activeTab === "Display" && (
            <Section title="Display Settings">
              <Select
                label="Theme"
                value={settings.theme}
                options={["Light", "Dark"]}
                onChange={(v) => handleChange("theme", v)}
              />
              <Select
                label="Language"
                value={settings.language}
                options={["English", "French"]}
                onChange={(v) => handleChange("language", v)}
              />
              <Select
                label="Date Format"
                value={settings.dateFormat}
                options={["MM/DD/YYYY", "DD/MM/YYYY"]}
                onChange={(v) => handleChange("dateFormat", v)}
              />
            </Section>
          )}

          {/* Save Button */}
          <div className="text-right mt-6">
            <button
              onClick={handleSave}
              className="bg-[#b0372b] text-white px-6 py-2 rounded hover:bg-[#992c23] transition"
            >
              Save Settings
            </button>
          </div>
        </div>
        
      </div>
    </div>
  );
}

/* 🧩 Reusable Components */

function Section({ title, children }) {
  return (
    <div className="mb-6">
      <h2 className="text-xl font-semibold mb-4 text-gray-800">{title}</h2>
      <div className="space-y-4">{children}</div>
    </div>
  );
}

function Toggle({ label, checked, onChange }) {
  return (
    <div className="flex justify-between items-center">
      <span className="text-gray-800 font-medium">{label}</span>
      <input
        type="checkbox"
        checked={checked}
        onChange={onChange}
        className="w-5 h-5 accent-[#b0372b]"
      />
    </div>
  );
}

function Select({ label, value, options, onChange }) {
  return (
    <div className="flex justify-between items-center">
      <span className="text-gray-800 font-medium">{label}</span>
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring focus:ring-[#b0372b]"
      >
        {options.map((opt) => (
          <option key={opt}>{opt}</option>
        ))}
      </select>
    </div>
  );
}
