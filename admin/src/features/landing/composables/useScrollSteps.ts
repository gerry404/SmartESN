import { onBeforeUnmount, onMounted, ref, type Ref } from 'vue'

/**
 * Pinned "scroll storytelling" helper.
 *
 * Bind the returned `progress` (0 → 1) to a section whose wrapper is taller than
 * the viewport and whose inner panel is `sticky top-0 h-screen`. As the user
 * scrolls through the tall wrapper, the panel stays pinned and `progress`
 * advances, which callers translate into a discrete active step.
 *
 * Pinning is only enabled at ≥1024px (matching the `lg` layout). On smaller
 * screens `enabled` stays false so sections render as a normal stacked block.
 */
export function useScrollSteps(target: Ref<HTMLElement | null>) {
  const progress = ref(0)
  const enabled = ref(false)
  let ticking = false
  let mql: MediaQueryList | null = null

  const measure = () => {
    ticking = false
    const el = target.value
    if (!el || !enabled.value) {
      progress.value = 0
      return
    }
    const total = el.offsetHeight - window.innerHeight
    if (total <= 0) {
      progress.value = 0
      return
    }
    const scrolled = Math.min(Math.max(-el.getBoundingClientRect().top, 0), total)
    progress.value = scrolled / total
  }

  const onScroll = () => {
    if (!ticking) {
      ticking = true
      requestAnimationFrame(measure)
    }
  }

  const onResize = () => {
    enabled.value = !!mql && mql.matches
    measure()
  }

  onMounted(() => {
    mql = window.matchMedia('(min-width: 1024px)')
    enabled.value = mql.matches
    window.addEventListener('scroll', onScroll, { passive: true })
    window.addEventListener('resize', onResize)
    mql.addEventListener('change', onResize)
    measure()
  })

  onBeforeUnmount(() => {
    window.removeEventListener('scroll', onScroll)
    window.removeEventListener('resize', onResize)
    mql?.removeEventListener('change', onResize)
  })

  return { progress, enabled }
}
