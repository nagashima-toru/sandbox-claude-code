'use client';

import { ChevronDown, Globe } from 'lucide-react';

import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import type { Locale } from '@/contexts/LocaleContext';
import { cn } from '@/lib/utils';

// Language names are shown in their own language so they are recognizable in any UI language
const LANGUAGES: ReadonlyArray<{ locale: Locale; name: string }> = [
  { locale: 'ja', name: '日本語' },
  { locale: 'en', name: 'English' },
];

export interface LanguageMenuProps {
  locale: Locale;
  onLocaleChange: (locale: Locale) => void;
  /** Render the menu open on first paint (used by screen stories) */
  defaultOpen?: boolean;
  className?: string;
}

export function LanguageMenu({
  locale,
  onLocaleChange,
  defaultOpen = false,
  className,
}: LanguageMenuProps) {
  const current = LANGUAGES.find((language) => language.locale === locale) ?? LANGUAGES[0];

  return (
    <DropdownMenu defaultOpen={defaultOpen}>
      <DropdownMenuTrigger asChild>
        <Button
          variant="outline"
          data-testid="language-switcher"
          aria-label="言語 / Language"
          className={cn('max-md:w-10 max-md:px-0', className)}
        >
          <Globe aria-hidden="true" />
          <span className="max-md:hidden">{current.name}</span>
          <ChevronDown aria-hidden="true" className="max-md:hidden" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuRadioGroup
          value={locale}
          onValueChange={(value) => onLocaleChange(value as Locale)}
        >
          {LANGUAGES.map((language) => (
            <DropdownMenuRadioItem
              key={language.locale}
              value={language.locale}
              data-testid={`language-option-${language.locale}`}
            >
              <span>{language.name}</span>
              <span className="text-xs text-muted-foreground">{language.locale.toUpperCase()}</span>
            </DropdownMenuRadioItem>
          ))}
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
