import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useTranslation } from 'react-i18next'
import {
  ArrowRight, ArrowUpRight, BadgeCheck, CalendarDays, Check,
  Clock3, CreditCard, Database, Leaf, MapPin, PackageCheck,
  RefreshCw, Search, Sprout, Users, WalletCards,
} from 'lucide-react'
import {
  Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts'
import { useAuth } from '../context/AuthContext'
import { LanguageSelector } from '../components/LanguageSelector'
import i18n from '../i18n'
import { getErrorMessage } from '../services/api'
import { getCenters, searchCentersByDistrict } from '../services/centerService'
import { getDashboardSummary } from '../services/dashboardService'
import { createFarmerProfile, getFarmerProfile, updateFarmerProfile } from '../services/farmerService'
import { getPayments } from '../services/paymentService'
import { getProcurements } from '../services/procurementService'
import { callNextFarmer, completeProcurement, getMyQueue, getQueueForSlot, joinQueue, startProcessing } from '../services/queueService'
import { getSlots, getSlotsForCenter } from '../services/slotService'
import { bookToken, cancelToken, getMyTokens } from '../services/tokenService'

const currency = { format: (value) => new Intl.NumberFormat(i18n.resolvedLanguage === 'hi' ? 'hi-IN' : 'en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value) }
const dayFormat = { format: (value) => new Intl.DateTimeFormat(i18n.resolvedLanguage === 'hi' ? 'hi-IN' : 'en-IN', { day: 'numeric', month: 'short', year: 'numeric' }).format(value) }
const emptyResponse = () => Promise.resolve(null)

function useRemote(loader) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [status, setStatus] = useState(null)
  const refresh = useCallback(async () => {
    setLoading(true)
    setError('')
    setStatus(null)
    try {
      setData(await loader())
    } catch (requestError) {
      setError(getErrorMessage(requestError))
      setStatus(requestError.response?.status ?? null)
    } finally {
      setLoading(false)
    }
  }, [loader])

  useEffect(() => {
    let active = true
    Promise.resolve().then(() => {
      if (active) setLoading(true)
      return loader()
    }).then((result) => {
      if (active) {
        setData(result)
        setError('')
      }
    }).catch((requestError) => {
      if (active) {
        setError(getErrorMessage(requestError))
        setStatus(requestError.response?.status ?? null)
      }
    }).finally(() => {
      if (active) setLoading(false)
    })
    return () => { active = false }
  }, [loader])
  return { data, setData, loading, error, status, refresh }
}

function PageHeading({ eyebrow, title, description, action }) {
  const { t } = useTranslation()
  return (
    <div className="page-heading">
      <div><span className="eyebrow">{t(eyebrow)}</span><h1>{t(title)}</h1><p>{t(description)}</p></div>
      {action && <div className="heading-action">{action}</div>}
    </div>
  )
}

function Notice({ children, tone = 'info', values }) {
  const { t } = useTranslation()
  return <div className={`notice notice-${tone}`} role={tone === 'error' ? 'alert' : 'status'}>{typeof children === 'string' ? t(children, values) : children}</div>
}

function LoadingPanel({ rows = 3 }) {
  const { t } = useTranslation()
  return <div className="skeleton-list" aria-label={t('Loading data')} role="status">{Array.from({ length: rows }, (_, index) => <div className="skeleton-row" key={index}><i /><span /><b /></div>)}</div>
}

function EmptyState({ icon: Icon = Sprout, title, text, action }) {
  const { t } = useTranslation()
  return (
    <div className="empty-state">
      <span className="empty-icon"><Icon size={22} /></span>
      <h3>{t(title)}</h3>
      <p>{t(text)}</p>
      {action}
    </div>
  )
}

function StatusPill({ value }) {
  const { t } = useTranslation()
  const status = String(value || 'unknown').toLowerCase().replaceAll('_', ' ')
  const statusLabels = {
    active: 'Active',
    inactive: 'Inactive',
    available: 'Available',
    full: 'Full',
    paid: 'Paid',
    pending: 'Pending',
    failed: 'Failed',
    booked: 'Booked',
    cancelled: 'Cancelled',
    canceled: 'Cancelled',
    waiting: 'Waiting',
    called: 'Called',
    processing: 'Processing',
    completed: 'Completed',
    complete: 'Complete',
    verified: 'Verified',
    unknown: 'Unknown',
    'in queue': 'In queue',
  }
  const kind = /complete|paid|active|available|verified/.test(status) ? 'success' : /pending|waiting|booked|processing/.test(status) ? 'warning' : /failed|cancel|inactive/.test(status) ? 'danger' : 'neutral'
  return <span className={`status-pill status-${kind}`}><i />{t(statusLabels[status] || status)}</span>
}

function StatCard({ icon: Icon, label, value, note, tone = 'green', index = 0 }) {
  const { t } = useTranslation()
  return (
    <motion.article className="stat-card" initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: index * 0.045 }}>
      <div className="stat-top"><span className={`stat-icon icon-${tone}`}><Icon size={18} /></span><span className="stat-period">{t(note || 'All time')}</span></div>
      <strong className="stat-value">{value}</strong><span className="stat-label">{t(label)}</span>
    </motion.article>
  )
}

