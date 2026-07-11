<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import BaseIcon from '@/features/landing/components/ui/BaseIcon.vue'
import {
  listConversations,
  creerConversation,
  getMessages,
  sendMessage,
  supprimerConversation,
  type ChatMessage,
  type Conversation,
} from '../services/chatService'

const conversations = ref<Conversation[]>([])
const currentId = ref<number | null>(null)
const messages = ref<ChatMessage[]>([])
const draft = ref('')
const thinking = ref(false)
const scroller = ref<HTMLElement | null>(null)

const suggestions = [
  'Quel est mon taux de conversion actuel et comment l’améliorer ?',
  'Analyse la répartition de mes demandes par statut.',
  'Quel type de projet me rapporte le plus de chiffre d’affaires ?',
  'Sur quels projets mon estimation s’écarte le plus du budget signé ?',
]

onMounted(async () => {
  try {
    conversations.value = await listConversations()
  } catch {
    conversations.value = []
  }
})

async function scrollBottom() {
  await nextTick()
  const el = scroller.value
  if (el) el.scrollTop = el.scrollHeight
}

// Ouvre une discussion existante et charge ses messages (mémoire)
async function ouvrir(id: number) {
  currentId.value = id
  try {
    messages.value = await getMessages(id)
  } catch {
    messages.value = []
  }
  scrollBottom()
}

async function envoyer(texte?: string) {
  const content = (texte ?? draft.value).trim()
  if (!content || thinking.value) return

  // crée une discussion à la volée si aucune n'est ouverte
  if (currentId.value === null) {
    try {
      const conv = await creerConversation()
      conversations.value.unshift(conv)
      currentId.value = conv.id
    } catch {
      messages.value.push({ role: 'assistant', content: "Impossible de créer la discussion." })
      return
    }
  }

  messages.value.push({ role: 'user', content })
  draft.value = ''
  thinking.value = true
  scrollBottom()

  try {
    const reply = await sendMessage(currentId.value!, content)
    messages.value.push({ role: 'assistant', content: reply })
    // rafraîchit la liste (titre auto + ordre)
    conversations.value = await listConversations()
  } catch {
    messages.value.push({
      role: 'assistant',
      content: "Désolé, l'assistant est momentanément indisponible.",
    })
  } finally {
    thinking.value = false
    scrollBottom()
  }
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    envoyer()
  }
}

function nouvelle() {
  currentId.value = null
  messages.value = []
  draft.value = ''
}

async function supprimer(id: number, e: Event) {
  e.stopPropagation()
  try {
    await supprimerConversation(id)
    conversations.value = conversations.value.filter((c) => c.id !== id)
    if (currentId.value === id) nouvelle()
  } catch {
    /* ignore */
  }
}
</script>

