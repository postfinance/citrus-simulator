import { expect, Page, test } from '@playwright/test';
import {
  goToAllNavigationTabsAndOptionallyValidateContent,
  mockResponseForAllNavbarLinkedSites
} from './helpers/helper-functions';

test.beforeEach(async ({ page }) => {
  await page.goto('http://localhost:9000/');
});

test('should show error banner if there is an error code in the backend response while loading any page', async ({ page }) => {
  await mockResponseForAllNavbarLinkedSites(page, mock500ErrorResponseForApiURL);

  await goToAllNavigationTabsAndOptionallyValidateContent(page, verifyErrorBannerIsVisible);
});

const verifyErrorBannerIsVisible = async (pageToPass: Page): Promise<void> => await expect(pageToPass.getByTestId('error')).toBeVisible();

const mock500ErrorResponseForApiURL = async (page: Page, apiLink: string): Promise<void> => {
  await page.route(apiLink, async route => {
    await route.fulfill({
      status: 500,
    });
  });
};
