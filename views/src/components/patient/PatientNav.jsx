import { Link } from "react-router-dom";

export default function PatientNav({ activePage }) {
  const navItems = [
    { label: "Enter Symptoms", path: "/patient/enter-symptoms" },
    { label: "View Predictions", path: "/patient/view-predictions" },
    { label: "Medical History", path: "/patient/medical-history" },
   
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
      <Link to="/" className="ml-auto text-white hover:underline">
        ← Dashboard
      </Link>
    </nav>
  );
}