function DashboardPage() {
  const { user } = useAuth()
  const { t } = useTranslation()
  const isFarmer = user.role === 'FARMER'
  const navigate = useNavigate()
  const summary = useRemote(isFarmer ? emptyResponse : getDashboardSummary)
  const tokens = useRemote(isFarmer ? getMyTokens : emptyResponse)
  const queue = useRemote(isFarmer ? getMyQueue : emptyResponse)
  const profile = useRemote(isFarmer ? getFarmerProfile : emptyResponse)
  const procurements = useRemote(isFarmer ? emptyResponse : getProcurements)
  const payments = useRemote(isFarmer ? emptyResponse : getPayments)
  const displayName = (isFarmer ? profile.data?.fullName : user.fullName) || user.fullName?.split(' ')[0] || 'there'
  const activeQueue = Array.isArray(queue.data) ? queue.data.find((item) => !/complete|cancel/i.test(item.status)) : null
  const nextToken = Array.isArray(tokens.data) ? tokens.data.find((item) => !/cancel|complete/i.test(item.status)) : null

  const farmerStats = [
    { icon: PackageCheck, label: 'Procurements', value: '—', note: 'Your history' },
    { icon: CalendarDays, label: 'Upcoming slots', value: tokens.loading ? '…' : (tokens.data || []).filter((item) => !/cancel|complete/i.test(item.status)).length, note: 'Booked by you', tone: 'amber' },
    { icon: Users, label: 'Queue position', value: activeQueue?.queuePosition ?? '—', note: activeQueue ? 'Live queue' : 'No active queue', tone: 'blue' },
    { icon: WalletCards, label: 'Payments received', value: '—', note: 'Available in payments', tone: 'purple' },
  ]
  const staffStats = [
    { icon: Users, label: 'Registered farmers', value: summary.data?.totalFarmers ?? '—', note: 'All time' },
    { icon: PackageCheck, label: 'Procurements', value: summary.data?.totalProcurements ?? '—', note: `${summary.data?.completedProcurements ?? 0} completed`, tone: 'blue' },
    { icon: WalletCards, label: 'Procurement value', value: summary.data?.totalProcurementAmount == null ? '—' : currency.format(summary.data.totalProcurementAmount), note: 'All time', tone: 'purple' },
    { icon: CreditCard, label: 'Payments issued', value: summary.data?.totalPaymentAmount == null ? '—' : currency.format(summary.data.totalPaymentAmount), note: `${summary.data?.completedPayments ?? 0} completed`, tone: 'amber' },
  ]

  return (
    <>
      <PageHeading eyebrow={dayFormat.format(new Date())} title={t(`Good ${new Date().getHours() < 12 ? 'morning' : new Date().getHours() < 17 ? 'afternoon' : 'evening'}, {{name}}.`, { name: displayName.split(' ')[0] })} description={t(isFarmer ? 'Here’s what’s happening with your harvest.' : 'A clear view of today’s procurement activity.')} action={<button className="subtle-button" onClick={() => [summary, tokens, queue, profile, procurements, payments].forEach((item) => item.refresh())}><RefreshCw size={15} /> {t('Refresh')}</button>} />
      {isFarmer ? (
        <div className="dashboard-quick-actions">
          <Link className="primary-button" to="/slots"><CalendarDays size={17} /> {t('Book a procurement slot')}</Link>
          <Link className="secondary-button" to="/centers"><MapPin size={17} /> {t('Find a center')}</Link>
        </div>
      ) : summary.error ? <Notice tone="error">{summary.error}</Notice> : null}
      {isFarmer ? (
        <div className="stats-grid">
          {farmerStats.map((item, index) => <StatCard key={item.label} {...item} index={index} />)}
        </div>
      ) : (
        <div className="stats-grid">
          {staffStats.map((item, index) => <StatCard key={item.label} {...item} index={index} />)}
        </div>
      )}
      <div className="dashboard-grid">
        <section className="surface-card overview-card">
          <div className="section-heading"><div><span className="eyebrow">{t(isFarmer ? 'YOUR NEXT STEP' : 'OPERATIONS')}</span><h2>{t(isFarmer ? 'Upcoming procurement' : 'Procurement snapshot')}</h2></div><span className="heading-mark"><Leaf size={17} /></span></div>
          {isFarmer ? (
            tokens.loading ? <LoadingPanel rows={2} /> : tokens.error ? <Notice tone="error">{tokens.error}</Notice> : nextToken ? (
              <div className="appointment-card">
                <span className="appointment-date"><strong>{nextToken.slotDate ? new Date(`${nextToken.slotDate}T00:00:00`).getDate() : '—'}</strong><small>{nextToken.slotDate ? new Date(`${nextToken.slotDate}T00:00:00`).toLocaleDateString('en-IN', { month: 'short' }) : 'DATE'}</small></span>
                <div className="appointment-detail"><strong>{nextToken.centerName || t('Procurement center')}</strong><span>{nextToken.cropName || t('Crop not specified')} · {nextToken.startTime || ''}</span><small>{t('Token')} {nextToken.tokenNumber || '—'}</small></div>
                <StatusPill value={nextToken.status} />
              </div>
            ) : <EmptyState icon={CalendarDays} title="Nothing booked yet" text="Choose a center and reserve a procurement slot that works for you." action={<Link className="text-link" to="/slots">{t('Explore available slots')} <ArrowRight size={15} /></Link>} />
          ) : (
            summary.loading ? <LoadingPanel rows={2} /> : summary.error ? <EmptyState icon={Database} title="Snapshot unavailable" text="The dashboard summary could not be loaded." action={<button className="text-link" onClick={summary.refresh}>{t('Try again')} <RefreshCw size={15} /></button>} /> : (
              <div className="snapshot-grid">
                <div><span>{t('Completed procurement')}</span><strong>{summary.data?.completedProcurements ?? 0}</strong><small>{t('of {{count}} total', { count: summary.data?.totalProcurements ?? 0 })}</small></div>
                <div><span>{t('Completed payments')}</span><strong>{summary.data?.completedPayments ?? 0}</strong><small>{t('of {{count}} total', { count: summary.data?.totalPayments ?? 0 })}</small></div>
              </div>
            )
          )}
        </section>
        <section className="surface-card journey-card">
          <div className="section-heading"><div><span className="eyebrow">{t('YOUR JOURNEY')}</span><h2>{t(isFarmer ? 'Queue status' : 'Queue activity')}</h2></div><Link className="icon-link" to="/queue" aria-label={t('View queue')}><ArrowRight size={17} /></Link></div>
          {isFarmer ? (
            queue.loading ? <LoadingPanel rows={2} /> : queue.error ? <Notice tone="error">{queue.error}</Notice> : activeQueue ? (
              <div className="queue-summary">
                <div className="queue-token"><small>{t('YOUR TOKEN')}</small><strong>{activeQueue.tokenNumber || '—'}</strong></div>
                <div className="queue-position"><span>{t('Position in line')}</span><strong>{activeQueue.queuePosition ?? '—'}</strong></div>
                <div className="queue-progress"><i style={{ width: `${activeQueue.queuePosition ? Math.max(12, Math.min(88, 100 / activeQueue.queuePosition)) : 8}%` }} /></div>
                <div className="queue-footer"><span>{activeQueue.centerName || t('Center details unavailable')}</span><StatusPill value={activeQueue.status} /></div>
                <p className="prediction-note"><Clock3 size={14} /> {activeQueue.estimatedWaitingTime != null ? t('Estimated wait: {{minutes}} min', { minutes: activeQueue.estimatedWaitingTime }) : t('Waiting time prediction will be available soon.')}</p>
              </div>
            ) : <EmptyState icon={Users} title="No one in line for you" text="After booking a slot, join the queue to follow your progress." action={<Link className="text-link" to="/queue">{t('Open my queue')} <ArrowRight size={15} /></Link>} />
          ) : (
            <div className="staff-queue-empty"><span className="queue-pulse"><Users size={20} /></span><div><strong>{t('Queue actions')}</strong><p>{t('Choose an active slot to see its live farmer queue and process the next entry.')}</p></div><Link to="/queue" className="text-link">{t('Open queue')} <ArrowRight size={15} /></Link></div>
          )}
        </section>
      </div>
      {!isFarmer && <OperationsCharts procurements={procurements} payments={payments} />}
      <div className="section-heading content-section-heading"><div><span className="eyebrow">{t('QUICK ACCESS')}</span><h2>{t('Make your next move')}</h2></div></div>
      <div className="quick-grid">
        <QuickCard icon={MapPin} title={isFarmer ? 'Explore centers' : 'Center directory'} detail={isFarmer ? 'Find a nearby procurement center.' : 'View centers connected to your workspace.'} to="/centers" tone="green" />
        <QuickCard icon={CalendarDays} title={isFarmer ? 'Book a slot' : 'Published slots'} detail={isFarmer ? 'Pick a time that suits your harvest.' : 'Review published procurement slots.'} to="/slots" tone="blue" />
        <QuickCard icon={WalletCards} title={isFarmer ? 'Payment history' : 'Payment records'} detail={isFarmer ? 'Payment view depends on farmer API access.' : 'Review payment records from the service.'} to="/payments" tone="amber" />
      </div>
      <div className="dashboard-footnote"><span><BadgeCheck size={16} /> {t('Live data from your procurement service')}</span><button onClick={() => navigate('/profile')}>{t('Account settings')} <ArrowRight size={14} /></button></div>
    </>
  )
}

