import { http } from '@/utils/http'
import type { Statistiques } from '@/features/demandes/types'

export function getStatistiques(): Promise<Statistiques> {
  return http<Statistiques>('/statistiques')
}
