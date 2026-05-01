# UI components

This folder contains small, framework-agnostic UI primitives used across the frontend.

Usage:
- Import from `@/components/ui` e.g. `import { Button, Input } from '@/components/ui'`
- Components are mobile-first and use Tailwind utility classes

Notes:
- Icons use `lucide-react`. If you hit peer dependency issues with Shadcn/Radix, prefer adding `lucide-react` directly with `--legacy-peer-deps`.
- Keep these primitives small and composable; do not add heavy state logic here.
