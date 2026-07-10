<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { mdiArrowLeft } from '@mdi/js'
import Logo from '@/features/landing/components/ui/Logo.vue'
import MdiIcon from '@/features/landing/components/ui/MdiIcon.vue'

const iframeSnippet = `<iframe
  src="https://form.smartesn.com/?ent=ent_pub_ax3f9"
  style="width:100%;max-width:640px;height:720px;border:0"
  title="Décrivez votre projet"
></iframe>`

const webcomponentSnippet = `<script src="https://cdn.smartesn.com/intake.js" defer><\/script>

<smartesn-intake
  api-base-url="https://api.smartesn.com"
  entreprise-key="ent_pub_ax3f9"
></smartesn-intake>`

const eventSnippet = `document.querySelector('smartesn-intake')
  .addEventListener('submitted', (e) => {
    console.log('Demande n°', e.detail.id)
  })`
</script>

<template>
  <div class="min-h-screen bg-page-bg text-text">
    <!-- Header minimal -->
    <header class="px-margin py-6 max-w-[1100px] mx-auto flex items-center justify-between">
      <RouterLink to="/"><Logo /></RouterLink>
      <RouterLink to="/" class="inline-flex items-center gap-1.5 text-[14px] text-muted hover:text-text transition-colors">
        <MdiIcon :path="mdiArrowLeft" /> Accueil
      </RouterLink>
    </header>

    <main class="px-margin max-w-[860px] mx-auto pt-16 pb-32">
      <p class="text-[13px] tracking-wide text-muted mb-4">Documentation</p>
      <h1 class="font-display text-5xl sm:text-6xl font-bold tracking-tight leading-[1.02] mb-6">
        Intégrer le formulaire
      </h1>
      <p class="text-body-lg text-muted max-w-xl mb-20">
        Le formulaire vivant s'ajoute à n'importe quel site en quelques lignes, et remonte les
        demandes qualifiées directement dans votre espace SmartESN.
      </p>

      <!-- Méthode 1 -->
      <section class="mb-20">
        <div class="flex items-baseline gap-4 mb-4">
          <span class="font-display text-2xl text-muted-light">01</span>
          <h2 class="font-display text-3xl font-bold tracking-tight">En iframe</h2>
        </div>
        <p class="text-muted max-w-xl mb-6">
          La voie la plus simple, sans dépendance. Idéale pour un CMS (WordPress, Webflow) ou un
          site statique.
        </p>
        <pre class="code">{{ iframeSnippet }}</pre>
      </section>

      <!-- Méthode 2 -->
      <section class="mb-20">
        <div class="flex items-baseline gap-4 mb-4">
          <span class="font-display text-2xl text-muted-light">02</span>
          <h2 class="font-display text-3xl font-bold tracking-tight">En composant web</h2>
        </div>
        <p class="text-muted max-w-xl mb-6">
          Un rendu inline, intégré à votre page. On charge un script, puis on pose la balise
          <code class="inline-code">&lt;smartesn-intake&gt;</code>.
        </p>
        <pre class="code">{{ webcomponentSnippet }}</pre>
      </section>

      <!-- Événement -->
      <section class="mb-20">
        <div class="flex items-baseline gap-4 mb-4">
          <span class="font-display text-2xl text-muted-light">03</span>
          <h2 class="font-display text-3xl font-bold tracking-tight">Récupérer la demande</h2>
        </div>
        <p class="text-muted max-w-xl mb-6">
          Le composant émet un événement <code class="inline-code">submitted</code> à la
          soumission, avec l'identifiant de la demande.
        </p>
        <pre class="code">{{ eventSnippet }}</pre>
      </section>

      <!-- Config -->
      <section>
        <h2 class="font-display text-3xl font-bold tracking-tight mb-6">Configuration</h2>
        <dl class="divide-y divide-line">
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-2 py-5">
            <dt class="font-semibold">api-base-url</dt>
            <dd class="sm:col-span-2 text-muted">URL de votre plateforme SmartESN.</dd>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-2 py-5">
            <dt class="font-semibold">entreprise-key</dt>
            <dd class="sm:col-span-2 text-muted">Clé publique de l'entreprise — route la demande vers le bon pipe.</dd>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-2 py-5">
            <dt class="font-semibold">submitted</dt>
            <dd class="sm:col-span-2 text-muted">Événement émis à la fin (contient l'id de la demande).</dd>
          </div>
        </dl>
      </section>
    </main>
  </div>
</template>

<style scoped>
.code {
  background: #14201a;
  color: #e6f0dd;
  border-radius: 16px;
  padding: 1.25rem 1.5rem;
  font-family: 'SF Mono', 'JetBrains Mono', ui-monospace, Menlo, Consolas, monospace;
  font-size: 13px;
  line-height: 1.7;
  overflow-x: auto;
  white-space: pre;
}
.inline-code {
  font-family: ui-monospace, Menlo, Consolas, monospace;
  font-size: 0.9em;
  background: var(--tw-prose, #e0eacd);
  background: #e0eacd;
  padding: 0.1em 0.4em;
  border-radius: 6px;
}
</style>
