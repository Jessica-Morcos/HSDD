import { Link } from "react-router-dom";

export default function ProfilePage() {
  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <Link to="/" className="text-blue-600 hover:underline">← Back to Dashboard</Link>
      <h1 className="text-3xl font-bold mb-4">Profile</h1>
      <p className="text-gray-700">Display and edit personal information here.</p>
    </div>
  );
}
