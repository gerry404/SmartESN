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

/** Robustesse « 2026 » : min 8, majuscule, minuscule, chiffre. */
export function validatePassword(value: string): string | null {
  if (!value) return 'Le mot de passe est requis.'
  if (value.length < 8) return 'Au moins 8 caractères.'
  if (!/[a-z]/.test(value)) return 'Ajoutez une minuscule.'
  if (!/[A-Z]/.test(value)) return 'Ajoutez une majuscule.'
  if (!/[0-9]/.test(value)) return 'Ajoutez un chiffre.'
  return null
}

export function validateConfirm(password: string, confirm: string): string | null {
  if (!confirm) return 'Confirmez le mot de passe.'
  if (password !== confirm) return 'Les mots de passe ne correspondent pas.'
  return null
}

export interface PasswordStrength {
  score: 0 | 1 | 2 | 3 | 4
  label: string
  color: string
}

/** Évalue la force du mot de passe (0 à 4) pour l'indicateur visuel. */
export function passwordStrength(value: string): PasswordStrength {
  if (!value) return { score: 0, label: '', color: '#e2e8f0' }
  let score = 0
  if (value.length >= 8) score++
  if (value.length >= 12) score++
  if (/[a-z]/.test(value) && /[A-Z]/.test(value)) score++
  if (/[0-9]/.test(value)) score++
  if (/[^A-Za-z0-9]/.test(value)) score++
  const s = Math.min(score, 4) as 0 | 1 | 2 | 3 | 4
  const table: Record<number, { label: string; color: string }> = {
    0: { label: 'Très faible', color: '#ef4444' },
    1: { label: 'Faible', color: '#f97316' },
    2: { label: 'Moyen', color: '#eab308' },
    3: { label: 'Bon', color: '#22c55e' },
    4: { label: 'Excellent', color: '#16a34a' },
  }
  return { score: s, ...table[s] }
}