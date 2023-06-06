package org.nsu.fit.tests.ui.screen;

import org.nsu.fit.services.browser.Browser;
import org.nsu.fit.shared.Screen;
import org.openqa.selenium.By;

public class AdditionPlanScreen extends Screen {
    public AdditionPlanScreen(Browser browser) {
        super(browser);
    }
    public AdditionPlanScreen fillName(String name) {
        browser.typeText(By.xpath("//input[@name='name']"), name);
        return this;
    }

    public AdditionPlanScreen fillDetails(String details) {
        browser.typeText(By.xpath("//input[@name='details']"), details);
        return this;
    }

    public AdditionPlanScreen fillFee(int fee) {
        browser.typeText(By.xpath("//input[@name='fee']"), String.valueOf(fee));
        return this;
    }

    public AdminScreen clickSubmit() {
        browser.click(By.xpath("//button[@type='submit']"));
        return new AdminScreen(browser);
    }

    public AdminScreen clickCancel() {
        browser.click(By.xpath("//button[@type='button']"));
        return new AdminScreen(browser);
    }
}