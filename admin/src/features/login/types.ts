export interface User {
    email: string
    role: string
}

export interface Credentials{
  email: string
  password: string
}
export interface RegisterPayload {
    nomEntreprise: string
    nomAdmin: string
    email: string
    motDePasse: string
}
export interface AuthResponse{
  token: string
  email: string
  role: string
}