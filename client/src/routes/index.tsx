import { createFileRoute } from '@tanstack/react-router'
import Hero from '@/components/pages/index/Hero'
import Features from '@/components/pages/index/Features'
import CallToAction from '@/components/pages/index/CallToAction'

export const Route = createFileRoute('/')({
  component: App,
})

function App() {
  return (
    <main className="flex-1">
      <Hero />
      <Features />
      <CallToAction />
    </main>
  )
}
