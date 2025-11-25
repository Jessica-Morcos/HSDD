import { useState } from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";

// AUTH
import AuthForm from "./components/AuthForm.jsx";

// DASHBOARDS
import DashboardPatient from "./components/DashboardPatient.jsx";
import DashboardDoctor from "./components/DashboardDoctor.jsx";
import DashboardAdmin from "./components/DashboardAdmin.jsx";

// COMMON
import NotificationsPage from "./components/NotificationsPage.jsx";
import ProfilePage from "./components/ProfilePage.jsx";
import ChangePasswordPage from "./components/ChangePasswordPage.jsx";

// ADMIN
import ManageUsersPage from "./components/admin/ManageUsersPage.jsx";
import UserDetailPage from "./components/admin/UserDetailPage.jsx";
import AddUserPage from "./components/admin/AddUserPage.jsx";
import SystemHealthPage from "./components/admin/SystemHealthPage.jsx";
import ViewLogsPage from "./components/admin/ViewLogsPage.jsx";
import SettingsPage from "./components/admin/SettingsPage.jsx";

// DOCTOR
import LowConfidencePage from "./components/doctor/LowConfidencePage.jsx";
import PatientsPage from "./components/doctor/PatientsPage.jsx";
import AllReportsPage from "./components/doctor/AllReportsPage.jsx";
import UserManagementPage from "./components/doctor/UserManagementPage.jsx";
import FlaggedReportPage from "./components/doctor/FlaggedReportPage.jsx";
import PatientRecordPage from "./components/doctor/PatientRecordPage.jsx";
import ReportDetailsPage from "./components/doctor/ReportDetailsPage.jsx";


// PATIENT
import EnterSymptomsPage from "./components/patient/EnterSymptomsPage.jsx";
import ViewPredictionsPage from "./components/patient/ViewPredictionsPage.jsx";
import MedicalHistoryPage from "./components/patient/MedicalHistoryPage.jsx";
import MessageDoctorPage from "./components/patient/MessageDoctorPage.jsx";

export default function App() {
  const savedToken = localStorage.getItem("authToken");
const savedRole  = localStorage.getItem("role");

const initialRole = savedToken && savedRole ? savedRole : null;

const [userRole, setUserRole] = useState(initialRole);


  return (
    <BrowserRouter>
      <Routes>
        {/* LOGIN SCREEN */}
        {!userRole && (
          <Route path="*" element={<AuthForm onLogin={setUserRole} />} />
        )}

        {/* AUTHENTICATED ROUTES */}
        {userRole && (
          <>
            <Route
              path="/"
              element={
                userRole === "admin" ? (
                  <DashboardAdmin />
                ) : userRole === "doctor" ? (
                  <DashboardDoctor />
                ) : (
                  <DashboardPatient />
                )
              }
            />

            {/* ADMIN */}
            <Route path="/admin/dashboard" element={<DashboardAdmin />} />
            <Route path="/admin/manage-users" element={<ManageUsersPage />} />
            <Route path="/admin/user/:id" element={<UserDetailPage />} />
            <Route path="/admin/users/add" element={<AddUserPage />} />
            <Route path="/admin/system-health" element={<SystemHealthPage />} />
            <Route path="/admin/view-logs" element={<ViewLogsPage />} />
            <Route path="/admin/settings" element={<SettingsPage />} />

            {/* DOCTOR */}
            <Route path="/doctor/dashboard" element={<DashboardDoctor />} />
            <Route path="/doctor/low-confidence" element={<LowConfidencePage />} />
            <Route path="/doctor/patients" element={<PatientsPage />} />
            <Route path="/doctor/patient/:patientId/record" element={<PatientRecordPage />}/>
            <Route path="/doctor/report/:reportId" element={<ReportDetailsPage />} />
            <Route path="/doctor/all-reports" element={<AllReportsPage />} />
            <Route path="/doctor/user-management" element={<UserManagementPage />} />
            <Route path="/doctor/flagged-report" element={<FlaggedReportPage />} />

            {/* PATIENT */}
            <Route path="/patient/dashboard" element={<DashboardPatient />} />
            <Route path="/patient/enter-symptoms" element={<EnterSymptomsPage />} />
            <Route path="/patient/view-predictions" element={<ViewPredictionsPage />} />
            <Route path="/patient/medical-history" element={<MedicalHistoryPage />} />
            <Route path="/patient/message-doctor" element={<MessageDoctorPage />} />

            {/* COMMON */}
            <Route
              path="/notifications"
              element={<NotificationsPage userRole={userRole} />}
            />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/change-password" element={<ChangePasswordPage />} />
          </>
        )}
      </Routes>
    </BrowserRouter>
  );
}
