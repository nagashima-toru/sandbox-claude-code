import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { LanguageSwitcher } from '@/components/common/LanguageSwitcher';
import { LocaleContext } from '@/contexts/LocaleContext';
import jaMessages from '../../messages/ja.json';
import enMessages from '../../messages/en.json';
import type { ReactNode } from 'react';

function createJaWrapper(setLocale = vi.fn()) {
  function JaWrapper({ children }: { children: ReactNode }) {
    return (
      <LocaleContext.Provider
        value={{
          locale: 'ja',
          setLocale,
          messages: jaMessages as Record<string, unknown>,
        }}
      >
        {children}
      </LocaleContext.Provider>
    );
  }
  return JaWrapper;
}

function createEnWrapper(setLocale = vi.fn()) {
  function EnWrapper({ children }: { children: ReactNode }) {
    return (
      <LocaleContext.Provider
        value={{
          locale: 'en',
          setLocale,
          messages: enMessages as Record<string, unknown>,
        }}
      >
        {children}
      </LocaleContext.Provider>
    );
  }
  return EnWrapper;
}

// The menu itself (keyboard, Esc, check mark) is covered by LanguageMenu.test.tsx.
// These tests cover only the connection to LocaleContext.
describe('LanguageSwitcher', () => {
  describe('表示', () => {
    it('日本語設定時にボタンに「日本語」が表示される', () => {
      render(<LanguageSwitcher />, { wrapper: createJaWrapper() });
      expect(screen.getByTestId('language-switcher')).toHaveTextContent('日本語');
    });

    it('英語設定時にボタンに「English」が表示される', () => {
      render(<LanguageSwitcher />, { wrapper: createEnWrapper() });
      expect(screen.getByTestId('language-switcher')).toHaveTextContent('English');
    });

    it('data-testid="language-switcher" で要素を取得できる', () => {
      render(<LanguageSwitcher />, { wrapper: createJaWrapper() });
      const button = screen.getByTestId('language-switcher');
      expect(button).toBeVisible();
    });
  });

  describe('言語の選択', () => {
    it('日本語設定時にメニューで English を選ぶと setLocale("en") が呼ばれる', async () => {
      const setLocale = vi.fn();
      const user = userEvent.setup();

      render(<LanguageSwitcher />, { wrapper: createJaWrapper(setLocale) });

      await user.click(screen.getByTestId('language-switcher'));
      await user.click(screen.getByTestId('language-option-en'));

      expect(setLocale).toHaveBeenCalledOnce();
      expect(setLocale).toHaveBeenCalledWith('en');
    });

    it('英語設定時にメニューで日本語を選ぶと setLocale("ja") が呼ばれる', async () => {
      const setLocale = vi.fn();
      const user = userEvent.setup();

      render(<LanguageSwitcher />, { wrapper: createEnWrapper(setLocale) });

      await user.click(screen.getByTestId('language-switcher'));
      await user.click(screen.getByTestId('language-option-ja'));

      expect(setLocale).toHaveBeenCalledOnce();
      expect(setLocale).toHaveBeenCalledWith('ja');
    });

    it('ボタンを押しただけでは setLocale は呼ばれない', async () => {
      const setLocale = vi.fn();
      const user = userEvent.setup();

      render(<LanguageSwitcher />, { wrapper: createJaWrapper(setLocale) });

      await user.click(screen.getByTestId('language-switcher'));

      expect(setLocale).not.toHaveBeenCalled();
    });
  });

  describe('className prop', () => {
    it('className が渡された場合にクラスが適用される', () => {
      render(<LanguageSwitcher className="custom-class" />, { wrapper: createJaWrapper() });
      const button = screen.getByTestId('language-switcher');
      expect(button).toHaveClass('custom-class');
    });
  });

  describe('サイズ', () => {
    it('デフォルトサイズで表示される（size="sm" が適用されていない）', () => {
      render(<LanguageSwitcher />, { wrapper: createJaWrapper() });
      const button = screen.getByTestId('language-switcher');
      // Default size uses h-10; sm size uses h-9
      expect(button).toHaveClass('h-10');
      expect(button).not.toHaveClass('h-9');
    });
  });
});
