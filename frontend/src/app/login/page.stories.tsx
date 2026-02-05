import type { Meta, StoryObj } from '@storybook/nextjs-vite';
import LoginPage from './page';

const meta = {
  title: 'Pages/LoginPage',
  component: LoginPage,
  parameters: {
    layout: 'fullscreen',
    nextjs: {
      appDirectory: true,
    },
  },
  tags: ['autodocs'],
} satisfies Meta<typeof LoginPage>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  parameters: {
    docs: {
      description: {
        story: 'ログインページのデフォルト表示。開発環境用の認証情報が表示されています。',
      },
    },
  },
};
