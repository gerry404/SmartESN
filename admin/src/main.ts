import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './features/login/stores/authStore'

import './assets/styles/main.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Restaure l'utilisateur (et donc son rôle) au démarrage si un token est présent.
// Sans ça, après un rechargement, auth.user est null → les onglets Admin disparaissent.
const auth = useAuthStore()
async function bootstrap() {
  if (auth.token) {
    await auth.fetchMe()
  }
  app.mount('#app')
}
bootstrap()
