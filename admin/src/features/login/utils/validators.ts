export function validateName(value: string): string | null {
    if (!value.trim()) return 'Le nom est requis.'
    if(value.trim().length < 2) return 'Au moins 2 caracteres.'
    return null
}

export function validateEmail(value: string): string | null {
    if (!value.trim()) return 'L’email est requis.'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) return 'Email invalide.'
    return null
}

export function validatePassword(value: string): string | null {
  if (!value) return 'Le mot de passe est requis.'
  if (value.length < 6) return 'Au moins 6 caractères.'
  return null
}