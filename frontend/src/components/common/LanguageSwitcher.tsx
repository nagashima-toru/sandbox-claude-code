'use client';

import { Button } from '@/components/ui/button';
import { useLocale } from '@/contexts/LocaleContext';
import { cn } from '@/lib/utils';

export interface LanguageSwitcherProps {
  className?: string;
}

export function LanguageSwitcher({ className }: LanguageSwitcherProps) {
  const { locale, setLocale } = useLocale();

  const handleClick = () => {
    setLocale(locale === 'ja' ? 'en' : 'ja');
  };

  return (
    <Button
      variant="outline"
      size="sm"
      onClick={handleClick}
      data-testid="language-switcher"
      className={cn(className)}
      aria-label={locale === 'ja' ? 'Switch to English' : '日本語に切り替え'}
    >
      {locale === 'ja' ? 'EN' : '日'}
    </Button>
  );
}
