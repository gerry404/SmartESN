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


export function login(creds: Credentials): Promise<AuthResponse> {

  return http<AuthResponse>(ENDPOINTS.login, {
    method: 'POST',
    body: JSON.stringify({ email: creds.email, motDePasse: creds.password }),
  })
}


export function register(payload: RegisterPayload): Promise<AuthResponse> {
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