package org.nsu.fit.tests.ui;

import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.browser.Browser;
import org.nsu.fit.services.browser.BrowserService;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OpenAdminPageTest {
    private Browser browser = null;
    @BeforeClass
    public void beforeClass() {
        browser = BrowserService.openNewBrowser();
    }

    @Test(description = "Try to open admin page via UI.")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Auth feature")
    public void tryToOpenAdminPage() {
        browser.openPage("http://localhost:8090/tm-frontend/admin");
        Assert.assertFalse(browser.isElementPresent(
                By.xpath("//div[@class='MuiPaper-root MuiPaper-elevation2 MuiPaper-rounded'][1]")));
    }

    @AfterClass
    public void afterClass() {
        if (browser != null) {
            browser.close();
        }
    }
}
