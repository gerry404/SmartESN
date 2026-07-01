import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getHomeContent } from '../services/homeService'
import type { HomeContent } from '../types'

export const useLandingStore = defineStore('landing', () => {
  const content = ref<HomeContent | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function loadContent() {
    if (content.value || loading.value) return
    loading.value = true
    error.value = null
    try {
      content.value = await getHomeContent()
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Erreur de chargement du contenu.'
    } finally {
      loading.value = false
    }
  }

  return { content, loading, error, loadContent }
})
