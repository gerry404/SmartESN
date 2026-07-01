import type { HomeContent } from '../types'



const IMG = {
  hero: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCZfadtPwCaL7GcKGqde5_m46RsgQuGHyvmFJKZWsW5jfFqnNirmZPOdmGitXGSosoyvd2ARpUMWjjL8IEKefG6Ke85eAGiFMBHIfFA02wVwfcf75CnI7Zlw-OjEat9LRRuqHbfAWLeBZeTEUbVQRAoPIjSlWUOZmW8R32Bk0XUblejaVIpv6gSC4cngTCtp7tXSmIz5jJIUVZIPoKSyGDhck_ulpZZnIfCQmxw2h73JqSgD__x2T1wiMf4Q_TQCvHgDTzQ3sY3JjqV',
  c1: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDGxwLNMP2KVAWtuqvWXiFZkBm5YgSXjM45PkkRPvB3NsuAPX4Nse6nAySmOSPxXiaWvlTk0fB0Pagx0EkNf1L8x3CFyxgAWpQbdP2z4ZXhaiVorAeLCKBcI2qvs9oG9HqdMdiJrUYvBgL1Dr8nKNzygffqFMIlgWyshucf41s65I_hV3kCGtjFYShml4zOn9O6MuvVW54WVcjP7bBLEAYguBfutmc0Yq73HP9-yBiY6b98be0hC6KYAZFesHH3FkL4Gx0Yks2fyrpu',
  c2: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDRI6ZfedfqJ42Kz8fkZTlqC5CbQ5Q_0kVkLxIu7Xh8-ABjTl2K4v_yJEBQE1wi3AZMYD9IMvFxFOJyc86piFVAfQ0g6nyc1fRoYl7wB1_YDB3zlrCqOGtEow28qlTRV6LL9s1-rRPvAYojJgQhjJ9ERQ7Jf-IhHeg0bAcA4QWd3YKyDnVxSjTGeJ--ir7xqCghMtZPJ5OjvBwWSTiw7xPHbFrwOa_f5RTQGV6WOEWxuC-xwV7Ayb1q0kBzYF_oQHCxP3Dn3PL-aewa',
  c4: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCRd7pAyBrUx31zx6klyDiCWD04EiIaiDfNYNdGO8HozSTYH3bTc4IhWoBbOh4CpW4jAmMk2H_ecdJwzZL71ta4NvHIyMqapKo2BK36h0hn674xlIS0FoLhl6jQwDTwu_Idk9ZLj6MN8k5s-VrWMSox9J5YNpnlpqVlAVnzCXtAeDkwyvNWzXtGXt76wUk85CfXUmdE9Vi6RxqxkyBORzc1__OdpVEqiYNrMD1dKFJIU7gC874F--k_Kf5C1_yMSnhoytWkS9XWPwsm',
  c5: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAJHfj4_xGj0yuLj8FKt6X72bCIskF-kJl3LA3T3_1-Qfv6mfKwCLXqKuU41nv1J3p_M1VLaKyUwt7jymaxu9IqrMBeOZwhDvKB2NclDw7qctzcy_CAPxlD6DRpxJ5uMf6G4nCRrvnMp59n2r2y9HaRxoemHYv9Vm4p_TMdRUwKitMvWXTR13rOY5umPLmIVd6L5RMbQMm1ZFC8-WMQdUrzWugK5J4Eh0Ph9XOn2e4G84KG4Az4KXAh3-n2Y52A1ycQ8Ow3OGXHosuz',
  c6: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDbheL8VQt2V3qxmEXBukkZcNptuzepd42MEKEg9-qDVlpepWCcPZIR5s9MzcSFv4FH-6IuiF9uWjjxSWg2IQ-A_CdbSU7IbdGFdbRPcFqz1DyoSSA1w-kOaLoqdLsFYVQuBBDENJ0aa2OHfgIFFJ1Y7GClriULppWYz7d7fGuv-GPC_YjW-mQfwtuP1rbtxhAb-IzFfX6o7nj41jI_XdcVGj6on-6bwuQl_W5NMa4g3MvEW_sVCeo4Tgf1W3QkrnYFQKfSyl3ScLps',
  avatar: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDxUZpAK7kBAK1H5Anlz-i_nh19XyaGUt1hLDC6ZASnuBK2vRcwFDP46vx7HodMWCGvWJVA3aKLfp00pDRGNPsH0LVYvOGBSQ1KJIx2wTCxFcEJMNlnv9DHJKT0wyVelxRunXJN3d41Z2MPDK4poxQF_iybQaYL4ebVjC52RWnq9U6Y-VA7k5seZ7Z4c1n3nq7ply6CwSNm8-OH_B6wae1-h-Zmn2y3jh9urDyal2zhahuy408MHHCcPTmA9TNKUOA7WEsjuEy1iMvC',
  brief: 'https://lh3.googleusercontent.com/aida-public/AB6AXuChKRPJn1gSFDrt1GQDzfiU4I3yCzTQVQeuNO8ytisTcKlK31yEv5K86cdWMKJBg3GC95w5q1ERJKHJLKU3w-6SojCl15IPwtoKItbHfym_OWSwde4Lc_X0Q39r2NUE_oZu-ti8_Zs_OibuoUCAcJYE-cSQuZwNsvFjQJC5rdU9maW6xzUq0FkX5WNlRsxq4L4CR7oinVYlFK6v9BDjMPShHM5UEvXrG_3JYGqBIWaiRdb9MTmRBmaZdMBZC44wnynC-oFH9TaZlB9Q',
  social: 'https://lh3.googleusercontent.com/aida-public/AB6AXuABa0q5BJ_DreMgIZGB-8at3sYpuFks_tT431tznVrVWB2C49LzdUC651wzXTj9LD-7VYCWRRpo9bqyyAQ_b54KnjtoPjXSRfeIXbBgjwHXIr8Ojo51Y8MG8CC0mGtvupgQSf4RnLcvReiqPWlUs5L2KkemFlhV-MfAQAzfyxIvGWfFM2Orh3ChuU5A6sOS2VBQbJyPWAQcwMlr6YWpCbln_V8DC4Yo90e6mK4QAINc-FrScaJ_KPin-t6UJ12p4Dnl5CbWpvGS2GiO',
  deck: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCAsZf4UPkmlE6_mXYyjLE0rRS6z3wyPwTyXyHBEx2fzb_1GTK9oiAbqHMYNp1BpaU32inyxnVzpmayXVCbCKB_1Vo5pWerZ__YxCtY56FNpcHMwYf9urWgXAJQyGfscYHxMbULHWIsDeg5XCImbVDEPCwNhnAFC1xNYFgDTRv4KBYk1lGhIhBi4Pez_FSe2stW1TrgJkEhZUjpZcpgdrnwOaIDJo-2fibASWue5TrVKAp0CiX5ahI1LePNwx8xkgPfEO5mMALLcQWo',
  voice: 'https://lh3.googleusercontent.com/aida-public/AB6AXuB5cY_8WSiYE8XLX9-TC1PN-9mihq_rEdAuIWhtvcYj7w3Jym37KSRruLnz0ZQY5zy1_DktUdT0RLTDQ4dIOGeKAvBWKyz6Fg4AS8M9VGl8uRkbhFz7qmqDNDpZ-CnVzYng1m39Wxi--hMoM4aMjONRhxBfdlgygDuhMZXzobuhZkCisfF6llXxrL9fOU48PhgyUNZx5V5gH-adc6HxffpgIWavNNpFCUnafc-d7HEDWlEQx3S8yVrTddmzqlMzQIOc0ib4Bxpxxe-W',
}