function QuickCard({ icon: Icon, title, detail, to, tone }) {
  const { t } = useTranslation()
  return <Link className="quick-card surface-card" to={to}><span className={`quick-icon icon-${tone}`}><Icon size={19} /></span><strong>{t(title)}</strong><span>{t(detail)}</span><ArrowUpRight size={16} className="quick-arrow" /></Link>
}

function OperationsCharts({ procurements, payments }) {
  const { t } = useTranslation()
  const monthlyData = useMemo(() => {
    const grouped = new Map()
    for (const item of procurements.data || []) {
      if (!item.createdAt) continue
      const date = new Date(item.createdAt)
      if (Number.isNaN(date.getTime())) continue
      const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
      const current = grouped.get(key) || { key, month: date.toLocaleDateString(i18n.resolvedLanguage === 'hi' ? 'hi-IN' : 'en-IN', { month: 'short', year: '2-digit' }), amount: 0 }
      current.amount += Number(item.totalAmount || 0)
      grouped.set(key, current)
    }
    return [...grouped.values()].sort((a, b) => a.key.localeCompare(b.key)).slice(-6)
  }, [procurements.data])
  const cropData = useMemo(() => {
    const grouped = new Map()
    for (const item of procurements.data || []) {
      if (!item.cropName) continue
      grouped.set(item.cropName, (grouped.get(item.cropName) || 0) + Number(item.actualQuantity || 0))
    }
    return [...grouped].map(([crop, quantity]) => ({ crop, quantity })).sort((a, b) => b.quantity - a.quantity).slice(0, 5)
  }, [procurements.data])
  const paymentData = useMemo(() => {
    const grouped = new Map()
    for (const item of payments.data || []) {
      const status = String(item.status || 'UNKNOWN').replaceAll('_', ' ')
      grouped.set(status, (grouped.get(status) || 0) + Number(item.amount || 0))
    }
    return [...grouped].map(([status, amount]) => ({ status, amount }))
  }, [payments.data])

  return (
    <section className="analytics-section">
      <div className="section-heading content-section-heading"><div><span className="eyebrow">{t('LIVE SERVICE DATA')}</span><h2>{t('Operations analytics')}</h2></div><span className="quiet-note">{t('Based on records currently returned by the API')}</span></div>
      <div className="chart-grid">
        <ChartCard title="Procurement value by month" loading={procurements.loading} error={procurements.error} data={monthlyData} dataKey="amount" axisKey="month" color="#6B7F18" formatter={(value) => currency.format(value)} empty="No dated procurement records are available." />
        <ChartCard title="Quantity by crop" loading={procurements.loading} error={procurements.error} data={cropData} dataKey="quantity" axisKey="crop" color="#D98A32" formatter={(value) => `${value} q`} empty="Crop quantities are not available yet." />
        <ChartCard title="Payment value by status" loading={payments.loading} error={payments.error} data={paymentData} dataKey="amount" axisKey="status" color="#A78A4A" formatter={(value) => currency.format(value)} empty="No payment records are available." />
      </div>
    </section>
  )
}

function ChartCard({ title, loading, error, data, dataKey, axisKey, color, formatter, empty }) {
  const { t } = useTranslation()
  return (
    <section className="surface-card chart-card">
      <div className="chart-heading"><h3>{t(title)}</h3><span>API</span></div>
      {loading ? <LoadingPanel rows={3} /> : error ? <p className="chart-empty">{t(error)}</p> : data.length ? (
        <div className="chart-container">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data} margin={{ top: 8, right: 4, bottom: 0, left: -12 }}>
              <CartesianGrid vertical={false} stroke="var(--border)" strokeDasharray="3 5" />
              <XAxis dataKey={axisKey} tick={{ fill: 'var(--text-soft)', fontSize: 9 }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fill: 'var(--text-soft)', fontSize: 8 }} axisLine={false} tickLine={false} />
              <Tooltip formatter={(value) => formatter(value)} cursor={{ fill: 'rgba(95, 155, 111, .08)' }} contentStyle={{ borderRadius: 9, border: '1px solid var(--border)', background: 'var(--surface)', color: 'var(--text)', fontSize: 10 }} />
              <Bar dataKey={dataKey} fill={color} radius={[5, 5, 0, 0]} maxBarSize={34} isAnimationActive={false} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      ) : <p className="chart-empty">{t(empty)}</p>}
    </section>
  )
}

