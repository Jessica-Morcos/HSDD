import { useState, useEffect } from "react";
import AdminNav from "./AdminNav";

export default function SystemHealthPage() {
  const [systemData, setSystemData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    const token = localStorage.getItem("token");

    fetch("http://localhost:8080/api/admin/system-health", {
      headers: {
        "Authorization": "Bearer " + token,
        "Content-Type": "application/json",
      },
    })
      .then((res) => {
        if (!res.ok) throw new Error("Failed");
        return res.json();
      })
      .then((data) => {
        console.log("System health received:", data);
        setSystemData(data);
      })
      .catch((err) => {
        console.error("Error fetching system health:", err);
        setError("Failed to load system health.");
      });
  }, []);

  if (error)
    return (
      <div className="h-screen flex items-center justify-center text-red-600 text-xl">
        {error}
      </div>
    );

  if (!systemData)
    return (
      <div className="h-screen flex items-center justify-center text-gray-500 text-xl">
        Loading...
      </div>
    );

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <header className="items-center justify-between mb-8 bg-[#b0372b] p-4 relative">
        <AdminNav activePage="System Health" />
      </header>

      <div className="m-6">
        <h1 className="text-3xl font-bold mb-4">System Health</h1>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mb-10">
          <HealthCard title="Uptime" value={systemData.uptime} color="bg-green-100" />
          <HealthCard title="CPU Usage" value={`${systemData.cpuUsage}%`} color="bg-blue-100" />
          <HealthCard title="Memory Usage" value={`${systemData.memoryUsage}%`} color="bg-yellow-100" />
          <HealthCard title="Active Users" value={systemData.activeUsers} color="bg-purple-100" />
          <HealthCard title="Last Backup" value={systemData.lastBackup} color="bg-gray-100" />
        </div>

        
      </div>
    </div>
  );
}

function HealthCard({ title, value, color }) {
  return (
    <div className={`p-6 rounded-lg shadow ${color}`}>
      <h3 className="text-lg font-semibold mb-2 text-gray-800">{title}</h3>
      <p className="text-2xl font-bold text-gray-900">{value}</p>
    </div>
  );
}
