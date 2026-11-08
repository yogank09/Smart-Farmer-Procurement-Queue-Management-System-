import { useEffect, useMemo, useState } from 'react'
import { motion } from 'framer-motion'
import { ArrowLeft, ArrowRight, Eye, EyeOff, Leaf, ShieldCheck } from 'lucide-react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useForm, useWatch } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../context/AuthContext'
import { getErrorMessage } from '../services/api'
import { register, verifyEmail } from '../services/authService'
import { LanguageSelector } from '../components/LanguageSelector'

const createEmailSchema = (t) => z.string().trim().email(t('Enter a valid email address.'))
const createLoginSchema = (t) => z.object({
  email: createEmailSchema(t),
  password: z.string().min(1, t('Enter your password.')),
})

const createRegisterSchema = (t) => z.object({
  fullName: z.string().trim().min(2, t('Enter your full name.')).max(100, t('Full name must be 100 characters or fewer.')),
  email: createEmailSchema(t),
  phoneNumber: z.string().regex(/^\+[1-9]\d{7,14}$/, t('Use your country code, e.g. +919876543210.')),
  dateOfBirth: z.string().refine((value) => {
    if (!value) return false
    const date = new Date(`${value}T00:00:00`)
    const cutoff = new Date()
    cutoff.setFullYear(cutoff.getFullYear() - 18)
    return date < new Date() && date <= cutoff
  }, t('You must be at least 18 years old.')),
  password: z.string()
    .min(8, t('Use at least 8 characters.'))
    .max(72, t('Password must be 72 characters or fewer.'))
    .regex(/[A-Z]/, t('Add an uppercase letter.'))
    .regex(/[a-z]/, t('Add a lowercase letter.'))
    .regex(/\d/, t('Add a number.'))
    .regex(/[^A-Za-z0-9]/, t('Add a symbol.')),
  confirmPassword: z.string(),
  acceptedTerms: z.literal(true, { errorMap: () => ({ message: t('Please accept the terms to continue.') }) }),
}).refine((values) => values.password === values.confirmPassword, {
  path: ['confirmPassword'],
  message: t('Passwords do not match.'),
})

function BrandMark() {
  return <span className="brand-mark"><Leaf size={19} strokeWidth={2.2} /></span>
}

function AuthFrame({ children, eyebrow, title, description, backLink }) {
  const { t } = useTranslation()
  return (
    <main className="auth-page">
      <div className="auth-visual" aria-hidden="true">
        <div className="auth-visual-top"><BrandMark /><span>KisanMitra</span></div>
        <div className="auth-visual-copy">
          <span className="eyebrow light-eyebrow">{t('PROCUREMENT, MADE SIMPLE')}</span>
          <h1>{t('Good work')}<br />{t('grows together.')}</h1>
          <p>{t('One place to plan your harvest, find a center, and stay in the loop.')}</p>
        </div>
        <div className="field-art"><span /><span /><span /><span /><span /></div>
        <div className="visual-footnote"><span className="live-dot" /> {t('A clearer view of your harvest journey')}</div>
      </div>
      <section className="auth-panel">
        <div className="auth-mobile-brand"><BrandMark /><span>KisanMitra</span></div>
        <div className="auth-content">
          <div className="auth-tools"><LanguageSelector compact />{backLink && <Link className="back-link" to={backLink}><ArrowLeft size={16} /> {t('Back')}</Link>}</div>
          <span className="eyebrow">{t(eyebrow)}</span>
          <h2>{t(title)}</h2>
          <p className="auth-description">{t(description)}</p>
          {children}
          <p className="auth-security"><ShieldCheck size={15} /> {t('Your account details are protected.')}</p>
        </div>
        <p className="auth-copyright">© {new Date().getFullYear()} KisanMitra · {t('Made for the people who grow our food')}</p>
      </section>
    </main>
  )
}

function Field({ label, name, registerField, error, type = 'text', ...props }) {
  return (
    <label className="form-field">
      <span>{label}</span>
      <input type={type} {...registerField(name)} aria-invalid={Boolean(error)} {...props} />
      {error && <small className="field-error">{error}</small>}
    </label>
  )
}