function CentersPage() {
  const { t } = useTranslation()
  const [district, setDistrict] = useState('')
  const [query, setQuery] = useState('')
  const [notice, setNotice] = useState('')
  const { data: centers, setData: setCenters, loading, error, refresh } = useRemote(getCenters)
  const navigate = useNavigate()
  const filtered = useMemo(() => (centers || []).filter((center) => `${center.centerName} ${center.address} ${center.district} ${center.state}`.toLowerCase().includes(query.toLowerCase())), [centers, query])

  async function searchDistrict(event) {
    event.preventDefault()
    const value = district.trim()
    try {
      if (!value) {
        await refresh()
        return
      }
      setNotice('')
      setCenters(await searchCentersByDistrict(value))
    } catch (searchError) {
      setNotice(getErrorMessage(searchError))
    }
  }

  return (
    <>
      <PageHeading eyebrow="YOUR LOCAL NETWORK" title="Find a procurement center" description="Explore active centers and choose where you’d like to bring your harvest." action={<button className="subtle-button" onClick={refresh}><RefreshCw size={15} /> {t('Refresh')}</button>} />
      <section className="surface-card filter-bar">
        <label className="search-control"><Search size={17} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder={t('Search name, village, or district')} aria-label={t('Search centers')} /></label>
        <form className="district-search" onSubmit={searchDistrict}><MapPin size={16} /><input value={district} onChange={(event) => setDistrict(event.target.value)} placeholder={t('Filter by district')} aria-label={t('District')} /><button type="submit">{t('Apply')}</button></form>
      </section>
      {notice && <Notice tone="error">{notice}</Notice>}
      {loading ? <LoadingPanel /> : error ? <ErrorPanel message={error} retry={refresh} /> : filtered.length ? (
        <div className="center-grid">
          {filtered.map((center, index) => (
            <motion.article className="center-card surface-card" key={center.id} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: index * 0.035 }}>
              <div className="center-card-top"><span className="center-symbol"><Leaf size={19} /></span><StatusPill value={center.active ? 'Active' : 'Inactive'} /></div>
              <h2>{center.centerName}</h2>
              <p className="center-address"><MapPin size={15} />{[center.address, center.village, center.district, center.state, center.pincode].filter(Boolean).join(', ') || t('Address not provided')}</p>
              <div className="center-meta"><span><small>{t('CENTER CODE')}</small><strong>{center.centerCode || '—'}</strong></span><span><small>{t('CONTACT')}</small><strong>{center.contactNumber || t('Not listed')}</strong></span></div>
              <div className="center-actions"><button className="secondary-button" onClick={() => navigate(`/slots?center=${center.id}`)}>{t('View slots')}</button><button className="primary-button compact-button" onClick={() => navigate(`/slots?center=${center.id}`)}>{t('Book slot')} <ArrowRight size={15} /></button></div>
            </motion.article>
          ))}
        </div>
      ) : <EmptyState icon={MapPin} title="No centers found" text={query || district ? 'Try another center name or district.' : 'There are no active centers available from the service right now.'} action={(query || district) && <button className="text-link" onClick={() => { setQuery(''); setDistrict(''); refresh() }}>{t('Clear filters')}</button>} />}
    </>
  )
}

