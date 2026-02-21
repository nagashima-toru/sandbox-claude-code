import type { Meta, StoryObj } from '@storybook/nextjs-vite';
import { LocaleProvider, LocaleContext, useLocale } from './LocaleContext';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

/**
 * Demo component that displays current locale state
 */
function LocaleDemo() {
  const { locale, setLocale } = useLocale();

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Locale Context Demo</CardTitle>
          <CardDescription>Current language setting</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span>Current Locale:</span>
              <Badge>{locale}</Badge>
            </div>
            <div className="flex gap-2">
              <Button
                variant={locale === 'ja' ? 'default' : 'outline'}
                onClick={() => setLocale('ja')}
              >
                日本語
              </Button>
              <Button
                variant={locale === 'en' ? 'default' : 'outline'}
                onClick={() => setLocale('en')}
              >
                English
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

const meta = {
  title: 'Contexts/LocaleContext',
  component: LocaleProvider,
  parameters: {
    layout: 'padded',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof LocaleProvider>;

export default meta;
type Story = StoryObj<typeof meta>;

/**
 * Default locale (Japanese)
 */
export const DefaultJapanese: Story = {
  args: {
    children: <LocaleDemo />,
  },
};

/**
 * English locale preset
 */
export const EnglishLocale: Story = {
  args: {
    children: <LocaleDemo />,
  },
  render: (_args) => (
    <LocaleContext.Provider
      value={{
        locale: 'en',
        setLocale: () => {},
        messages: {},
      }}
    >
      <LocaleDemo />
    </LocaleContext.Provider>
  ),
};

/**
 * Japanese locale preset
 */
export const JapaneseLocale: Story = {
  args: {
    children: <LocaleDemo />,
  },
  render: (_args) => (
    <LocaleContext.Provider
      value={{
        locale: 'ja',
        setLocale: () => {},
        messages: {},
      }}
    >
      <LocaleDemo />
    </LocaleContext.Provider>
  ),
};
