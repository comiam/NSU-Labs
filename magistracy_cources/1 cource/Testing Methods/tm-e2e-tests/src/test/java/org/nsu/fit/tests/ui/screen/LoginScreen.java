package org.nsu.fit.tests.ui.screen;

import org.nsu.fit.services.browser.Browser;
import org.nsu.fit.shared.Screen;
import org.openqa.selenium.By;

// Лабораторная 4: Необходимо имплементировать реализацию методов данного класса,
// а при необходимости расширить его.
public class LoginScreen extends Screen {
    public LoginScreen(Browser browser) {
        super(browser);
    }

    public AdminScreen loginAsAdmin() {
        browser.waitForElement(By.id("email"));

        browser.typeText(By.id("email"), "admin");
        browser.typeText(By.id("password"), "setup");

        browser.click(By.xpath("//button[@type = 'submit']"));

        // Лабораторная 4: В текущий момент в браузере еще не открылась
        // нужная страница (AdminScreen), и при обращении к ее элементам, могут происходить
        // те или иные ошибки. Подумайте как обработать эту ситуацию...
        browser.waitForElement(By.xpath("//button[@title='Add Customer']"));
        return new AdminScreen(browser);
    }

    public CustomerScreen loginAsCustomer(String userName, String password) {
        browser.waitForElement(By.id("email"));

        browser.typeText(By.id("email"), userName);
        browser.typeText(By.id("password"), password);

        browser.click(By.xpath("//button[@type = 'submit']"));
        return new CustomerScreen(browser);
    }
}