function SlotsPage() {
  const { user } = useAuth()
  const { t } = useTranslation()
  const location = useLocation()
  const [centerId, setCenterId] = useState(new URLSearchParams(location.search).get('center') || '')
  const [quantity, setQuantity] = useState('1')
  const [notice, setNotice] = useState(null)
  const [busySlot, setBusySlot] = useState(null)
  const isFarmer = user.role === 'FARMER'
  const { data: centers, loading: centersLoading } = useRemote(getCenters)
  const { data: myTokens, refresh: refreshTokens } = useRemote(isFarmer ? getMyTokens : emptyResponse)
  const loader = useMemo(() => centerId ? () => getSlotsForCenter(centerId) : getSlots, [centerId])
  const { data: slots, loading, error, refresh } = useRemote(loader)

  async function reserve(slot) {
    setNotice(null)
    setBusySlot(slot.id)
    try {
      const token = await bookToken({ slotId: slot.id, quantity: Number(quantity) })
      setNotice({ tone: 'success', text: 'Your slot is booked. Token {{token}}', options: { token: token.tokenNumber || '' } })
      await Promise.all([refresh(), refreshTokens()])
    } catch (bookingError) {
      setNotice({ tone: 'error', text: getErrorMessage(bookingError) })
    } finally {
      setBusySlot(null)
    }
  }

  async function cancelBooking(token) {
    if (!window.confirm(t('Cancel procurement token {{token}}?', { token: token.tokenNumber }))) return
    setNotice(null)
    setBusySlot(token.id)
    try {
      await cancelToken(token.id)
      setNotice({ tone: 'success', text: 'Token {{token}} was cancelled.', options: { token: token.tokenNumber } })
      await Promise.all([refresh(), refreshTokens()])
    } catch (cancelError) {
      setNotice({ tone: 'error', text: getErrorMessage(cancelError) })
    } finally {
      setBusySlot(null)
    }
  }

  const visibleSlots = (slots || []).filter((slot) => !centerId || String(slot.centerId) === String(centerId))
  return (
    <>
      <PageHeading eyebrow="PLAN YOUR HARVEST" title="Procurement slots" description={isFarmer ? 'Choose an available center and reserve a time for your crop.' : 'Review current procurement capacity across your centers.'} />
      {notice && <Notice tone={notice.tone} values={notice.options}>{notice.text}</Notice>}
      <section className="surface-card slot-filter">
        <label className="form-field"><span>{t('Procurement center')}</span><select value={centerId} onChange={(event) => setCenterId(event.target.value)}><option value="">{t('All centers')}</option>{(centers || []).map((center) => <option key={center.id} value={center.id}>{center.centerName}</option>)}</select></label>
        {isFarmer && <label className="form-field quantity-field"><span>{t('Quantity (quintals)')}</span><input type="number" min="1" step="1" value={quantity} onChange={(event) => setQuantity(event.target.value)} /></label>}
        <button className="subtle-button slot-refresh" onClick={refresh}><RefreshCw size={15} /> {t('Refresh slots')}</button>
      </section>
      {centersLoading && <p className="quiet-note">{t('Loading centers…')}</p>}
      {loading ? <LoadingPanel /> : error ? <ErrorPanel message={error} retry={refresh} /> : visibleSlots.length ? (
        <div className="slot-list">
          {visibleSlots.map((slot) => {
            const capacity = Number(slot.availableCapacity ?? 0)
            const available = capacity > 0 && !/CANCEL|FULL/i.test(slot.status || '')
            return (
              <article className="slot-card surface-card" key={slot.id}>
                <span className="slot-calendar-icon"><CalendarDays size={20} /></span>
                <div className="slot-detail"><strong>{slot.centerName || centers?.find((center) => center.id === slot.centerId)?.centerName || t('Center {{id}}', { id: slot.centerId })}</strong><span>{slot.cropName || t('Crop not specified')}</span></div>
                <div className="slot-date-time"><span><CalendarDays size={15} />{slot.slotDate ? dayFormat.format(new Date(`${slot.slotDate}T00:00:00`)) : t('Date not set')}</span><span><Clock3 size={15} />{slot.startTime || '—'}{slot.endTime ? ` – ${slot.endTime}` : ''}</span></div>
                <div className="slot-capacity"><span>{t('{{count}} available', { count: capacity })}</span><i><b style={{ width: `${slot.maxCapacity ? Math.max(4, ((slot.maxCapacity - capacity) / slot.maxCapacity) * 100) : 0}%` }} /></i></div>
                <StatusPill value={available ? 'Available' : slot.status || 'Full'} />
                {isFarmer && <button className="primary-button compact-button" disabled={!available || busySlot === slot.id || !Number(quantity)} onClick={() => reserve(slot)}>{busySlot === slot.id ? t('Booking…') : t('Book slot')} {!busySlot && <ArrowRight size={15} />}</button>}
              </article>
            )
          })}
        </div>
      ) : <EmptyState icon={CalendarDays} title="No slots available" text="No slots are currently published for this selection. Check back later or try another center." action={<Link className="text-link" to="/centers">{t('Browse centers')} <ArrowRight size={15} /></Link>} />}
      {isFarmer && <section className="bookings-section"><div className="section-heading content-section-heading"><div><span className="eyebrow">{t('YOUR RESERVATIONS')}</span><h2>{t('Booked slots')}</h2></div></div>
        {myTokens?.length ? <div className="slot-list">{myTokens.map((token) => <article className="surface-card booking-row" key={token.id}><span className="slot-calendar-icon"><CalendarDays size={19} /></span><div className="slot-detail"><strong>{token.centerName || t('Procurement center')}</strong><span>{token.cropName || t('Crop not specified')} · {token.tokenNumber}</span></div><div className="slot-date-time"><span>{token.slotDate ? dayFormat.format(new Date(`${token.slotDate}T00:00:00`)) : t('Date not set')}</span><span>{token.startTime || '—'}</span></div><StatusPill value={token.status} />{['BOOKED', 'WAITING'].includes(token.status) && <button className="table-action cancel-booking" disabled={busySlot === token.id} onClick={() => cancelBooking(token)}>{busySlot === token.id ? t('Cancelling…') : t('Cancel booking')}</button>}</article>)}</div> : null}
      </section>}
    </>
  )
}

