const BASE_URL = (import.meta.env.VITE_API_URL as string | undefined) ?? '/api'

export interface HttpError extends Error {
  status: number
  data?: unknown
}

function authHeader(): Record<string, string> {
    const token = localStorage.getItem('auth_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function http<T>(path: string, options: RequestInit = {}): Promise<T> {
  const { headers, ...rest } = options

  const res = await fetch(`${BASE_URL}${path}`, {
    ...rest,
    headers: { 'Content-Type': 'application/json', ...authHeader(), ...headers },
  })

  const isJson = res.headers.get('content-type')?.includes('application/json')
  const data = isJson ? await res.json() : await res.text()

  if (!res.ok) {
    // 401 : token absent/expiré → purge + redirection vers login (sauf si on y est déjà).
    if (res.status === 401) {
      localStorage.removeItem('auth_token')
      if (!location.pathname.startsWith('/login')) {
        location.assign('/login?redirect=' + encodeURIComponent(location.pathname))
      }
    }
    const message =
      data && typeof data === 'object' && 'message' in data
        ? String((data as { message: unknown }).message)
        : `Erreur ${res.status}`
    const err = new Error(message) as HttpError
    err.status = res.status
    err.data = data
    throw err
  }

  return data as T
}