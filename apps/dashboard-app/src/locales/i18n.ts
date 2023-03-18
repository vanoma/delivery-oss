import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import { TRANSLATIONS_EN } from './en/translations';
import { TRANSLATIONS_RW } from './rw/translations';
import { TRANSLATIONS_FR } from './fr/translations';
import storage from '../services/storage';

const chosenLanguage = storage.getItem('chosenLanguage');

i18n.use(LanguageDetector)
    .use(initReactI18next)
    .init({
        resources: {
            en: {
                translation: TRANSLATIONS_EN,
            },
            rw: {
                translation: TRANSLATIONS_RW,
            },
            fr: {
                translation: TRANSLATIONS_FR,
            },
        },
    });

i18n.changeLanguage(chosenLanguage ?? 'en');

export default i18n;
