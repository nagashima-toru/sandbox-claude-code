'use client';

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import jaMessages from '../../messages/ja.json';
import enMessages from '../../messages/en.json';

export type Locale = 'ja' | 'en';

const messagesByLocale: Record<Locale, Record<string, unknown>> = {
  ja: jaMessages as Record<string, unknown>,
  en: enMessages as Record<string, unknown>,
};

export interface LocaleContextValue {
  locale: Locale;
  setLocale: (locale: Locale) => void;
  messages: Record<string, unknown>;
}

export const LocaleContext = createContext<LocaleContextValue | undefined>(undefined);

export function useLocale(): LocaleContextValue {
  const context = useContext(LocaleContext);

  if (context === undefined) {
    throw new Error('useLocale must be used within LocaleProvider');
  }

  return context;
}

function getInitialLocale(): Locale {
  // Guard for SSR - localStorage doesn't exist on server
  if (typeof window === 'undefined') return 'ja';
  try {
    const saved = localStorage.getItem('locale');
    if (saved === 'ja' || saved === 'en') {
      return saved;
    }
    return 'ja';
  } catch {
    return 'ja';
  }
}

export interface LocaleProviderProps {
  children: ReactNode;
}

export function LocaleProvider({ children }: LocaleProviderProps) {
  // Lazy initializer reads from localStorage on client, returns 'ja' on server
  const [locale, setLocaleState] = useState<Locale>(getInitialLocale);

  // Update document lang attribute when locale changes
  useEffect(() => {
    document.documentElement.lang = locale;
  }, [locale]);

  const setLocale = useCallback((newLocale: Locale) => {
    try {
      localStorage.setItem('locale', newLocale);
    } catch {
      // localStorage unavailable (e.g., private browsing) - continue without persistence
    }
    setLocaleState(newLocale);
  }, []);

  const value = useMemo<LocaleContextValue>(
    () => ({
      locale,
      setLocale,
      messages: messagesByLocale[locale],
    }),
    [locale, setLocale]
  );

  return <LocaleContext.Provider value={value}>{children}</LocaleContext.Provider>;
}
