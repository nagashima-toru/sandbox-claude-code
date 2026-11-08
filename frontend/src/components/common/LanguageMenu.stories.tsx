import type { Meta, StoryObj } from '@storybook/nextjs-vite';
import { LogOut } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { LanguageMenu } from './LanguageMenu';

/**
 * Screen stories for the language switch (specs/ui/language-switch/screens.md).
 * One story per state; stories tagged 'screen' are pinned by VRT (ADR-0002).
 * The switch is shown in the list-screen header, next to the logout button.
 */
const meta = {
  title: 'Screens/LanguageSwitch',
  component: LanguageMenu,
  parameters: {
    layout: 'fullscreen',
  },
  args: {
    locale: 'ja',
    onLocaleChange: () => {},
  },
  decorators: [
    (Story, context) => (
      <div className="min-h-[220px] bg-background">
        <header className="flex items-center justify-between gap-2 border-b px-4 py-3">
          <h1 className="text-lg font-semibold">
            {context.args.locale === 'en' ? 'Message Management' : 'メッセージ管理'}
          </h1>
          <div className="flex items-center gap-2">
            <Story />
            <Button variant="outline">
              <LogOut className="mr-2 h-4 w-4" />
              {context.args.locale === 'en' ? 'Logout' : 'ログアウト'}
            </Button>
          </div>
        </header>
      </div>
    ),
  ],
} satisfies Meta<typeof LanguageMenu>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Web width, menu closed, Japanese */
export const WebClosedJa: Story = {
  tags: ['screen'],
};

/** Web width, menu closed, English */
export const WebClosedEn: Story = {
  tags: ['screen'],
  args: { locale: 'en' },
};

/** Web width, menu open */
export const WebOpen: Story = {
  tags: ['screen'],
  args: { defaultOpen: true },
};

/** Phone width (< 768px), icon only */
export const MobileClosed: Story = {
  tags: ['screen', 'mobile'],
};

/** Phone width (< 768px), menu open */
export const MobileOpen: Story = {
  tags: ['screen', 'mobile'],
  args: { defaultOpen: true },
};
