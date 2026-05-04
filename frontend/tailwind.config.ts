import type { Config } from 'tailwindcss'
import defaultTheme from 'tailwindcss/defaultTheme'

const config: Config = {
  content: [
    './src/**/*.{ts,tsx,js,jsx,mdx}',
    './app/**/*.{ts,tsx,js,jsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        border: "hsl(var(--border))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        primary: {
          DEFAULT: "hsl(var(--primary))",
          foreground: "hsl(var(--primary-foreground))",
        },
        secondary: {
          DEFAULT: "hsl(var(--secondary))",
          foreground: "hsl(var(--secondary-foreground))",
        },
        accent: {
          DEFAULT: "hsl(var(--accent))",
          foreground: "hsl(var(--accent-foreground))",
        },
        card: {
          DEFAULT: "hsl(var(--card))",
          foreground: "hsl(var(--card-foreground))",
        },
        brand: {
          50: '#f2fdfa',
          100: '#e6faf6',
          200: '#bff3ea',
          300: '#8beeda',
          400: '#40dbc5',
          500: '#0fb9a9',
          600: '#0e9a8f',
          700: '#0f766e',
          800: '#0f5d55',
          900: '#084341',
        },
      },
      spacing: {
        18: '4.5rem',
        22: '5.5rem',
      },
      fontFamily: {
        sans: ['var(--font-be-vietnam-pro)', ...defaultTheme.fontFamily.sans],
      },
      borderRadius: {
        xl: '0.75rem',
      },
    },
  },
  plugins: [],
}

export default config
