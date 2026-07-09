import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import * as authService from '../services/authService'
import type { Credentials, RegisterPayload,User } from '../types'

const TOKEN_KEY = 'auth_token'

export const useAuthStore = defineStore('auth',() => {
    const user = ref<User | null>(null)
    const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
    const loading = ref(false)
    const error = ref<string | null>(null)
    
    const isAuthenticated = computed(() => !!token.value)

    function setToken(value: string | null) {
    token.value = value
    if (value) localStorage.setItem(TOKEN_KEY, value)
    else localStorage.removeItem(TOKEN_KEY)
    }

  async function login(creds: Credentials): Promise<boolean> {
    loading.value = true
    error.value = null

    try {
        const res = await authService.login(creds)
        setToken(res.token)
        user.value = res.user
        return true
    }catch (e) {
      error.value = e instanceof Error ? e.message : 'Connexion impossible.'
      return false
    } finally {
      loading.value = false
    }
  }



  async function fetchMe(): Promise<void> {
    if (!token.value) return
    try {
      user.value = await authService.fetchMe()
    } catch {
      await logout() 
    }
  }

  async function logout(): Promise<void> {
    try {
      await authService.logout()
    } catch {
      
    }
    setToken(null)
    user.value = null
  }

  return { user, token, loading, error, isAuthenticated, login, register, fetchMe, logout }

})