package com.highspring.shopping.selenium.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class OwnerInputPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public OwnerInputPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    public OwnerInputPage waitUntilVisible() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".owner-input")));
        return this;
    }

    public CartListPage loginAs(String username) {
        driver.findElement(By.cssSelector(".combobox input")).sendKeys(username);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        return new CartListPage(driver).waitUntilVisible();
    }
}
