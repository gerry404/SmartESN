// Domain types for the SmartESN landing page content.
// SmartESN  Qualification et chiffrage assistés par IA des demandes clients pour une ESN.

export interface NavLink {
  label: string
  href: string
}

export interface Brand {
  name: string
}

export interface HeroContent {
  title: string
  subtitle: string
  image: string
  imageAlt: string
  primaryCta: string
  secondaryCta: string
}

export interface CollageBadge {
  icon: string
  label: string
}

export interface CollageCard {
  id: string
  type: 'image' | 'stat' | 'toggle'
  image?: string
  imageAlt?: string
  style: string
  badge?: CollageBadge
  progressLabel?: string
  progressValue?: number
  title?: string
  subtitle?: string
}

export interface FeatureItem {
  title: string
  description: string
}

export interface FeatureBlock {
  eyebrow: string
  title: string
  items: FeatureItem[]
  reversed?: boolean
}

export interface TeamTab {
  id: string
  label: string
}

export interface TeamCard {
  tab: string
  title: string
  description: string
  image: string
  imageAlt: string
}

export interface Testimonial {
  quote: string
  author: string
  role: string
  avatar: string
}

export interface Update {
  category: string
  date: string
  title: string
  image: string
  imageAlt: string
}

export interface FooterColumn {
  title: string
  links: NavLink[]
}

export interface CtaContent {
  title: string
  primaryCta: string
  secondaryCta: string
}

export interface HomeContent {
  nav: {
    brand: string
    links: NavLink[]
    cta: string
  }
  hero: HeroContent
  brands: Brand[]
  collage: {
    title: string[]
    cards: CollageCard[]
  }
  manifesto: string
  featureBlocks: FeatureBlock[]
  studio: {
    eyebrow: string
    title: string
    items: FeatureItem[]
    promptLabel: string
    promptText: string
  }
  teams: {
    title: string
    tabs: TeamTab[]
    cards: TeamCard[]
  }
  testimonial: Testimonial
  updates: {
    title: string
    items: Update[]
  }
  cta: CtaContent
  footer: {
    tagline: string
    columns: FooterColumn[]
    legal: string
    legalLinks: NavLink[]
  }
}
