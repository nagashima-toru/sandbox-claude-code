import { expect, test } from '@playwright/test';

// Stories tagged 'screen' are the approved design (ADR-0002) and are pinned by screenshot.
const SCREEN_TAG = 'screen';

interface StoryIndexEntry {
  id: string;
  type: 'story' | 'docs';
  tags?: string[];
}

interface StoryIndex {
  entries: Record<string, StoryIndexEntry>;
}

test('screen stories match approved design baselines', async ({ page, request }) => {
  const response = await request.get('/index.json');
  expect(response.ok()).toBe(true);
  const index = (await response.json()) as StoryIndex;

  const storyIds = Object.values(index.entries)
    .filter((entry) => entry.type === 'story' && entry.tags?.includes(SCREEN_TAG))
    .map((entry) => entry.id)
    .sort();

  test.skip(storyIds.length === 0, `No stories tagged '${SCREEN_TAG}'`);

  for (const id of storyIds) {
    await test.step(id, async () => {
      await page.goto(`/iframe.html?id=${id}&viewMode=story`);
      await page.waitForLoadState('networkidle');
      await page.evaluate(() => document.fonts.ready);
      await expect.soft(page).toHaveScreenshot(`${id}.png`, { fullPage: true });
    });
  }
});
