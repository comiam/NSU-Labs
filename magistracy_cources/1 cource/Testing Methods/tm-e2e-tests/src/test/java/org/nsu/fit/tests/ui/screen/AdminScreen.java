package org.nsu.fit.tests.ui.screen;

import org.nsu.fit.services.browser.Browser;
import org.nsu.fit.services.rest.data.CustomerPojo;
import org.nsu.fit.services.rest.data.PlanPojo;
import org.nsu.fit.shared.Screen;
import org.openqa.selenium.By;

public class AdminScreen extends Screen {
    private static final String customers = "//div[@class='MuiPaper-root MuiPaper-elevation2 MuiPaper-rounded'][1]";
    private static final String plans = "//div[@class='MuiPaper-root MuiPaper-elevation2 MuiPaper-rounded'][2]";

    public AdminScreen(Browser browser) {
        super(browser);
    }

    public CreateCustomerScreen createCustomer() {
        browser.click(By.xpath("//button[@title='Add Customer']"));
        return new CreateCustomerScreen(browser);
    }

    public CustomerPojo findCustomer(String login) {
        browser.typeText(By.xpath("//*//input[@type='text' and @placeholder='Search']"),
                login);
        return findFirstCustomer();
    }

    public CustomerPojo findFirstCustomer() {
        try {
            browser.waitForElement(By.xpath(customers + "//tr[1]/td[2]"));
            CustomerPojo customerPojo = new CustomerPojo();
            customerPojo.login = browser.getValue(By.xpath(customers + "//tr[1]/td[2]"));
            customerPojo.firstName = browser.getValue(By.xpath(customers + "//tr[1]/td[3]"));
            customerPojo.lastName = browser.getValue(By.xpath(customers + "//tr[1]/td[4]"));
            return customerPojo;
        } catch (Exception ex) {
            return null;
        }
    }

    public AdminScreen makeFirstCustomer(String login) {
        browser.typeText(By.xpath("//*//input[@type='text' and @placeholder='Search']"),
                login);
        try {
            browser.waitForElement(By.xpath(customers + "//tr[1]/td[2]"));
        } catch (Exception ex) {
        }
        return this;
    }

    public AdminScreen deleteFirstCustomer() {
        browser.click(By.xpath(customers + "//tr[1]/td[1]/div[1]/button[1]"));
        return this;
    }

    public AdminScreen deleteFirstCustomerSave() {
        browser.click(By.xpath(customers + "//tr[1]/td[1]/div[1]/button[1]"));
        return this;
    }

    public AdminScreen deleteFirstCustomerCancel() {
        browser.click(By.xpath(customers + "//tr[1]/td[1]/div[1]/button[2]"));
        return this;
    }

    public AdditionPlanScreen createPlan() {
        browser.click(By.xpath("//button[@title='Add plan']"));
        return new AdditionPlanScreen(browser);
    }

    public PlanPojo findPlan(String name) {
        browser.typeText(By.xpath(plans + "//input[@type='text' and @placeholder='Search']"), name);
        return findFirstPlan();
    }

    public PlanPojo findFirstPlan() {
        try {
            browser.waitForElement(By.xpath(plans + "//tr[1]/td[2]"));
            PlanPojo plan = new PlanPojo();
            plan.name = browser.getValue(By.xpath(plans + "//tr[1]/td[2]"));
            plan.details = browser.getValue(By.xpath(plans + "//tr[1]/td[3]"));
            plan.fee = Integer.parseInt(browser.getValue(By.xpath(plans + "//tr[1]/td[4]")));
            return plan;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public AdminScreen deleteFirstPlan() {
        browser.click(By.xpath(plans + "//tr[1]/td[1]/div/button[1]"));
        return this;
    }

    public AdminScreen deleteFirstPlanSave() {
        browser.click(By.xpath(plans + "//tr[1]/td[1]/div/button[1]"));
        return this;
    }

    public AdminScreen deleteFirstPlanCancel() {
        browser.click(By.xpath(plans + "//tr[1]/td[1]/div/button[2]"));
        return this;
    }

    public LoginScreen logout() {
        browser.click(By.xpath("//a[@href='/tm-frontend/login']"));
        return new LoginScreen(browser);
    }
}
