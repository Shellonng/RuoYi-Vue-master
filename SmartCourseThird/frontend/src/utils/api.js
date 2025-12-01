const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8083";

async function request(path, options = {}) {
  const resp = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });
  let body = {};
  try {
    body = await resp.json();
  } catch (e) {
    body = {};
  }
  if (!resp.ok || body.code !== 200) {
    const message = body?.message || body?.msg || resp.statusText || "请求失败";
    throw new Error(message);
  }
  return body.data ?? body;
}

export async function login(username, password) {
  return request("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
}

export async function listStudents(params = {}) {
  const search = new URLSearchParams();
  if (params.courseId) search.set("courseId", params.courseId);
  if (params.page) search.set("page", params.page);
  if (params.pageSize) search.set("pageSize", params.pageSize);
  const query = search.toString();
  const data = await request(`/api/v1/students${query ? `?${query}` : ""}`);
  return data?.students || data?.records || data;
}

// AI Grading 相关 API 函数
export async function listPendingGrading() {
  return request("/api/ai-grading/pending");
}

export async function triggerBatchGrading(submissionIds, llmModel = null) {
  const body = { submissionIds };
  if (llmModel) {
    body.llmModel = llmModel;
  }
  return request("/api/ai-grading/grading/batch", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export async function getGradingResult(submissionId) {
  return request(`/api/ai-grading/grading/${submissionId}/result`);
}

export async function confirmGrading(submissionId, teacherId, data = {}) {
  const params = new URLSearchParams({ teacherId }).toString();
  return request(`/api/ai-grading/grading/${submissionId}/confirm?${params}`, {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function getGradingHistory(submissionId) {
  return request(`/api/ai-grading/grading/${submissionId}/history`);
}

export async function listAssignments(params = {}) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      search.set(key, value);
    }
  });
  const query = search.toString();
  return request(`/api/ai-grading/assignments${query ? `?${query}` : ""}`);
}

export async function listSubmissions(params = {}) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      search.set(key, value);
    }
  });
  const query = search.toString();
  return request(`/api/ai-grading/submissions${query ? `?${query}` : ""}`);
}

export async function getSubmissionDetail(submissionId) {
  return request(`/api/ai-grading/submissions/${submissionId}`);
}

export async function getModelList() {
  return request("/api/ai-grading/models");
}

export { BASE_URL, request };