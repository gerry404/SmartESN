import { http } from '@/utils/http'

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

export interface Conversation {
  id: number
  titre: string
  dateMaj: string
}

// Liste les discussions de l'utilisateur (récentes d'abord)
export function listConversations(): Promise<Conversation[]> {
  return http<Conversation[]>('/chat/conversations')
}

// Crée une nouvelle discussion vide
export function creerConversation(): Promise<Conversation> {
  return http<Conversation>('/chat/conversations', { method: 'POST' })
}

// Récupère les messages d'une discussion (mémoire persistée)
export function getMessages(conversationId: number): Promise<ChatMessage[]> {
  return http<ChatMessage[]>(`/chat/conversations/${conversationId}/messages`)
}

// Envoie un message : le backend persiste et appelle le LLM avec tout l'historique
export async function sendMessage(conversationId: number, content: string): Promise<string> {
  const res = await http<{ role: string; content: string }>(
    `/chat/conversations/${conversationId}/messages`,
    { method: 'POST', body: JSON.stringify({ content }) },
  )
  return res.content
}

// Supprime une discussion
export function supprimerConversation(id: number): Promise<void> {
  return http<void>(`/chat/conversations/${id}`, { method: 'DELETE' })
}
