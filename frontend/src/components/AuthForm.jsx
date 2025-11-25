import { useState } from "react";
import InputField from "./InputField";
import hands from "../assets/hands.jpeg";
import carepathLogo from "../assets/carepath-logo.svg";
import { signup, login } from "../api";
import { useNavigate } from "react-router-dom";

export default function AuthForm({ onLogin }) {
  const [isLogin, setIsLogin] = useState(true);
  const [language, setLanguage] = useState("en");
  const navigate = useNavigate();

  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    firstName: "",
    lastName: "",
    dateOfBirth: "",
    phone: ""
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // -------------------------------------------
  // ⭐ FIXED LOGIN — correct token, safe clearing,
  //    consistent session keys, proper redirects
  // -------------------------------------------

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const data = await login({
        username: form.username,
        password: form.password,
      });

      console.log("LOGIN RESPONSE:", data);

      // 🧹 CLEAR OLD SESSION (fixes token confusion)
      localStorage.clear();

      // ⭐ SAVE CONSISTENT KEYS
      localStorage.setItem("token", data.token);
      localStorage.setItem("role", data.role);
      localStorage.setItem("userId", data.userId);
      localStorage.setItem("patientId", data.patientId);

      // Update App.jsx state
      onLogin(data.role);

      // Correct redirect target based on role
      if (data.role === "patient") navigate("/patient/dashboard");
      if (data.role === "doctor") navigate("/doctor/dashboard");
      if (data.role === "admin") navigate("/admin/dashboard");

    } catch (err) {
      console.error("LOGIN ERROR:", err);
      setError("Invalid username or password.");
    }
  };


  // SIGNUP (unchanged)
  const handleSignup = async () => {
    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    try {
      const result = await signup({
        username: form.username,
        email: form.email,
        password: form.password,
        firstName: form.firstName,
        lastName: form.lastName,
        dateOfBirth: form.dateOfBirth,
        phone: form.phone
      });

      localStorage.setItem("userId", result.userId);
      localStorage.setItem("patientId", result.patientId);

      setSuccess("Signup successful. You can now log in.");
      setError("");
      setIsLogin(true);

    } catch (err) {
      const msg = err.message;

      if (msg.includes("Username already exists")) {
        setError("This username is already taken. Try another one.");
      } else if (msg.includes("Email already exists")) {
        setError("This email is already registered.");
      } else {
        setError("Signup failed: " + msg);
      }
    }
  };

  const t = (en, fr) => (language === "en" ? en : fr);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  return (
    <div className="flex h-[90vh] items-center justify-center bg-gray-100 p-0 mx-0">
      <div className="flex bg-white rounded-3xl shadow-lg overflow-hidden w-full max-w-[65rem] h-[38rem] flex-col md:flex-row">

        {/* LEFT IMAGE */}
        <div className="md:w-1/2 w-full h-56 md:h-auto relative">
          <img src={hands} alt="CarePath" className="w-full h-full object-cover" />
          <div className="absolute top-[0px] flex items-center gap-2">
            <img src={carepathLogo} alt="CarePath Logo" className="w-50 h-auto drop-shadow-md" />
          </div>
        </div>

        {/* RIGHT SECTION */}
        <div className="md:w-1/2 w-full flex flex-col px-8 py-8 relative">

          <div className="absolute top-4 right-6">
            <button
              onClick={() => setLanguage(language === "en" ? "fr" : "en")}
              className="text-xs font-medium text-gray-600 border border-gray-300 px-3 py-1 rounded-full hover:bg-gray-200 transition"
            >
              {language === "en" ? "Français" : "English"}
            </button>
          </div>

          <div className="relative flex w-fit mb-6 self-center bg-gray-300 rounded-full p-1">
            <div
              className={`absolute top-1 bottom-1 w-1/2 rounded-full bg-white shadow-md transition-all duration-300 ${
                isLogin ? "left-1" : "left-1/2"
              }`}
            />

            <button
              onClick={() => setIsLogin(true)}
              className={`relative z-10 w-24 text-center text-sm font-medium py-2 ${
                isLogin ? "text-black" : "text-gray-600"
              }`}
            >
              {t("Login", "Connexion")}
            </button>

            <button
              onClick={() => setIsLogin(false)}
              className={`relative z-10 w-24 text-center text-sm font-medium py-2 ${
                !isLogin ? "text-black" : "text-gray-600"
              }`}
            >
              {t("Sign Up", "Inscription")}
            </button>
          </div>

          <h2 className="text-center font-semibold mb-1 text-lg">
            {isLogin ? t("Welcome Back!", "Bon retour!") : t("Welcome!", "Bienvenue!")}
          </h2>

          {/* FORM */}
          {isLogin ? (
            <>
              <InputField label={t("Username", "Nom d'utilisateur")} name="username" onChange={handleChange} />
              <InputField label={t("Password", "Mot de passe")} type="password" name="password" onChange={handleChange} />

              <button
                onClick={handleLogin}
                className="mt-6 bg-black text-white rounded-md py-2 hover:bg-blue-600"
              >
                {t("Login", "Connexion")}
              </button>

              {error && <p className="text-red-500 text-sm mt-3">{error}</p>}
            </>
          ) : (
            <>
              <div className="flex gap-8">
                <InputField label={t("Username","Nom d'utilisateur")} name="username" onChange={handleChange} required />
                <InputField label={t("First Name","Prénom")} name="firstName" onChange={handleChange} required />
                <InputField label={t("Last Name","Nom")} name="lastName" onChange={handleChange} required />
              </div>

              <div className="flex gap-8">
                <InputField label={t("Date of Birth","Date de naissance")} type="date" name="dateOfBirth" onChange={handleChange} required />
                <InputField label={t("Phone Number","Numéro de téléphone")} type="tel" name="phone" onChange={handleChange} required />
              </div>

              <InputField label={t("Email","Courriel")} type="email" name="email" onChange={handleChange} required />
              <InputField label={t("Password","Mot de passe")} type="password" name="password" onChange={handleChange} required />
              <InputField label={t("Confirm Password","Confirmer le mot de passe")} type="password" name="confirmPassword" onChange={handleChange} required />

              <button
                onClick={handleSignup}
                className="mt-4 bg-black text-white rounded-md py-2 hover:bg-blue-600"
              >
                {t("Confirm", "Confirmer")}
              </button>

              {error && <p className="text-red-500 text-sm mt-3">{error}</p>}
              {success && <p className="text-green-600 text-sm mt-3">{success}</p>}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
