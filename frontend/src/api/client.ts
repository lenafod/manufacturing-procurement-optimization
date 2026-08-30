const BASE_URL = '/api';

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

// Spring Boot-ov podrazumevani error body (BasicErrorController), sad da uključuje "message"
// zahvaljujući spring.web.error.include-message=always u application.properties.
interface SpringErrorBody {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
}

async function request<T>(path: string, init?: RequestInit, jsonBody = true): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    ...(jsonBody ? { headers: { 'Content-Type': 'application/json' } } : {}),
    ...init,
  });

  if (!response.ok) {
    let message = `${response.status} ${response.statusText}`;
    try {
      const body: SpringErrorBody = await response.json();
      message = body.message || message;
    } catch {
      // odgovor nije JSON, koristi status tekst
    }
    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const apiClient = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  patch: <T>(path: string) => request<T>(path, { method: 'PATCH' }),
  // bez 'Content-Type': 'application/json' - browser sam postavlja tacan multipart/form-data
  // boundary kad je telo FormData, rucno postavljanje bi ga pokvarilo
  uploadFile: <T>(path: string, formData: FormData) => request<T>(path, { method: 'POST', body: formData }, false),
};