function QueuePage() {
  const { user } = useAuth()
  const { t } = useTranslation()
  const isFarmer = user.role === 'FARMER'
  const { data: tokens, refresh: refreshTokens } = useRemote(isFarmer ? getMyTokens : emptyResponse)
  const { data: myQueue, loading: queueLoading, error: queueError, refresh: refreshQueue } = useRemote(isFarmer ? getMyQueue : emptyResponse)
  const { data: slots, loading: slotsLoading } = useRemote(getSlots)
  const [selectedToken, setSelectedToken] = useState('')
  const [selectedSlot, setSelectedSlot] = useState('')
  const [operatorQueue, setOperatorQueue] = useState(null)
  const [notice, setNotice] = useState(null)
  const [busy, setBusy] = useState(false)

  async function action(run, successText) {
    setBusy(true)
    setNotice(null)
    try {
      await run()
      setNotice({ tone: 'success', text: successText })
      if (isFarmer) await refreshQueue()
      else if (selectedSlot) {
        setOperatorQueue(await getQueueForSlot(selectedSlot))
      }
    } catch (error) {
      setNotice({ tone: 'error', text: getErrorMessage(error) })
    } finally {
      setBusy(false)
    }
  }

  async function loadSlotQueue() {
    if (!selectedSlot) return
    setBusy(true)
    setNotice(null)
    try {
      setOperatorQueue(await getQueueForSlot(selectedSlot))
    } catch (error) {
      setNotice({ tone: 'error', text: getErrorMessage(error) })
    } finally {
      setBusy(false)
    }
  }

  const pendingTokens = (tokens || []).filter((token) => !/cancel|complete|in.queue/i.test(token.status))
  return (
    <>
      <PageHeading eyebrow={isFarmer ? 'YOUR HARVEST JOURNEY' : 'CENTER OPERATIONS'} title={isFarmer ? 'Your queue' : 'Queue operations'} description={isFarmer ? 'See your place in line and follow live procurement progress.' : 'Call the next farmer and update the status of each procurement.'} action={<button className="subtle-button" onClick={() => { refreshQueue(); refreshTokens() }}><RefreshCw size={15} /> {t('Refresh')}</button>} />
      {notice && <Notice tone={notice.tone}>{notice.text}</Notice>}
      {isFarmer ? (
        <>
          {pendingTokens.length > 0 && (
            <section className="surface-card join-queue-panel">
              <div><span className="eyebrow">{t('READY WHEN YOU ARE')}</span><h2>{t('Join the queue')}</h2><p>{t('Select one of your booked tokens to join its procurement line.')}</p></div>
              <div className="join-controls"><select aria-label={t('Select a booked token')} value={selectedToken} onChange={(event) => setSelectedToken(event.target.value)}><option value="">{t('Choose your token')}</option>{pendingTokens.map((token) => <option key={token.id} value={token.id}>{token.tokenNumber} · {token.centerName}</option>)}</select><button className="primary-button compact-button" disabled={!selectedToken || busy} onClick={() => action(() => joinQueue(Number(selectedToken)), 'You joined the queue successfully.')}>{busy ? t('Joining…') : t('Join queue')} <ArrowRight size={15} /></button></div>
            </section>
          )}
          {queueLoading ? <LoadingPanel /> : queueError ? <ErrorPanel message={queueError} retry={refreshQueue} /> : myQueue?.length ? (
            <div className="queue-card-list">{myQueue.map((entry) => <QueueEntry key={entry.id} entry={entry} />)}</div>
          ) : <EmptyState icon={Users} title="Your queue is clear" text="When you join a queue for a booked slot, your token and position will appear here." action={<Link className="text-link" to="/slots">{t('View your slots')} <ArrowRight size={15} /></Link>} />}
        </>
      ) : (
        <section className="surface-card operator-queue-panel">
          <div className="section-heading"><div><span className="eyebrow">{t('LIVE QUEUE')}</span><h2>{t('Select a procurement slot')}</h2></div></div>
          <div className="operator-toolbar"><select aria-label={t('Procurement slot')} value={selectedSlot} onChange={(event) => setSelectedSlot(event.target.value)}><option value="">{t('Choose a slot')}</option>{(slots || []).map((slot) => <option key={slot.id} value={slot.id}>{slot.centerName} · {slot.cropName || t('Crop')} · {slot.slotDate}</option>)}</select><button className="secondary-button" disabled={!selectedSlot || busy} onClick={loadSlotQueue}>{busy ? t('Loading…') : t('View queue')}</button><button className="primary-button compact-button" disabled={!selectedSlot || busy} onClick={() => action(() => callNextFarmer(Number(selectedSlot)), 'Next farmer has been called.')}>{t('Call next farmer')} <ArrowRight size={15} /></button></div>
          {slotsLoading && <p className="quiet-note">{t('Loading slots…')}</p>}
          {operatorQueue && (operatorQueue.length ? <div className="responsive-table-wrap"><table className="data-table"><thead><tr><th>{t('Token')}</th><th>{t('Crop')}</th><th>{t('Position')}</th><th>{t('Joined')}</th><th>{t('Status')}</th><th>{t('Action')}</th></tr></thead><tbody>{operatorQueue.map((entry) => <tr key={entry.id}><td><strong>{entry.tokenNumber}</strong></td><td>{entry.cropName || '—'}</td><td>{entry.queuePosition ?? '—'}</td><td>{entry.joinedAt ? dayFormat.format(new Date(entry.joinedAt)) : '—'}</td><td><StatusPill value={entry.status} /></td><td>{entry.status === 'CALLED' ? <button className="table-action" onClick={() => action(() => startProcessing(entry.id), 'Procurement processing started.')}>{t('Start processing')}</button> : entry.status === 'PROCESSING' ? <button className="table-action" onClick={() => action(() => completeProcurement(entry.id), 'Procurement marked complete.')}>{t('Complete')}</button> : '—'}</td></tr>)}</tbody></table></div> : <EmptyState icon={Users} title="No farmers in this queue" text="When farmers join the selected slot, they’ll appear here." />)}
          {!slotsLoading && !selectedSlot && !operatorQueue && <p className="quiet-note">{t('Queue records are loaded from the selected slot; no sample data is shown.')}</p>}
        </section>
      )}
    </>
  )
}

function QueueEntry({ entry }) {
  const { t } = useTranslation()
  const progress = /complete/i.test(entry.status) ? 100 : /processing/i.test(entry.status) ? 66 : /called/i.test(entry.status) ? 34 : 12
  return (
    <article className="surface-card queue-entry">
      <div className="queue-entry-header"><span className="queue-token"><small>{t('YOUR TOKEN')}</small><strong>{entry.tokenNumber || '—'}</strong></span><StatusPill value={entry.status} /></div>
      <div className="queue-entry-center"><MapPin size={15} />{entry.centerName || t('Procurement center')}<span>·</span>{entry.cropName || t('Crop not specified')}</div>
      <div className="queue-position-row"><span>{t('Queue position')}</span><strong>{entry.queuePosition ?? '—'}</strong></div>
      <div className="queue-progress"><i style={{ width: `${progress}%` }} /></div>
      <div className="queue-facts"><span><small>{t('JOINED')}</small>{entry.joinedAt ? new Date(entry.joinedAt).toLocaleTimeString(i18n.resolvedLanguage === 'hi' ? 'hi-IN' : 'en-IN', { hour: 'numeric', minute: '2-digit' }) : '—'}</span><span><small>{t('WAIT TIME')}</small>{entry.estimatedWaitingTime != null ? t('{{minutes}} min', { minutes: entry.estimatedWaitingTime }) : t('Prediction coming soon')}</span></div>
    </article>
  )
}

