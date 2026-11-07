import { Globe } from 'lucide-react'
import { useTranslation } from 'react-i18next'

export function LanguageSelector({ compact = false }) {
  const { i18n, t } = useTranslation()
  const language = i18n.resolvedLanguage?.startsWith('hi') ? 'hi' : 'en'

  return (
    <label className={`language-selector ${compact ? 'language-selector-compact' : ''}`}>
      <Globe size={16} aria-hidden="true" />
      <span className="sr-only">{t('Choose language')}</span>
      <select value={language} aria-label={t('Choose language')} onChange={(event) => i18n.changeLanguage(event.target.value)}>
        <option value="en">{t('English')}</option>
        <option value="hi">{t('हिन्दी')}</option>
      </select>
    </label>
  )
}
