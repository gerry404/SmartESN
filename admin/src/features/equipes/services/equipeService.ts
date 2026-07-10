import { http } from '@/utils/http'
import type { Equipe } from '@/features/demandes/types'

export function listEquipes(): Promise<Equipe[]> {
  return http<Equipe[]>('/equipes')
}