function ProcurementsPage() {
  const { t } = useTranslation()
  const { data, loading, error, refresh } = useRemote(getProcurements)
  const [search, setSearch] = useState('')
  const rows = (data || []).filter((item) => `${item.id} ${item.cropName} ${item.status}`.toLowerCase().includes(search.toLowerCase()))
  return (
    <>
      <PageHeading eyebrow="RECORDS" title="Procurement history" description="Review procurement records returned by your operations service." action={<button className="subtle-button" onClick={refresh}><RefreshCw size={15} /> {t('Refresh')}</button>} />
      <section className="surface-card table-card"><div className="table-toolbar"><label className="search-control"><Search size={16} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder={t('Search crop, ID, or status')} aria-label={t('Search procurements')} /></label><span className="record-count">{t('{{count}} records', { count: data?.length ?? 0 })}</span></div>
        {loading ? <LoadingPanel /> : error ? <ErrorPanel message={error} retry={refresh} /> : rows.length ? <div className="responsive-table-wrap"><table className="data-table"><thead><tr><th>{t('Procurement ID')}</th><th>{t('Crop')}</th><th>{t('Quantity')}</th><th>{t('Quality')}</th><th>{t('Center')}</th><th>{t('Date')}</th><th>{t('Amount')}</th><th>{t('Status')}</th></tr></thead><tbody>{rows.map((item) => <tr key={item.id}><td><strong>PR-{item.id}</strong></td><td>{item.cropName || '—'}</td><td>{item.actualQuantity ?? '—'} q</td><td>{item.qualityGrade || '—'}</td><td>{item.centerId || '—'}</td><td>{item.createdAt ? dayFormat.format(new Date(item.createdAt)) : '—'}</td><td>{item.totalAmount == null ? '—' : currency.format(item.totalAmount)}</td><td><StatusPill value={item.status} /></td></tr>)}</tbody></table></div> : <EmptyState icon={PackageCheck} title="No procurement records" text="There are no records available for the selected search." />}</section>
    </>
  )
}

function PaymentsPage() {
  const { t } = useTranslation()
  const { data, loading, error, refresh } = useRemote(getPayments)
  const payments = data || []
  const paidTotal = payments.filter((payment) => payment.status === 'PAID').reduce((total, payment) => total + Number(payment.amount || 0), 0)
  const pendingTotal = payments.filter((payment) => payment.status === 'PENDING').reduce((total, payment) => total + Number(payment.amount || 0), 0)
  return (
    <>
      <PageHeading eyebrow="SETTLEMENTS" title="Payment records" description="A transparent view of payments processed through your workspace." action={<button className="subtle-button" onClick={refresh}><RefreshCw size={15} /> {t('Refresh')}</button>} />
      <div className="stats-grid payment-stats"><StatCard icon={WalletCards} label="Paid" value={currency.format(paidTotal)} note="From loaded records" /><StatCard icon={Clock3} label="Pending" value={currency.format(pendingTotal)} note="From loaded records" tone="amber" /><StatCard icon={CreditCard} label="Transactions" value={payments.length} note="All statuses" tone="blue" /></div>
      <section className="surface-card table-card"><div className="table-toolbar"><div><span className="eyebrow">{t('TRANSACTION LEDGER')}</span><h2>{t('Payment history')}</h2></div><span className="record-count">{t('{{count}} records', { count: payments.length })}</span></div>
        {loading ? <LoadingPanel /> : error ? <ErrorPanel message={error} retry={refresh} /> : payments.length ? <div className="responsive-table-wrap"><table className="data-table"><thead><tr><th>{t('Transaction')}</th><th>{t('Procurement')}</th><th>{t('Method')}</th><th>{t('Date')}</th><th>{t('Amount')}</th><th>{t('Status')}</th></tr></thead><tbody>{payments.map((payment) => <tr key={payment.id}><td><strong>{payment.transactionReference || `PAY-${payment.id}`}</strong></td><td>PR-{payment.procurementId}</td><td>{payment.paymentMethod || '—'}</td><td>{payment.paymentDate ? dayFormat.format(new Date(payment.paymentDate)) : '—'}</td><td>{currency.format(payment.amount || 0)}</td><td><StatusPill value={payment.status} /></td></tr>)}</tbody></table></div> : <EmptyState icon={WalletCards} title="No payments found" text="Payment transactions will appear here when returned by the service." />}</section>
    </>
  )
}

