import { useEffect, useMemo, useRef, useState } from 'react'
import { useQueries, useQuery } from '@tanstack/react-query'
import { AlertTriangle, Wifi, WifiOff } from 'lucide-react'
import type {
  DeviceData,
  DhtData,
  FanData,
  GasData,
  LedPirData,
  PageResponse,
  PirData,
} from '@/types/responses/sensorResponses'
import { api } from '@/auth/authApi'
import { toast } from 'react-toastify'

const BASE_API_URL: string = import.meta.env.VITE_API_URL || ''
const HISTORY_LIMIT = 80
const RECONNECT_DELAY = 5000 // ms

function useDevicesSSE(
  devices?: Array<DeviceData>,
  initialDhtMap?: Partial<Record<string, Array<DhtData>>>, // keyed by device.macAddress
  initialReady?: boolean,
) {
  const sourcesRef = useRef<Map<string, EventSource>>(new Map())
  const lastAttemptRef = useRef<Map<string, number>>(new Map())
  const seededMacsRef = useRef<Set<string>>(new Set())

  type DeviceState = {
    dhtHistory: Array<DhtData>
    lastPir?: PirData
    lastGas?: GasData
    lastFans: Array<FanData>
    lastLedPir?: LedPirData
    lastSeenAt?: number
    online: boolean
  }

  const [stateMap, setStateMap] = useState<Record<string, DeviceState>>({})

  // helper to upsert dht history
  const pushDht = (mac: string, d: DhtData) => {
    setStateMap((prev) => {
      const existing = prev[mac] ?? { dhtHistory: [], lastFans: [] }
      const history = [...existing.dhtHistory]
      history.push(d)
      if (history.length > HISTORY_LIMIT)
        history.splice(0, history.length - HISTORY_LIMIT)
      return {
        ...prev,
        [mac]: {
          ...existing,
          dhtHistory: history,
          lastSeenAt: Date.now(),
          online: true,
        },
      }
    })
  }

  const setPir = (mac: string, p: PirData) => {
    setStateMap((prev) => {
      const existing = prev[mac] ?? { dhtHistory: [], lastFans: [] }
      return {
        ...prev,
        [mac]: {
          ...existing,
          lastPir: p,
          lastSeenAt: Date.now(),
          online: true,
        },
      }
    })
  }

  const setGas = (mac: string, g: GasData) => {
    setStateMap((prev) => {
      const existing = prev[mac] ?? { dhtHistory: [], lastFans: [] }
      return {
        ...prev,
        [mac]: {
          ...existing,
          lastGas: g,
          lastSeenAt: Date.now(),
          online: true,
        },
      }
    })
  }

  const setFan = (mac: string, f: FanData) => {
    setStateMap((prev) => {
      const existing = prev[mac] ?? {
        dhtHistory: [],
        lastFans: [] as Array<FanData>,
      }
      // keep only most recent N fan events
      const fans = [...existing.lastFans, f].slice(-8)
      return {
        ...prev,
        [mac]: {
          ...existing,
          lastFans: fans,
          lastSeenAt: Date.now(),
          online: true,
        },
      }
    })
  }

  const setLedPir = (mac: string, l: LedPirData) => {
    setStateMap((prev) => {
      const existing = prev[mac] ?? { dhtHistory: [], lastFans: [] }
      return {
        ...prev,
        [mac]: {
          ...existing,
          lastLedPir: l,
          lastSeenAt: Date.now(),
          online: true,
        },
      }
    })
  }

  // Seed initial histories once per device when initialReady is true
  useEffect(() => {
    if (!devices || !initialReady || !initialDhtMap) return
    for (const d of devices) {
      const mac = d.macAddress
      if (seededMacsRef.current.has(mac)) continue
      const seed = initialDhtMap[mac] ?? []
      if (seed.length === 0) {
        seededMacsRef.current.add(mac)
        continue
      }
      setStateMap((prev) => {
        const existing = prev[mac] ?? { dhtHistory: [], lastFans: [] }
        const merged = [...existing.dhtHistory, ...seed]
        const history = merged.slice(-HISTORY_LIMIT)
        return {
          ...prev,
          [mac]: {
            ...existing,
            dhtHistory: history,
            online: !!existing.online,
          },
        }
      })
      seededMacsRef.current.add(mac)
    }
  }, [devices, initialReady, initialDhtMap])

  useEffect(() => {
    // cleanup any sources for devices that were removed
    const currentMacs = new Set((devices || []).map((d) => d.macAddress))
    for (const [mac, src] of sourcesRef.current.entries()) {
      if (!currentMacs.has(mac)) {
        src.close()
        sourcesRef.current.delete(mac)
        setStateMap((prev) => {
          const copy = { ...prev }
          delete copy[mac]
          return copy
        })
      }
    }

    if (!devices || !initialReady) return

    devices.forEach((device) => {
      const mac = device.macAddress
      if (sourcesRef.current.has(mac)) return // already connected

      try {
        const url = `${BASE_API_URL}sse/subscribe/${mac}`
        const now = Date.now()
        const last = lastAttemptRef.current.get(mac) || 0
        if (now - last < RECONNECT_DELAY) return
        lastAttemptRef.current.set(mac, now)
        const es = new EventSource(url)

        setStateMap((prev) => {
          const existing = prev[mac] ?? { dhtHistory: [], lastFans: [] }
          return {
            ...prev,
            [mac]: {
              ...existing,
              online: true,
              lastSeenAt: Date.now(),
            },
          }
        })

        es.addEventListener('open', () => {
          setStateMap((prev) => {
            const existing = prev[mac] ?? { dhtHistory: [], lastFans: [] }
            return {
              ...prev,
              [mac]: {
                ...existing,
                online: true,
                lastSeenAt: Date.now(),
              },
            }
          })
        })

        es.addEventListener('error', (e) => {
          // EventSource will try to reconnect automatically. Mark offline if error observed
          setStateMap((prev) => {
            const existing = prev[mac] ?? { dhtHistory: [], lastFans: [] }
            return {
              ...prev,
              [mac]: {
                ...existing,
                online: false,
              },
            }
          })
          console.warn(`SSE error for ${mac}`, e)
          // close and allow reconnect after delay
          try {
            es.close()
          } catch {}
        })

        es.addEventListener('dht-data', (e: MessageEvent) => {
          try {
            const payload: DhtData = JSON.parse(e.data)
            pushDht(mac, payload)
          } catch (err) {
            console.warn('Failed to parse dht_data', err)
          }
        })

        es.addEventListener('pir-data', (e: MessageEvent) => {
          try {
            const payload: PirData = JSON.parse(e.data)
            setPir(mac, payload)
          } catch (err) {
            console.warn('Failed to parse pir_data', err)
          }
        })

        es.addEventListener('gas-data', (e: MessageEvent) => {
          try {
            const payload: GasData = JSON.parse(e.data)
            setGas(mac, payload)
          } catch (err) {
            console.warn('Failed to parse gas_data', err)
          }
        })

        es.addEventListener('fan-data', (e: MessageEvent) => {
          try {
            const payload: FanData = JSON.parse(e.data)
            setFan(mac, payload)
          } catch (err) {
            console.warn('Failed to parse fan_data', err)
          }
        })

        es.addEventListener('led-pir-data', (e: MessageEvent) => {
          try {
            const payload: LedPirData = JSON.parse(e.data)
            setLedPir(mac, payload)
          } catch (err) {
            console.warn('Failed to parse led_pir_data', err)
          }
        })

        sourcesRef.current.set(mac, es)
      } catch (err) {
        console.error(
          'Failed to create EventSource for',
          device.macAddress,
          err,
        )
      }
    })

    // cleanup on unmount
    return () => {
      for (const es of sourcesRef.current.values()) es.close()
      sourcesRef.current.clear()
    }
  }, [devices, initialReady])

  // compute overall connection status: if any device online => online, else offline
  const connectionStatus = useMemo(() => {
    const states = Object.values(stateMap)
    if (states.length === 0) return 'offline'
    return states.some((s) => s.online) ? 'online' : 'offline'
  }, [stateMap])

  return { stateMap, connectionStatus }
}