export function LoginPage() {
  const { t, i18n } = useTranslation()
  const { signIn } = useAuth()
  const navigate = useNavigate()
  const [visible, setVisible] = useState(false)
  const [requestError, setRequestError] = useState('')
  const loginSchema = useMemo(() => createLoginSchema(t), [t])
  const { register: bind, handleSubmit, trigger, formState: { errors, isSubmitting, isSubmitted } } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  })

  useEffect(() => {
    if (isSubmitted) trigger()
  }, [i18n.resolvedLanguage, isSubmitted, trigger])

  async function onSubmit(credentials) {
    setRequestError('')
    try {
      await signIn(credentials)
      navigate('/dashboard', { replace: true })
    } catch (error) {
      setRequestError(getErrorMessage(error))
    }
  }

  return (
    <AuthFrame
      eyebrow="WELCOME BACK"
      title="Sign in to your account"
      description="Pick up where you left off. Your next step is just around the corner."
    >
      <form className="auth-form" onSubmit={handleSubmit(onSubmit)} noValidate>
        {requestError && <div className="form-alert" role="alert">{t(requestError)}</div>}
        <Field label={t('Email address')} name="email" type="email" autoComplete="email" registerField={bind} error={errors.email?.message} />
        <label className="form-field">
          <span className="label-row"><span>{t('Password')}</span><span className="muted-link">{t('Password reset unavailable')}</span></span>
          <span className="password-input">
            <input type={visible ? 'text' : 'password'} autoComplete="current-password" aria-invalid={Boolean(errors.password)} {...bind('password')} />
            <button type="button" className="reveal-button" onClick={() => setVisible((value) => !value)} aria-label={t(visible ? 'Hide password' : 'Show password')}>
              {visible ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </span>
          {errors.password && <small className="field-error">{errors.password.message}</small>}
        </label>
        <button className="primary-button auth-submit" type="submit" disabled={isSubmitting}>
          {isSubmitting ? t('Signing in…') : t('Sign in')} {!isSubmitting && <ArrowRight size={17} />}
        </button>
        <p className="auth-switch">{t('New to KisanMitra?')} <Link to="/register">{t('Create an account')}</Link></p>
      </form>
    </AuthFrame>
  )
}