<template>
  <div class="h-full flex bg-page-bg">
    <!-- Sidebar : historique des discussions -->
    <aside class="hidden md:flex w-64 shrink-0 flex-col border-r border-line bg-white-card">
      <div class="p-3">
        <button
          class="w-full rounded-xl border border-line px-3 py-2 text-[13px] font-semibold flex items-center gap-2 hover:bg-page-bg transition-colors"
          @click="nouvelle"
        >
          <BaseIcon name="add" class="text-lg" /> Nouvelle discussion
        </button>
      </div>
      <div class="flex-1 min-h-0 overflow-y-auto px-2 pb-2">
        <button
          v-for="c in conversations"
          :key="c.id"
          class="group w-full text-left rounded-lg px-3 py-2 text-[13px] flex items-center justify-between gap-2 transition-colors"
          :class="c.id === currentId ? 'bg-page-bg font-semibold' : 'hover:bg-page-bg'"
          @click="ouvrir(c.id)"
        >
          <span class="truncate">{{ c.titre }}</span>
          <BaseIcon
            name="delete"
            class="text-base opacity-0 group-hover:opacity-60 hover:!opacity-100 shrink-0"
            @click="supprimer(c.id, $event)"
          />
        </button>
        <p v-if="!conversations.length" class="text-center text-[12px] text-muted mt-4">
          Aucune discussion
        </p>
      </div>
    </aside>

    <!-- Colonne principale -->
    <div class="flex-1 min-w-0 flex flex-col">
    <!-- Barre supérieure -->
    <header class="shrink-0 h-14 flex items-center justify-between px-6 border-b border-line">
      <div class="flex items-center gap-2.5">
        <span class="w-7 h-7 rounded-full bg-gradient-to-tr from-brand-from to-brand-to flex items-center justify-center">
          <BaseIcon name="auto_awesome" class="text-white text-sm" />
        </span>
        <span class="font-semibold">Assistant SmartESN</span>
      </div>
      <button class="text-[13px] font-semibold text-muted hover:text-text flex items-center gap-1.5" @click="nouvelle">
        <BaseIcon name="add" class="text-lg" /> Nouvelle discussion
      </button>
    </header>

    <!-- Zone messages -->
    <div ref="scroller" class="flex-1 min-h-0 overflow-y-auto">
      <div class="max-w-3xl mx-auto px-4 py-8">
        <!-- État vide -->
        <div v-if="!messages.length" class="flex flex-col items-center text-center pt-16">
          <span class="w-14 h-14 rounded-2xl bg-gradient-to-tr from-brand-from to-brand-to flex items-center justify-center mb-5">
            <BaseIcon name="auto_awesome" class="text-white text-2xl" />
          </span>
          <h1 class="text-2xl font-bold mb-2">Comment puis-je aider ?</h1>
          <p class="text-muted mb-8 max-w-md">
            Posez une question sur la qualification, le chiffrage ou une demande client.
          </p>
          <div class="flex flex-col gap-2 w-full max-w-lg">
            <button
              v-for="s in suggestions"
              :key="s"
              class="text-left rounded-xl border border-line bg-white-card px-4 py-3 text-[14px] hover:border-text/30 transition-colors"
              @click="envoyer(s)"
            >
              {{ s }}
            </button>
          </div>
        </div>

        <!-- Fil de discussion -->
        <div v-else class="flex flex-col gap-6">
          <div v-for="(m, i) in messages" :key="i" class="flex gap-3" :class="m.role === 'user' ? 'flex-row-reverse' : ''">
            <span
              class="shrink-0 w-8 h-8 rounded-full flex items-center justify-center text-[12px] font-bold"
              :class="m.role === 'user' ? 'bg-text text-white' : 'bg-gradient-to-tr from-brand-from to-brand-to text-white'"
            >
              <BaseIcon v-if="m.role === 'assistant'" name="auto_awesome" class="text-sm" />
              <template v-else>Moi</template>
            </span>
            <div
              class="rounded-2xl px-4 py-3 text-[14px] leading-relaxed whitespace-pre-line max-w-[80%]"
              :class="m.role === 'user' ? 'bg-text text-white' : 'bg-white-card border border-line'"
            >
              {{ m.content }}
            </div>
          </div>

          <!-- Indicateur de réflexion -->
          <div v-if="thinking" class="flex gap-3">
            <span class="shrink-0 w-8 h-8 rounded-full bg-gradient-to-tr from-brand-from to-brand-to flex items-center justify-center">
              <BaseIcon name="auto_awesome" class="text-white text-sm" />
            </span>
            <div class="rounded-2xl px-4 py-3 bg-white-card border border-line flex items-center gap-1">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Saisie -->
    <div class="shrink-0 border-t border-line bg-page-bg">
      <div class="max-w-3xl mx-auto px-4 py-4">
        <div class="flex items-end gap-2 rounded-2xl border border-line bg-white-card p-2 focus-within:border-text/40 transition-colors">
          <textarea
            v-model="draft"
            rows="1"
            placeholder="Écrivez votre message…  (Entrée pour envoyer, Maj+Entrée pour un retour à la ligne)"
            class="flex-1 resize-none bg-transparent px-3 py-2 outline-none max-h-40 text-[14px]"
            @keydown="onKeydown"
          ></textarea>
          <button
            class="shrink-0 w-10 h-10 rounded-xl bg-black text-white flex items-center justify-center disabled:opacity-40"
            :disabled="!draft.trim() || thinking"
            @click="envoyer()"
          >
            <BaseIcon name="arrow_upward" class="text-lg" />
          </button>
        </div>
        <p class="text-center text-[11px] text-muted mt-2">L'assistant peut se tromper. Vérifiez les informations importantes.</p>
      </div>
    </div>
    </div>
    <!-- fin colonne principale -->
  </div>
</template>

<style scoped>
.dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: #a6b49a;
  animation: blink 1.2s infinite ease-in-out;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes blink {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}
</style>