function ProfilePage() {
  const { user } = useAuth()
  const { t, i18n } = useTranslation()
  const isFarmer = user.role === 'FARMER'
  const profile = useRemote(isFarmer ? getFarmerProfile : emptyResponse)
  const dateFormat = new Intl.DateTimeFormat(i18n.resolvedLanguage === 'hi' ? 'hi-IN' : 'en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
  const role = user.role === 'CENTER_OPERATOR' ? 'Center operator' : user.role === 'ADMIN' ? 'Administrator' : 'Farmer'
  return (
    <>
      <PageHeading eyebrow="ACCOUNT" title="Your profile" description="Keep your contact and farm details up to date." />
      <section className="surface-card language-settings">
        <div><h2>{t('Language')}</h2><p>{t('Choose the language used across your workspace.')}</p></div>
        <LanguageSelector />
      </section>
      <section className="surface-card account-profile">
        <div className="profile-header">
          <span className="profile-avatar"><Leaf size={24} /></span>
          <div><span className="eyebrow">{t('PROFILE DETAILS')}</span><h2>{user.fullName || t('Account')}</h2><p>{t(role)}</p></div>
        </div>
        <dl className="account-details">
          <div><dt>{t('Full name')}</dt><dd>{user.fullName || '—'}</dd></div>
          <div><dt>{t('Email address')}</dt><dd>{user.email || '—'}</dd></div>
          <div><dt>{t('Phone number')}</dt><dd>{user.phoneNumber || '—'}</dd></div>
          <div><dt>{t('Date of birth')}</dt><dd>{user.dateOfBirth ? dateFormat.format(new Date(`${user.dateOfBirth}T00:00:00`)) : '—'}</dd></div>
          <div><dt>{t('Role')}</dt><dd>{t(role)}</dd></div>
        </dl>
      </section>
      {isFarmer && (
        <section className="farmer-profile-section">
          <div className="section-heading content-section-heading"><div><span className="eyebrow">{t('FARMER PROFILE')}</span><h2>{t('Farm details')}</h2></div></div>
          {profile.loading ? <LoadingPanel rows={3} />
            : profile.status === 404 ? <div className="surface-card profile-missing"><EmptyState icon={Sprout} title="No farmer details are available yet." text="Add your farm location and land information to complete your profile." /><ProfileEditor profile={profile} user={user} /></div>
              : profile.error ? <ErrorPanel message={profile.error} retry={profile.refresh} />
                : profile.data ? <ProfileEditor profile={profile} user={user} /> : <EmptyState icon={Sprout} title="No profile details were returned by the server." text="Your account information is shown above." />}
        </section>
      )}
    </>
  )
}

function ProfileEditor({ profile, user }) {
  const { t } = useTranslation()
  const [isNew, setIsNew] = useState(!profile.data)
  const [draft, setDraft] = useState(() => ({
    ...profile.data,
    fullName: profile.data?.fullName || user.fullName,
    email: profile.data?.email || user.email,
    phoneNumber: profile.data?.phoneNumber || user.phoneNumber || '',
    village: profile.data?.village || '',
    district: profile.data?.district || '',
    state: profile.data?.state || '',
    address: profile.data?.address || '',
    farmerId: profile.data?.farmerId || '',
    landArea: profile.data?.landArea ?? '',
    landAreaUnit: profile.data?.landAreaUnit || '',
  }))
  const [notice, setNotice] = useState(null)
  const [saving, setSaving] = useState(false)

  async function save(event) {
    event.preventDefault()
    setSaving(true)
    setNotice(null)
    try {
      const request = {
        village: draft.village.trim(),
        district: draft.district.trim(),
        state: draft.state.trim(),
        address: draft.address.trim(),
        farmerId: draft.farmerId.trim() || null,
        landArea: Number(draft.landArea),
        landAreaUnit: draft.landAreaUnit.trim(),
      }
      const updated = isNew ? await createFarmerProfile(request) : await updateFarmerProfile(request)
      profile.setData(updated)
      setIsNew(false)
      setNotice({ tone: 'success', text: isNew ? 'Your farmer profile has been created.' : 'Your profile has been updated.' })
    } catch (error) {
      setNotice({ tone: 'error', text: getErrorMessage(error) })
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      {notice && <Notice tone={notice.tone}>{notice.text}</Notice>}
      <form className="surface-card profile-form" onSubmit={save}>
        <div className="profile-header"><span className="profile-avatar"><Leaf size={24} /></span><div><span className="eyebrow">{t('FARMER PROFILE')}</span><h2>{profile.data?.fullName || user.fullName}</h2><p>{profile.data?.farmerId ? `${t('Farmer ID')} · ${profile.data.farmerId}` : user.email}</p></div></div>
        <div className="profile-fields">
          {['fullName', 'email', 'phoneNumber', 'village', 'district', 'state', 'address'].map((key) => (
            <label className={`form-field ${key === 'address' ? 'field-span-two' : ''}`} key={key}><span>{t({ fullName: 'Full name', email: 'Email address', phoneNumber: 'Phone number', village: 'Village', district: 'District', state: 'State', address: 'Address' }[key])}</span><input required={['village', 'district', 'state', 'address'].includes(key)} maxLength={key === 'address' ? 255 : 100} value={draft[key] || ''} type={key === 'email' ? 'email' : 'text'} readOnly={['fullName', 'email', 'phoneNumber'].includes(key)} onChange={(event) => setDraft((current) => ({ ...current, [key]: event.target.value }))} /></label>
          ))}
          <label className="form-field"><span>{t('Farmer ID (optional)')}</span><input maxLength="50" value={draft.farmerId} onChange={(event) => setDraft((current) => ({ ...current, farmerId: event.target.value }))} /></label>
          <label className="form-field"><span>{t('Farm land area')}</span><input type="number" min="0.01" step="0.01" required value={draft.landArea} onChange={(event) => setDraft((current) => ({ ...current, landArea: event.target.value }))} /></label>
          <label className="form-field"><span>{t('Area unit')}</span><input required value={draft.landAreaUnit} onChange={(event) => setDraft((current) => ({ ...current, landAreaUnit: event.target.value }))} placeholder={t('e.g. acres')} /></label>
        </div>
        <div className="profile-form-footer"><p>{t(isNew ? 'Complete your farm details to set up your profile.' : 'Name, email, phone, date of birth, and password are not editable through the current profile API.')}</p><button className="primary-button compact-button" type="submit" disabled={saving}>{saving ? t('Saving…') : t(isNew ? 'Create profile' : 'Save changes')} <Check size={16} /></button></div>
      </form>
    </>
  )
}

function NotificationsPage() {
  return <IntegrationPlaceholder title="Notifications" text="Notifications are not available in the current backend API. No sample alerts are shown here; this page will be connected when notification endpoints are added." />
}

function IntegrationPlaceholder({ title, text }) {
  const { t } = useTranslation()
  return <><PageHeading eyebrow="INTEGRATION STATUS" title={title} description="We only show information provided by your procurement service." /><section className="surface-card integration-placeholder"><span className="integration-icon"><Database size={22} /></span><span className="eyebrow">{t('BACKEND ENDPOINT REQUIRED')}</span><h2>{t('This section is not connected yet')}</h2><p>{t(text)}</p><div className="integration-callout"><span className="live-dot" /> {t('No fabricated records or actions are displayed.')}</div></section></>
}

function ErrorPanel({ message, retry }) {
  const { t } = useTranslation()
  return <div className="surface-card error-panel"><div><span className="error-mark">!</span><div><strong>{t('We couldn’t load this information')}</strong><p>{t(message)}</p></div></div><button className="secondary-button" onClick={retry}><RefreshCw size={15} /> {t('Try again')}</button></div>
}

export function WorkspacePage({ section }) {
  if (section === 'dashboard') return <DashboardPage />
  if (section === 'centers') return <CentersPage />
  if (section === 'slots') return <SlotsPage />
  if (section === 'queue') return <QueuePage />
  if (section === 'procurements') return <ProcurementRoute />
  if (section === 'payments') return <PaymentRoute />
  if (section === 'profile') return <ProfileRoute />
  return <NotificationsPage />
}

function ProcurementRoute() {
  const { user } = useAuth()
  return user.role === 'FARMER'
    ? <IntegrationPlaceholder title="Procurement history" text="The current backend does not expose a farmer-scoped procurement history endpoint. Your records will appear here when that endpoint is available." />
    : <ProcurementsPage />
}

function PaymentRoute() {
  const { user } = useAuth()
  return user.role === 'FARMER'
    ? <IntegrationPlaceholder title="Payments" text="The current backend only exposes payments to center operators and administrators. A farmer-scoped payments endpoint is needed to show your private payment history." />
    : <PaymentsPage />
}

function ProfileRoute() {
  return <ProfilePage />
}