export function RegisterPage() {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const [visible, setVisible] = useState(false)
  const [requestError, setRequestError] = useState('')
  const registerSchema = useMemo(() => createRegisterSchema(t), [t])
  const { register: bind, handleSubmit, control, trigger, formState: { errors, isSubmitting, isSubmitted } } = useForm({
    resolver: zodResolver(registerSchema),
    defaultValues: { fullName: '', email: '', phoneNumber: '+91', dateOfBirth: '', password: '', confirmPassword: '', acceptedTerms: false },
  })
  useEffect(() => {
    if (isSubmitted) trigger()
  }, [i18n.resolvedLanguage, isSubmitted, trigger])
  const password = useWatch({ control, name: 'password' }) || ''
  const strength = [
    password.length >= 8,
    /[A-Z]/.test(password) && /[a-z]/.test(password),
    /\d/.test(password),
    /[^A-Za-z0-9]/.test(password),
  ].filter(Boolean).length

  async function onSubmit(values) {
    setRequestError('')
    try {
      await register({
        fullName: values.fullName.trim(),
        email: values.email.trim(),
        phoneNumber: values.phoneNumber,
        dateOfBirth: values.dateOfBirth,
        password: values.password,
      })
      navigate('/verify-email', { state: { email: values.email.trim() } })
    } catch (error) {
      setRequestError(getErrorMessage(error))
    }
  }

  return (
    <AuthFrame
      eyebrow="GET STARTED"
      title="Create your account"
      description="Join your local procurement network and make every harvest count."
      backLink="/login"
    >
      <form className="auth-form register-form" onSubmit={handleSubmit(onSubmit)} noValidate>
        {requestError && <div className="form-alert" role="alert">{t(requestError)}</div>}
        <Field label={t('Full name')} name="fullName" autoComplete="name" registerField={bind} error={errors.fullName?.message} />
        <Field label={t('Email address')} name="email" type="email" autoComplete="email" registerField={bind} error={errors.email?.message} />
        <Field label={t('Phone number')} name="phoneNumber" type="tel" autoComplete="tel" registerField={bind} error={errors.phoneNumber?.message} />
        <Field label={t('Date of birth')} name="dateOfBirth" type="date" autoComplete="bday" max={new Date().toISOString().slice(0, 10)} registerField={bind} error={errors.dateOfBirth?.message} />
        <label className="form-field">
          <span>{t('Password')}</span>
          <span className="password-input">
            <input type={visible ? 'text' : 'password'} autoComplete="new-password" aria-invalid={Boolean(errors.password)} {...bind('password')} />
            <button type="button" className="reveal-button" onClick={() => setVisible((value) => !value)} aria-label={t(visible ? 'Hide password' : 'Show password')}>
              {visible ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </span>
          <span className="strength-meter" aria-label={t('Password strength: {{count}} of 4', { count: strength })}>
            {[1, 2, 3, 4].map((part) => <i key={part} className={strength >= part ? 'filled' : ''} />)}
            <small>{t(strength < 2 ? 'Add a few more details' : strength < 4 ? 'Getting stronger' : 'Strong password')}</small>
          </span>
          {errors.password && <small className="field-error">{errors.password.message}</small>}
        </label>
        <Field label={t('Confirm password')} name="confirmPassword" type="password" autoComplete="new-password" registerField={bind} error={errors.confirmPassword?.message} />
        <label className="check-field">
          <input type="checkbox" {...bind('acceptedTerms')} />
          <span>{t('I agree to the terms of service and privacy policy.')}</span>
        </label>
        {errors.acceptedTerms && <small className="field-error terms-error">{errors.acceptedTerms.message}</small>}
        <button className="primary-button auth-submit" type="submit" disabled={isSubmitting}>
          {isSubmitting ? t('Creating account…') : t('Create account')} {!isSubmitting && <ArrowRight size={17} />}
        </button>
        <p className="auth-switch">{t('Already have an account?')} <Link to="/login">{t('Sign in')}</Link></p>
      </form>
    </AuthFrame>
  )
}

export function VerifyEmailPage() {
  const { t } = useTranslation()
  const location = useLocation()
  const navigate = useNavigate()
  const email = location.state?.email || ''
  const [otp, setOtp] = useState('')
  const [errorText, setErrorText] = useState('')
  const [verified, setVerified] = useState(false)
  const [loading, setLoading] = useState(false)

  async function onSubmit(event) {
    event.preventDefault()
    if (!email) {
      setErrorText('Start registration first so we know which email to verify.')
      return
    }
    if (!/^\d{6}$/.test(otp)) {
      setErrorText('Enter the six-digit code from your email.')
      return
    }
    setLoading(true)
    setErrorText('')
    try {
      await verifyEmail({ email, otp })
      setVerified(true)
    } catch (error) {
      setErrorText(getErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthFrame     eyebrow="EMAIL VERIFICATION" title={verified ? 'You’re all set.' : 'Check your inbox'} description={verified ? 'Your email is verified. Sign in to continue to your workspace.' : t('Enter the 6-digit verification code sent to {{email}}.', { email: email || t('your registered email address') })} backLink="/register">
      {verified ? (
        <motion.div initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="verified-card">
          <span className="success-check">✓</span>
          <p>{t('Email successfully verified')}</p>
          <button type="button" className="primary-button auth-submit" onClick={() => navigate('/login', { replace: true })}>{t('Continue to sign in')} <ArrowRight size={17} /></button>
        </motion.div>
      ) : (
        <form className="auth-form" onSubmit={onSubmit}>
          {errorText && <div className="form-alert" role="alert">{t(errorText)}</div>}
          <label className="form-field">
            <span>{t('Verification code')}</span>
            <input className="otp-input" inputMode="numeric" autoComplete="one-time-code" maxLength={6} value={otp} onChange={(event) => setOtp(event.target.value.replace(/\D/g, '').slice(0, 6))} aria-label={t('Six-digit verification code')} />
          </label>
          <p className="integration-note">{t('A resend-code option will be available when the backend exposes an OTP resend endpoint.')}</p>
          <button className="primary-button auth-submit" type="submit" disabled={loading}>{loading ? t('Verifying…') : t('Verify email')} {!loading && <ArrowRight size={17} />}</button>
        </form>
      )}
    </AuthFrame>
  )
}
