import { Link } from "react-router-dom";

export default function AdminNav({ activePage }) {
  const navItems = [
    { label: "Manage Users", path: "/admin/manage-users" },
    { label: "System Health", path: "/admin/system-health" },
    { label: "View Logs", path: "/admin/view-logs" },
    { label: "Settings", path: "/admin/settings" },
  ];

  return (
    <nav className="flex gap-6 h-10  text-white font-medium">
      {navItems.map((item) => (
        <Link
          key={item.path}
          to={item.path}
          className={`hover:underline ${
            activePage === item.label ? "underline font-bold" : ""
          }`}
        >
          {item.label}
        </Link>
      ))}
      <Link to="/" className="ml-auto right-0 text-white hover:underline">
        ← Dashboard
      </Link>
    </nav>
  );
}