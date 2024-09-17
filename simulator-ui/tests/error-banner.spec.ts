import {expect, Page, test} from '@playwright/test';
import {
  goToAllNavigationTabsAndOptionallyValidateContent,
  mockErrorResponseForAllNavbarLinkedSites
} from './helpers/helper-functions';

test.beforeEach(async ({ page }) => {
  await page.goto('http://localhost:9000/');
});

test('should show error banner if there is an error code in the backend response while loading any page', async ({ page }) => {
  await mockErrorResponseForAllNavbarLinkedSites(page);

  await goToAllNavigationTabsAndOptionallyValidateContent(page, verifyErrorBannerIsVisible);
});

const verifyErrorBannerIsVisible = async (pageToPass: Page): Promise<void> => await expect(pageToPass.getByTestId('error')).toBeVisible();
