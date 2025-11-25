import { Link } from "react-router-dom";

export default function ChangePasswordPage() {
  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <Link to="/" className="text-blue-600 hover:underline">← Back to Dashboard</Link>
      <h1 className="text-3xl font-bold mb-4">Change Password</h1>
      <p className="text-gray-700">Form to update account password goes here.</p>
    </div>
  );
}
