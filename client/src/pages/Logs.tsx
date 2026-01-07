import { useMemo, useState } from 'react'

type EventType =
  | 'door_open'
  | 'wrong_password'
  | 'light_toggle'
  | 'fan_toggle'
  | 'fire_alert'

interface LogEntry {
  id: string
  timestamp: number
  type: EventType
  user?: string
  details?: string
}

const EVENT_LABELS: Record<EventType, string> = {
  door_open: 'Người dùng mở cửa',
  wrong_password: 'Sai mật khẩu',
  light_toggle: 'Bật/tắt đèn',
  fan_toggle: 'Bật/tắt quạt',
  fire_alert: 'Cảnh báo cháy',
}

function generateMockLogs(count = 120): LogEntry[] {
  const types: EventType[] = [
    'door_open',
    'wrong_password',
    'light_toggle',
    'fan_toggle',
    'fire_alert',
  ]
  const now = Date.now()
  const logs: LogEntry[] = []
  for (let i = 0; i < count; i++) {
    const type = types[Math.floor(Math.random() * types.length)]
    const ts = now - Math.floor(Math.random() * 1000 * 60 * 60 * 24 * 7) // last 7 days
    const user = Math.random() > 0.4 ? `user${Math.ceil(Math.random() * 8)}` : undefined

    let details = ''
    switch (type) {
      case 'door_open':
        details = user ? `Mở bởi ${user}` : 'Mở cửa tự động'
        break
      case 'wrong_password':
        details = `Thử mật khẩu: ${Math.random().toString(36).slice(2, 8)}`
        break
      case 'light_toggle':
        details = Math.random() > 0.5 ? 'Bật đèn' : 'Tắt đèn'
        break
      case 'fan_toggle':
        details = Math.random() > 0.5 ? 'Bật quạt' : 'Tắt quạt'
        break
      case 'fire_alert':
        details = 'Cảm biến khói/nhiệt: giá trị vượt ngưỡng'
        break
    }

    logs.push({ id: String(i + 1), timestamp: ts, type, user, details })
  }
  return logs.sort((a, b) => b.timestamp - a.timestamp)
}

export default function Logs() {
  const [allLogs] = useState<LogEntry[]>(() => generateMockLogs(180))

  const [selectedTypes, setSelectedTypes] = useState<Record<EventType, boolean>>({
    door_open: true,
    wrong_password: true,
    light_toggle: true,
    fan_toggle: true,
    fire_alert: true,
  })

  const [dateRange, setDateRange] = useState<'24h' | '7d' | '30d' | 'all'>('7d')
  const [query, setQuery] = useState('')

  const filtered = useMemo(() => {
    const now = Date.now()
    const cutoff =
      dateRange === '24h'
        ? now - 1000 * 60 * 60 * 24
        : dateRange === '7d'
        ? now - 1000 * 60 * 60 * 24 * 7
        : dateRange === '30d'
        ? now - 1000 * 60 * 60 * 24 * 30
        : 0

    return allLogs.filter((l) => {
      if (!selectedTypes[l.type]) return false
      if (l.timestamp < cutoff) return false
      if (query.trim()) {
        const q = query.toLowerCase()
        return (
          (l.user ?? '').toLowerCase().includes(q) ||
          (l.details ?? '').toLowerCase().includes(q) ||
          EVENT_LABELS[l.type].toLowerCase().includes(q)
        )
      }
      return true
    })
  }, [allLogs, selectedTypes, dateRange, query])

  function toggleType(t: EventType) {
    setSelectedTypes((s) => ({ ...s, [t]: !s[t] }))
  }

  return (
    <main className="mx-auto w-full max-w-6xl px-4 py-8">
      <header className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.12em] text-gray-500">Logs</p>
          <h1 className="text-2xl font-bold text-gray-900">Lịch sử hoạt động</h1>
          <p className="text-sm text-gray-600">Lọc và xem các sự kiện: mở cửa, sai mật khẩu, bật/tắt đèn/quạt, cảnh báo cháy.</p>
        </div>

        <div className="mt-3 flex items-center gap-3">
          <input
            type="text"
            placeholder="Tìm theo người dùng hoặc nội dung..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="rounded-md border border-gray-200 px-3 py-2 text-sm"
          />
          <select
            value={dateRange}
            onChange={(e) => setDateRange(e.target.value as any)}
            className="rounded-md border border-gray-200 px-3 py-2 text-sm"
          >
            <option value="24h">24 giờ</option>
            <option value="7d">7 ngày</option>
            <option value="30d">30 ngày</option>
            <option value="all">Tất cả</option>
          </select>
        </div>
      </header>

      <section className="mt-6 grid gap-4 lg:grid-cols-4">
        <aside className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
          <h3 className="text-sm font-semibold text-gray-900">Bộ lọc sự kiện</h3>
          <div className="mt-3 space-y-2 text-sm">
            {(Object.keys(EVENT_LABELS) as EventType[]).map((t) => (
              <label key={t} className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={selectedTypes[t]}
                  onChange={() => toggleType(t)}
                  className="h-4 w-4"
                />
                <span className="text-gray-700">{EVENT_LABELS[t]}</span>
              </label>
            ))}

            <div className="mt-3">
              <button
                onClick={() => {
                  setSelectedTypes({
                    door_open: true,
                    wrong_password: true,
                    light_toggle: true,
                    fan_toggle: true,
                    fire_alert: true,
                  })
                }}
                className="rounded-md bg-gray-100 px-3 py-1 text-sm"
              >
                Chọn tất cả
              </button>
              <button
                onClick={() => {
                  setSelectedTypes({
                    door_open: false,
                    wrong_password: false,
                    light_toggle: false,
                    fan_toggle: false,
                    fire_alert: false,
                  })
                }}
                className="ml-2 rounded-md bg-gray-50 px-3 py-1 text-sm"
              >
                Bỏ chọn
              </button>
            </div>
          </div>
        </aside>

        <div className="lg:col-span-3">
          <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-gray-900">Kết quả ({filtered.length})</h3>
            </div>

            <ul className="mt-4 divide-y divide-gray-100">
              {filtered.length === 0 && (
                <li className="py-6 text-center text-sm text-gray-500">Không có log phù hợp.</li>
              )}

              {filtered.map((l) => (
                <li key={l.id} className="flex items-start gap-4 py-3">
                  <div className="w-36 text-xs text-gray-500">
                    {new Date(l.timestamp).toLocaleString()}
                  </div>

                  <div className="flex-1">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <span className="rounded-full bg-gray-100 px-2 py-1 text-xs font-medium text-gray-800">{EVENT_LABELS[l.type]}</span>
                        {l.user && <span className="text-xs text-gray-500">{l.user}</span>}
                      </div>

                      <div className="text-xs text-gray-400">ID: {l.id}</div>
                    </div>

                    <p className="mt-2 text-sm text-gray-700">{l.details}</p>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </section>
    </main>
  )
}
