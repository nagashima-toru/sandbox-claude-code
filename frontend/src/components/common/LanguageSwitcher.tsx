'use client';

import { LanguageMenu } from '@/components/common/LanguageMenu';
import { useLocale } from '@/contexts/LocaleContext';

export interface LanguageSwitcherProps {
  className?: string;
}

// Connects the presentational LanguageMenu (approved design, specs/ui/language-switch) to the locale state
export function LanguageSwitcher({ className }: LanguageSwitcherProps) {
  const { locale, setLocale } = useLocale();

  return <LanguageMenu locale={locale} onLocaleChange={setLocale} className={className} />;
}
