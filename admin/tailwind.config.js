/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        // Palette "vert" extraite de l'image (ancienne valeur beige en commentaire).
        'page-bg': '#F2F6EA', //   was #FAF9F5
        'panel-bg': '#E9F0DC', //  was #F0EFEB
        'soft-card': '#E0EACD', // was #E9E8E4
        'soft-card-2': '#EEF3E2', // was #F4F3EF
        text: '#16241A', //        was #1B1B1B
        muted: '#74806A', //       was #8C8880
        'muted-light': '#ABB79A', // was #BDBAB2
        line: '#D2DEBE', //        was #D9D7D0
        black: '#14201A', //       was #181818
        'deep-black': '#0E1712', // was #151515
        'white-card': '#FBFDF4', // was #FFFDF8
        'white-soft': '#F2F6EA', // was #FAF9F5
        brand: {
          from: '#3F8F2E', //      was #FF416C
          to: '#A6D84B', //        was #FF4B2B
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
        // Titres d'affichage  Roundex
        display: ['var(--font-display)'],
        h1: ['var(--font-display)'],
        h2: ['var(--font-display)'],
        // Corps, sous-titres, UI - HiJO
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