const homeContent: HomeContent = {
  nav: {
    brand: 'SmartESN',
    links: [
      { label: 'Produit', href: '#produit' },
      { label: 'Solutions', href: '#solutions' },
      { label: 'À propos', href: '#a-propos' },
      { label: 'Ressources', href: '#ressources' },
    ],
    cta: 'Commencer',
  },
  hero: {
    title: 'Qualifiez et chiffrez chaque demande client en minutes',
    subtitle:
      "SmartESN analyse vos appels d'offres et demandes entrantes, en extrait le besoin, et produit une qualification et un chiffrage fiables , pour que vos équipes avant-vente se concentrent sur ce qui compte.",
    image: IMG.hero,
    imageAlt: "Tableau de bord SmartESN de qualification des demandes",
    primaryCta: 'Demander une démo',
    secondaryCta: 'Voir le produit',
  },
  brands: [
    { name: 'Néorélis' },
    { name: 'Axendia' },
    { name: 'Velosys' },
    { name: 'Groupe Meridian' },
    { name: 'Damaris IT' },
  ],
  collage: {
    title: ['Intelligence', "d'avant-vente"],
    cards: [
      {
        id: 'processing',
        type: 'image',
        image: IMG.c1,
        imageAlt: 'Analyse de la demande en cours',
        style: 'left: 10%; top: 12%; width: 340px; height: 260px; z-index: 5;',
        progressLabel: 'Qualification',
        progressValue: 78,
      },
      {
        id: 'ai',
        type: 'image',
        image: IMG.c2,
        imageAlt: 'Extraction du besoin par IA',
        style: 'right: 12%; top: 15%; width: 280px; height: 320px; z-index: 5;',
        badge: { icon: 'auto_awesome', label: '' },
      },
      {
        id: 'sync',
        type: 'toggle',
        style: 'left: 6%; top: 45%; width: 260px; z-index: 15;',
        title: 'CRM synchronisé',
        subtitle: 'Connecté',
      },
      {
        id: 'compliance',
        type: 'image',
        image: IMG.c4,
        imageAlt: 'Conformité du chiffrage',
        style: 'right: 8%; top: 52%; width: 320px; height: 220px; z-index: 5;',
        badge: { icon: '', label: 'Grille tarifaire' },
      },
      {
        id: 'scoring',
        type: 'image',
        image: IMG.c5,
        imageAlt: 'Scoring des opportunités',
        style: 'left: 15%; top: 72%; width: 380px; height: 260px; z-index: 5;',
        badge: { icon: 'insights', label: 'Scoring go / no-go' },
      },
      {
        id: 'wide',
        type: 'image',
        image: IMG.c6,
        imageAlt: 'Vue portefeuille de demandes',
        style: 'right: 15%; top: 78%; width: 360px; height: 240px; z-index: 5;',
      },
    ],
  },
  manifesto:
    "À mesure que les demandes clients affluent ; appels d'offres, cahiers des charges, e-mails , disposer d'une lecture unique et fiable du besoin n'a jamais été aussi décisif. SmartESN unifie la qualification et le chiffrage de votre avant-vente.",
  featureBlocks: [
    {
      eyebrow: 'Produit',
      title: 'Le socle intelligent de votre avant-vente.',
      items: [
        {
          title: 'Analyse automatique des demandes',
          description:
            "Importez un cahier des charges ou un appel d'offres : SmartESN en extrait le périmètre, les technologies, les livrables et les contraintes.",
        },
        {
          title: 'Qualification contextuelle',
          description:
            "Notre IA comprend le contexte métier de vos clients et met en évidence les risques, les zones d'ombre et les questions à poser.",
        },
        {
          title: 'Source de vérité unique',
          description:
            "Toutes vos demandes, qualifications et chiffrages réunis dans un espace cohérent, recherchable et partagé entre les équipes.",
        },
      ],
    },
  ],
  studio: {
    eyebrow: 'Studio',
    title: 'Produisez un chiffrage fiable en quelques secondes.',
    items: [
      {
        title: 'Chiffrage assisté',
        description:
          "Estimation des charges par lot, profils mobilisés et durée , à partir de vos référentiels de projets passés.",
      },
      {
        title: 'Grilles tarifaires respectées',
        description:
          "Chaque devis est aligné automatiquement sur vos TJM, marges cibles et conditions commerciales.",
      },
      {
        title: 'Hypothèses transparentes',
        description:
          "Chaque estimation est justifiée : hypothèses, risques et facteurs de complexité sont explicités.",
      },
      {
        title: 'Historique & apprentissage',
        description:
          "SmartESN s'améliore à partir de vos affaires gagnées pour affiner ses estimations au fil du temps.",
      },
    ],
    promptLabel: 'SMARTESN STUDIO',
    promptText:
      "Chiffre une refonte d'application web pour un client retail : 3 mois, stack React/Node, en respectant nos TJM et une marge cible de 28 %.",
  },
  teams: {
    title: 'Pensé pour toute l’ESN.',
    tabs: [
      { id: 'avant-vente', label: 'Avant-vente' },
      { id: 'commerce', label: 'Commerce' },
      { id: 'delivery', label: 'Delivery' },
      { id: 'direction', label: 'Direction' },
    ],
    cards: [
      {
        tab: 'avant-vente',
        title: 'Réponses aux appels d’offres',
        description: 'Générez des dossiers de réponse structurés et alignés sur le besoin réel du client.',
        image: IMG.brief,
        imageAlt: 'Réponses aux appels d’offres',
      },
      {
        tab: 'commerce',
        title: 'Devis & propositions',
        description: 'Produisez des chiffrages cohérents, sans ressaisie ni écart de marge.',
        image: IMG.social,
        imageAlt: 'Devis et propositions commerciales',
      },
      {
        tab: 'delivery',
        title: 'Cadrage de projet',
        description: 'Transmettez au delivery un périmètre clair, chiffré et documenté.',
        image: IMG.deck,
        imageAlt: 'Cadrage de projet',
      },
      {
        tab: 'direction',
        title: 'Pilotage du pipe',
        description: 'Suivez le taux de qualification, les go / no-go et la charge d’avant-vente.',
        image: IMG.voice,
        imageAlt: 'Pilotage du pipeline commercial',
      },
    ],
  },
  testimonial: {
    quote:
      "SmartESN a été conçu pour libérer les équipes avant-vente des tâches répétitives de qualification et de chiffrage, afin qu'elles se concentrent sur la relation client et la stratégie.",
    author: 'Camille Reynaud',
    role: 'Directrice Avant-vente, Néorélis',
    avatar: IMG.avatar,
  },
  updates: {
    title: 'Actualités',
    items: [
      {
        category: 'Produit • 12 juin',
        date: '12 juin',
        title: 'SmartESN Studio : le chiffrage assisté par IA arrive',
        image: IMG.brief,
        imageAlt: 'SmartESN Studio',
      },
      {
        category: 'Analyses • 8 juin',
        date: '8 juin',
        title: "Réduire de 60 % le temps de qualification d'une demande",
        image: IMG.deck,
        imageAlt: 'Réduction du temps de qualification',
      },
      {
        category: 'Actualité • 24 mai',
        date: '24 mai',
        title: 'SmartESN lève des fonds pour accélérer sur l’IA',
        image: IMG.voice,
        imageAlt: 'Levée de fonds SmartESN',
      },
    ],
  },
  cta: {
    title: "Passez à l'avant-vente augmentée par l'IA",
    primaryCta: 'Planifier un échange',
    secondaryCta: 'Commencer',
  },
  footer: {
    tagline:
      "La plateforme de qualification et de chiffrage assistés par IA pour les ESN. Plus de clarté, moins de temps perdu.",
    columns: [
      {
        title: 'Produit',
        links: [
          { label: 'Plateforme', href: '#' },
          { label: 'Qualification IA', href: '#' },
          { label: 'Studio de chiffrage', href: '#' },
          { label: 'Intégrations', href: '#' },
        ],
      },
      {
        title: 'Entreprise',
        links: [
          { label: 'À propos', href: '#' },
          { label: 'Carrières', href: '#' },
          { label: 'Actualités', href: '#' },
          { label: 'Contact', href: '#' },
        ],
      },
      {
        title: 'Ressources',
        links: [
          { label: 'Blog', href: '#' },
          { label: 'Documentation', href: '#' },
          { label: 'Communauté', href: '#' },
          { label: 'Support', href: '#' },
        ],
      },
    ],
    legal: `© ${new Date().getFullYear()} SmartESN. Tous droits réservés.`,
    legalLinks: [
      { label: 'Confidentialité', href: '#' },
      { label: "Conditions d'utilisation", href: '#' },
      { label: 'Cookies', href: '#' },
    ],
  },
}

export function getHomeContent(): Promise<HomeContent> {
  return Promise.resolve(homeContent)
}
