import { useEffect, useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import {
  Bell, ChevronDown, Command, CreditCard, Grid2X2, Leaf, LogOut, MapPin,
  Menu, Moon, Search, Settings2, Sun, Tractor, Users, X,
} from 'lucide-react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../context/AuthContext'
import { LanguageSelector } from './LanguageSelector'

const linksByRole = {
  FARMER: [
    { to: '/dashboard', label: 'Overview', icon: Grid2X2 },
    { to: '/centers', label: 'Find a center', icon: MapPin },
    { to: '/slots', label: 'Book a slot', icon: Command },
    { to: '/queue', label: 'My queue', icon: Users },
    { to: '/procurements', label: 'Procurement history', icon: Tractor },
    { to: '/payments', label: 'Payments', icon: CreditCard },
  ],
  CENTER_OPERATOR: [
    { to: '/dashboard', label: 'Overview', icon: Grid2X2 },
    { to: '/centers', label: 'Centers', icon: MapPin },
    { to: '/slots', label: 'Slots', icon: Command },
    { to: '/queue', label: 'Queue operations', icon: Users },
    { to: '/procurements', label: 'Procurements', icon: Tractor },
    { to: '/payments', label: 'Payments', icon: CreditCard },
  ],
  ADMIN: [
    { to: '/dashboard', label: 'Overview', icon: Grid2X2 },
    { to: '/centers', label: 'Centers', icon: MapPin },
    { to: '/slots', label: 'Slots', icon: Command },
    { to: '/queue', label: 'Queue operations', icon: Users },
    { to: '/procurements', label: 'Procurements', icon: Tractor },
    { to: '/payments', label: 'Payments', icon: CreditCard },
  ],
}

const titles = {
  dashboard: 'Overview',
  centers: 'Procurement centers',
  slots: 'Book a slot',
  queue: 'Queue',
  procurements: 'Procurement history',
  payments: 'Payments',
  profile: 'Your profile',
  notifications: 'Notifications',
}

function getTheme() {
  try {
    const saved = window.localStorage.getItem('fieldnote-theme')
    return ['light', 'dark', 'system'].includes(saved) ? saved : 'system'
  } catch {
    return 'system'
  }
}

export function AppShell() {
  const { user, signOut } = useAuth()
  const { t } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [theme, setTheme] = useState(getTheme)
  const [menuOpen, setMenuOpen] = useState(false)
  const [searchValue, setSearchValue] = useState('')
  const [logoutError, setLogoutError] = useState('')
  const pageKey = location.pathname.split('/')[1] || 'dashboard'
  const links = linksByRole[user?.role] || []

  useEffect(() => {
    document.documentElement.dataset.theme = theme
    try {
      window.localStorage.setItem('fieldnote-theme', theme)
    } catch {
      // The app remains usable when browser storage is disabled.
    }
  }, [theme])

  function cycleTheme() {
    setTheme((current) => current === 'system' ? 'light' : current === 'light' ? 'dark' : 'system')
  }

  async function leave() {
    setLogoutError('')
    try {
      await signOut()
      navigate('/login', { replace: true })
    } catch {
      setLogoutError('Sign out failed. Check your connection and try again.')
    }
  }

  function searchSection(event) {
    event.preventDefault()
    const value = searchValue.trim().toLowerCase()
    if (!value) return
    const match = links.find((item) => t(item.label).toLowerCase().includes(value))
      || links.find((item) => value.split(/\s+/).some((word) => t(item.label).toLowerCase().includes(word)))
    if (match) {
      navigate(match.to)
      setSearchValue('')
    }
  }

  const ThemeIcon = theme === 'dark' ? Moon : Sun
  const initials = user?.fullName?.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase() || 'FN'

  return (
    <div className="workspace">
      <AnimatePresence>
        {sidebarOpen && <motion.button className="sidebar-backdrop" aria-label={t('Close navigation')} initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onClick={() => setSidebarOpen(false)} />}
      </AnimatePresence>
      <aside className={`sidebar ${sidebarOpen ? 'sidebar-open' : ''}`} aria-label={t('Main navigation')}>
        <div className="sidebar-brand">
          <span className="brand-mark"><Leaf size={19} /></span>
          <span>KisanMitra<span className="brand-period">.</span></span>
          <button className="icon-button mobile-close" onClick={() => setSidebarOpen(false)} aria-label={t('Close menu')}><X size={19} /></button>
        </div>
        <div className="workspace-switcher">
          <div className="workspace-avatar"><Leaf size={16} /></div>
          <div><strong>{t('My workspace')}</strong><small>{t(user?.role === 'CENTER_OPERATOR' ? 'Center operator' : user?.role === 'ADMIN' ? 'Administrator' : 'Farmer')}</small></div>
          <ChevronDown size={16} className="switcher-chevron" />
        </div>
        <span className="sidebar-label">{t('WORKSPACE')}</span>
        <nav className="sidebar-nav">
          {links.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to} end onClick={() => setSidebarOpen(false)} className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <Icon size={18} strokeWidth={1.8} /><span>{t(label)}</span>
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-spacer" />
        <div className="sidebar-support">
          <div className="support-orb"><Leaf size={18} /></div>
          <strong>{t('Rooted in progress')}</strong>
          <p>{t('Your harvest journey, all in one place.')}</p>
        </div>
        <div className="sidebar-bottom">
          <NavLink to="/profile" onClick={() => setSidebarOpen(false)} className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}><Settings2 size={18} /><span>{t('Profile & settings')}</span></NavLink>
          <LanguageSelector />
          <button className="nav-link theme-control" onClick={cycleTheme}><ThemeIcon size={18} /><span>{t('Appearance')}</span><small>{t(theme)}</small></button>
          <div className="account-row">
            <span className="user-avatar">{initials}</span>
            <span className="account-copy"><strong>{user?.fullName || t('Account')}</strong><small>{user?.email}</small></span>
            <button className="icon-button logout-button" onClick={leave} aria-label={t('Sign out')}><LogOut size={17} /></button>
          </div>
        </div>
      </aside>

      <div className="workspace-main">
        <header className="topbar">
          <button className="icon-button mobile-menu" onClick={() => setSidebarOpen(true)} aria-label={t('Open navigation')}><Menu size={21} /></button>
          <div className="topbar-breadcrumb"><span>{t('Workspace')}</span><span className="crumb-divider">/</span><strong>{t(titles[pageKey] || 'Overview')}</strong></div>
          <div className="topbar-actions">
            <form className="global-search" role="search" onSubmit={searchSection}><Search size={16} /><input value={searchValue} onChange={(event) => setSearchValue(event.target.value)} placeholder={t('Find a section...')} aria-label={t('Search workspace sections')} /></form>
            <LanguageSelector compact />
            <button className="icon-button notification-button" onClick={() => navigate('/notifications')} aria-label={t('Notifications')}><Bell size={19} /></button>
            <button className="topbar-user" onClick={() => setMenuOpen((value) => !value)} aria-expanded={menuOpen} aria-label={t('Account menu')}><span className="user-avatar">{initials}</span><ChevronDown size={15} /></button>
            {menuOpen && <div className="account-menu"><NavLink to="/profile" onClick={() => setMenuOpen(false)}>{t('Profile & settings')}</NavLink><button onClick={leave}>{t('Sign out')}</button></div>}
          </div>
        </header>
        <main className="page-content">
          {logoutError && <div className="notice notice-error" role="alert">{t(logoutError)}</div>}
          <Outlet />
        </main>
        <footer className="workspace-footer"><span>KisanMitra · {t('Procurement, made simpler')}</span><span>{t('Your session is securely verified when you return.')}</span></footer>
      </div>
    </div>
  )
}
