import { Component, lazy, Suspense } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { MotionConfig } from 'framer-motion'
import { useTranslation } from 'react-i18next'
import { AuthProvider, useAuth } from './context/AuthContext'
import { AppShell } from './components/AppShell'

const LoginPage = lazy(() => import('./pages/AuthPages').then((module) => ({ default: module.LoginPage })))
const RegisterPage = lazy(() => import('./pages/AuthPages').then((module) => ({ default: module.RegisterPage })))
const VerifyEmailPage = lazy(() => import('./pages/AuthPages').then((module) => ({ default: module.VerifyEmailPage })))
const WorkspacePage = lazy(() => import('./pages/WorkspacePages').then((module) => ({ default: module.WorkspacePage })))

class AppErrorBoundary extends Component {
  state = { hasError: false }

  static getDerivedStateFromError() {
    return { hasError: true }
  }

  componentDidCatch(error, info) {
    console.error('Unhandled application rendering error', error, info)
  }

  render() {
    return this.state.hasError ? this.props.fallback : this.props.children
  }
}

function ErrorFallback() {
  const { t } = useTranslation()
  return (
    <main className="app-loading error-screen" role="alert">
      <div><h1>{t('We hit an unexpected error.')}</h1><p>{t('Your data is safe. Reload the page to try again.')}</p><button className="secondary-button" onClick={() => window.location.reload()}>{t('Reload application')}</button></div>
    </main>
  )
}

function RoleRoute({ children, roles }) {
  const { t } = useTranslation()
  const { user, ready, initializationError, retrySession } = useAuth()

  if (!ready) return <div className="app-loading" role="status">{t('Loading your workspace…')}</div>
  if (initializationError) return <div className="app-loading"><div role="alert">{t(initializationError)}</div><button className="secondary-button" onClick={retrySession}>{t('Retry')}</button></div>
  if (!user) return <Navigate to="/login" replace />
  if (roles && !roles.includes(user.role)) return <Navigate to="/" replace />
  return children
}

function HomeRoute() {
  const { t } = useTranslation()
  const { user, ready, initializationError, retrySession } = useAuth()
  if (!ready) return <div className="app-loading" role="status">{t('Loading your workspace…')}</div>
  if (initializationError) return <div className="app-loading"><div role="alert">{t(initializationError)}</div><button className="secondary-button" onClick={retrySession}>{t('Retry')}</button></div>
  return <Navigate to={user ? '/dashboard' : '/login'} replace />
}

function AppRoutes() {
  const { t } = useTranslation()
  return (
    <Suspense fallback={<div className="app-loading" role="status">{t('Preparing your workspace…')}</div>}>
      <Routes>
        <Route path="/" element={<HomeRoute />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route element={<RoleRoute><AppShell /></RoleRoute>}>
          <Route path="/dashboard" element={<WorkspacePage section="dashboard" />} />
          <Route path="/centers" element={<WorkspacePage section="centers" />} />
          <Route path="/slots" element={<WorkspacePage section="slots" />} />
          <Route path="/queue" element={<WorkspacePage section="queue" />} />
          <Route path="/procurements" element={<WorkspacePage section="procurements" />} />
          <Route path="/payments" element={<WorkspacePage section="payments" />} />
          <Route path="/profile" element={<WorkspacePage section="profile" />} />
          <Route path="/notifications" element={<WorkspacePage section="notifications" />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  )
}

function App() {
  useTranslation()
  return (
    <AppErrorBoundary fallback={<ErrorFallback />}>
      <MotionConfig reducedMotion="user">
        <BrowserRouter>
          <AuthProvider>
            <AppRoutes />
          </AuthProvider>
        </BrowserRouter>
      </MotionConfig>
    </AppErrorBoundary>
  )
}

export default App
