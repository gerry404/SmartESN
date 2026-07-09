import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useLandingStore } from '../stores/storeSrevice'


export function useHome() {
  const store = useLandingStore()
  const { content, loading, error } = storeToRefs(store)

  onMounted(() => {
    store.loadContent()
  })

  return { content, loading, error }
}
