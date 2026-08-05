# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: e2e\search.spec.ts >> Search System QA >> Deve realizar o debounce, pesquisar e validar UX do dropdown
- Location: e2e\search.spec.ts:4:7

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('.search-input')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for locator('.search-input')

```

```yaml
- banner:
  - text: AnyService
  - button "Abrir Menu Principal"
- main:
  - heading "AnyService" [level=1]
  - paragraph: Bem-vindo de volta! Faça login na sua conta.
  - text: E-mail
  - textbox "E-mail":
    - /placeholder: nome@email.com
  - text: Senha
  - textbox "Senha":
    - /placeholder: ••••••••
  - link "Esqueceu a senha?":
    - /url: /forgot-password
  - button "Entrar"
  - text: Não tem uma conta?
  - link "Registre-se agora":
    - /url: /register
```

# Test source

```ts
  1  | import { test, expect } from '@playwright/test';
  2  | 
  3  | test.describe('Search System QA', () => {
  4  |   test('Deve realizar o debounce, pesquisar e validar UX do dropdown', async ({ page }) => {
  5  |     await page.route('**/api/users/search?q=Kai', async route => {
  6  |       await route.fulfill({
  7  |         status: 200,
  8  |         contentType: 'application/json',
  9  |         body: JSON.stringify([{ id: 1, name: 'Kaio', username: 'kaio123', profilePictureUrl: '' }])
  10 |       });
  11 |     });
  12 | 
  13 |     await page.goto('http://localhost:4200');
  14 | 
  15 |     const searchInput = page.locator('.search-input');
> 16 |     await expect(searchInput).toBeVisible();
     |                               ^ Error: expect(locator).toBeVisible() failed
  17 | 
  18 |     await searchInput.pressSequentially('Kax', { delay: 50 });
  19 |     await searchInput.press('Backspace');
  20 |     await searchInput.pressSequentially('i', { delay: 50 });
  21 | 
  22 |     const dropdownItem = page.locator('.dropdown-item').first();
  23 |     await expect(dropdownItem).toBeVisible();
  24 |     
  25 |     await expect(dropdownItem.locator('.avatar')).toBeVisible();
  26 |     await expect(dropdownItem.locator('.name')).toHaveText('Kaio');
  27 |     await expect(dropdownItem.locator('.username')).toHaveText('@kaio123');
  28 |   });
  29 | });
  30 | 
```