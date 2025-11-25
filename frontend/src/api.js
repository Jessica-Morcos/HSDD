const API_BASE = "http://localhost:8080";


export async function signup(payload) {
  const res = await fetch(`${API_BASE}/api/auth/signup`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  if (!res.ok) throw new Error(await res.text());
  return res.json(); // { userId, patientId }
}

export async function login(payload) {
  const res = await fetch(`${API_BASE}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  if (!res.ok) throw new Error(await res.text());
  return res.json(); // { token, username, role }
}



export async function submitSymptoms(token, patientId, text, tags) {
  console.log("📡 API CALL → submitSymptoms()");
  console.log("📨 Sending body →", { patientId, text, tags });

  const response = await fetch(`${API_BASE}/api/patient/symptoms`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + token,
    },
    body: JSON.stringify({ patientId, text, tags }),
  });

  console.log("🔍 HTTP STATUS:", response.status);

  const raw = await response.text();
  console.log("📩 RAW BACKEND RESPONSE:", raw);

  try {
    return JSON.parse(raw);
  } catch (err) {
    console.error("❌ Failed to parse JSON");
    throw err;
  }
}


function authHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  };
}


// ⬅️⬅️⬅️ THIS WAS MISSING — NOW RESTORED
export async function fetchUsers() {
  const res = await fetch(`${API_BASE}/api/admin/users`, {
    method: "GET",
    headers: authHeaders(),
  });

  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

// GET single user
export async function fetchUser(id) {
  const res = await fetch(`${API_BASE}/api/admin/users/${id}`, {
    method: "GET",
    headers: authHeaders(),
  });

  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

// CREATE new user (doctor or patient)
export async function createUser(payload) {
  const res = await fetch(`${API_BASE}/api/admin/users`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(payload),
  });

  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

// UPDATE existing user
export async function updateUser(id, payload) {
  const res = await fetch(`${API_BASE}/api/admin/users/${id}`, {
    method: "PUT",
    headers: authHeaders(),
    body: JSON.stringify(payload),
  });

  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

// DEACTIVATE user
export async function deactivateUser(id) {
  const res = await fetch(`${API_BASE}/api/admin/users/${id}`, {
    method: "DELETE",
    headers: authHeaders(),
  });

  // DELETE returns 204
  if (!res.ok && res.status !== 204) throw new Error(await res.text());
  return { success: true };
}

// REACTIVATE user
export async function reactivateUser(id) {
  const res = await fetch(`${API_BASE}/api/admin/users/${id}/reactivate`, {
    method: "PUT",
    headers: authHeaders(),
  });

  if (!res.ok) throw new Error(await res.text());
  return res.json(); // updated DTO
}

// Fetch Audit Logs

export async function fetchAuditLogs() {
  const res = await fetch(`${API_BASE}/api/admin/audit-logs`, {
    method: "GET",
    headers: authHeaders(),
  });

  if (!res.ok) throw new Error(await res.text());
  return res.json();
}
