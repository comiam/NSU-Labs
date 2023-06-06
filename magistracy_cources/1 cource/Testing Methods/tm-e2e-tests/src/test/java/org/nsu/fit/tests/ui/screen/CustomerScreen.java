package org.nsu.fit.tests.ui.screen;

import org.nsu.fit.services.browser.Browser;
import org.nsu.fit.shared.Screen;
import org.openqa.selenium.By;

public class CustomerScreen extends Screen {
    private static final String subscriptions = "//div[@class='MuiPaper-root MuiPaper-elevation2 MuiPaper-rounded'][1]";
    private static final String plans = "//div[@class='MuiPaper-root MuiPaper-elevation2 MuiPaper-rounded'][2]";
    public CustomerScreen(Browser browser) {
        super(browser);
    }

    public CustomerScreen buyFirstPlan() {
        browser.click(By.xpath(plans + "//tr[@index='0'][1]/td[1]/div/button[1]"));
        return this;
    }

    public CustomerScreen buyFirstPlanOk() {
        browser.click(By.xpath(plans + "//tr[@index='0' and @mode='delete'][1]/td[1]/div/button[1]"));
        return this;
    }

}
