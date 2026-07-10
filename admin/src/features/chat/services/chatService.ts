import { http } from '@/utils/http'

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

// 👇 Faux modèle le temps que le backend expose un endpoint de chat.
//    Passe à `false` et adapte l'URL/format quand `/chat` sera disponible.
const USE_MOCK = true

function fakeDelay<T>(value: T, ms = 700): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(value), ms))
}

/**
 * Envoie l'historique au modèle et renvoie la réponse de l'assistant.
 * Version réelle attendue : POST /chat { messages } → { reply }.
 */
export async function sendMessage(messages: ChatMessage[]): Promise<string> {
  if (USE_MOCK) {
    const last = messages.filter((m) => m.role === 'user').at(-1)?.content ?? ''
    return fakeDelay(
      `Bien reçu. Concernant « ${last.slice(0, 80)} » : je peux vous aider à qualifier ` +
        `le besoin, estimer les charges et préparer un chiffrage cohérent. ` +
        `(Réponse simulée — le modèle réel sera branché sur l'endpoint /chat.)`,
    )
  }
  const res = await http<{ reply: string }>('/chat', {
    method: 'POST',
    body: JSON.stringify({ messages }),
  })
  return res.reply
}