// -----------------------------------------------------------------------------
// Small sparkline component (pure SVG) — single-series
// -----------------------------------------------------------------------------
function SparklineSingle({
  data,
  idPrefix,
  height = 48,
}: {
  data: Array<number>
  idPrefix: string
  height?: number
}) {
  const width = 320
  const padding = 6
  const points = data.slice(-32)

  if (points.length === 0) return <div className="h-12 w-full" />

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
      className="w-full"
      aria-hidden
    >
      <defs>
        <linearGradient id={gradientId} x1="0" x2="0" y1="0" y2="1">
          <stop offset="0%" stopColor="#ea580c" stopOpacity="0.22" />
          <stop offset="100%" stopColor="#ea580c" stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={areaPath} fill={`url(#${gradientId})`} />
      <path
        d={linePath}
        fill="none"
        stroke="#ea580c"
        strokeWidth={2.5}
        strokeLinejoin="round"
        strokeLinecap="round"
      />
    </svg>
  )
}

// -----------------------------------------------------------------------------
// Aggregate card: combine all device data into one UI card
// -----------------------------------------------------------------------------
function AggregateCard({
  devices,
  stateMap,
}: {
  devices?: Array<DeviceData>
  stateMap: Record<string, any>
}) {
  // merge all DHT entries across devices and sort by timestamp
  const mergedDht = useMemo(() => {
    const all: Array<{ t: number; d: DhtData }> = []
    for (const v of Object.values(stateMap)) {
      for (const d of v.dhtHistory || []) {
        const ts = d.receivedAt ? new Date(d.receivedAt).getTime() : Date.now()
        all.push({ t: ts, d })
      }
    }
    all.sort((a, b) => a.t - b.t)
    // keep last HISTORY_LIMIT entries
    return all
      .slice(-HISTORY_LIMIT)
      .map((x) => ({ ...x.d, receivedAt: new Date(x.t).toISOString() }))
  }, [stateMap])

  const tempSeries = useMemo(
    () => mergedDht.map((d) => d.temperature),
    [mergedDht],
  )
  const humiditySeries = useMemo(
    () => mergedDht.map((d) => d.humidity),
    [mergedDht],
  )

  const anyMotion = useMemo(
    () =>
      Object.values(stateMap).some((s) => s.lastPir && s.lastPir.motion === 1),
    [stateMap],
  )
  const anyGasAlert = useMemo(
    () =>
      Object.values(stateMap).some((s) => s.lastGas && s.lastGas.state === 1),
    [stateMap],
  )
  const totalFanEvents = useMemo(
    () =>
      Object.values(stateMap).reduce(
        (acc, s) => acc + ((s.lastFans || []).length || 0),
        0,
      ),
    [stateMap],
  )
  const onlineCount = useMemo(
    () => Object.values(stateMap).filter((s) => s.online).length,
    [stateMap],
  )
  const lastSeenAt = useMemo(() => {
    const times = Object.values(stateMap).map((s) => s.lastSeenAt || 0)
    const max = Math.max(...times, 0)
    return max ? new Date(max) : null
  }, [stateMap])

  if (anyGasAlert) toast.error('Cảnh báo có khí ga vượt mức')

  return (
    <article className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
      <header className="flex items-start justify-between gap-3">
        <div>
          <h3 className="text-lg font-semibold text-gray-900">
            Tổng quan cảm biến
          </h3>
        </div>
        <div className="text-right text-xs">
          <p
            className={`font-medium ${onlineCount > 0 ? 'text-emerald-600' : 'text-gray-500'}`}
          >
            {onlineCount} online
          </p>
          {lastSeenAt && (
            <p className="text-gray-400">
              Cập nhật: {lastSeenAt.toLocaleTimeString()}
            </p>
          )}
        </div>
      </header>

      <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-3">
        <div className="col-span-2">
          <div className="rounded-lg border border-gray-100 p-3">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-gray-500">Nhiệt độ (°C)</p>
                <p className="text-lg font-semibold text-gray-900">
                  {tempSeries.length
                    ? `${tempSeries[tempSeries.length - 1].toFixed(1)}°C`
                    : '—'}
                </p>
              </div>
              <div className="text-xs text-gray-500">
                {mergedDht.length} dữ liệu
              </div>
            </div>
            <div className="mt-2">
              <SparklineSingle
                data={tempSeries}
                idPrefix="agg-temp"
                height={64}
              />
            </div>
          </div>

          <div className="mt-3 rounded-lg border border-gray-100 p-3">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-gray-500">Độ ẩm (%)</p>
                <p className="text-lg font-semibold text-gray-900">
                  {humiditySeries.length
                    ? `${humiditySeries[humiditySeries.length - 1].toFixed(1)}%`
                    : '—'}
                </p>
              </div>
              <div className="text-xs text-gray-500">Biểu đồ</div>
            </div>
            <div className="mt-2">
              <SparklineSingle
                data={humiditySeries}
                idPrefix="agg-hum"
                height={48}
              />
            </div>
          </div>
        </div>

        <div className="col-span-1 space-y-3">
          <div className="rounded-lg border border-gray-100 p-3">
            <p className="text-xs text-gray-500">Chuyển động</p>
            <div className="mt-2 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="rounded-full bg-emerald-50 px-3 py-1 text-emerald-700">
                  {anyMotion ? 'Đang' : 'Không'}
                </div>
                <p className="text-xs text-gray-500">
                  Phát hiện: {anyMotion ? 'Có' : 'Không'}
                </p>
              </div>
            </div>
          </div>

          <div className="rounded-lg border border-gray-100 p-3">
            <p className="text-xs text-gray-500">Khí & cảnh báo</p>
            <div className="mt-2 flex items-center justify-between">
              <div className="flex items-center gap-2">
                {anyGasAlert ? (
                  <span className="inline-flex items-center gap-2 rounded-full bg-rose-50 px-3 py-1 text-rose-700">
                    {' '}
                    <AlertTriangle className="h-4 w-4" /> Cảnh báo
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-2 rounded-full bg-emerald-50 px-3 py-1 text-emerald-700">
                    {' '}
                    An toàn
                  </span>
                )}
              </div>
            </div>
          </div>

          <div className="rounded-lg border border-gray-100 p-3">
            <p className="text-xs text-gray-500">Quạt</p>
            <div className="mt-2 flex items-center justify-between">
              <div className="text-lg font-semibold text-gray-900">
                {totalFanEvents}
              </div>
              <div className="text-xs text-gray-500">Sự kiện gần đây</div>
            </div>
          </div>
        </div>
      </div>
    </article>
  )
}
export default function Dashboard() {
  const {
    data: devices,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['devices'],
    queryFn: async () => {
      const res = await api.get<Array<DeviceData>>('devices/user')
      return res.data
    },
  })
  // Fetch initial DHT histories per device
  const initialDhtQueries = useQueries({
    queries: (devices || []).map((device) => ({
      queryKey: ['dhtsensor', device.id],
      queryFn: async () => {
        const res = await api.get<PageResponse<Array<DhtData>>>(
          `dhtsensor?size=100&deviceId=${device.id}`,
        )
        return res.data
      },
      enabled: !!devices,
      staleTime: 60_000,
    })),
  })

  const initialReady = useMemo(() => {
    if (!devices) return false
    if (initialDhtQueries.length === 0) return true
    return initialDhtQueries.every((q) => q.isFetched)
  }, [devices, initialDhtQueries])

  const initialDhtMap = useMemo(() => {
    const map: Record<string, Array<DhtData>> = {}
    if (!devices || !initialReady) return map
    for (let i = 0; i < devices.length; i++) {
      const device = devices[i]
      const mac = device.macAddress
      const q = initialDhtQueries[i]
      const content = q.data!.content
      // Sort by receivedAt ascending, then limit
      const sorted = [...content].sort((a, b) => {
        const ta = new Date(a.receivedAt).getTime()
        const tb = new Date(b.receivedAt).getTime()
        return ta - tb
      })
      map[mac] = sorted.slice(-HISTORY_LIMIT)
    }
    return map
  }, [devices, initialDhtQueries, initialReady])

  const { stateMap, connectionStatus } = useDevicesSSE(
    devices,
    initialDhtMap,
    initialReady,
  )

  if (isLoading) return <div />
  if (error) return <div className="p-4">Lỗi khi tải danh sách thiết bị.</div>

  return (
    <main className="mx-auto w-full max-w-5xl px-4 py-8">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Nhà thông minh — Tổng quan
          </h1>
        </div>
        <div className="flex items-center gap-3 rounded-full border border-gray-200 bg-white px-4 py-2 shadow-sm">
          {connectionStatus === 'online' ? (
            <Wifi className="h-5 w-5 text-green-600" />
          ) : (
            <WifiOff className="h-5 w-5 text-gray-400" />
          )}
          <div className="text-sm leading-tight text-gray-700">
            <p className="font-semibold capitalize">{connectionStatus}</p>
            <p className="text-xs text-gray-500">Kết nối SSE với server</p>
          </div>
        </div>
      </header>

      <section className="mt-6 grid gap-4">
        <AggregateCard devices={devices} stateMap={stateMap} />
      </section>
    </main>
  )
}
