const KEY = "smartcourse.session";

export function saveSession(data) {
  localStorage.setItem(KEY, JSON.stringify(data || {}));
}

export function updateSession(partial) {
  let current = getSession();
  if (!current) {
    console.error("[Session] Error: updateSession called but no current session found in localStorage.");
    // Attempt to recover? Or just init empty?
    // If we are updating, we expect a session to exist (usually).
    // But let's proceed with empty object if null, but log it.
    current = {};
  }

  // Safety check: if we are setting selectedCourse, we MUST NOT lose 'user' and 'courses'
  if (partial.selectedCourse && !current.user) {
    console.error("[Session] CRITICAL: Updating selectedCourse but 'user' is missing in current session!", current);
  }

  const next = { ...current, ...partial };
  saveSession(next);
  return next;
}

export function getSession() {
  try {
    const raw = localStorage.getItem(KEY);
    // console.log("[Session] Get raw:", raw);
    return raw ? JSON.parse(raw) : null;
  } catch (e) {
    console.warn("session parse error", e);
    return null;
  }
}

export function clearSession() {
  localStorage.removeItem(KEY);
}

export function setSession(payload) {
  saveSession(payload);
  return payload;
}

export function getUser() {
  return getSession()?.user || null;
}

export function getCourses() {
  return getSession()?.courses || [];
}

export function setSelectedCourse(course) {
  const picked = course
    ? {
      id: course.id,
      title: course.title,
      description: course.description,
      term: course.term,
      status: course.status,
      teacherUserId: course.teacherUserId,
      studentCount: course.studentCount,
    }
    : null;
  return updateSession({ selectedCourse: picked });
}

export function getSelectedCourse() {
  return getSession()?.selectedCourse || null;
}
