import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as api from '../services/demandeService'
import type { DemandeDetail, DemandeListItem, StatutDemande } from '../types'

export const useDemandesStore = defineStore('demandes', () => {
  const items = ref<DemandeListItem[]>([])
  const current = ref<DemandeDetail | null>(null)
  const filtreStatut = ref<StatutDemande | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function charger() {
    loading.value = true
    error.value = null
    try {
      items.value = await api.listDemandes(filtreStatut.value ?? undefined)
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Erreur de chargement.'
    } finally {
      loading.value = false
    }
  }

  async function chargerDetail(id: number) {
    loading.value = true
    error.value = null
    current.value = null
    try {
      current.value = await api.getDemande(id)
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Demande introuvable.'
    } finally {
      loading.value = false
    }
  }

  function setFiltre(s: StatutDemande | null) {
    filtreStatut.value = s
    charger()
  }

  // Met à jour la demande courante avec la réponse d'une action serveur.
  function appliquer(d: DemandeDetail) {
    current.value = d
    const i = items.value.findIndex((x) => x.id === d.id)
    if (i !== -1) items.value[i] = d
  }

  return {
    items,
    current,
    filtreStatut,
    loading,
    error,
    charger,
    chargerDetail,
    setFiltre,
    appliquer,
  }
})
