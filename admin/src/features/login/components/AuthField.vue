<script setup lang="ts">
defineProps<{
    label: string
    modelValue: string 
    type?: string
    placeholder?: string
    autocomplete?: string
    error?: string | null
}>()
defineEmits<{ (e: 'update:modelValue', value: string): void }>()
</script>

<template>
     <label class="block">
        <span class="font-label text-[12px] font-bold text-text">{{ label }}</span>
    <input
      :type="type ?? 'text'"
      :value="modelValue"
      :placeholder="placeholder"
      :autocomplete="autocomplete"
      class="mt-2 w-full rounded-2xl border bg-white-card px-4 py-3 font-body-md text-text outline-none transition-colors focus:border-text"
      :class="error ? 'border-brand-from' : 'border-line'"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
    <span v-if="error" class="mt-1.5 block font-body-md text-[12px] text-brand-from">{{ error }}</span>
     </label>
</template>