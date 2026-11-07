import i18n from 'i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import { initReactI18next } from 'react-i18next'
import en from './locales/en/translation.json'
import hi from './locales/hi/translation.json'

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      hi: { translation: hi },
    },
    fallbackLng: 'en',
    supportedLngs: ['en', 'hi'],
    defaultNS: 'translation',
    keySeparator: false,
    interpolation: { escapeValue: false },
    detection: {
      order: ['localStorage', 'navigator'],
      lookupLocalStorage: 'fieldnote-language',
      caches: ['localStorage'],
    },
    react: { useSuspense: false },
  })

export default i18n
