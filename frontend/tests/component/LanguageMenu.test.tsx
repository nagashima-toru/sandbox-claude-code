import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { LanguageMenu } from '@/components/common/LanguageMenu';

describe('LanguageMenu', () => {
  describe('表示', () => {
    it('日本語設定時にトリガーに「日本語」が表示される', () => {
      render(<LanguageMenu locale="ja" onLocaleChange={vi.fn()} />);
      expect(screen.getByTestId('language-switcher')).toHaveTextContent('日本語');
    });

    it('英語設定時にトリガーに「English」が表示される', () => {
      render(<LanguageMenu locale="en" onLocaleChange={vi.fn()} />);
      expect(screen.getByTestId('language-switcher')).toHaveTextContent('English');
    });

    it('トリガーのアクセシブルな名前は言語名に依存しない固定の名前である', () => {
      render(<LanguageMenu locale="ja" onLocaleChange={vi.fn()} />);
      expect(screen.getByRole('button', { name: '言語 / Language' })).toBeInTheDocument();
    });

    it('初期状態ではメニューが閉じている', () => {
      render(<LanguageMenu locale="ja" onLocaleChange={vi.fn()} />);
      expect(screen.queryByRole('menu')).not.toBeInTheDocument();
    });

    it('defaultOpen のときメニューが開いている', () => {
      render(<LanguageMenu locale="ja" onLocaleChange={vi.fn()} defaultOpen />);
      expect(screen.getByRole('menu')).toBeInTheDocument();
    });
  });

  describe('メニュー', () => {
    it('すべての言語が言語名で並び、現在の言語だけが選択状態になる', () => {
      render(<LanguageMenu locale="en" onLocaleChange={vi.fn()} defaultOpen />);
      const ja = screen.getByRole('menuitemradio', { name: /日本語/ });
      const en = screen.getByRole('menuitemradio', { name: /English/ });
      expect(ja).toHaveAttribute('aria-checked', 'false');
      expect(en).toHaveAttribute('aria-checked', 'true');
    });

    it('各言語の data-testid が付いている', () => {
      render(<LanguageMenu locale="ja" onLocaleChange={vi.fn()} defaultOpen />);
      expect(screen.getByTestId('language-option-ja')).toBeInTheDocument();
      expect(screen.getByTestId('language-option-en')).toBeInTheDocument();
    });
  });

  describe('操作', () => {
    it('トリガーをキーボード（Enter）で操作するとメニューが開く', async () => {
      const user = userEvent.setup();
      render(<LanguageMenu locale="ja" onLocaleChange={vi.fn()} />);
      screen.getByTestId('language-switcher').focus();
      await user.keyboard('{Enter}');
      expect(await screen.findByRole('menu')).toBeInTheDocument();
    });

    it('別の言語を選ぶと onLocaleChange がその言語で呼ばれる', async () => {
      const user = userEvent.setup();
      const onLocaleChange = vi.fn();
      render(<LanguageMenu locale="ja" onLocaleChange={onLocaleChange} defaultOpen />);
      await user.click(screen.getByTestId('language-option-en'));
      expect(onLocaleChange).toHaveBeenCalledWith('en');
    });

    it('選ぶとメニューが閉じる', async () => {
      const user = userEvent.setup();
      render(<LanguageMenu locale="ja" onLocaleChange={vi.fn()} defaultOpen />);
      await user.click(screen.getByTestId('language-option-en'));
      expect(screen.queryByRole('menu')).not.toBeInTheDocument();
    });

    it('Esc でメニューが閉じ、トリガーにフォーカスが戻る', async () => {
      const user = userEvent.setup();
      render(<LanguageMenu locale="ja" onLocaleChange={vi.fn()} defaultOpen />);
      await user.keyboard('{Escape}');
      expect(screen.queryByRole('menu')).not.toBeInTheDocument();
      expect(screen.getByTestId('language-switcher')).toHaveFocus();
    });
  });
});
