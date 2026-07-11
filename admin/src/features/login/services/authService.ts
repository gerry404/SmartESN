import { http } from '@/utils/http'
import type {
  AuthResponse,
  Credentials,
  RegisterPayload,
  User,
} from '../types'

const ENDPOINTS = {
  login: '/auth/login',
  register: '/auth/register',
  me: '/auth/me',
  logout: '/auth/logout',
}


const USE_MOCK = false

function fakeDelay<T>(value: T, ms = 500): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(value), ms))
}
function fakeAuth(email: string): AuthResponse {
  return { token: 'mock-token-' + Date.now(), email, role: 'ADMIN' }
}

export function login(creds: Credentials): Promise<AuthResponse> {
  if (USE_MOCK) {
    // Accepte n'importe quel email + mot de passe ≥ 6 caractères.
    if (creds.password.length < 6) return Promise.reject(new Error('Identifiants invalides.'))
    return fakeDelay(fakeAuth(creds.email))
  }
  return http<AuthResponse>(ENDPOINTS.login, {
    method: 'POST',
    body: JSON.stringify({ email: creds.email, motDePasse: creds.password }),
  })
}

export function register(payload: RegisterPayload): Promise<AuthResponse> {
  if (USE_MOCK) return fakeDelay(fakeAuth(payload.email))
  return http<AuthResponse>(ENDPOINTS.register, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}


export function fetchMe(): Promise<User> {
  return http<User>(ENDPOINTS.me)
}


export function logout(): Promise<void> {
  return http<void>(ENDPOINTS.logout, {
    method: 'POST',
  })
}