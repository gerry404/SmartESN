<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

type Variant = 'dark' | 'outline-dark' | 'outline-light' | 'light'
type Size = 'sm' | 'md'

const props = withDefaults(
  defineProps<{
    variant?: Variant
    size?: Size
    to?: string // lien interne (router)
    href?: string // lien externe / ancre
  }>(),
  { variant: 'dark', size: 'md' },
)

const base =
  'font-label rounded-[60px] transition-all duration-300 shrink-0 inline-flex items-center justify-center uppercase tracking-wider font-bold cursor-pointer'

const variants: Record<Variant, string> = {
  dark: 'bg-black text-white hover:bg-text/80',
  light: 'bg-white text-black hover:bg-text-white/80',
  'outline-dark': 'bg-transparent border border-text/30 text-text hover:bg-text hover:text-white',
  'outline-light': 'border border-page-bg/30 text-page-bg hover:bg-page-bg hover:text-black',
}

const sizes: Record<Size, string> = {
  sm: 'px-4 text-[10px] h-[42px]',
  md: 'px-6 text-[10px] h-[50px]',
}

const classes = computed(() => [base, variants[props.variant], sizes[props.size]])

// choisit l'élément rendu : RouterLink (to), <a> (href) ou <button>
const tag = computed(() => (props.to ? RouterLink : props.href ? 'a' : 'button'))
</script>

<template>
  <component :is="tag" :to="to" :href="href" :class="classes">
    <slot />
  </component>
</template>
