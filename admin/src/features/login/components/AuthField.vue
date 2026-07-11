<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  label: string
  modelValue: string
  type?: string
  placeholder?: string
  autocomplete?: string
  error?: string | null
}>()
defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'blur'): void
}>()

// bascule afficher/masquer pour les champs mot de passe
const revealed = ref(false)
const isPassword = props.type === 'password'
</script>

<template>
  <label class="block">
    <span class="font-label text-[12px] font-bold text-text">{{ label }}</span>
    <div class="relative">
      <input
        :type="isPassword && revealed ? 'text' : (type ?? 'text')"
        :value="modelValue"
        :placeholder="placeholder"
        :autocomplete="autocomplete"
        class="mt-2 w-full rounded-2xl border bg-white-card px-4 py-3 font-body-md text-text outline-none transition-colors focus:border-text"
        :class="[error ? 'border-brand-from' : 'border-line', isPassword ? 'pr-16' : '']"
        @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
        @blur="$emit('blur')"
      />
      <button
        v-if="isPassword"
        type="button"
        class="absolute right-3 top-1/2 -translate-y-1/2 mt-1 text-[12px] font-bold text-muted hover:text-text"
        tabindex="-1"
        @click="revealed = !revealed"
      >
        {{ revealed ? 'Masquer' : 'Afficher' }}
      </button>
    </div>
    <span v-if="error" class="mt-1.5 block font-body-md text-[12px] text-brand-from">{{ error }}</span>
  </label>
</template>
