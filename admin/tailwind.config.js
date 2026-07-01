/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        'page-bg': '#FAF9F5',
        'panel-bg': '#F0EFEB',
        'soft-card': '#E9E8E4',
        'soft-card-2': '#F4F3EF',
        text: '#1B1B1B',
        muted: '#8C8880',
        'muted-light': '#BDBAB2',
        line: '#D9D7D0',
        black: '#181818',
        'deep-black': '#151515',
        'white-card': '#FFFDF8',
        'white-soft': '#FAF9F5',
        brand: {
          from: '#FF416C',
          to: '#FF4B2B',
        },
      },
      borderRadius: {
        DEFAULT: '0.125rem',
        lg: '0.25rem',
        xl: '0.5rem',
        full: '0.75rem',
        '3xl': '2rem',
        '4xl': '3rem',
      },
      spacing: {
        gutter: '24px',
        unit: '4px',
        sm: '16px',
        md: '32px',
        xl: '128px',
        lg: '64px',
        margin: '40px',
        xs: '8px',
        section: '160px',
      },
      fontFamily: {
        // Titres d'affichage — Roundex
        display: ['var(--font-display)'],
        h1: ['var(--font-display)'],
        h2: ['var(--font-display)'],
        // Corps, sous-titres, UI — HiJO
        sans: ['var(--font-body)'],
        h3: ['var(--font-body)'],
        label: ['var(--font-body)'],
        'body-lg': ['var(--font-body)'],
        'body-md': ['var(--font-body)'],
      },
      fontSize: {
        label: ['12px', { lineHeight: '1', letterSpacing: '0.05em', fontWeight: '600' }],
        'body-lg': ['18px', { lineHeight: '1.6', letterSpacing: '0', fontWeight: '400' }],
        display: ['82px', { lineHeight: '1.05', letterSpacing: '-0.04em', fontWeight: '700' }],
        h2: ['32px', { lineHeight: '1.2', letterSpacing: '-0.02em', fontWeight: '600' }],
        h1: ['48px', { lineHeight: '1.1', letterSpacing: '-0.03em', fontWeight: '700' }],
        h3: ['24px', { lineHeight: '1.3', letterSpacing: '-0.01em', fontWeight: '600' }],
        'body-md': ['15px', { lineHeight: '1.6', letterSpacing: '0', fontWeight: '400' }],
      },
      keyframes: {
        scroll: {
          '0%': { transform: 'translateX(0)' },
          '100%': { transform: 'translateX(calc(-50% - 50px))' },
        },
      },
      animation: {
        marquee: 'scroll 30s linear infinite',
      },
    },
  },
  plugins: [],
}
