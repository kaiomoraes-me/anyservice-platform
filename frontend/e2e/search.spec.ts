import { test, expect } from '@playwright/test';

test.describe('Search System QA', () => {
  test('Deve realizar o debounce, pesquisar e validar UX do dropdown', async ({ page }) => {
    await page.route('**/api/users/search?q=Kai', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([{ id: 1, name: 'Kaio', username: 'kaio123', profilePictureUrl: '' }])
      });
    });

    await page.goto('http://localhost:4200');

    const searchInput = page.locator('.search-input');
    await expect(searchInput).toBeVisible();

    await searchInput.pressSequentially('Kax', { delay: 50 });
    await searchInput.press('Backspace');
    await searchInput.pressSequentially('i', { delay: 50 });

    const dropdownItem = page.locator('.dropdown-item').first();
    await expect(dropdownItem).toBeVisible();
    
    await expect(dropdownItem.locator('.avatar')).toBeVisible();
    await expect(dropdownItem.locator('.name')).toHaveText('Kaio');
    await expect(dropdownItem.locator('.username')).toHaveText('@kaio123');
  });
});
