export interface User {
    id: string
    name: string
    email: string
}

export interface Credentials{
  email: string
  password: string
}
export interface RegisterPayload {
    name: string
    email: string
    password: string
}
export interface AuthResponse{
  token: string
  email: string
  role: string
}