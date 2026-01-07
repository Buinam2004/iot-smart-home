import { useEffect, useMemo, useState } from 'react'
import {
  Activity,
  AlertTriangle,
  Clock3,
  Droplets,
  Flame,
  Radar,
  ThermometerSun,
  Wifi,
  WifiOff,
} from 'lucide-react'
import DeviceControl from '@/components/DeviceControl'

interface SensorSample {
  timestamp: number
  temperature: number
  humidity: number
  motion: boolean
  fire: boolean
}

const HISTORY_LIMIT = 40

export default function Dashboard() {
  const { latest, history, connectionStatus } = useRealtimeSensors()
  const previous = history.length > 1 ? history[history.length - 2] : undefined

  const metrics = useMemo(
    () => [
      {
        key: 'temperature',
        label: 'Nhiệt độ',
        value: `${latest.temperature.toFixed(1)}°C`,
        delta: previous
          ? (latest.temperature - previous.temperature).toFixed(1)
          : undefined,
        series: history.map((point) => point.temperature),
        icon: ThermometerSun,
        color: 'from-orange-500 to-red-500',
        stroke: '#ea580c',
      },
      {
        key: 'humidity',
        label: 'Độ ẩm',
        value: `${latest.humidity.toFixed(1)}%`,
        delta: previous
          ? (latest.humidity - previous.humidity).toFixed(1)
          : undefined,
        series: history.map((point) => point.humidity),
        icon: Droplets,
        color: 'from-sky-500 to-blue-600',
        stroke: '#0284c7',
      },
      {
        key: 'motion',
        label: 'Chuyển động',
        value: latest.motion ? 'Đang phát hiện' : 'Yên tĩnh',
        delta: undefined,
        series: history.map((point) => (point.motion ? 1 : 0)),
        icon: Radar,
        color: 'from-emerald-500 to-green-600',
        stroke: '#16a34a',
      },
      {
        key: 'fire',
        label: 'Cảnh báo cháy',
        value: latest.fire ? 'Có cảnh báo' : 'An toàn',
        delta: undefined,
        series: history.map((point) => (point.fire ? 1 : 0)),
        icon: Flame,
        color: 'from-rose-500 to-red-600',
        stroke: '#e11d48',
      },
    ],
    [
      history,
      latest.fire,
      latest.humidity,
      latest.motion,
      latest.temperature,
      previous,
    ],
  )

  const recentEvents = useMemo(() => {
    return [...history]
      .slice(-6)
      .reverse()
      .map((sample) => ({
        id: sample.timestamp,
        label: formatTime(sample.timestamp),
        message: buildEventMessage(sample),
        severity: sample.fire ? 'high' : sample.motion ? 'medium' : 'low',
      }))
  }, [history])

  return (
    <main className="mx-auto w-full max-w-6xl px-4 py-8">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Nhà thông minh</h1>
          <p className="text-gray-600">
            Theo dõi nhiệt độ, độ ẩm, chuyển động và cảnh báo cháy theo thời
            gian thực.
          </p>
        </div>
        <div className="flex items-center gap-3 rounded-full border border-gray-200 bg-white px-4 py-2 shadow-sm">
          {connectionStatus === 'online' ? (
            <Wifi className="h-5 w-5 text-green-600" />
          ) : (
            <WifiOff className="h-5 w-5 text-gray-400" />
          )}
          <div className="text-sm leading-tight text-gray-700">
            <p className="font-semibold capitalize">{connectionStatus}</p>
            <p className="text-xs text-gray-500">
              Cập nhật: {formatTime(latest.timestamp)}
            </p>
          </div>
        </div>
      </header>

      <section className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {metrics.map((metric) => (
          <article
            key={metric.key}
            className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm"
          >
            <div className="flex items-center justify-between gap-2">
              <div className="flex items-center gap-2">
                <div
                  className={`flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br ${metric.color} text-white`}
                >
                  <metric.icon className="h-5 w-5" />
                </div>
                <div>
                  <p className="text-xs uppercase tracking-wide text-gray-500">
                    {metric.label}
                  </p>
                  <p className="text-2xl font-semibold text-gray-900">
                    {metric.value}
                  </p>
                </div>
              </div>
              {metric.delta !== undefined && (
                <span
                  className={`rounded-full px-3 py-1 text-xs font-medium ${Number(metric.delta) >= 0 ? 'bg-green-50 text-green-700' : 'bg-rose-50 text-rose-700'}`}
                >
                  {Number(metric.delta) >= 0 ? '+' : ''}
                  {metric.delta}
                </span>
              )}
            </div>
            <div className="mt-3">
              <Sparkline
                data={metric.series}
                color={metric.stroke}
                idPrefix={`spark-${metric.key}`}
              />
            </div>
          </article>
        ))}
      </section>

      <section className="mt-6 grid gap-4 lg:grid-cols-2">
        <div className="space-y-4">
          <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-gray-900">Trạng thái</h3>
              <Activity className="h-4 w-4 text-gray-400" />
            </div>
            <dl className="mt-3 space-y-3 text-sm">
              <StatusRow
                label="Chuyển động"
                active={latest.motion}
                description={
                  latest.motion
                    ? 'Có người / vật thể đang di chuyển'
                    : 'Không phát hiện chuyển động'
                }
              />
              <StatusRow
                label="Cảnh báo cháy"
                active={latest.fire}
                tone="alert"
                description={
                  latest.fire
                    ? 'Vui lòng kiểm tra ngay nguồn phát cháy.'
                    : 'Không ghi nhận tín hiệu cảnh báo.'
                }
              />
            </dl>
          </div>

          <DeviceControl
            initial={{ light: false, fan: false, doorOpen: false }}
            onChange={(s) => console.log('DeviceControl state:', s)}
          />
        </div>

        <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-gray-900">
              Sự kiện mới nhất
            </h3>
            <AlertTriangle className="h-4 w-4 text-amber-500" />
          </div>
          <ul className="mt-3 space-y-3 text-sm">
            {recentEvents.map((event) => (
              <li
                key={event.id}
                className="flex items-start gap-3 rounded-lg bg-gray-50 px-3 py-2"
              >
                <div
                  className={`mt-0.5 h-2.5 w-2.5 rounded-full ${
                    event.severity === 'high'
                      ? 'bg-rose-500'
                      : event.severity === 'medium'
                        ? 'bg-amber-500'
                        : 'bg-emerald-500'
                  }`}
                />
                <div>
                  <p className="text-gray-900">{event.message}</p>
                  <p className="text-xs text-gray-500">{event.label}</p>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </section>
    </main>
  )
}

function useRealtimeSensors(intervalMs = 3500) {
  const [history, setHistory] = useState<SensorSample[]>(() => {
    const seed: SensorSample[] = []
    let previous: SensorSample | undefined
    for (let i = 0; i < 10; i += 1) {
      const next = generateSample(previous)
      seed.push(next)
      previous = next
    }
    return seed
  })
  const [online, setOnline] = useState(true)

  useEffect(() => {
    setOnline(true)
    const timer = window.setInterval(() => {
      setHistory((prev) => {
        const latest = prev[prev.length - 1]
        const next = generateSample(latest)
        const nextHistory = [...prev, next]
        if (nextHistory.length > HISTORY_LIMIT) {
          return nextHistory.slice(nextHistory.length - HISTORY_LIMIT)
        }
        return nextHistory
      })
    }, intervalMs)

    const jitter = window.setInterval(() => {
      setOnline(Math.random() > 0.02)
    }, 20000)

    return () => {
      window.clearInterval(timer)
      window.clearInterval(jitter)
    }
  }, [intervalMs])

  const latest = history[history.length - 1] ?? generateSample()

  return { latest, history, connectionStatus: online ? 'online' : 'offline' }
}

function generateSample(previous?: SensorSample): SensorSample {
  const baseTemp = previous?.temperature ?? 27
  const baseHumidity = previous?.humidity ?? 62
  const now = Date.now()

  const temperature = clamp(baseTemp + randomBetween(-0.5, 0.8), 18, 40)
  const humidity = clamp(baseHumidity + randomBetween(-1.8, 1.8), 30, 95)

  const motionTriggered = previous?.motion
    ? Math.random() > 0.35
    : Math.random() > 0.78
  const fireTriggered = previous?.fire
    ? Math.random() > 0.6
    : Math.random() > 0.97

  return {
    timestamp: now,
    temperature: Number(temperature.toFixed(1)),
    humidity: Number(humidity.toFixed(1)),
    motion: motionTriggered,
    fire: fireTriggered,
  }
}

function Sparkline({
  data,
  color,
  idPrefix,
}: {
  data: number[]
  color: string
  idPrefix: string
}) {
  const width = 220
  const height = 64
  const padding = 8
  const points = data.slice(-16)

  if (points.length === 0) return null

  const min = Math.min(...points)
  const max = Math.max(...points)
  const range = max - min || 1
  const step =
    points.length > 1 ? (width - padding * 2) / (points.length - 1) : 0

  const toCoords = (value: number, index: number) => {
    const x = padding + index * step
    const y =
      height - padding - ((value - min) / range) * (height - padding * 2)
    return { x, y }
  }

  const linePath = points
    .map((value, index) => {
      const { x, y } = toCoords(value, index)
      return `${index === 0 ? 'M' : 'L'}${x} ${y}`
    })
    .join(' ')

  const lastPoint = toCoords(points[points.length - 1], points.length - 1)
  const areaPath = `${linePath} L ${lastPoint.x} ${height - padding} L ${padding} ${height - padding} Z`

  const gradientId = `${idPrefix}-fill`

  return (
    <svg
      viewBox={`0 0 ${width} ${height}`}
      role="presentation"
      className="h-16 w-full"
      aria-hidden
    >
      <defs>
        <linearGradient id={gradientId} x1="0" x2="0" y1="0" y2="1">
          <stop offset="0%" stopColor={color} stopOpacity="0.28" />
          <stop offset="100%" stopColor={color} stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={areaPath} fill={`url(#${gradientId})`} />
      <path
        d={linePath}
        fill="none"
        stroke={color}
        strokeWidth={2.5}
        strokeLinejoin="round"
        strokeLinecap="round"
      />
    </svg>
  )
}

function TrendCard({
  title,
  value,
  series,
  color,
  stroke,
}: {
  title: string
  value: string
  series: number[]
  color: string
  stroke: string
}) {
  return (
    <div className="rounded-xl border border-gray-200 bg-gray-50/80 p-4">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs uppercase tracking-wide text-gray-500">
            {title}
          </p>
          <p className="text-2xl font-semibold text-gray-900">{value}</p>
        </div>
        <div
          className={`h-9 w-9 rounded-full bg-gradient-to-br ${color} opacity-90`}
          aria-hidden
        />
      </div>
      <div className="mt-3">
        <Sparkline data={series} color={stroke} idPrefix={`trend-${title}`} />
      </div>
    </div>
  )
}

function StatusRow({
  label,
  description,
  active,
  tone = 'info',
}: {
  label: string
  description: string
  active: boolean
  tone?: 'info' | 'alert'
}) {
  return (
    <div className="rounded-lg border border-gray-200 bg-gray-50 px-3 py-2">
      <div className="flex items-center justify-between text-sm font-medium text-gray-900">
        <span>{label}</span>
        <span
          className={`flex items-center gap-2 rounded-full px-3 py-1 text-xs font-semibold ${
            active
              ? tone === 'alert'
                ? 'bg-rose-100 text-rose-700'
                : 'bg-emerald-100 text-emerald-700'
              : 'bg-gray-200 text-gray-700'
          }`}
        >
          {active ? 'Hoạt động' : 'Tắt'}
        </span>
      </div>
      <p className="mt-1 text-xs text-gray-600">{description}</p>
    </div>
  )
}

function formatTime(timestamp: number): string {
  const date = new Date(timestamp)
  return date.toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

function formatRelative(timestamp: number): string {
  const diffMs = Date.now() - timestamp
  const diffSec = Math.max(0, Math.floor(diffMs / 1000))
  if (diffSec < 60) return `${diffSec}s trước`
  const diffMin = Math.floor(diffSec / 60)
  if (diffMin < 60) return `${diffMin} phút trước`
  const diffHour = Math.floor(diffMin / 60)
  return `${diffHour} giờ trước`
}

function randomBetween(min: number, max: number): number {
  return Math.random() * (max - min) + min
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max)
}

function buildEventMessage(sample: SensorSample): string {
  if (sample.fire)
    return 'Báo động cháy: cảm biến ghi nhận khói/nhiệt bất thường.'
  if (sample.motion) return 'Phát hiện chuyển động trong khu vực giám sát.'
  return `Nhiệt ${sample.temperature.toFixed(1)}°C · Ẩm ${sample.humidity.toFixed(1)}%`
}
