import { Link } from "react-router-dom";

export default function DoctorNav({ activePage }) {
  const navItems = [
    { label: "Low Confidence", path: "/doctor/low-confidence" },
    { label: "Flagged Report", path: "/doctor/flagged-report" },
    { label: "Patients", path: "/doctor/patients" },
    { label: "All Reports", path: "/doctor/all-reports" },
  
    
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
      <Link to="/doctor/dashboard" className="ml-auto text-white hover:underline">
        ← Dashboard
      </Link>
    </nav>
  );
}